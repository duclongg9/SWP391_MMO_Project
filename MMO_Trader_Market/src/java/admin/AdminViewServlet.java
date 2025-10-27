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

@WebServlet(name = "AdminRouter", urlPatterns = {"/admin/*"})
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
        if (s == null) {
            return null;
        }
        String v = s.trim();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(v, FLEX_DMY);
        } catch (Exception e) {
            return null;
        }
    }

    // Trim an toàn cho servlet: "" -> null
    private static String clean(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            path = "/dashboard";
        }

        String content;
        String title;
        String active;

        switch (path.toLowerCase()) {
            case "/users": {
                String q = clean(req.getParameter("q"));
                String role = clean(req.getParameter("role"));
                String from = clean(req.getParameter("from"));
                String to = clean(req.getParameter("to"));

                // --- phân trang ---
                final int DEFAULT_SIZE = 8;
                int page, size;
                try {
                    page = Integer.parseInt(clean(req.getParameter("page")));
                } catch (Exception e) {
                    page = 1;
                }
                try {
                    size = Integer.parseInt(clean(req.getParameter("size")));
                } catch (Exception e) {
                    size = DEFAULT_SIZE;
                }
                if (page < 1) {
                    page = 1;
                }
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD = tryParseDate(to);

                if (fromD != null) {
                    fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                }
                if (toD != null) {
                    toAt = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));
                }

                try (Connection con = DBConnect.getConnection()) {
                    ManageUserDAO dao = new ManageUserDAO(con);
                    List<Users> list = dao.searchUsers(q, role, fromAt, toAt); // đã lọc bằng DAO

                    // (tuỳ chọn) sắp xếp mới -> cũ theo createdAt nếu có; không có thì thôi
                    try {
                        list = list.stream()
                                .sorted(Comparator.comparing(
                                        u -> u.getCreatedAt() == null ? new java.util.Date(0) : u.getCreatedAt(),
                                        Comparator.reverseOrder()
                                ))
                                .toList();
                    } catch (Exception ignore) {
                    }

                    // --- cắt trang ---
                    int total = list.size();
                    int pages = (int) Math.ceil(total / (double) size);
                    if (pages > 0 && page > pages) {
                        page = pages;
                    }

                    int fromIdx = Math.max(0, (page - 1) * size);
                    int toIdx = Math.min(total, fromIdx + size);
                    List<Users> pageList = list.subList(fromIdx, toIdx);

                    req.setAttribute("userList", pageList);
                    req.setAttribute("pg_total", total);
                    req.setAttribute("pg_page", page);
                    req.setAttribute("pg_size", size);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                // giữ lại filter cho JSP
                String fromIso = (fromD == null ? "" : fromD.toString());
                String toIso = (toD == null ? "" : toD.toString());
                req.setAttribute("q", q == null ? "" : q);
                req.setAttribute("role", role == null ? "all" : role);
                req.setAttribute("from", fromIso);
                req.setAttribute("to", toIso);

                content = "/WEB-INF/views/Admin/pages/users.jsp";
                title = "Quản lý người dùng";
                active = "users";
                break;
            }

            case "/cashs": {
                // ---- Đọc tham số filter ----
                String type = clean(req.getParameter("type"));      // all | Deposit | Withdrawal
                String q = clean(req.getParameter("q"));         // search theo userName
                String status = clean(req.getParameter("status"));    // all | Pending | Completed | Rejected
                String order = clean(req.getParameter("order"));     // newest | oldest

                // ---- Phân trang ----
                final int DEFAULT_SIZE = 8;
                int page;  // 1-based
                int size;

                try {
                    page = Integer.parseInt(clean(req.getParameter("page")));
                } catch (Exception ignore) {
                    page = 1;
                }
                try {
                    size = Integer.parseInt(clean(req.getParameter("size")));
                } catch (Exception ignore) {
                    size = DEFAULT_SIZE;
                }
                if (page < 1) {
                    page = 1;
                }
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }

                try (Connection con = DBConnect.getConnection()) {
                    dao.admin.CashDAO dao = new dao.admin.CashDAO(con);
                    java.util.List<model.CashTxn> txns = dao.listAll();   // lấy tất cả, chưa đụng DAO

                    // ---- Lọc theo loại ----
                    if (type != null && !"all".equalsIgnoreCase(type)) {
                        final String t = type;
                        txns = txns.stream().filter(x -> t.equalsIgnoreCase(x.getType())).toList();
                    }

                    // ---- Lọc theo keyword userName ----
                    if (q != null && !q.isEmpty()) {
                        final String qLower = q.toLowerCase();
                        txns = txns.stream()
                                .filter(x -> x.getUserName() != null && x.getUserName().toLowerCase().contains(qLower))
                                .toList();
                    }

                    // ---- Lọc theo trạng thái ----
                    if (status != null && !"all".equalsIgnoreCase(status)) {
                        final String s = status;
                        txns = txns.stream().filter(x -> s.equalsIgnoreCase(x.getStatus())).toList();
                    }

                    // ---- Sắp xếp theo ngày tạo ----
                    boolean newestFirst = (order == null || order.isBlank() || "newest".equalsIgnoreCase(order));
                    java.util.Comparator<model.CashTxn> cmp
                            = java.util.Comparator.comparing(x -> x.getCreatedAt() == null ? new java.util.Date(0) : x.getCreatedAt());
                    txns = newestFirst ? txns.stream().sorted(cmp.reversed()).toList()
                            : txns.stream().sorted(cmp).toList();

                    // ---- CẮT TRANG ----
                    int total = txns.size();
                    int pages = (int) Math.ceil(total / (double) size);
                    if (page > pages && pages > 0) {
                        page = pages;
                    }

                    int from = Math.max(0, (page - 1) * size);
                    int to = Math.min(total, from + size);
                    java.util.List<model.CashTxn> pageList = txns.subList(from, to);

                    // ---- Gán cho JSP ----
                    req.setAttribute("txList", pageList);
                    req.setAttribute("pg_total", total);
                    req.setAttribute("pg_page", page);
                    req.setAttribute("pg_size", size);
                } catch (Exception e) {
                    throw new ServletException("Lỗi tải giao dịch: " + e.getMessage(), e);
                }

                // giữ lại filter cho JSP
                req.setAttribute("f_type", type == null ? "all" : type);
                req.setAttribute("f_q", q == null ? "" : q);
                req.setAttribute("f_status", status == null ? "all" : status);
                req.setAttribute("f_order", order == null ? "newest" : order);

                req.setAttribute("pageTitle", "Nạp / Rút");
                req.setAttribute("active", "cashs");
                req.setAttribute("content", "/WEB-INF/views/Admin/pages/cashs.jsp");
                req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
                return;
            }

            case "/shops": {
                String q = clean(req.getParameter("q"));
                String status = clean(req.getParameter("status"));
                String from = clean(req.getParameter("from"));
                String to = clean(req.getParameter("to"));

                // --- phân trang ---
                final int DEFAULT_SIZE = 8;
                int page, size;
                try {
                    page = Integer.parseInt(clean(req.getParameter("page")));
                } catch (Exception e) {
                    page = 1;
                }
                try {
                    size = Integer.parseInt(clean(req.getParameter("size")));
                } catch (Exception e) {
                    size = DEFAULT_SIZE;
                }
                if (page < 1) {
                    page = 1;
                }
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD = tryParseDate(to);
                if (fromD != null) {
                    fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                }
                if (toD != null) {
                    toAt = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));
                }

                try (Connection con = DBConnect.getConnection()) {
                    ManageShopDAO dao = new ManageShopDAO(con);
                    List<Shops> list = dao.getAllShops();

                    // 1) lọc tên
                    if (q != null && !q.isEmpty()) {
                        final String qLower = q.toLowerCase();
                        list = list.stream()
                                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(qLower))
                                .toList();
                    }
                    // 2) lọc trạng thái
                    if (status != null && !"all".equalsIgnoreCase(status)) {
                        final String st = status;
                        list = list.stream()
                                .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(st))
                                .toList();
                    }
                    // 3) lọc ngày
                    if (fromAt != null || toAt != null) {
                        final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                        final long toMs = (toAt == null ? Long.MAX_VALUE : toAt.getTime());
                        list = list.stream().filter(s -> {
                            java.util.Date d = s.getCreatedAt();
                            long t = (d == null ? Long.MIN_VALUE : d.getTime());
                            return t >= fromMs && t <= toMs;
                        }).toList();
                    }
                    // 4) sắp xếp mới -> cũ
                    list = list.stream()
                            .sorted(Comparator.comparing(
                                    s -> s.getCreatedAt() == null ? new java.util.Date(0) : s.getCreatedAt(),
                                    Comparator.reverseOrder()
                            ))
                            .toList();

                    // --- cắt trang ---
                    int total = list.size();
                    int pages = (int) Math.ceil(total / (double) size);
                    if (pages > 0 && page > pages) {
                        page = pages;
                    }

                    int fromIdx = Math.max(0, (page - 1) * size);
                    int toIdx = Math.min(total, fromIdx + size);
                    List<Shops> pageList = list.subList(fromIdx, toIdx);

                    req.setAttribute("shopList", pageList);
                    req.setAttribute("pg_total", total);
                    req.setAttribute("pg_page", page);
                    req.setAttribute("pg_size", size);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                // giữ filter + chuẩn yyyy-MM-dd cho input date
                String fromIso = (fromD == null ? "" : fromD.toString());
                String toIso = (toD == null ? "" : toD.toString());
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
                String q = clean(req.getParameter("q"));
                String status = clean(req.getParameter("status"));      // all | 1 | 2 | 3
                String from = clean(req.getParameter("from"));
                String to = clean(req.getParameter("to"));
                String sort = clean(req.getParameter("sort"));        // date_desc | date_asc | status_asc | status_desc

                // --- phân trang ---
                final int DEFAULT_SIZE = 8;
                int page, size;
                try {
                    page = Integer.parseInt(clean(req.getParameter("page")));
                } catch (Exception e) {
                    page = 1;
                }
                try {
                    size = Integer.parseInt(clean(req.getParameter("size")));
                } catch (Exception e) {
                    size = DEFAULT_SIZE;
                }
                if (page < 1) {
                    page = 1;
                }
                if (size <= 0) {
                    size = DEFAULT_SIZE;
                }

                Timestamp fromAt = null, toAt = null;
                LocalDate fromD = tryParseDate(from);
                LocalDate toD = tryParseDate(to);
                if (fromD != null) {
                    fromAt = Timestamp.valueOf(fromD.atStartOfDay());
                }
                if (toD != null) {
                    toAt = Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));
                }

                Integer statusId = null;
                if (status != null && !"all".equalsIgnoreCase(status)) {
                    try {
                        statusId = Integer.parseInt(status);
                    } catch (NumberFormatException ignored) {
                    }
                }

                try (Connection con = DBConnect.getConnection()) {
                    ManageKycDAO dao = new ManageKycDAO(con);
                    List<KycRequests> list = dao.getAllKycRequests();

                    // lọc theo tên
                    if (q != null && !q.isEmpty()) {
                        final String qLower = q.toLowerCase();
                        list = list.stream()
                                .filter(k -> k.getUserName() != null && k.getUserName().toLowerCase().contains(qLower))
                                .toList();
                    }

                    // lọc theo trạng thái
                    if (statusId != null) {
                        final int s = statusId;
                        list = list.stream().filter(k -> k.getStatusId() == s).toList();
                    }

                    // lọc theo ngày gửi
                    if (fromAt != null || toAt != null) {
                        final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                        final long toMs = (toAt == null ? Long.MAX_VALUE : toAt.getTime());
                        list = list.stream().filter(k -> {
                            java.util.Date d = k.getCreatedAt();
                            long t = (d == null ? Long.MIN_VALUE : d.getTime());
                            return t >= fromMs && t <= toMs;
                        }).toList();
                    }

                    // sắp xếp
                    String s = (sort == null || sort.isBlank()) ? "date_desc" : sort;
                    java.util.Comparator<KycRequests> byDate
                            = java.util.Comparator.comparing(k -> k.getCreatedAt() == null ? new java.util.Date(0) : k.getCreatedAt());
                    java.util.function.Function<Integer, Integer> statusRank = v -> switch (v == null ? 0 : v) {
                        case 1 ->
                            1;  // Pending
                        case 2 ->
                            2;  // Approved
                        case 3 ->
                            3;  // Rejected
                        default ->
                            9;
                    };
                    java.util.Comparator<KycRequests> byStatus
                            = java.util.Comparator.comparing(k -> statusRank.apply(k.getStatusId()));

                    list = switch (s) {
                        case "date_asc" ->
                            list.stream().sorted(byDate).toList();
                        case "status_asc" ->
                            list.stream().sorted(byStatus).toList();
                        case "status_desc" ->
                            list.stream().sorted(byStatus.reversed()).toList();
                        default /*date_desc*/ ->
                            list.stream().sorted(byDate.reversed()).toList();
                    };

                    // cắt trang
                    int total = list.size();
                    int pages = (int) Math.ceil(total / (double) size);
                    if (pages > 0 && page > pages) {
                        page = pages;
                    }

                    int fromIdx = Math.max(0, (page - 1) * size);
                    int toIdx = Math.min(total, fromIdx + size);
                    List<KycRequests> pageList = list.subList(fromIdx, toIdx);

                    // đẩy ra JSP
                    req.setAttribute("kycList", pageList);
                    req.setAttribute("pg_total", total);
                    req.setAttribute("pg_page", page);
                    req.setAttribute("pg_size", size);
                } catch (Exception e) {
                    throw new ServletException("Lỗi khi tải danh sách KYC: " + e.getMessage(), e);
                }

                // giữ filter cho JSP
                req.setAttribute("q", q == null ? "" : q);
                req.setAttribute("status", status == null ? "all" : status);
                req.setAttribute("from", from == null ? "" : from);
                req.setAttribute("to", to == null ? "" : to);
                req.setAttribute("sort", (sort == null || sort.isBlank()) ? "date_desc" : sort);

                req.setAttribute("pageTitle", "Quản lý KYC");
                req.setAttribute("active", "kycs");
                req.setAttribute("content", "/WEB-INF/views/Admin/pages/kycs.jsp");
                req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
                return;
            }

            case "/systems": {
                content = "/WEB-INF/views/Admin/pages/systems.jsp";
                title = "Quản lí hệ thống";
                active = "systems";
                break;
            }

            case "/dashboard":
            default: {
                content = "/WEB-INF/views/Admin/pages/dashboard.jsp";
                title = "Tổng quan";
                active = "dashboard";
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
        if (path == null) {
            resp.sendError(404, "Not Found");
            return;
        }

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
        if (path == null) {
            resp.sendError(404);
            return;
        }

        // ----- 1) Cập nhật trạng thái (ban/unban) -----
        if ("/users/status".equalsIgnoreCase(path)) {
            req.setCharacterEncoding("UTF-8");
            String action = safe(req.getParameter("action")); // ban | unban
            String idStr = safe(req.getParameter("id"));
            if (action == null || idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Thiếu hoặc sai tham số");
                return;
            }
            int userId = Integer.parseInt(idStr);
            int newStatus = "unban".equalsIgnoreCase(action) ? 1 : 0;

            try (Connection con = DBConnect.getConnection()) {
                ManageUserDAO dao = new ManageUserDAO(con);
                int rows = dao.updateStatus(userId, newStatus);   // chỉ BUYER/SELLER
                if (rows > 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                } else {
                    resp.sendError(404, "User không tồn tại hoặc không thể cập nhật");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
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
            String name = safe(req.getParameter("name"));
            String email = safe(req.getParameter("email"));
            String password = safe(req.getParameter("password"));
            String role = safe(req.getParameter("role"));

            if (name == null || email == null || password == null) {
                resp.sendError(400, "Thiếu dữ liệu");
                return;
            }

            if (role == null) {
                role = "buyer"; // mặc định buyer
            }
            // Hash mật khẩu
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));

            try (Connection con = DBConnect.getConnection()) {
                // Lấy role_id theo tên role
                Integer roleId = null;
                try (PreparedStatement rp = con.prepareStatement(
                        "SELECT id FROM roles WHERE UPPER(name)=UPPER(?) LIMIT 1")) {
                    rp.setString(1, role);
                    try (ResultSet rs = rp.executeQuery()) {
                        if (rs.next()) {
                            roleId = rs.getInt(1);
                        }
                    }
                }
                if (roleId == null) {
                    resp.sendError(400, "Vai trò không hợp lệ");
                    return;
                }

                // Kiểm tra email trùng
                try (PreparedStatement ck = con.prepareStatement(
                        "SELECT 1 FROM users WHERE email=? LIMIT 1")) {
                    ck.setString(1, email);
                    try (ResultSet rs = ck.executeQuery()) {
                        if (rs.next()) {
                            resp.sendError(409, "Email đã tồn tại");
                            return;
                        }
                    }
                }

                // Thêm user
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users(name,email,hashed_password,role_id,status,created_at,updated_at) "
                        + "VALUES (?, ?, ?, ?, 1, NOW(), NOW())")) {
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
            String action = req.getParameter("action");    // approve | reject
            String idStr = req.getParameter("id");
            String feedback = req.getParameter("feedback");

            if (idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Sai ID");
                return;
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
                    resp.sendError(400, "Action không hợp lệ");
                    return;
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
            String idStr = req.getParameter("id");
            String action = req.getParameter("action"); // accept | reject

            if (idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Sai ID");
                return;
            }
            if (action == null || !(action.equalsIgnoreCase("accept") || action.equalsIgnoreCase("reject"))) {
                resp.sendError(400, "Action không hợp lệ");
                return;
            }

            int shopId = Integer.parseInt(idStr);
            String newStatus = action.equalsIgnoreCase("accept") ? "Active" : "Rejected";

            try (Connection con = DBConnect.getConnection()) {
                ManageShopDAO dao = new ManageShopDAO(con);
                boolean ok = dao.updateStatus(shopId, newStatus);
                req.getSession().setAttribute("flash", ok ? "Cập nhật trạng thái thành công" : "Không có thay đổi");
                resp.sendRedirect(req.getContextPath() + "/admin/shops");
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
            }
            return;
        }

        if ("/cashs/withdraw/status".equalsIgnoreCase(path)) {
            req.setCharacterEncoding("UTF-8");
            String idStr = req.getParameter("id");
            String action = req.getParameter("action"); // accept | reject
            String note = req.getParameter("note");   // tạm time: text tự do
            String proof = req.getParameter("proof_url");

            if (idStr == null || !idStr.matches("\\d+")) {
                resp.sendError(400, "Sai ID");
                return;
            }
            if (!"accept".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
                resp.sendError(400, "Action không hợp lệ");
                return;
            }
            int wid = Integer.parseInt(idStr);

            try (Connection con = DBConnect.getConnection()) {
                // Nếu DB của bạn CHƯA có cột admin_note trong withdrawal_requests:
                // - Accept: cập nhật status + processed_at + (tuỳ chọn) admin_proof_url
                // - Reject: cập nhật status + processed_at
                // - (Tuỳ chọn) nếu bạn muốn lưu note: tạm thời có thể bỏ qua,
                //   hoặc thêm cột admin_note sau này.

                int updated;
                if ("accept".equalsIgnoreCase(action)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE withdrawal_requests "
                            + "SET status='Completed', processed_at=NOW(), admin_proof_url = COALESCE(?, admin_proof_url) "
                            + "WHERE id=? AND status='Pending'")) {
                        ps.setString(1, proof);
                        ps.setInt(2, wid);
                        updated = ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE withdrawal_requests "
                            + "SET status='Rejected', processed_at=NOW() "
                            + "WHERE id=? AND status='Pending'")) {
                        ps.setInt(1, wid);
                        updated = ps.executeUpdate();
                    }
                    // Nếu bạn có bảng reason_map và muốn ghim 1 reason mặc định:
                    // try (PreparedStatement rp = con.prepareStatement(
                    //   "INSERT INTO withdrawal_request_reasons_map(request_id, reason_id) VALUES (?, 2)")) {
                    //   rp.setInt(1, wid);
                    //   rp.executeUpdate();
                    // }
                }

                req.getSession().setAttribute("flash", updated > 0 ? "Cập nhật giao dịch thành công" : "Không có thay đổi");
                resp.sendRedirect(req.getContextPath() + "/admin/cashs");
                return;

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, e.getMessage());
                return;
            }
        }

        // --- Không khớp route nào ---
        resp.sendError(404);
    }

    private static String safe(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}
