package controller.dashboard;

import controller.seller.SellerBaseController;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.shop.ShopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Shops;
import service.ProductService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * Controller cho dashboard của seller. Hiển thị thống kê tổng sản phẩm, doanh thu tháng và đơn đã bán.
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();
    private final ProductDAO productDAO = new ProductDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    private final OrderDAO orderDAO = new OrderDAO();

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
            request.setAttribute("products", productService.homepageHighlights());
            request.setAttribute("totalProducts", 0);
            request.setAttribute("monthlyRevenue", BigDecimal.ZERO);
            request.setAttribute("completedOrders", 0);
            forward(request, response, "dashboard/index");
            return;
        }

        // Đếm tổng sản phẩm
        int totalProducts = productDAO.countByShopId(shop.getId());

        // Tính doanh thu tháng hiện tại
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp thisMonthStart = new Timestamp(cal.getTimeInMillis());

        cal.add(Calendar.MONTH, 1);
        Timestamp thisMonthEnd = new Timestamp(cal.getTimeInMillis());

        BigDecimal monthlyRevenue = orderDAO.getRevenueByShop(shop.getId(), thisMonthStart, thisMonthEnd);

        // Đếm số đơn đã bán (Completed)
        int completedOrders = orderDAO.countCompletedOrdersByShop(shop.getId());

        // Lấy sản phẩm của shop để hiển thị
        List<model.Products> products = productDAO.findByShopId(shop.getId());

        // Pass dữ liệu vào request
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("monthlyRevenue", monthlyRevenue);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("products", products);
        request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "dashboard/index");
    }

}
