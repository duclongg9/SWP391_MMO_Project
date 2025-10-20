package service;

import dao.order.OrderDAO;
import model.Order;
import model.OrderStatus;
import model.PaginatedResult;
import model.Products;
import model.Users;
import queue.OrderQueueProducer;
import service.dto.OrderDetailView;
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
 */
public class OrderService {

    private static final Set<String> ALLOWED_PRODUCT_STATUSES = Set.of("APPROVED");
    private static final Set<Integer> PURCHASER_ROLES = Set.of(2, 3); // 2: Seller, 3: Buyer

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService();
    private final WalletService walletService = new WalletService();
    private final OrderQueueProducer orderQueueProducer = OrderQueueProducer.getInstance();

    public PaginatedResult<Order> listOrders(int buyerId, int page, int pageSize, OrderStatus status) {
        if (buyerId <= 0) {
            throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Số lượng mỗi trang phải lớn hơn 0.");
        }
        int safePage = Math.max(page, 1);
        int totalItems = orderDAO.countByBuyer(buyerId, status);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages > 0 && safePage > totalPages) {
            safePage = totalPages;
        }
        int offset = (safePage - 1) * pageSize;
        List<Order> items = totalItems == 0
                ? List.of()
                : orderDAO.findByBuyer(buyerId, status, pageSize, offset);
        return new PaginatedResult<>(items, safePage, totalPages, pageSize, totalItems);
    }

    public Products validatePurchasableProduct(int productId) {
        Products product = productService.findOptionalById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm bạn chọn không tồn tại hoặc đã bị gỡ."));
        if (!isPurchasable(product.getStatus())) {
            throw new IllegalStateException("Sản phẩm hiện chưa sẵn sàng để bán.");
        }
        return product;
    }

    public OrderPlacementResult placeOrder(Users buyer, int productId, int quantity, String existingToken) {
        if (buyer == null || buyer.getId() == null) {
            throw new IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        if (!PURCHASER_ROLES.contains(buyer.getRoleId())) {
            throw new IllegalStateException("Tài khoản của bạn không được phép mua hàng.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0.");
        }

        String normalizedToken = existingToken == null ? null : existingToken.trim();
        if (normalizedToken != null && !normalizedToken.isEmpty()) {
            Optional<Order> existing = orderDAO.findByTokenAndBuyer(normalizedToken, buyer.getId());
            if (existing.isPresent()) {
                Order current = existing.get();
                return new OrderPlacementResult(current.getId(), current.getOrderToken(),
                        current.getProduct(), quantityOf(current, quantity), current.getStatus());
            }
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
            var holdRecord = walletService.hold(buyer.getId(), sellerId, total, pending.getId(), orderToken);
            orderDAO.updatePaymentTransaction(pending.getId(), holdRecord.transactionId());
        } catch (RuntimeException ex) {
            orderDAO.updateStatus(pending.getId(), orderToken, OrderStatus.FAILED);
            throw ex;
        }

        orderQueueProducer.publish(pending.getId(), orderToken);
        return new OrderPlacementResult(pending.getId(), orderToken, product, quantity, pending.getStatus());
    }

    public OrderStatusView getOrderStatus(int orderId, String orderToken, Users currentUser) {
        if (orderToken == null || orderToken.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã theo dõi đơn hàng.");
        }
        Order order = orderDAO.findByIdAndToken(orderId, orderToken)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại hoặc đã hết hạn."));
        ensureOrderAccessibility(order, currentUser);
        boolean deliverable = isDeliverable(order.getStatus());
        String activation = null;
        String deliveryLink = null;
        if (deliverable) {
            List<String> credentials = orderDAO.findCredentials(order.getId());
            if (!credentials.isEmpty()) {
                activation = credentials.get(0);
            }
        }
        return new OrderStatusView(order.getId(), orderToken, order.getStatus(),
                deliverable, activation, deliveryLink);
    }

    public OrderDetailView getOrderDetail(int orderId, Users currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        Order order = orderDAO.findByIdAndBuyer(orderId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        ensureOrderAccessibility(order, currentUser);
        List<String> credentials = isDeliverable(order.getStatus())
                ? orderDAO.findCredentials(order.getId())
                : List.of();
        return new OrderDetailView(order, credentials);
    }

    public String getStatusBadgeClass(OrderStatus status) {
        if (status == null) {
            return "badge";
        }
        return switch (status) {
            case PENDING -> "badge badge--warning";
            case CONFIRMED -> "badge badge--success";
            case FAILED -> "badge badge--danger";
            case DELIVERED -> "badge";
            case REFUNDED -> "badge badge--ghost";
            case CANCELLED -> "badge badge--ghost";
        };
    }

    public String getFriendlyStatus(OrderStatus status) {
        if (status == null) {
            return "Không xác định";
        }
        return switch (status) {
            case PENDING -> "Đang xử lý";
            case CONFIRMED -> "Đã thanh toán";
            case FAILED -> "Thất bại";
            case DELIVERED -> "Đã bàn giao";
            case REFUNDED -> "Đã hoàn tiền";
            case CANCELLED -> "Đã huỷ";
        };
    }

    private boolean isDeliverable(OrderStatus status) {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.DELIVERED || status == OrderStatus.REFUNDED;
    }

    private boolean isPurchasable(String status) {
        if (status == null) {
            return false;
        }
        return ALLOWED_PRODUCT_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
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

    private int quantityOf(Order order, int fallback) {
        Integer quantity = order.getQuantity();
        return quantity == null || quantity <= 0 ? fallback : quantity;
    }
}
