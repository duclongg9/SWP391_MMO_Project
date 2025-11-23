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

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 12;

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

        // Lấy tham số tìm kiếm và phân trang
        String keyword = request.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        
        int page = parsePage(request.getParameter("page"));
        int pageSize = parsePageSize(request.getParameter("size"));
        
        // Đếm tổng sản phẩm của seller (từ tất cả shops) - có hoặc không có keyword
        int totalProducts = productDAO.countByOwnerId(userId, keyword);
        
        // Tính tổng số trang
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;
        
        // Tính offset
        int offset = (page - 1) * pageSize;

        // Tính doanh thu tháng hiện tại của seller (từ tất cả shops)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp thisMonthStart = new Timestamp(cal.getTimeInMillis());

        cal.add(Calendar.MONTH, 1);
        Timestamp thisMonthEnd = new Timestamp(cal.getTimeInMillis());

        BigDecimal monthlyRevenue = orderDAO.getRevenueByOwner(userId, thisMonthStart, thisMonthEnd);

        // Đếm số đơn đã bán (Completed) của seller (từ tất cả shops)
        int completedOrders = orderDAO.countCompletedOrdersByOwner(userId);
        
        // Tính tổng tồn kho của seller (từ tất cả shops)
        int totalInventory = productDAO.getTotalInventoryByOwnerId(userId);

        // Lấy sản phẩm của seller (từ tất cả shops) để hiển thị (có phân trang và tìm kiếm)
        List<model.Products> products = productDAO.findByOwnerId(userId, keyword, pageSize, offset);

        // Pass dữ liệu vào request
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("monthlyRevenue", monthlyRevenue);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("totalInventory", totalInventory);
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "dashboard/index");
    }
    
    private int parsePage(String pageStr) {
        if (pageStr == null || pageStr.trim().isEmpty()) {
            return DEFAULT_PAGE;
        }
        try {
            int page = Integer.parseInt(pageStr.trim());
            return page < 1 ? DEFAULT_PAGE : page;
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE;
        }
    }
    
    private int parsePageSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int size = Integer.parseInt(sizeStr.trim());
            return size < 1 ? DEFAULT_PAGE_SIZE : size;
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE_SIZE;
        }
    }

}
