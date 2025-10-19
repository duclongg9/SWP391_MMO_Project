package service.dto;

import model.OrderStatus;
import model.Products;

/**
 * Represents the immediate response returned to the UI after the buyer submits
 * the checkout form. The order is still being processed asynchronously by the
 * worker but we provide enough context for the polling loop to continue.
 */
public class OrderPlacementResult {

    private final int orderId;
    private final String orderToken;
    private final Products product;
    private final int quantity;
    private final OrderStatus status;

    public OrderPlacementResult(int orderId, String orderToken, Products product, int quantity, OrderStatus status) {
        this.orderId = orderId;
        this.orderToken = orderToken;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderToken() {
        return orderToken;
    }

    public Products getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
