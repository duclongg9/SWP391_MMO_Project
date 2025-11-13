package admin;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.AdnimDashboardService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends AbstractAdminServlet {

    private final AdnimDashboardService adminDashboard = new AdnimDashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        LocalDate now = LocalDate.now();

        // 1. Năm có dữ liệu
        List<Integer> availableYears = new ArrayList<>();
        try {
            availableYears = adminDashboard.getAvailableYears();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2. Năm được chọn
        String yearParam = req.getParameter("year");
        int selectedYear;
        try {
            if (yearParam != null && !yearParam.isBlank()) {
                selectedYear = Integer.parseInt(yearParam.trim());
            } else {
                selectedYear = now.getYear();
            }
        } catch (NumberFormatException e) {
            selectedYear = now.getYear();
        }
        if (!availableYears.isEmpty() && !availableYears.contains(selectedYear)) {
            selectedYear = now.getYear();
        }

        // 3. Dữ liệu theo tháng
        BigDecimal[] listDeposit = new BigDecimal[12];
        BigDecimal[] listWithdraw = new BigDecimal[12];
        int[] listOrder = new int[12];
        int[] listUser = new int[12];

        try {
            listDeposit = adminDashboard.arrayDepositByMonth(selectedYear);
            listWithdraw = adminDashboard.arrayWithdrawByMonth(selectedYear);
            listOrder = adminDashboard.arrayOrderByMonth(selectedYear);
            listUser = adminDashboard.arrayUserByMonth(selectedYear);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Gson gson = new Gson();
        req.setAttribute("depositByMonth", gson.toJson(listDeposit));
        req.setAttribute("withdrawByMonth", gson.toJson(listWithdraw));
        req.setAttribute("orderByMonth", gson.toJson(listOrder));
        req.setAttribute("userByMonth", gson.toJson(listUser));

        // 4. Tháng hiện tại & trước đó
        int presentMonth = now.getMonthValue();
        int presentYear = now.getYear();
        int lastMonth = (presentMonth == 1) ? 12 : presentMonth - 1;
        int lastYear = (presentMonth == 1) ? presentYear - 1 : presentYear;

        // 5. Tổng deposit/withdraw
        BigDecimal totalDepositLastMonth = BigDecimal.ZERO;
        BigDecimal totalWithdrawLastMonth = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdraw = BigDecimal.ZERO;

        try {
            totalDepositLastMonth = adminDashboard.totalDeposite(lastMonth, lastYear);
            totalWithdrawLastMonth = adminDashboard.totalWithdraw(lastMonth, lastYear);
            totalDeposit = adminDashboard.totalDeposite(presentMonth, presentYear);
            totalWithdraw = adminDashboard.totalWithdraw(presentMonth, presentYear);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 6. Thống kê khác
        int orderByMonth = 0;
        int shopByMonth = 0;
        int totalUser = 0;
        int shopPending = 0;
        int shopSuspended = 0;
        try {
            orderByMonth = adminDashboard.totalOrder(presentMonth, presentYear);
            shopByMonth = adminDashboard.totalActiveShop();
            totalUser = adminDashboard.totalActiveUser();
            shopPending = adminDashboard.totalPendingShop();
            shopSuspended = adminDashboard.totalSuspendedShop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 7. % thay đổi (giữ nguyên cách gọi units.PercentChange của bạn)
        BigDecimal percentDepositChanged =
                units.PercentChange.calculatePercentChange(totalDeposit, totalDepositLastMonth);
        BigDecimal percentWithdrawChanged =
                units.PercentChange.calculatePercentChange(totalWithdraw, totalWithdrawLastMonth);

        // 8. Set attribute
        req.setAttribute("years", availableYears);
        req.setAttribute("selectedYear", selectedYear);

        req.setAttribute("totalOrder", orderByMonth);
        req.setAttribute("shopByMonth", shopByMonth);
        req.setAttribute("totalUser", totalUser);
        req.setAttribute("totalDeposit", totalDeposit);
        req.setAttribute("totalWithdraw", totalWithdraw);
        req.setAttribute("persentDepositChanged", percentDepositChanged);
        req.setAttribute("persentWithdrawChanged", percentWithdrawChanged);

        req.setAttribute("totalActiveShop", shopByMonth);
        req.setAttribute("totalPendingShop", shopPending);
        req.setAttribute("totalSuspended", shopSuspended);

        req.setAttribute("pageTitle", "Tổng quan hệ thống");
        req.setAttribute("active", "dashboard");
        req.setAttribute("content", "/WEB-INF/views/Admin/pages/dashboard.jsp");
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }
}
