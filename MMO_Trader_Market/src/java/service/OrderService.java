package service;

import dao.order.OrderDAO;
import dao.user.BuyerDAO;
import model.Order;
import model.OrderStatus;
import model.PaginatedResult;
import model.Products;
import model.Users;
import queue.OrderQueueProducer;
import service.dto.OrderPlacementResult;
import service.dto.OrderStatusView;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Contains the business rules for processing a buyer checkout request.
 * @version 1.0 21/05/2024
 */
public class OrderService {

    private static final Set<String> PURCHASABLE_STATUSES = Set.of("AVAILABLE");

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService();
    private final BuyerDAO buyerDAO = new BuyerDAO();
    private final WalletService walletService = new WalletService();
    private final OrderQueueProducer orderQueueProducer = OrderQueueProducer.getInstance();

    public PaginatedResult<Order> listOrders(int page, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Số lượng mỗi trang phải lớn hơn 0.");
        }
        if (page < 1) {
            throw new IllegalArgumentException("Số trang phải lớn hơn hoặc bằng 1.");
        }
        int totalItems = orderDAO.countAll();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(page, totalPages);
        int offset = (currentPage - 1) * pageSize;
        List<Order> items = totalItems == 0
                ? List.of()
                : orderDAO.findAll(pageSize, offset);
        return new PaginatedResult<>(items, currentPage, totalPages, pageSize, totalItems);
    }

    /**
     * Ensures the product exists and has been approved before buyers can continue checkout.
     * @param productId identifier coming from the UI
     * @return the validated product
     */
    public Products validatePurchasableProduct(int productId) {
        Products product = productService.findOptionalById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm bạn chọn không tồn tại hoặc đã bị gỡ."));
        if (!isPurchasable(product.getStatus())) {
            throw new IllegalStateException("Sản phẩm hiện chưa sẵn sàng để bán.");
        }
        return product;
    }

    public OrderPlacementResult placeOrder(Users buyer, int productId, int quantity) {
        if (buyer == null || buyer.getId() == null) {
            throw new IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0.");
        }

        Products product = validatePurchasableProduct(productId);
        if (product.getInventoryCount() != null && product.getInventoryCount() < quantity) {
            throw new IllegalStateException("Sản phẩm không đủ tồn kho.");
        }

        int sellerId = productService.findOwnerIdByProduct(productId)
                .orElseThrow(() -> new IllegalStateException("Không xác định được chủ shop của sản phẩm."));
        if (buyer.getId().equals(sellerId)) {
            throw new IllegalStateException("Bạn không thể mua sản phẩm do chính mình bán.");
        }

        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        String orderToken = UUID.randomUUID().toString();

        Order pending;
        try {
            pending = orderDAO.insertPendingOrder(buyer.getId(), productId, quantity, total, orderToken);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể khởi tạo đơn hàng mới.", ex);
        }

        try {
            walletService.hold(buyer.getId(), sellerId, total, pending.getId(), orderToken);
        } catch (RuntimeException ex) {
            orderDAO.updateStatus(pending.getId(), orderToken, OrderStatus.FAILED);
            throw new IllegalStateException("Không thể giữ tiền thanh toán. Vui lòng thử lại sau.", ex);
        }

        orderQueueProducer.publish(pending.getId(), orderToken);

        return new OrderPlacementResult(pending.getId(), orderToken, product, quantity, pending.getStatus());
    }

    /**
     * Creates a new order after validating the buyer information and generates delivery assets.
     * @param productId the product that the buyer wants to purchase
     * @param buyerEmail email used to deliver digital goods
     * @param paymentMethod the payment option selected during checkout
     * @return the persisted order enriched with activation details
     */
    public Order createOrder(int productId, String buyerEmail, String paymentMethod) {
        if (buyerEmail == null || buyerEmail.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email nhận sản phẩm.");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        }
        Products product = validatePurchasableProduct(productId);
        Users buyer = resolveBuyer(buyerEmail.trim());
        try {
            return orderDAO.createOrder(product, buyer.getId(), paymentMethod.trim());
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo đơn hàng. Vui lòng thử lại sau.", ex);
        }
    }

    public OrderStatusView getOrderStatus(int orderId, String orderToken, Users currentUser) {
        if (orderToken == null || orderToken.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã theo dõi đơn hàng.");
        }
        Order order = orderDAO.findByIdAndToken(orderId, orderToken)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại hoặc đã hết hạn."));
        ensureOrderAccessibility(order, currentUser);
        return new OrderStatusView(order.getId(), orderToken, order.getStatus(),
                order.hasDeliveryInformation(), order.getActivationCode(), order.getDeliveryLink());
    }

    /**
     * Maps order status to the appropriate badge style.
     * @param status current order status
     * @return CSS class for the badge component
     */
    public String getStatusBadgeClass(OrderStatus status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case COMPLETED -> "badge";
            case PROCESSING, PENDING -> "badge badge--warning";
            case DISPUTED, FAILED -> "badge badge--danger";
            case REFUNDED -> "badge badge--ghost";
        };
    }

    /**
     * Converts the status enum to a localized label for display.
     * @param status current order status
     * @return user-friendly Vietnamese label
     */
    public String getFriendlyStatus(OrderStatus status) {
        if (status == null) {
            return "Không xác định";
        }
        return switch (status) {
            case COMPLETED -> "Hoàn thành";
            case PROCESSING -> "Đang xử lý";
            case DISPUTED -> "Đang khiếu nại";
            case PENDING -> "Chờ xử lý";
            case FAILED -> "Thanh toán thất bại";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    private Users resolveBuyer(String email) {
        Optional<Users> existing = buyerDAO.findActiveBuyerByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            return buyerDAO.createBuyer(email);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể khởi tạo người mua mới.", ex);
        }
    }

    private boolean isPurchasable(String status) {
        if (status == null) {
            return false;
        }
        return PURCHASABLE_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
    }

    private void ensureOrderAccessibility(Order order, Users currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        if (order.getBuyerId() != null && currentUser.getId().equals(order.getBuyerId())) {
            return;
        }
        int productId = order.getProduct() == null ? -1 : order.getProduct().getId();
        if (productId > 0) {
            Optional<Integer> ownerId = productService.findOwnerIdByProduct(productId);
            if (ownerId.isPresent() && ownerId.get().equals(currentUser.getId())) {
                return;
            }
        }
        throw new SecurityException("Bạn không có quyền truy cập đơn hàng này.");
    }
}
