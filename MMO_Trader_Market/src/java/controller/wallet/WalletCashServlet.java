package controller.wallet;

import dao.user.UserCashDAO;
import dao.connect.DBConnect;
import model.CashTxn;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "WalletCashServlet", urlPatterns = {"/wallet/cash"})
public class WalletCashServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Kiểm tra đăng nhập ---
        HttpSession session = request.getSession(false);
        Integer userId = (session == null) ? null : (Integer) session.getAttribute("userId");

        System.out.println("[WalletCash] session=" + (session == null ? "null" : session.getId())
                + ", userId=" + (userId == null ? "null" : userId));

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // --- 2. Đọc & chuẩn hóa tham số lọc ---
        String type   = nv(request.getParameter("type"), "all");
        String status = nv(request.getParameter("status"), "all");
        String preset = trimOrNull(request.getParameter("preset"));
        String sort   = nv(request.getParameter("sort"), "date_desc");

        BigDecimal minAmount = parseBigDecimal(request.getParameter("minAmount"));
        BigDecimal maxAmount = parseBigDecimal(request.getParameter("maxAmount"));
        Date start = parseSqlDate(request.getParameter("start"));
        Date end   = parseSqlDate(request.getParameter("end"));

        if (preset != null && !preset.isBlank()) {
            LocalDate today = LocalDate.now();
            LocalDate s = null, e = null;
            switch (preset) {
                case "today" -> { s = today; e = today; }
                case "7d"    -> { s = today.minusDays(6); e = today; }
                case "30d"   -> { s = today.minusDays(29); e = today; }
            }
            if (s != null && e != null) {
                start = Date.valueOf(s);
                end = Date.valueOf(e);
            }
        }

        if (start != null && end != null && start.after(end)) start = end;

        int page = Math.max(1, parseInt(request.getParameter("page"), 1));
        int size = Math.max(1, parseInt(request.getParameter("size"), 10));

        System.out.println("[WalletCash] filters -> userId=" + userId
                + ", type=" + type + ", status=" + status
                + ", min=" + minAmount + ", max=" + maxAmount
                + ", start=" + start + ", end=" + end
                + ", sort=" + sort + ", page=" + page + ", size=" + size);

        // --- 3. Lấy dữ liệu chỉ của user hiện tại ---
        List<CashTxn> all;
        try (Connection con = DBConnect.getConnection()) {
            UserCashDAO dao = new UserCashDAO(con);
            all = dao.listUserTransactionsFiltered(
                    userId, type, status, minAmount, maxAmount, start, end, sort
            );
        } catch (Exception ex) {
            System.out.println("[WalletCash][ERROR] " + ex.getMessage());
            request.setAttribute("emg", "Không tải được danh sách giao dịch: " + ex.getMessage());
            all = new ArrayList<>();
        }

        // --- 4. Phân trang ---
        int total = all.size();
        int pages = Math.max(1, (int) Math.ceil(total / (double) size));
        if (page > pages) page = pages;

        int fromIdx = Math.max(0, (page - 1) * size);
        int toIdx   = Math.min(total, fromIdx + size);
        List<CashTxn> pageData = (fromIdx < toIdx) ? all.subList(fromIdx, toIdx) : new ArrayList<>();

        // --- 5. Gửi dữ liệu xuống JSP ---
        request.setAttribute("cashList", pageData);
        request.setAttribute("page", page);
        request.setAttribute("size", size);
        request.setAttribute("pages", pages);

        // --- 6. Forward sang JSP ---
        request.getRequestDispatcher("/WEB-INF/views/wallet/wallet-cash.jsp").forward(request, response);
    }

    // -------- Helpers --------
    private static String nv(String s, String def) {
        return (s == null || s.isBlank()) ? def : s.trim();
    }
    private static String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
    private static BigDecimal parseBigDecimal(String s) {
        try { return (s == null || s.isBlank()) ? null : new BigDecimal(s.trim()); }
        catch (Exception e) { return null; }
    }
    private static Date parseSqlDate(String s) {
        try { return (s == null || s.isBlank()) ? null : Date.valueOf(s.trim()); }
        catch (Exception e) { return null; }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
