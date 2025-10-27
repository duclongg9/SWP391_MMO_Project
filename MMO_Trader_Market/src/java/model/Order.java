package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Đại diện cho đơn hàng sau khi người mua hoàn tất thao tác đặt mua.
 *
 * @author longpdhe171902
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

    /**
     * Tạo thực thể đơn hàng tối giản chỉ với thông tin bắt buộc.
     */
    public Order(int id, Products product, String buyerEmail, String paymentMethod,
            OrderStatus status, LocalDateTime createdAt) {
        this(id, product, buyerEmail, paymentMethod, status, createdAt, null, null);
    }

    /**
     * Tạo thực thể đơn hàng đầy đủ bao gồm dữ liệu bàn giao.
     */
    public Order(int id, Products product, String buyerEmail, String paymentMethod,
            OrderStatus status, LocalDateTime createdAt, String activationCode, String deliveryLink) {
        this.id = id;
        this.product = product;
        this.buyerEmail = buyerEmail;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.activationCode = activationCode;
        this.deliveryLink = deliveryLink;
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

    /**
     * Đánh dấu đơn hàng đang được xử lý.
     */
    public void markProcessing() {
        this.status = OrderStatus.PROCESSING;
        this.activationCode = null;
        this.deliveryLink = null;
    }

    /**
     * Đánh dấu đơn đã hoàn thành và lưu thông tin bàn giao.
     *
     * @param activationCode mã kích hoạt chuyển cho người mua
     * @param deliveryLink đường dẫn tải sản phẩm (có thể null)
     */
    public void markCompleted(String activationCode, String deliveryLink) {
        this.status = OrderStatus.COMPLETED;
        this.activationCode = activationCode;
        this.deliveryLink = deliveryLink;
    }

    /**
     * Đánh dấu đơn đang tranh chấp, giữ lại mã kích hoạt phục vụ đối soát.
     *
     * @param activationCode mã kích hoạt hiển thị trong quá trình xử lý tranh
     * chấp
     */
    public void markDisputed(String activationCode) {
        this.status = OrderStatus.DISPUTED;
        this.activationCode = activationCode;
    }

    /**
     * Kiểm tra thông tin bàn giao đã sẵn sàng để hiển thị hay chưa.
     *
     * @return {@code true} nếu đã có mã kích hoạt hợp lệ
     */
    public boolean hasDeliveryInformation() {
        return activationCode != null && !activationCode.isBlank();
    }
}
