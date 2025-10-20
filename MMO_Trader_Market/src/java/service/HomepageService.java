package service;

import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import model.Products;
import model.Shops;
import model.OrderStatus;
import model.view.MarketplaceSummary;

import java.math.BigDecimal;
import java.util.List;

public class HomepageService {

    private static final int SHOP_LIMIT = 4;

    private final ProductService productService = new ProductService();
    private final ShopDAO shopDAO = new ShopDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public List<Products> loadFeaturedProducts() {
        return productService.homepageHighlights();
    }

    public List<Shops> loadActiveShops() {
        return shopDAO.findActive(SHOP_LIMIT);
    }

    public MarketplaceSummary loadMarketplaceSummary() {
        long availableProducts = productService.countAvailableProducts();
        long pendingOrders = orderDAO.countByStatus(OrderStatus.PENDING);
        long completedOrders = orderDAO.countByStatus(OrderStatus.COMPLETED);
        BigDecimal revenue = orderDAO.sumRevenueByStatus(OrderStatus.COMPLETED);
        return new MarketplaceSummary(availableProducts, pendingOrders, completedOrders, revenue);
    }
}

