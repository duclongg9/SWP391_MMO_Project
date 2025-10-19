package service.dto;

import model.OrderStatus;

/**
 * Data transfer object used by the status polling endpoint to expose the
 * current order state and whether the delivery information is ready.
 */
public class OrderStatusView {

    private final int orderId;
    private final String orderToken;
    private final OrderStatus status;
    private final boolean deliverable;
    private final String activationCode;
    private final String deliveryLink;

    public OrderStatusView(int orderId, String orderToken, OrderStatus status,
            boolean deliverable, String activationCode, String deliveryLink) {
        this.orderId = orderId;
        this.orderToken = orderToken;
        this.status = status;
        this.deliverable = deliverable;
        this.activationCode = activationCode;
        this.deliveryLink = deliveryLink;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderToken() {
        return orderToken;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public String getDeliveryLink() {
        return deliveryLink;
    }
}
