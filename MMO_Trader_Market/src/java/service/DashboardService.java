package service;

import dao.order.OrderDAO;
import dao.product.ProductDAO;
import model.Products;
import service.dto.DashboardOverview;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class DashboardService {

    private static final int FEATURED_LIMIT = 6;

    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    public DashboardService() {
        this(new ProductDAO(), new OrderDAO());
    }

    public DashboardService(ProductDAO productDAO, OrderDAO orderDAO) {
        this.productDAO = Objects.requireNonNull(productDAO, "productDAO must not be null");
        this.orderDAO = Objects.requireNonNull(orderDAO, "orderDAO must not be null");
    }

    public DashboardOverview getOverview(int ownerId) {
        long totalProducts = productDAO.countAllByOwner(ownerId);
        long pendingOrders = orderDAO.countPendingByOwner(ownerId);
        LocalDate today = LocalDate.now();
        BigDecimal monthlyRevenue = orderDAO.sumMonthlyRevenueByOwner(ownerId, today.getYear(), today.getMonthValue());
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }
        List<Products> featured = List.copyOf(productDAO.findFeaturedByOwner(ownerId, FEATURED_LIMIT));
        return new DashboardOverview(totalProducts, pendingOrders, monthlyRevenue, featured);
    }
}
