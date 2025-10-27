package service;

import dao.message.ConversationMessageDAO;
import dao.order.OrderDAO;
import dao.shop.ShopDAO;
import dao.system.SystemConfigDAO;
import dao.user.BuyerDAO;
import model.Shops;
import model.SystemConfigs;
import model.Users;
import model.view.ConversationMessageView;
import model.view.CustomerProfileView;
import model.view.MarketplaceSummary;
import model.view.product.ProductCategorySummary;
import model.view.product.ProductSummaryView;
import model.view.product.ProductTypeOption;
import model.OrderStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Dịch vụ tổng hợp dữ liệu cho trang chủ: sản phẩm nổi bật, shop hoạt động,
 * thống kê marketplace.</p>
 * <p>
 * Lớp này điều phối nhiều DAO (sản phẩm, shop, đơn hàng, tin nhắn) và
 * {@link ProductService} để chuẩn bị view model trước khi controller đẩy sang
 * JSP.</p>
 *
 * @author longpdhe171902
 */
public class HomepageService {

    private static final int SHOP_LIMIT = 4;
    private static final int MESSAGE_LIMIT = 3;

    private final ProductService productService = new ProductService();
    private final ShopDAO shopDAO = new ShopDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final BuyerDAO buyerDAO = new BuyerDAO();
    private final ConversationMessageDAO conversationMessageDAO = new ConversationMessageDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    /**
     * Lấy danh sách sản phẩm nổi bật từ
     * {@link ProductService#getHomepageHighlights()}.
     */
    public List<ProductSummaryView> loadFeaturedProducts() {
        return productService.getHomepageHighlights();
    }

    /**
     * Truy vấn các shop đang hoạt động để hiển thị trong carousel.
     */
    public List<Shops> loadActiveShops() {
        return shopDAO.findActive(SHOP_LIMIT);
    }

    /**
     * Tính toán số sản phẩm theo từng loại cho menu lọc nhanh.
     */
    public List<ProductCategorySummary> loadProductCategories() {
        return productService.getHomepageCategories();
    }

    /**
     * Tổng hợp thống kê chung về marketplace: đơn hoàn tất, shop và buyer hoạt
     * động.
     */
    public MarketplaceSummary loadMarketplaceSummary() {
        long completedOrders = orderDAO.countByStatus(OrderStatus.COMPLETED);
        long activeShops = shopDAO.countActive();
        long activeBuyers = buyerDAO.countActiveBuyers();
        return new MarketplaceSummary(completedOrders, activeShops, activeBuyers);
    }

    /**
     * Lấy thông tin khách hàng tiêu biểu dựa theo số lượng đơn hoàn tất.
     */
    public CustomerProfileView loadHighlightedBuyer() {
        return buyerDAO.findTopBuyerByCompletedOrders()
                .map(this::buildProfile)
                .orElse(null);
    }

    /**
     * Lấy danh sách tin nhắn gần nhất để hiển thị bằng block testimonial.
     */
    public List<ConversationMessageView> loadRecentMessages() {
        return conversationMessageDAO.findLatest(MESSAGE_LIMIT);
    }

    /**
     * Lấy ghi chú/hướng dẫn hệ thống để hiển thị cho người dùng mới.
     */
    public List<SystemConfigs> loadSystemNotes() {
        return systemConfigDAO.findAll();
    }

    /**
     * Load danh sách loại sản phẩm phục vụ dropdown bộ lọc ở trang chủ.
     */
    public List<ProductTypeOption> loadFilterTypeOptions() {
        return productService.getTypeOptions();
    }

    /**
     * Dựng view model khách hàng tiêu biểu từ bản ghi {@link Users} kết hợp
     * thống kê đơn hàng.
     */
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

    /**
     * Chuyển đổi {@link Date} sang {@link LocalDate} để đồng nhất với các view
     * khác.
     */
    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Làm tròn điểm hài lòng tới 1 chữ số thập phân.
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
