package controller.seller;

import dao.order.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import model.ShopStatsView;
import model.statistics.BestSellerProduct;
import model.statistics.QuarterRevenue;
import model.view.OrderRow;
import service.ShopService;

/**
 * Trang chi tiết từng shop dành cho seller.
 * <p>
 * Hiển thị doanh thu theo quý, sản phẩm bán chạy và danh sách đơn hoàn thành.
 * </p>
 */
@WebServlet(name = "SellerShopDetailController", urlPatterns = {"/seller/shops/detail"})
public class SellerShopDetailController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 8;

    private final ShopService shopService = new ShopService();

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int shopId = parseIntOrDefault(request.getParameter("id"), -1);
        if (shopId <= 0) {
            redirectWithError(session, response, request.getContextPath());
            return;
        }

        ShopStatsView shop;
        try {
            Optional<ShopStatsView> detail = shopService.findDetailWithStats(shopId, userId);
            if (detail.isEmpty()) {
                redirectWithError(session, response, request.getContextPath());
                return;
            }
            shop = detail.get();
        } catch (SQLException ex) {
            session.setAttribute("flashMessage", "Không thể tải thông tin shop. Vui lòng thử lại sau.");
            session.setAttribute("flashType", "danger");
            response.sendRedirect(request.getContextPath() + "/seller/shops");
            return;
        }

        int range = parseRange(request.getParameter("range"));
        List<QuarterRevenue> quarterlyRevenue = shopService.getQuarterlyRevenue(shop.getId(), range);
        BigDecimal quarterTotal = quarterlyRevenue.stream()
                .map(QuarterRevenue::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<BestSellerProduct> bestSeller = shopService.findBestSellerProduct(shop.getId());

        int completedOrders = orderDAO.countCompletedOrdersByShop(shop.getId());

        int page = parseIntOrDefault(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }
        int pageSize = parseIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        int offset = (page - 1) * pageSize;
        List<OrderRow> orders = orderDAO.findCompletedOrdersByShop(shop.getId(), pageSize, offset);
        int totalPages = (int) Math.ceil(completedOrders / (double) pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
            offset = (page - 1) * pageSize;
            orders = orderDAO.findCompletedOrdersByShop(shop.getId(), pageSize, offset);
        }

        request.setAttribute("shop", shop);
        request.setAttribute("quarterRevenue", quarterlyRevenue);
        request.setAttribute("quarterRevenueTotal", quarterTotal);
        request.setAttribute("selectedRange", range);
        request.setAttribute("bestSeller", bestSeller.orElse(null));
        request.setAttribute("completedOrdersCount", completedOrders);
        request.setAttribute("orders", orders);
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalOrders", completedOrders);

        request.setAttribute("pageTitle", "Chi tiết shop - " + shop.getName());
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");

        forward(request, response, "seller/shops/detail");
    }

    /**
     * Ghi nhận thông báo lỗi vào session và chuyển hướng về danh sách shop.
     */
    private void redirectWithError(HttpSession session, HttpServletResponse response, String contextPath)
            throws IOException {
        session.setAttribute("flashMessage", "Shop không hợp lệ hoặc bạn không có quyền truy cập.");
        session.setAttribute("flashType", "danger");
        response.sendRedirect(contextPath + "/seller/shops");
    }

    /**
     * Chuẩn hóa tham số khoảng thời gian doanh thu.
     */
    private int parseRange(String raw) {
        int range = parseIntOrDefault(raw, 3);
        return switch (range) {
            case 6, 12 -> range;
            default -> 3;
        };
    }

    /**
     * Parse số nguyên với giá trị mặc định khi rỗng hoặc không hợp lệ.
     */
    private int parseIntOrDefault(String raw, int defaultValue) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}

