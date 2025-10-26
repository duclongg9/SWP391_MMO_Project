package controller.seller;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Orders;
import model.Shops;
import service.SellerService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xem đơn hàng bán ra của seller.
 * 
 * @version 1.0
 * @author AI Assistant
 */
@WebServlet(name = "SellerOrderController", urlPatterns = {"/seller/orders"})
public class SellerOrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int ROLE_SELLER = 2;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

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

        // Kiểm tra shop
        Optional<Shops> shopOpt = sellerService.getSellerShop(sellerId);
        if (shopOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/seller/shop/create");
            return;
        }

        int shopId = shopOpt.get().getId();
        listOrders(request, response, shopId);
    }

    /**
     * Hiển thị danh sách đơn hàng.
     */
    private void listOrders(HttpServletRequest request, HttpServletResponse response, int shopId)
            throws ServletException, IOException {
        
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);
        String status = normalize(request.getParameter("status"));

        // Lấy đơn hàng
        List<Orders> orders = sellerService.getOrders(shopId, status, page, size);
        long totalOrders = sellerService.countOrders(shopId, status);
        int totalPages = (int) Math.ceil((double) totalOrders / size);

        // Lấy thống kê đơn hàng theo trạng thái
        Map<String, Long> orderStats = sellerService.getOrderStatsByStatus(shopId);

        // Tính doanh thu
        java.math.BigDecimal revenue = sellerService.getRevenue(shopId);

        request.setAttribute("orders", orders);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("status", status != null ? status : "");
        request.setAttribute("orderStats", orderStats);
        request.setAttribute("revenue", revenue);
        request.setAttribute("pageTitle", "Quản Lý Đơn Hàng");

        forward(request, response, "seller/orders");
    }

    /**
     * Parse integer từ string, trả về -1 nếu không hợp lệ.
     */
    private int parsePositiveInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Parse integer từ string, trả về defaultValue nếu không hợp lệ.
     */
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        int parsed = parsePositiveInt(value);
        return parsed > 0 ? parsed : defaultValue;
    }

    /**
     * Normalize string.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
