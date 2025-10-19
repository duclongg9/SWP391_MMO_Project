package service;

import dao.message.ConversationMessageDAO;
import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import dao.system.SystemConfigDAO;
import dao.user.BuyerDAO;
import model.Products;
import model.Shops;
import model.SystemConfigs;
import model.Users;
import model.view.ConversationMessageView;
import model.view.CustomerProfileView;
import model.view.MarketplaceSummary;
import model.OrderStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomepageService {

    private static final int SHOP_LIMIT = 4;
    private static final int MESSAGE_LIMIT = 3;

    private final ProductService productService = new ProductService();
    private final ShopDAO shopDAO = new ShopDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final BuyerDAO buyerDAO = new BuyerDAO();
    private final ConversationMessageDAO conversationMessageDAO = new ConversationMessageDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    public List<Products> loadFeaturedProducts() {
        return productService.homepageHighlights();
    }

    public List<Shops> loadActiveShops() {
        return shopDAO.findActive(SHOP_LIMIT);
    }

    public MarketplaceSummary loadMarketplaceSummary() {
        long completedOrders = orderDAO.countByStatus(OrderStatus.COMPLETED);
        long activeShops = shopDAO.countActive();
        long activeBuyers = buyerDAO.countActiveBuyers();
        return new MarketplaceSummary(completedOrders, activeShops, activeBuyers);
    }

    public CustomerProfileView loadHighlightedBuyer() {
        return buyerDAO.findTopBuyerByCompletedOrders()
                .map(this::buildProfile)
                .orElse(null);
    }

    public List<ConversationMessageView> loadRecentMessages() {
        return conversationMessageDAO.findLatest(MESSAGE_LIMIT);
    }

    public List<SystemConfigs> loadSystemNotes() {
        return systemConfigDAO.findAll();
    }

    private CustomerProfileView buildProfile(Users buyer) {
        long totalOrders = orderDAO.countByBuyer(buyer.getId());
        long completedOrders = orderDAO.countByBuyerAndStatus(buyer.getId(), OrderStatus.COMPLETED);
        long disputedOrders = orderDAO.countByBuyerAndStatus(buyer.getId(), OrderStatus.DISPUTED);
        double satisfaction = totalOrders == 0 ? 0 : roundToOneDecimal((completedOrders * 5.0) / totalOrders);
        LocalDate joinDate = toLocalDate(buyer.getCreatedAt());
        return new CustomerProfileView(
                Objects.toString(buyer.getName(), buyer.getEmail()),
                buyer.getEmail(),
                joinDate,
                totalOrders,
                completedOrders,
                disputedOrders,
                satisfaction);
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

