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
            case "/cashs": { // ✅ thêm route này để không rơi về dashboard
                content = "/WEB-INF/views/Admin/pages/cashs.jsp";
                title   = "Nạp / Rút"; active = "cashs";
                break;
            }
            case "/shops": { // ✅ thêm route này để không rơi về dashboard
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
            case "/kycs": { // ✅ thêm route này để không rơi về dashboard
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
            case "/systems": { // ✅ thêm route này để không rơi về dashboard
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

    // POST /admin/users (tạo user) – luôn HASH mật khẩu bằng BCrypt, mặc định role=buyer
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || !path.equalsIgnoreCase("/users")) {
            resp.sendError(404);
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String name     = safe(req.getParameter("name"));
        String email    = safe(req.getParameter("email"));
        String password = safe(req.getParameter("password"));
        String role     = safe(req.getParameter("role"));

        if (name == null || email == null || password == null) {
            resp.sendError(400, "Thiếu dữ liệu");
            return;
        }

        if (role == null) role = "buyer"; // ✅ mặc định buyer

        // ✅ HASH mật khẩu thành chuỗi dạng $2a$10$...
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));

        try (Connection con = DBConnect.getConnection()) {
            // Lấy role_id theo tên role trong bảng roles
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

            // Insert user mới
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(name,email,hashed_password,role_id,status,created_at,updated_at) " +
                            "VALUES (?, ?, ?, ?, 1, NOW(), NOW())")) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, hashed);   // ✅ lưu chuỗi bcrypt
                ps.setInt(4, roleId);
                ps.executeUpdate();
            }

            // PRG
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private static String safe(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }



}
