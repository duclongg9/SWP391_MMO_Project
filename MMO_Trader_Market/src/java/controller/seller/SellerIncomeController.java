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
        
        // Pass dữ liệu vào request
        request.setAttribute("thisMonthRevenue", thisMonthRevenue);
        request.setAttribute("growthText", growthText);
        request.setAttribute("pendingDisbursementCount", pendingDisbursementCount);
        request.setAttribute("cashFlowTransactions", cashFlowTransactions);
        request.setAttribute("pageTitle", "Thu nhập - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/income");
    }
}
