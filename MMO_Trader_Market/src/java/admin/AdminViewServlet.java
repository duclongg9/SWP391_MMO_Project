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
import java.util.Comparator;
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
                // lấy tham số
                String q      = clean(req.getParameter("q"));        // tìm theo TÊN cửa hàng
                String status = clean(req.getParameter("status"));   // Active/Banned/Rejected/Inactive/all
                String from   = clean(req.getParameter("from"));     // yyyy-MM-dd | dd-MM-yyyy | dd/MM/yyyy
                String to     = clean(req.getParameter("to"));

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD   = tryParseDate(to);

                if (fromD != null) fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                if (toD   != null) toAt   = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

                try (Connection con = DBConnect.getConnection()) {
                    ManageShopDAO dao = new ManageShopDAO(con);
                    List<Shops> list = dao.getAllShops();

                    // 1) Lọc theo TÊN cửa hàng (chỉ name)
                    if (q != null && !q.isEmpty()) {
                        final String qLower = q.toLowerCase();
                        list = list.stream()
                                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(qLower))
                                .toList();
                    }

                    // 2) Lọc theo trạng thái (String)
                    if (status != null && !"all".equalsIgnoreCase(status)) {
                        final String st = status;
                        list = list.stream()
                                .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(st))
                                .toList();
                    }

                    // 3) Lọc theo ngày tạo
                    if (fromAt != null || toAt != null) {
                        final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                        final long toMs   = (toAt   == null ? Long.MAX_VALUE : toAt.getTime());
                        list = list.stream().filter(s -> {
                            java.util.Date d = s.getCreatedAt();
                            long t = (d == null ? Long.MIN_VALUE : d.getTime());
                            return t >= fromMs && t <= toMs;
                        }).toList();
                    }

                    // 4) Sắp xếp mặc định: mới → cũ
                    list = list.stream()
                            .sorted(Comparator.comparing(
                                    s -> s.getCreatedAt() == null ? new java.util.Date(0) : s.getCreatedAt(),
                                    Comparator.reverseOrder()
                            ))
                            .toList();

                    req.setAttribute("shopList", list);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                // Giữ lại giá trị filter cho JSP
                // LƯU Ý: input type="date" cần yyyy-MM-dd
                String fromIso = (fromD == null ? "" : fromD.toString()); // yyyy-MM-dd
                String toIso   = (toD   == null ? "" : toD.toString());   // yyyy-MM-dd

                req.setAttribute("q", q == null ? "" : q);
                req.setAttribute("status", status == null ? "all" : status);
                req.setAttribute("from", fromIso);
                req.setAttribute("to", toIso);

                req.setAttribute("pageTitle", "Quản lý cửa hàng");
                req.setAttribute("active", "shops");
                req.setAttribute("content", "/WEB-INF/views/Admin/pages/shops.jsp");
                req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
                return;
            }

            case "/kycs": {
                String q      = clean(req.getParameter("q"));
                String status = clean(req.getParameter("status"));
                String from   = clean(req.getParameter("from"));
                String to     = clean(req.getParameter("to"));

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD   = tryParseDate(to);
                if (fromD != null) fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                if (toD   != null) toAt   = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

                Integer statusId = null;
                if (status != null && !"all".equalsIgnoreCase(status)) {
                    try { statusId = Integer.parseInt(status); } catch (NumberFormatException ignored) {}
                }

                try (Connection con = DBConnect.getConnection()) {
                    ManageKycDAO dao = new ManageKycDAO(con);
                    List<KycRequests> list = dao.getAllKycRequests();

                    // 1️⃣ Lọc theo TÊN NGƯỜI DÙNG
                    if (q != null && !q.isEmpty()) {
                        final String qLower = q.toLowerCase();
                        list = list.stream()
                                .filter(k -> k.getUserName() != null && k.getUserName().toLowerCase().contains(qLower))
                                .toList();
                    }

                    // 2️⃣ Lọc theo trạng thái
                    if (statusId != null) {
                        final int s = statusId;
                        list = list.stream().filter(k -> k.getStatusId() == s).toList();
                    }

                    // 3️⃣ Lọc theo ngày gửi
                    if (fromAt != null || toAt != null) {
                        final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                        final long toMs   = (toAt   == null ? Long.MAX_VALUE : toAt.getTime());
                        list = list.stream().filter(k -> {
                            java.util.Date d = k.getCreatedAt();
                            long t = (d == null ? Long.MIN_VALUE : d.getTime());
                            return t >= fromMs && t <= toMs;
                        }).toList();
                    }

                    // 4️⃣ Sắp xếp mặc định: ngày mới nhất trước
                    list = list.stream()
                            .sorted(Comparator.comparing(
                                    k -> k.getCreatedAt() == null ? new java.util.Date(0) : k.getCreatedAt(),
                                    Comparator.reverseOrder()
                            ))
                            .toList();

                    req.setAttribute("kycList", list);
                } catch (Exception e) {
                    throw new ServletException("Lỗi khi tải danh sách KYC: " + e.getMessage(), e);
                }

                req.setAttribute("q", q == null ? "" : q);
                req.setAttribute("status", status == null ? "all" : status);
                req.setAttribute("from", from == null ? "" : from);
                req.setAttribute("to", to == null ? "" : to);

                req.setAttribute("pageTitle", "Quản lý KYC");
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
            String action   = req.getParameter("action");    // approve | reject
            String idStr    = req.getParameter("id");
            String feedback = req.getParameter("feedback");

            if (idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Sai ID"); return;
            }
            int kycId = Integer.parseInt(idStr);

            try (Connection con = DBConnect.getConnection()) {
                ManageKycDAO dao = new ManageKycDAO(con);
                int rows;
                if ("approve".equalsIgnoreCase(action)) {
                    rows = dao.approveKycAndPromote(kycId, feedback);
                } else if ("reject".equalsIgnoreCase(action)) {
                    rows = dao.rejectKyc(kycId, feedback == null ? "" : feedback);
                } else {
                    resp.sendError(400, "Action không hợp lệ"); return;
                }

                // PRG: redirect về danh sách để tránh lỗi forward sai / response committed
                req.getSession().setAttribute("flash", rows > 0 ? "Cập nhật thành công" : "Không có thay đổi");
                resp.sendRedirect(req.getContextPath() + "/admin/kycs");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
                return;
            }
        }
        // POST /admin/shops/status   (action=accept|reject, id=<shopId>)
        if ("/shops/status".equalsIgnoreCase(path)) {
            String idStr  = req.getParameter("id");
            String action = req.getParameter("action"); // accept | reject

            if (idStr == null || !idStr.matches("\\d+")) { resp.sendError(400, "Sai ID"); return; }
            if (action == null || !(action.equalsIgnoreCase("accept") || action.equalsIgnoreCase("reject"))) {
                resp.sendError(400, "Action không hợp lệ"); return;
            }

            int shopId = Integer.parseInt(idStr);
            String newStatus = action.equalsIgnoreCase("accept") ? "Active" : "Rejected";

            try (Connection con = DBConnect.getConnection()) {
                ManageShopDAO dao = new ManageShopDAO(con);
                boolean ok = dao.updateStatus(shopId, newStatus);
                req.getSession().setAttribute("flash", ok ? "Cập nhật trạng thái thành công" : "Không có thay đổi");
                resp.sendRedirect(req.getContextPath() + "/admin/shops");
            } catch (Exception e) {
                e.printStackTrace(); resp.sendError(500, e.getMessage());
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
