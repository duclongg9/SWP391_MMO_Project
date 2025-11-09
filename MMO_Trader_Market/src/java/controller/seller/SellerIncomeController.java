package controller.seller;

import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Shops;
import model.WalletTransactions;
import model.Wallets;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * Trang thống kê thu nhập cho người bán.
 */
@WebServlet(name = "SellerIncomeController", urlPatterns = {"/seller/income"})
public class SellerIncomeController extends SellerBaseController {

    private static final long serialVersionUID = 1L;
    
    private final ShopDAO shopDAO = new ShopDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final WalletsDAO walletsDAO = new WalletsDAO();
    private final WalletTransactionDAO walletTransactionDAO = new WalletTransactionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Lấy shop của seller
        Shops shop = shopDAO.findByOwnerId(userId);
        if (shop == null) {
            request.setAttribute("errorMessage", "Bạn chưa có cửa hàng.");
            request.setAttribute("pageTitle", "Thu nhập - Quản lý cửa hàng");
            request.setAttribute("bodyClass", "layout");
            request.setAttribute("headerModifier", "layout__header--split");
            forward(request, response, "seller/income");
            return;
        }
        
        // Tính toán thời gian: tháng hiện tại và tháng trước
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp thisMonthStart = new Timestamp(cal.getTimeInMillis());
        
        cal.add(Calendar.MONTH, 1);
        Timestamp thisMonthEnd = new Timestamp(cal.getTimeInMillis());
        
        cal.add(Calendar.MONTH, -2);
        Timestamp lastMonthStart = new Timestamp(cal.getTimeInMillis());
        
        // Tính doanh thu tháng này
        BigDecimal thisMonthRevenue = orderDAO.getRevenueByShop(shop.getId(), thisMonthStart, thisMonthEnd);
        
        // Tính doanh thu tháng trước
        BigDecimal lastMonthRevenue = orderDAO.getRevenueByShop(shop.getId(), lastMonthStart, thisMonthStart);
        
        // Tính tăng trưởng (%)
        String growthText = "0% so với tháng trước";
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = thisMonthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            growthText = String.format("%+.0f%% so với tháng trước", growth.doubleValue());
        } else if (thisMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthText = "+100% so với tháng trước";
        }
        
        // Đếm đơn chờ giải ngân
        int pendingDisbursementCount = orderDAO.countPendingDisbursementOrders(shop.getId());
        
        // Lấy wallet của seller để lấy transactions
        Wallets wallet = walletsDAO.getUserWallet(userId);
        List<WalletTransactions> cashFlowTransactions = List.of();
        if (wallet != null) {
            // Lấy 10 giao dịch gần nhất
            List<WalletTransactions> transactions = walletTransactionDAO.getListWalletTransactionPaging(
                    wallet.getId(), 1, 10, null, null, null, null, null);
            if (transactions != null) {
                cashFlowTransactions = transactions;
            }
        }
        
        // Lấy tham số period (day, week, month, year) - mặc định là month
        String period = request.getParameter("period");
        if (period == null || period.trim().isEmpty()) {
            period = "month";
        }
        if (!period.matches("day|week|month|year")) {
            period = "month";
        }
        
        // Tính toán khoảng thời gian dựa trên period
        Timestamp statsStartDate = null;
        Timestamp statsEndDate = null;
        Calendar statsCal = Calendar.getInstance();
        
        switch (period) {
            case "day":
                // 30 ngày gần nhất
                statsCal.add(Calendar.DAY_OF_MONTH, -30);
                statsStartDate = new Timestamp(statsCal.getTimeInMillis());
                statsCal = Calendar.getInstance();
                statsCal.add(Calendar.DAY_OF_MONTH, 1);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsEndDate = new Timestamp(statsCal.getTimeInMillis());
                break;
            case "week":
                // 12 tuần gần nhất
                statsCal.add(Calendar.WEEK_OF_YEAR, -12);
                statsStartDate = new Timestamp(statsCal.getTimeInMillis());
                statsCal = Calendar.getInstance();
                statsCal.add(Calendar.WEEK_OF_YEAR, 1);
                statsCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsEndDate = new Timestamp(statsCal.getTimeInMillis());
                break;
            case "month":
                // 12 tháng gần nhất
                statsCal.add(Calendar.MONTH, -12);
                statsCal.set(Calendar.DAY_OF_MONTH, 1);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsStartDate = new Timestamp(statsCal.getTimeInMillis());
                statsCal = Calendar.getInstance();
                statsCal.add(Calendar.MONTH, 1);
                statsCal.set(Calendar.DAY_OF_MONTH, 1);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsEndDate = new Timestamp(statsCal.getTimeInMillis());
                break;
            case "year":
                // 5 năm gần nhất
                statsCal.add(Calendar.YEAR, -5);
                statsCal.set(Calendar.MONTH, 0);
                statsCal.set(Calendar.DAY_OF_MONTH, 1);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsStartDate = new Timestamp(statsCal.getTimeInMillis());
                statsCal = Calendar.getInstance();
                statsCal.add(Calendar.YEAR, 1);
                statsCal.set(Calendar.MONTH, 0);
                statsCal.set(Calendar.DAY_OF_MONTH, 1);
                statsCal.set(Calendar.HOUR_OF_DAY, 0);
                statsCal.set(Calendar.MINUTE, 0);
                statsCal.set(Calendar.SECOND, 0);
                statsCal.set(Calendar.MILLISECOND, 0);
                statsEndDate = new Timestamp(statsCal.getTimeInMillis());
                break;
        }
        
        // Lấy thống kê theo sản phẩm
        java.util.List<model.statistics.ProductStatistics> productStats = new java.util.ArrayList<>();
        try {
            productStats = orderDAO.getProductStatistics(
                    shop.getId(), statsStartDate, statsEndDate);
        } catch (Exception e) {
            System.err.println("Error getting product statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Lấy thống kê theo thời gian
        java.util.List<model.statistics.TimeStatistics> timeStats = new java.util.ArrayList<>();
        try {
            timeStats = orderDAO.getTimeStatistics(
                    shop.getId(), period, statsStartDate, statsEndDate);
        } catch (Exception e) {
            System.err.println("Error getting time statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Pass dữ liệu vào request
        request.setAttribute("thisMonthRevenue", thisMonthRevenue);
        request.setAttribute("growthText", growthText);
        request.setAttribute("pendingDisbursementCount", pendingDisbursementCount);
        request.setAttribute("cashFlowTransactions", cashFlowTransactions);
        request.setAttribute("period", period);
        request.setAttribute("productStats", productStats);
        request.setAttribute("timeStats", timeStats);
        request.setAttribute("pageTitle", "Thu nhập - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/income");
    }
}
