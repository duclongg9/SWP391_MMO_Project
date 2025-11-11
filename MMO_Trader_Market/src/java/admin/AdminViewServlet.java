package admin;

import com.google.gson.Gson;
import controller.dashboard.AdminDashboardController;
import dao.admin.ManageKycDAO;
import dao.admin.ManageShopDAO;
import dao.admin.ManageUserDAO;
import dao.connect.DBConnect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.KycRequests;
import model.Shops;
import model.Users;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.admin.ManageUserDAO;
import dao.system.SystemConfigDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// BCrypt (nếu cần ở nơi khác)
import org.mindrot.jbcrypt.BCrypt;
import service.AdnimDashboardService;

@WebServlet(name = "AdminRouter", urlPatterns = {"/admin/*"})
public class AdminViewServlet extends HttpServlet {

    Users user;
    AdnimDashboardService adminDashboard = new AdnimDashboardService();
    // ------ Date helpers ------
    // Chấp nhận: dd-MM-yyyy, yyyy-MM-dd, dd/MM/yyyy
    private static final DateTimeFormatter FLEX_DMY = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    private static final long DEFAULT_ESCROW_HOLD_SECONDS = 72L * 3600L;

    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

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

    // ------ String helpers ------
    private static String clean(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private static int parseIntOrDefault(String s, int dft) {
        try {
            return Integer.parseInt(clean(s));
        } catch (Exception e) {
            return dft;
        }
    }

    /**
     * Tạo chuỗi mô tả thời lượng escrow dựa trên ba thành phần giờ, phút, giây.
     */
    private static String formatEscrowDuration(int hours, int minutes, int seconds) {
        StringBuilder label = new StringBuilder();
        if (hours > 0) {
            label.append(hours).append(" giờ");
        }
        if (minutes > 0) {
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(minutes).append(" phút");
        }
        if (seconds > 0 || label.length() == 0) {
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(seconds).append(" giây");
        }
        return label.toString();
    }

    // ------ Pagination helpers ------ 20 , 8
    private static int ceilDiv(int total, int size) {
        return Math.max(1, (int) Math.ceil(total / (double) Math.max(1, size)));

    }

    private static int clampPage(int page, int pages) {
        if (page < 1) {
            return 1;
        }
        if (page > pages) {
            return pages;
        }
        return page;
    }

    private static String normalizeKycImageUrl(HttpServletRequest req, String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String url = raw;
        url = url.replaceFirst("^https?://[^/]+", "");
        url = url.replaceFirst("^/(SWP391_MMO_Project|MMO_Trader_Market)", "");
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return req.getContextPath() + url; // /<context>/uploads/...
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) {
            resp.sendError(404);
            return;
        }

        switch (path.toLowerCase()) {
            case "/users":
                handleCreateUser(req, resp);
                return;
            case "/users/status":
                handleUserStatus(req, resp);
                return;
            case "/kycs/status":
                handleKycStatus(req, resp);
                return;
            case "/shops/status":
                handleShopStatus(req, resp);
                return;
            case "/systems/escrow":
                handleUpdateEscrowConfig(req, resp);
                return;
            default:
                resp.sendError(404);
        }
    }

    private void handleKycStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action"));   // "approve" | "reject"
        String idStr = safe(req.getParameter("id"));
        String feedback = safe(req.getParameter("feedback"));

        // Validate cơ bản
        if (idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Lỗi: ID không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
            return;
        }
        if (!"approve".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
            req.getSession().setAttribute("flash", "Lỗi: Hành động không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
            return;
        }
        // Bắt buộc ghi chú khi từ chối
        if ("reject".equalsIgnoreCase(action) && (feedback == null || feedback.isBlank())) {
            req.getSession().setAttribute("flash", "Lỗi: Vui lòng nhập lý do từ chối");
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
            return;
        }

        int kycId = Integer.parseInt(idStr);

        try (Connection con = DBConnect.getConnection()) {
            ManageKycDAO dao = new ManageKycDAO(con);
            int rows;

            if ("approve".equalsIgnoreCase(action)) {
                rows = dao.approveKycAndPromote(kycId, feedback);
                req.getSession().setAttribute(
                        "flash",
                        rows > 0 ? "Duyệt KYC thành công!" : "Không có thay đổi"
                );
            } else { // reject
                rows = dao.rejectKyc(kycId, feedback);
                req.getSession().setAttribute(
                        "flash",
                        rows > 0 ? "Từ chối KYC thành công!" : "Không có thay đổi"
                );
            }

            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/kycs");
        }
    }

    private void handleShopStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String idStr = req.getParameter("id");
        String action = req.getParameter("action");   // accept | reject

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
    }

    /**
     * Cập nhật cấu hình thời gian giữ escrow dựa trên giá trị giờ-phút-giây được gửi từ form.
     */
    private void handleUpdateEscrowConfig(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        String hoursParam = safe(req.getParameter("escrowHoldHours"));
        String minutesParam = safe(req.getParameter("escrowHoldMinutes"));
        String secondsParam = safe(req.getParameter("escrowHoldSeconds"));
        HttpSession session = req.getSession();

        int hours;
        int minutes;
        int seconds;

        try {
            hours = hoursParam == null ? 0 : Integer.parseInt(hoursParam);
            minutes = minutesParam == null ? 0 : Integer.parseInt(minutesParam);
            seconds = secondsParam == null ? 0 : Integer.parseInt(secondsParam);
        } catch (NumberFormatException ex) {
            session.setAttribute("flashError", "Vui lòng nhập giá trị số hợp lệ cho giờ, phút và giây.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        if (hours < 0 || hours > 720) {
            session.setAttribute("flashError", "Giá trị giờ phải nằm trong khoảng 0 - 720.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (minutes < 0 || minutes >= 60) {
            session.setAttribute("flashError", "Giá trị phút phải nằm trong khoảng 0 - 59.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (seconds < 0 || seconds >= 60) {
            session.setAttribute("flashError", "Giá trị giây phải nằm trong khoảng 0 - 59.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        long totalSeconds = hours * 3600L + minutes * 60L + seconds;
        long maxSeconds = 720L * 3600L;
        if (totalSeconds <= 0) {
            session.setAttribute("flashError", "Thời gian escrow phải lớn hơn 0 giây.");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }
        if (totalSeconds > maxSeconds) {
            session.setAttribute("flashError", "Thời gian escrow không được vượt quá 720 giờ (2.592.000 giây).");
            resp.sendRedirect(req.getContextPath() + "/admin/systems");
            return;
        }

        boolean updated = systemConfigDAO.upsertValueByKey(
                "escrow.hold.default.seconds",
                Long.toString(totalSeconds)
        );

        if (updated) {
            session.setAttribute(
                    "flash",
                    "Đã cập nhật thời gian giữ tiền escrow thành "
                            + formatEscrowDuration(hours, minutes, seconds) + "."
            );
        } else {
            session.setAttribute("flashError", "Không thể lưu cấu hình thời gian escrow. Vui lòng thử lại sau.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/systems");
    }

    // ================== Tạo user: báo lỗi trong popup (không JS) ==================
    private void handleCreateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String name = safe(req.getParameter("name"));
        String email = safe(req.getParameter("email"));
        String password = safe(req.getParameter("password"));
        String role = safe(req.getParameter("role"));
        if (role == null || role.isBlank()) {
            role = "buyer";
        }

        java.util.Map<String, String> errs = new java.util.LinkedHashMap<>();

        // Validate cơ bản
        if (name == null || name.length() < 5) {
            errs.put("name", "Họ tên phải từ 2 ký tự.");
        }
        if (email == null || !email.matches("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errs.put("email", "Email không hợp lệ.");
        }
        if (password == null || password.length() < 6) {
            errs.put("password", "Mật khẩu tối thiểu 6 ký tự.");
        }

        Integer roleId = null;

        if (errs.isEmpty()) {
            try (Connection con = DBConnect.getConnection()) {
                // map role -> id
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
                    errs.put("form", "Vai trò không hợp lệ.");
                }

                // email trùng
                if (errs.isEmpty()) {
                    try (PreparedStatement ck = con.prepareStatement(
                            "SELECT 1 FROM users WHERE email=? LIMIT 1")) {
                        ck.setString(1, email);
                        try (ResultSet rs = ck.executeQuery()) {
                            if (rs.next()) {
                                errs.put("email", "Email đã tồn tại.");
                            }
                        }
                    }
                }

                // Insert
                if (errs.isEmpty()) {
                    String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(
                            password, org.mindrot.jbcrypt.BCrypt.gensalt(10));
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO users(name,email,hashed_password,role_id,status,created_at,updated_at) "
                            + "VALUES (?,?,?,?,0,NOW(),NOW())")) {
                        ps.setString(1, name);
                        ps.setString(2, email);
                        ps.setString(3, hashed);
                        ps.setInt(4, roleId);
                        ps.executeUpdate();
                    }
                    req.getSession().setAttribute("flash", "Tạo người dùng thành công!");
                    resp.sendRedirect(req.getContextPath() + "/admin/users");
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                errs.put("form", "Có lỗi hệ thống: " + e.getMessage());
            }
        }

        // Có lỗi → mở lại modal + giữ giá trị đã nhập, forward về trang list
        req.setAttribute("openCreateModal", true);
        req.setAttribute("form_name", name);
        req.setAttribute("form_email", email);
        req.setAttribute("form_role", role);
        req.setAttribute("form_errs", errs);

        // nạp lại bảng + phân trang rồi render
        handleUsers(req, resp);
    }

    // ================== Ban / Unban ==================
    private void handleUserStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        String action = safe(req.getParameter("action"));     // "ban" | "unban"
        String idStr = safe(req.getParameter("id"));

        if (action == null || idStr == null || !idStr.matches("\\d+")) {
            req.getSession().setAttribute("flash", "Tham số không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        int userId = Integer.parseInt(idStr);
        int newStatus = "unban".equalsIgnoreCase(action) ? 1 : 0;

        try (Connection con = DBConnect.getConnection()) {
            ManageUserDAO dao = new ManageUserDAO(con);
            int rows = dao.updateStatus(userId, newStatus);
            if (rows > 0) {
                req.getSession().setAttribute("flash",
                        ("unban".equalsIgnoreCase(action) ? "Mở khóa" : "Khóa") + " người dùng thành công");
            } else {
                req.getSession().setAttribute("flash", "User không tồn tại hoặc không thể cập nhật");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer user = (Integer) req.getSession().getAttribute("userId");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth");
            return;
        }
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            path = "/dashboard";
        }

        try {
            switch (path.toLowerCase()) {
                case "/dashboard" ->
                    handleDashboard(req, resp);
                case "/users" ->
                    handleUsers(req, resp);
                case "/cashs" ->
                    handleCashs(req, resp);
                case "/shops" ->
                    handleShops(req, resp);
                case "/kycs" ->
                    handleKycs(req, resp);
                case "/disputes" ->
                        handleDisputes(req, resp);
                case "/systems" ->
                    handleSystems(req, resp);
                default -> {
                    // Nếu path không hợp lệ, redirect về dashboard
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xử lý yêu cầu: " + e.getMessage());
        }
    }
    // ================== /admin/users ==================
    private void handleDisputes(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q         = clean(req.getParameter("q"));
        String status    = clean(req.getParameter("status"));     // all | Open | ...
        String issueType = clean(req.getParameter("issueType"));  // all | ACCOUNT_...
        String from      = clean(req.getParameter("from"));
        String to        = clean(req.getParameter("to"));

        LocalDate fromD = tryParseDate(from);
        LocalDate toD   = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt   = (toD   == null) ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        final int DEFAULT_SIZE = 10;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) size = DEFAULT_SIZE;

        try (Connection con = DBConnect.getConnection()) {
            ManageDisputeDAO dao = new ManageDisputeDAO(con);

            // Lấy list theo filter (tự viết như bên users/shops)
            List<Dispute> list = dao.search(q,
                    (status == null || "all".equalsIgnoreCase(status)) ? null : status,
                    (issueType == null || "all".equalsIgnoreCase(issueType)) ? null : issueType,
                    fromAt, toAt);

            // sort mới -> cũ
            list = list.stream()
                    .sorted(Comparator.comparing(
                            d -> d.getCreatedAt() == null ? new java.util.Date(0) : d.getCreatedAt(),
                            Comparator.reverseOrder()))
                    .toList();

            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx   = Math.min(total, fromIdx + size);
            List<Dispute> pageList = list.subList(fromIdx, toIdx);

            // 👈 RẤT QUAN TRỌNG: tên phải đúng "disputes"
            req.setAttribute("disputes", pageList);

            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page",  page);
            req.setAttribute("pg_size",  size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast",  page >= pages);
            req.setAttribute("pg_single",  pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải khiếu nại: " + e.getMessage(), e);
        }

        // giữ lại filter cho JSP
        req.setAttribute("q",         q == null ? "" : q);
        req.setAttribute("status",    status == null ? "all" : status);
        req.setAttribute("issueType", issueType == null ? "all" : issueType);
        req.setAttribute("from",      fromD == null ? "" : fromD.toString());
        req.setAttribute("to",        toD   == null ? "" : toD.toString());

        req.setAttribute("pageTitle", "Quản lý khiếu nại");
        req.setAttribute("active",    "disputes");
        req.setAttribute("content",   "/WEB-INF/views/Admin/pages/disputes.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }
    // ================== /admin/users ==================
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate now = LocalDate.now();
        
        // Danh sách năm có dữ liệu
        List<Integer> avaliableYear = new ArrayList<>();
        try {
            avaliableYear = adminDashboard.getAvailableYears();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // ----- 1. Lấy year được chọn (hoặc mặc định năm hiện tại) -----
        String yearParam = req.getParameter("year");
        int selectedYear;
        try {
            if (yearParam != null && !yearParam.isBlank()) {
                selectedYear = Integer.parseInt(yearParam.trim());
            } else {
                selectedYear = LocalDate.now().getYear();
            }
        } catch (NumberFormatException e) {
            selectedYear = LocalDate.now().getYear();
        }
        
        if (!avaliableYear.contains(selectedYear)) {
            selectedYear = LocalDate.now().getYear();
        }

        // ----- 2. Lấy dữ liệu deposit/withdraw theo tháng -----
        BigDecimal[] listDeposit = new BigDecimal[12];
        BigDecimal[] listWithdraw = new BigDecimal[12];
        
        int[] listOrder = new int[12];
        int[] listUser = new int[12];

        try {
            listDeposit = adminDashboard.arrayDepositByMonth(selectedYear);
            listWithdraw = adminDashboard.arrayWithdrawByMonth(selectedYear);
            listOrder = adminDashboard.arrayOrderByMonth(selectedYear);
            listUser = adminDashboard.arrayUserByMonth(selectedYear);
        } catch (SQLException ex) {
            // log + giữ mảng default = 0
            ex.printStackTrace();
        }

        Gson gson = new Gson();
        String depositJson = gson.toJson(listDeposit);
        String withdrawJson = gson.toJson(listWithdraw);
        String orderJson = gson.toJson(listOrder);
        String userJson = gson.toJson(listUser);

        // ----- 3. Tính tháng hiện tại & tháng trước -----
        
        int presentMonth = now.getMonthValue();
        int presentYear = now.getYear();

        int lastMonth;
        int lastYear;
        if (presentMonth == 1) {
            lastMonth = 12;
            lastYear = presentYear - 1;
        } else {
            lastMonth = presentMonth - 1;
            lastYear = presentYear;
        }

        // ----- 4. Tổng deposit/withdraw tháng trước & tháng này -----
        BigDecimal totalDepositLastMonth = BigDecimal.ZERO;
        BigDecimal totalWithdrawLastMonth = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdraw = BigDecimal.ZERO;

        try {
            totalDepositLastMonth = adminDashboard.totalDeposite(lastMonth, lastYear);
            totalWithdrawLastMonth = adminDashboard.totalWithdraw(lastMonth, lastYear);

            totalDeposit = adminDashboard.totalDeposite(presentMonth, presentYear);
            totalWithdraw = adminDashboard.totalWithdraw(presentMonth, presentYear);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // ----- 5. Thống kê khác -----
        int orderByMonth = 0;
        int shopByMonth = 0;
        int totalUser = 0;
        try {
            orderByMonth = adminDashboard.totalOrder(presentMonth, presentYear);
            shopByMonth = adminDashboard.totalActiveShop();
            totalUser = adminDashboard.totalActiveUser();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        //thống kê các shop hiện tại
        int shopPending = adminDashboard.totalPendingShop();
        int shopSuspended = adminDashboard.totalSuspendedShop();

        // ----- 6. % thay đổi -----
        BigDecimal persentDepositChanged
                = units.PercentChange.calculatePercentChange(totalDeposit, totalDepositLastMonth);
        BigDecimal persentWithdrawChanged
                = units.PercentChange.calculatePercentChange(totalWithdraw, totalWithdrawLastMonth);

        // ----- 8. Đẩy attribute cho JSP -----
        req.setAttribute("years", avaliableYear);

        req.setAttribute("totalOrder", orderByMonth);
        req.setAttribute("shopByMonth", shopByMonth);
        req.setAttribute("totalUser", totalUser);
        req.setAttribute("totalDeposit", totalDeposit);
        req.setAttribute("totalWithdraw", totalWithdraw);
        req.setAttribute("persentDepositChanged", persentDepositChanged);
        req.setAttribute("persentWithdrawChanged", persentWithdrawChanged);
        
        //shopInfo
        req.setAttribute("totalActiveShop", shopByMonth);
        req.setAttribute("totalPendingShop", shopPending);
        req.setAttribute("totalSuspended", shopSuspended);
        
        req.setAttribute("selectedYear", selectedYear);
        req.setAttribute("depositByMonth", depositJson);
        req.setAttribute("withdrawByMonth", withdrawJson);
        req.setAttribute("orderByMonth", orderJson);

        // Layout chung
        req.setAttribute("pageTitle", "Tổng quan hệ thống");
        req.setAttribute("active", "dashboard");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/dashboard.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    private void handleUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q = clean(req.getParameter("q"));
        String role = clean(req.getParameter("role"));
        String from = clean(req.getParameter("from"));
        String to = clean(req.getParameter("to"));

        LocalDate fromD = tryParseDate(from);
        LocalDate toD = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt = (toD == null) ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        try (Connection con = DBConnect.getConnection()) {
            ManageUserDAO dao = new ManageUserDAO(con);

            // TODO: Nên có dao.countUsers(filter) & dao.findUsers(filter, size, offset)
            List<Users> list = dao.searchUsers(q, role, fromAt, toAt);

            // sort mới -> cũ
            list = list.stream()
                    .sorted(Comparator.comparing(
                            u -> u.getCreatedAt() == null ? new java.util.Date(0) : u.getCreatedAt(),
                            Comparator.reverseOrder()))
                    .toList();

            final int DEFAULT_SIZE = 8;
            int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
            int page = parseIntOrDefault(req.getParameter("page"), 1);
            if (size <= 0) {
                size = DEFAULT_SIZE;
            }

            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx = Math.min(total, fromIdx + size);
            List<Users> pageList = list.subList(fromIdx, toIdx);

            req.setAttribute("userList", pageList);
            // set paging attrs (để JSP chỉ hiển thị)
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page", page);
            req.setAttribute("pg_size", size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast", page >= pages);
            req.setAttribute("pg_single", pages <= 1);

        } catch (Exception e) {
            throw new ServletException(e);
        }
        if ("1".equals(req.getParameter("openCreate"))) {
            req.setAttribute("openCreateModal", true);
        }
        // giữ filter (ISO cho input date)
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("role", role == null ? "all" : role);
        req.setAttribute("from", fromD == null ? "" : fromD.toString());
        req.setAttribute("to", toD == null ? "" : toD.toString());

        req.setAttribute("pageTitle", "Quản lý người dùng");
        req.setAttribute("active", "users");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/users.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    /**
     * Hiển thị trang cấu hình hệ thống và nạp thông tin cấu hình escrow hiện tại.
     */
    private void handleSystems(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object flash = session.getAttribute("flash");
            if (flash != null) {
                req.setAttribute("flash", flash);
                session.removeAttribute("flash");
            }
            Object flashError = session.getAttribute("flashError");
            if (flashError != null) {
                req.setAttribute("flashError", flashError);
                session.removeAttribute("flashError");
            }
        }

        long escrowHoldSeconds = DEFAULT_ESCROW_HOLD_SECONDS;
        String configValue = systemConfigDAO.findValueByKey("escrow.hold.default.seconds").orElse(null);
        if (configValue != null) {
            String trimmed = configValue.trim();
            if (!trimmed.isEmpty()) {
                try {
                    long parsedSeconds = Long.parseLong(trimmed);
                    if (parsedSeconds > 0) {
                        escrowHoldSeconds = parsedSeconds;
                    }
                } catch (NumberFormatException ignore) {
                    // fallback về mặc định khi cấu hình không hợp lệ
                }
            }
        }

        int escrowHoldHoursPart = (int) Math.min(escrowHoldSeconds / 3600L, Integer.MAX_VALUE);
        long remainder = escrowHoldSeconds % 3600L;
        int escrowHoldMinutesPart = (int) (remainder / 60L);
        int escrowHoldSecondsPart = (int) (remainder % 60L);

        req.setAttribute("escrowHoldHoursPart", escrowHoldHoursPart);
        req.setAttribute("escrowHoldMinutesPart", escrowHoldMinutesPart);
        req.setAttribute("escrowHoldSecondsPart", escrowHoldSecondsPart);
        req.setAttribute("escrowHoldDurationLabel",
                formatEscrowDuration(escrowHoldHoursPart, escrowHoldMinutesPart, escrowHoldSecondsPart));
        req.setAttribute("escrowHoldTotalSeconds", escrowHoldSeconds);
        req.setAttribute("pageTitle", "Cấu hình hệ thống");
        req.setAttribute("active", "systems");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/systems.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // ================== /admin/cashs ==================
    private void handleCashs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String type = clean(req.getParameter("type"));   // all | Deposit | Withdrawal
        String q = clean(req.getParameter("q"));      // search userName
        String status = clean(req.getParameter("status")); // all | Pending | Completed | Rejected
        String order = clean(req.getParameter("order"));  // newest | oldest

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }

        try (Connection con = DBConnect.getConnection()) {
            dao.admin.CashDAO dao = new dao.admin.CashDAO(con);
            List<model.CashTxn> txns = dao.listAll(); // TODO: chuyển sang DAO paging khi có

            // filter
            if (type != null && !"all".equalsIgnoreCase(type)) {
                final String t = type;
                txns = txns.stream().filter(x -> t.equalsIgnoreCase(x.getType())).toList();
            }
            if (q != null && !q.isEmpty()) {
                final String qLower = q.toLowerCase();
                txns = txns.stream()
                        .filter(x -> x.getUserName() != null && x.getUserName().toLowerCase().contains(qLower))
                        .toList();
            }
            if (status != null && !"all".equalsIgnoreCase(status)) {
                final String s = status;
                txns = txns.stream().filter(x -> s.equalsIgnoreCase(x.getStatus())).toList();
            }

            // sort
            Comparator<model.CashTxn> cmp = Comparator.comparing(
                    x -> x.getCreatedAt() == null ? new java.util.Date(0) : x.getCreatedAt());
            boolean newestFirst = (order == null || "newest".equalsIgnoreCase(order));
            txns = newestFirst ? txns.stream().sorted(cmp.reversed()).toList()
                    : txns.stream().sorted(cmp).toList();

            // paging
            int total = txns.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);
            int from = Math.max(0, (page - 1) * size);
            int to = Math.min(total, from + size);
            List<model.CashTxn> pageList = txns.subList(from, to);

            req.setAttribute("txList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page", page);
            req.setAttribute("pg_size", size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast", page >= pages);
            req.setAttribute("pg_single", pages <= 1);

        } catch (Exception e) {
            throw new ServletException("Lỗi tải giao dịch: " + e.getMessage(), e);
        }

        req.setAttribute("f_type", type == null ? "all" : type);
        req.setAttribute("f_q", q == null ? "" : q);
        req.setAttribute("f_status", status == null ? "all" : status);
        req.setAttribute("f_order", order == null ? "newest" : order);

        req.setAttribute("pageTitle", "Nạp / Rút");
        req.setAttribute("active", "cashs");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/cashs.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // ================== /admin/shops ==================
    private void handleShops(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q = clean(req.getParameter("q"));
        String status = clean(req.getParameter("status"));      // all | Active | Pending | Rejected | Banned...
        String from = clean(req.getParameter("from"));
        String to = clean(req.getParameter("to"));
        String sort = clean(req.getParameter("sort"));        // date_desc | date_asc | status_asc | status_desc
        if (sort == null || sort.isBlank()) {
            sort = "date_desc"; // giống KYC
        }
        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        LocalDate fromD = tryParseDate(from);
        LocalDate toD = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt = (toD == null) ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        try (Connection con = DBConnect.getConnection()) {
            ManageShopDAO dao = new ManageShopDAO(con);
            List<Shops> list = dao.getAllShops(); // TODO: chuyển DAO paging khi có

            // ===== Filter (giống tinh thần KYC) =====
            if (q != null && !q.isEmpty()) {
                final String qLower = q.toLowerCase();
                list = list.stream()
                        .filter(s
                                -> (s.getName() != null && s.getName().toLowerCase().contains(qLower))
                        || (s.getOwnerName() != null && s.getOwnerName().toLowerCase().contains(qLower))
                        )
                        .toList();
            }
            if (status != null && !"all".equalsIgnoreCase(status)) {
                final String st = status;
                list = list.stream()
                        .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(st))
                        .toList();
            }
            if (fromAt != null || toAt != null) {
                final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                final long toMs = (toAt == null ? Long.MAX_VALUE : toAt.getTime());
                list = list.stream().filter(s -> {
                    java.util.Date d = s.getCreatedAt();
                    long t = (d == null ? Long.MIN_VALUE : d.getTime());
                    return t >= fromMs && t <= toMs;
                }).toList();
            }

            // ===== Sort (đồng bộ cách của KYC) =====
            Comparator<Shops> byDate = Comparator.comparing(
                    s -> s.getCreatedAt() == null ? new java.util.Date(0) : s.getCreatedAt()
            );

            // rank trạng thái để sort status_asc/status_desc
            java.util.Map<String, Integer> rank = new java.util.HashMap<>();
            // Ưu tiên “đang chờ -> duyệt -> từ chối” tương tự 1/2/3 của KYC,
            // nhưng với string của Shop:
            rank.put("Pending", 1);
            rank.put("Active", 2);
            rank.put("Rejected", 3);
            rank.put("Banned", 4);

            Comparator<Shops> byStatus = Comparator.comparing(s -> {
                String st = (s.getStatus() == null ? "" : s.getStatus());
                return rank.getOrDefault(st, 9);
            });

            list = switch (sort) {
                case "date_asc" ->
                    list.stream().sorted(byDate).toList();
                case "status_asc" ->
                    list.stream().sorted(byStatus).toList();
                case "status_desc" ->
                    list.stream().sorted(byStatus.reversed()).toList();
                default ->
                    list.stream().sorted(byDate.reversed()).toList(); // date_desc
            };

            // ===== Paging =====
            int total = list.size();
            int pages = ceilDiv(total, size);
            page = clampPage(page, pages);

            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx = Math.min(total, fromIdx + size);
            List<Shops> pageList = list.subList(fromIdx, toIdx);

            req.setAttribute("shopList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page", page);
            req.setAttribute("pg_size", size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", page <= 1);
            req.setAttribute("pg_isLast", page >= pages);
            req.setAttribute("pg_single", pages <= 1);

        } catch (Exception e) {
            throw new ServletException(e);
        }

        // Giữ filter & sort cho JSP (giống KYC: date -> ISO yyyy-MM-dd)
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("status", status == null ? "all" : status);
        req.setAttribute("from", fromD == null ? "" : fromD.toString());
        req.setAttribute("to", toD == null ? "" : toD.toString());
        req.setAttribute("sort", sort);

        req.setAttribute("pageTitle", "Quản lý cửa hàng");
        req.setAttribute("active", "shops");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/shops.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    // ================== /admin/kycs ==================
    private void handleKycs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String q = clean(req.getParameter("q"));
        String status = clean(req.getParameter("status"));   // all | 1 | 2 | 3
        String from = clean(req.getParameter("from"));
        String to = clean(req.getParameter("to"));
        String sort = clean(req.getParameter("sort"));     // date_desc | date_asc | status_asc | status_desc
        if (sort == null || sort.isBlank()) {
            sort = "date_desc";
        }

        final int DEFAULT_SIZE = 8;
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }

        LocalDate fromD = tryParseDate(from);
        LocalDate toD = tryParseDate(to);
        Timestamp fromAt = (fromD == null) ? null : Timestamp.valueOf(fromD.atStartOfDay());
        Timestamp toAt = (toD == null) ? null : Timestamp.valueOf(toD.plusDays(1).atStartOfDay().minusSeconds(1));

        Integer statusId = null;
        if (status != null && !"all".equalsIgnoreCase(status)) {
            try {
                statusId = Integer.valueOf(status);
            } catch (Exception ignore) {
            }
        }

        try (Connection con = DBConnect.getConnection()) {
            ManageKycDAO dao = new ManageKycDAO(con);
            List<KycRequests> list = dao.getAllKycRequests();

            // filter theo tên
            if (q != null && !q.isEmpty()) {
                final String qLower = q.toLowerCase();
                list = list.stream()
                        .filter(k -> k.getUserName() != null && k.getUserName().toLowerCase().contains(qLower))
                        .toList();
            }
            // filter theo status
            if (statusId != null) {
                final int s = statusId;
                list = list.stream().filter(k -> k.getStatusId() == s).toList();
            }
            // filter theo ngày
            if (fromAt != null || toAt != null) {
                final long fromMs = (fromAt == null ? Long.MIN_VALUE : fromAt.getTime());
                final long toMs = (toAt == null ? Long.MAX_VALUE : toAt.getTime());
                list = list.stream().filter(k -> {
                    java.util.Date d = k.getCreatedAt();
                    long t = (d == null ? Long.MIN_VALUE : d.getTime());
                    return t >= fromMs && t <= toMs;
                }).toList();
            }

            // sort
            Comparator<KycRequests> byDate = Comparator.comparing(
                    k -> k.getCreatedAt() == null ? new java.util.Date(0) : k.getCreatedAt());
            Comparator<KycRequests> byStatus = Comparator.comparing(k -> {
                Integer v = k.getStatusId();
                return v == null ? 9 : switch (v) {
                    case 1 ->
                        1; // Pending
                    case 2 ->
                        2; // Approved
                    case 3 ->
                        3; // Rejected
                    default ->
                        9;
                };
            });

            list = switch (sort) {
                case "date_asc" ->
                    list.stream().sorted(byDate).toList();
                case "status_asc" ->
                    list.stream().sorted(byStatus).toList();
                case "status_desc" ->
                    list.stream().sorted(byStatus.reversed()).toList();
                default ->
                    list.stream().sorted(byDate.reversed()).toList(); // date_desc
            };

            // === Paging ===
            int total = list.size();
            int pages = (int) Math.ceil(total / (double) size);
            if (pages < 1) {
                pages = 1;
            }

            page = Math.max(1, Math.min(page, pages));
            int fromIdx = Math.max(0, (page - 1) * size);
            int toIdx = Math.min(total, fromIdx + size);
            List<KycRequests> pageList = list.subList(fromIdx, toIdx);

            // Chuẩn hoá URL ảnh (tuỳ chọn)
            for (KycRequests k : pageList) {
                k.setFrontImageUrl(normalizeKycImageUrl(req, k.getFrontImageUrl()));
                k.setBackImageUrl(normalizeKycImageUrl(req, k.getBackImageUrl()));
                k.setSelfieImageUrl(normalizeKycImageUrl(req, k.getSelfieImageUrl()));
            }

            // Cờ phân trang (tính SAU clamp)
            boolean isFirst = (page <= 1);
            boolean isLast = (page >= pages);
            boolean singlePage = (pages <= 1);

            // ĐẨY DỮ LIỆU XUỐNG JSP (đừng quên kycList!)
            req.setAttribute("kycList", pageList);
            req.setAttribute("pg_total", total);
            req.setAttribute("pg_page", page);
            req.setAttribute("pg_size", size);
            req.setAttribute("pg_pages", pages);
            req.setAttribute("pg_isFirst", isFirst);
            req.setAttribute("pg_isLast", isLast);
            req.setAttribute("pg_single", singlePage);
        } catch (Exception e) {
            throw new ServletException("Lỗi khi tải danh sách KYC: " + e.getMessage(), e);
        }

        // giữ lại filter cho JSP
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("status", status == null ? "all" : status);
        req.setAttribute("from", from == null ? "" : (tryParseDate(from) == null ? from : tryParseDate(from).toString()));
        req.setAttribute("to", to == null ? "" : (tryParseDate(to) == null ? to : tryParseDate(to).toString()));
        req.setAttribute("sort", sort);

        req.setAttribute("pageTitle", "Quản lý KYC");
        req.setAttribute("active", "kycs");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/kycs.jsp");
        System.out.printf(
                "[DEBUG-KYC] total=%d, size=%d, page=%d, pages=%d, isFirst=%s, isLast=%s, singlePage=%s%n",
                req.getAttribute("pg_total"),
                req.getAttribute("pg_size"),
                req.getAttribute("pg_page"),
                req.getAttribute("pg_pages"),
                req.getAttribute("pg_isFirst"),
                req.getAttribute("pg_isLast"),
                req.getAttribute("pg_single")
        );
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }

    private static String safe(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}
