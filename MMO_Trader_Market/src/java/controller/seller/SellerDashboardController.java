package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Shops;
import service.SellerService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho trang dashboard của Seller.
 * Hiển thị tổng quan về shop, sản phẩm và đơn hàng.
 * 
 * @version 1.0
 * @author AI Assistant
 */
@WebServlet(name = "SellerDashboardController", urlPatterns = {"/seller", "/seller/dashboard"})
public class SellerDashboardController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int ROLE_SELLER = 2;

    private final SellerService sellerService = new SellerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (!isSellerSession(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        Integer sellerId = (Integer) session.getAttribute("userId");
        if (sellerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Kiểm tra xem seller đã có shop chưa
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            // Chưa có shop, redirect đến trang tạo shop
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        Shops shop = shopOpt.get();
        int shopId = shop.getId();

        // Lấy thống kê
        Map<String, Object> stats = sellerService.getDashboardStats(shopId);

        // Set attributes
        request.setAttribute("shop", shop);
        request.setAttribute("stats", stats);
        request.setAttribute("pageTitle", "Dashboard - Quản lý Shop");

        forward(request, response, "seller/dashboard");
    }

    /**
     * Kiểm tra session có phải của seller không.
     */
    private boolean isSellerSession(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && ROLE_SELLER == role;
    }
}
