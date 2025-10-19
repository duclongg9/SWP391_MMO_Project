package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an order placed by a buyer.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final Products product;
    private final String buyerEmail;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final Integer buyerId;
    private final String orderToken;
    private final Integer quantity;
    private OrderStatus status;
    private String activationCode;
    private String deliveryLink;

    public Order(int id, Products product, String buyerEmail, BigDecimal totalAmount,
            OrderStatus status, LocalDateTime createdAt, String activationCode,
            String deliveryLink, Integer buyerId, String orderToken, Integer quantity) {
        this.id = id;
        this.product = product;
        this.buyerEmail = buyerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.activationCode = activationCode;
        this.deliveryLink = deliveryLink;
        this.buyerId = buyerId;
        this.orderToken = orderToken;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public Products getProduct() {
        return product;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public String getOrderToken() {
        return orderToken;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getDeliveryLink() {
        return deliveryLink;
    }

    public void setDeliveryLink(String deliveryLink) {
        this.deliveryLink = deliveryLink;
    }

    public boolean hasDeliveryInformation() {
        return activationCode != null && !activationCode.isBlank();
    }
}
