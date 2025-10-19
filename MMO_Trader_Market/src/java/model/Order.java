package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a purchase order created by a buyer after completing the checkout flow.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final Products product;
    private final String buyerEmail;
    private final String paymentMethod;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private String activationCode;
    private String deliveryLink;

    public Order(int id, Products product, String buyerEmail, String paymentMethod,
            OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.product = product;
        this.buyerEmail = buyerEmail;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public String getDeliveryLink() {
        return deliveryLink;
    }

    /**
     * Marks the order as currently being processed.
     */
    public void markProcessing() {
        this.status = OrderStatus.PROCESSING;
    }

    /**
     * Marks the order as completed and stores the delivery artefacts.
     * @param activationCode unique key or password delivered to the buyer
     * @param deliveryLink optional URL for downloading the product
     */
    public void markCompleted(String activationCode, String deliveryLink) {
        this.status = OrderStatus.COMPLETED;
        this.activationCode = activationCode;
        this.deliveryLink = deliveryLink;
    }

    /**
     * Marks the order as disputed but keeps the current activation code for reference.
     * @param activationCode activation code to show during dispute resolution
     */
    public void markDisputed(String activationCode) {
        this.status = OrderStatus.DISPUTED;
        this.activationCode = activationCode;
    }

    /**
     * Indicates whether the digital delivery information is ready to display to the buyer.
     * @return {@code true} if at least the activation code exists
     */
    public boolean hasDeliveryInformation() {
        return activationCode != null && !activationCode.isBlank();
    }
}
