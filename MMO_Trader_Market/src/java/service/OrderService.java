package service;

import dao.order.OrderDAO;
import dao.user.BuyerDAO;
import model.Order;
import model.OrderStatus;
import model.PaginatedResult;
import model.Products;
import model.Users;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Contains the business rules for processing a buyer checkout request.
 * @version 1.0 21/05/2024
 */
public class OrderService {

    private static final Set<String> PURCHASABLE_STATUSES = Set.of("AVAILABLE");

    private static final EnumSet<OrderStatus> CREDENTIAL_VISIBLE_STATUSES =
            EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.DELIVERED, OrderStatus.REFUNDED);

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService();
    private final BuyerDAO buyerDAO = new BuyerDAO();

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
        items.forEach(this::enforceCredentialVisibility);
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
            case DELIVERED -> "badge";
            case CONFIRMED -> "badge";
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
            case DELIVERED -> "Đã bàn giao";
            case CONFIRMED -> "Đã xác nhận";
            case PROCESSING -> "Đang xử lý";
            case DISPUTED -> "Đang khiếu nại";
            case PENDING -> "Chờ xử lý";
            case FAILED -> "Thanh toán thất bại";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    public Order loadOrderDetail(int orderId, Users currentUser) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ.");
        }
        if (currentUser == null || currentUser.getId() == null) {
            throw new SecurityException("Người dùng chưa đăng nhập.");
        }
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (!belongsToBuyer(order, currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền truy cập đơn hàng này.");
        }
        clearCredentialData(order);
        if (shouldExposeCredentials(order.getStatus())) {
            orderDAO.getCredentials(orderId).ifPresent(credential -> {
                order.setActivationCode(credential.getActivationCode());
                order.setDeliveryLink(credential.getDeliveryLink());
            });
        }
        return order;
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

    private void enforceCredentialVisibility(Order order) {
        if (!shouldExposeCredentials(order.getStatus())) {
            clearCredentialData(order);
        }
    }

    private void clearCredentialData(Order order) {
        order.setActivationCode(null);
        order.setDeliveryLink(null);
    }

    private boolean shouldExposeCredentials(OrderStatus status) {
        if (status == null) {
            return false;
        }
        return CREDENTIAL_VISIBLE_STATUSES.contains(status);
    }

    private boolean belongsToBuyer(Order order, Integer userId) {
        if (userId == null) {
            return false;
        }
        return order.getBuyerId() == userId;
    }
}
