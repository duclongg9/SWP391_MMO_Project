package service;

import dao.order.OrderDAO;
import java.util.List;
import java.util.Optional;
import model.Order;
import model.OrderStatus;
import model.Products;
import model.ProductStatus;

/**
 * Contains the business rules for processing a buyer checkout request.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductService productService = new ProductService();

    /**
     * Loads every order stored in the temporary in-memory repository.
     * @return immutable snapshot of all recorded orders
     */
    public List<Order> findAll() {
        return orderDAO.findAll();
    }

    /**
     * Ensures the product exists and has been approved before buyers can continue checkout.
     * @param productId identifier coming from the UI
     * @return the validated product
     */
    public Products validatePurchasableProduct(int productId) {
        Optional<Products> productOptional = productService.findById(productId);
        Products product = productOptional.orElseThrow(() ->
                new IllegalArgumentException("Sản phẩm bạn chọn không tồn tại hoặc đã bị gỡ."));
        if (product.getStatus() != ProductStatus.APPROVED) {
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
        Product product = validatePurchasableProduct(productId);
        Order order = orderDAO.save(product, buyerEmail.trim(), paymentMethod.trim());
        String activationCode = orderDAO.generateActivationCode(order);
        String deliveryLink = orderDAO.generateDeliveryLink(order);
        order.markCompleted(activationCode, deliveryLink);
        return order;
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
            case PROCESSING -> "badge badge--warning";
            case DISPUTED -> "badge badge--danger";
            default -> "badge badge--ghost";
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
            case PENDING_PAYMENT -> "Chờ thanh toán";
        };
    }
}
