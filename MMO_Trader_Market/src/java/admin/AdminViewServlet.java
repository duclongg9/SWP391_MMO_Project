package admin;

import dao.admin.ManageKycDAO;
import dao.admin.ManageShopDAO;
import dao.connect.DBConnect;
import model.KycRequests;
import model.Shops;
import model.Users;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.admin.ManageUserDAO;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// BCrypt
import org.mindrot.jbcrypt.BCrypt;

@WebServlet(name="AdminRouter", urlPatterns={"/admin/*"})
public class AdminViewServlet extends HttpServlet {

    // Chấp nhận: dd-MM-yyyy, yyyy-MM-dd, dd/MM/yyyy
    private static final DateTimeFormatter FLEX_DMY = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    private static LocalDate tryParseDate(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try { return LocalDate.parse(v, FLEX_DMY); }
        catch (Exception e) { return null; }
    }

    // Trim an toàn cho servlet: "" -> null
    private static String clean(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) path = "/dashboard";

        String content; String title; String active;

        switch (path.toLowerCase()) {
            case "/users": {
                String q    = clean(req.getParameter("q"));
                String role = clean(req.getParameter("role"));
                String from = clean(req.getParameter("from"));
                String to   = clean(req.getParameter("to"));

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD   = tryParseDate(to);

                if (fromD != null) fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                if (toD   != null) toAt   = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

                try (Connection con = DBConnect.getConnection()) {
                    ManageUserDAO dao = new ManageUserDAO(con);
                    List<Users> users = dao.searchUsers(q, role, fromAt, toAt);
                    req.setAttribute("userList", users);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                content = "/WEB-INF/views/Admin/pages/users.jsp";
                title   = "Quản lý người dùng"; active = "users";
                break;
            }
            case "/cashs": {
                content = "/WEB-INF/views/Admin/pages/cashs.jsp";
                title   = "Nạp / Rút"; active = "cashs";
                break;
            }
            case "/shops": {
                try (Connection con = DBConnect.getConnection()) {
                    ManageShopDAO dao = new ManageShopDAO(con);
                    List<Shops> shops = dao.getAllShops();
                    req.setAttribute("shopList", shops);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                req.setAttribute("pageTitle", "Quản lý cửa hàng");
                req.setAttribute("active", "shops");
                req.setAttribute("content", "/WEB-INF/views/Admin/pages/shops.jsp");
                req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
                return;
            }
            case "/kycs": {
                try (Connection con = DBConnect.getConnection()) {
                    ManageKycDAO dao = new ManageKycDAO(con);
                    List<KycRequests> shops = dao.getAllKyc();
                    req.setAttribute("kycList", shops);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                req.setAttribute("pageTitle", "Quản lý kyc");
                req.setAttribute("active", "kycs");
                req.setAttribute("content", "/WEB-INF/views/Admin/pages/kycs.jsp");
                req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
                return;
            }
            case "/systems": {
                content = "/WEB-INF/views/Admin/pages/systems.jsp";
                title   = "Quản lí hệ thống"; active = "systems";
                break;
            }

            case "/dashboard":
            default: {
                content = "/WEB-INF/views/Admin/pages/dashboard.jsp";
                title   = "Tổng quan"; active = "dashboard";
            }
        }

        req.setAttribute("pageTitle", title);
        req.setAttribute("active", active);
        req.setAttribute("content", content);
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // DELETE /admin/users/{id}
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String path = req.getPathInfo(); // ví dụ "/users/3"
        if (path == null) { resp.sendError(404, "Not Found"); return; }

        String[] parts = path.split("/"); // ["", "users", "3"]
        if (parts.length < 3 || !"users".equals(parts[1]) || !parts[2].matches("\\d+")) {
            resp.sendError(400, "Invalid or missing id");
            return;
        }

        int id = Integer.parseInt(parts[2]);

        try (Connection con = DBConnect.getConnection()) {
            int rows = new ManageUserDAO(con).deleteUser(id);
            if (rows > 0) {
                resp.getWriter().write("{\"ok\":true}");
            } else {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"User not found\"}");
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException fk) {
            resp.sendError(409, "Không thể xóa do ràng buộc dữ liệu (FK)");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    // POST /admin/users [...]  và  /admin/users/{id}/ban | /admin/users/{id}/unban
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();          // ví dụ: /users/status
        if (path == null) { resp.sendError(404); return; }

        // ----- 1) Cập nhật trạng thái (ban/unban) -----
        if ("/users/status".equalsIgnoreCase(path)) {
            req.setCharacterEncoding("UTF-8");
            String action = safe(req.getParameter("action")); // ban | unban
            String idStr  = safe(req.getParameter("id"));
            if (action == null || idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Thiếu hoặc sai tham số"); return;
            }
            int userId = Integer.parseInt(idStr);
            int newStatus = "unban".equalsIgnoreCase(action) ? 1 : 0;

            try (Connection con = DBConnect.getConnection()) {
                ManageUserDAO dao = new ManageUserDAO(con);
                int rows = dao.updateStatus(userId, newStatus);   // chỉ BUYER/SELLER
                if (rows > 0) resp.sendRedirect(req.getContextPath() + "/admin/users");
                else resp.sendError(404, "User không tồn tại hoặc không thể cập nhật");
            } catch (Exception e) {
                e.printStackTrace(); resp.sendError(500, e.getMessage());
            }
            return;
        }

        // ----- Tạo user: POST /admin/users (giữ nguyên logic cũ của bạn)
        if ("/users".equalsIgnoreCase(path)) {
            // ... phần tạo user của bạn ở đây (hash password, lookup role_id, insert ...)
            // (Không lặp lại để ngắn gọn)
            resp.sendError(501, "Create user handler not pasted here");
            return;
        }

    // --- TẠO USER: POST /admin/users ---
        if (path.equalsIgnoreCase("/users")) {
            req.setCharacterEncoding("UTF-8");
            String name     = safe(req.getParameter("name"));
            String email    = safe(req.getParameter("email"));
            String password = safe(req.getParameter("password"));
            String role     = safe(req.getParameter("role"));

            if (name == null || email == null || password == null) {
                resp.sendError(400, "Thiếu dữ liệu");
                return;
            }

            if (role == null) role = "buyer"; // mặc định buyer

            // Hash mật khẩu
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));

            try (Connection con = DBConnect.getConnection()) {
                // Lấy role_id theo tên role
                Integer roleId = null;
                try (PreparedStatement rp = con.prepareStatement(
                        "SELECT id FROM roles WHERE UPPER(name)=UPPER(?) LIMIT 1")) {
                    rp.setString(1, role);
                    try (ResultSet rs = rp.executeQuery()) {
                        if (rs.next()) roleId = rs.getInt(1);
                    }
                }
                if (roleId == null) { resp.sendError(400, "Vai trò không hợp lệ"); return; }

                // Kiểm tra email trùng
                try (PreparedStatement ck = con.prepareStatement(
                        "SELECT 1 FROM users WHERE email=? LIMIT 1")) {
                    ck.setString(1, email);
                    try (ResultSet rs = ck.executeQuery()) {
                        if (rs.next()) { resp.sendError(409, "Email đã tồn tại"); return; }
                    }
                }

                // Thêm user
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users(name,email,hashed_password,role_id,status,created_at,updated_at) " +
                                "VALUES (?, ?, ?, ?, 1, NOW(), NOW())")) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, hashed);
                    ps.setInt(4, roleId);
                    ps.executeUpdate();
                }

                // PRG
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
            }
            return;
        }
        if ("/kycs/status".equalsIgnoreCase(path)) {
            req.setCharacterEncoding("UTF-8");
            String action   = safe(req.getParameter("action"));   // approve | reject
            String idStr    = safe(req.getParameter("id"));       // kyc_id
            String feedback = safe(req.getParameter("feedback")); // ghi chú (bắt buộc khi reject)

            if (idStr == null || !idStr.matches("\\d+")) { resp.sendError(400, "Sai id"); return; }
            if ("reject".equalsIgnoreCase(action) && (feedback == null || feedback.isBlank())) {
                resp.sendError(400, "Nhập lý do khi từ chối"); return;
            }

            int kycId = Integer.parseInt(idStr);

            try (Connection con = DBConnect.getConnection()) {
                ManageKycDAO dao = new ManageKycDAO(con);
                int rows;

                if ("approve".equalsIgnoreCase(action)) {
                    // Đổi trạng thái KYC -> Approved và NÂNG users.role_id -> Seller (2)
                    rows = dao.updateKycAndMaybePromoteUser(
                            kycId, ManageKycDAO.KYC_APPROVED, ManageKycDAO.ROLE_SELLER, feedback);
                } else if ("reject".equalsIgnoreCase(action)) {
                    // Chỉ đổi trạng thái KYC -> Rejected (không đổi role)
                    rows = dao.updateKycAndMaybePromoteUser(
                            kycId, ManageKycDAO.KYC_REJECTED, null, feedback);
                } else {
                    resp.sendError(400, "Action không hợp lệ"); return;
                }

                if (rows > 0) resp.sendRedirect(req.getContextPath() + "/admin/kycs");
                else          resp.sendError(404, "Không tìm thấy KYC");
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
            }
            return;
        }

        // --- Không khớp route nào ---
        resp.sendError(404);
    }

    private static String safe(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}
