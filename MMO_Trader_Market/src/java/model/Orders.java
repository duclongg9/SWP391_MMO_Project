package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Thực thể ánh xạ bảng {@code orders} phục vụ xử lý luồng mua hàng.
 * 
 * Các thuộc tính được dùng ở nhiều tầng:
 * 
 * Controller/JSP: đọc
 * {@link #status}, {@link #totalAmount}, {@link #variantCode} để hiển thị.
 * Service/Worker: dựa vào {@link #idempotencyKey}, {@link #holdUntil} để
 * bảo vệ luồng tiền và lock hàng.
 * DAO: map dữ liệu từ ResultSet khi join cùng bảng sản phẩm.
 * 
 *
 * @author longpdhe171902
 */
public class Orders {

    /**
     * Khóa chính của bảng {@code orders}.
     */
    private Integer id;

    /**
     * Mã người mua, dùng để kiểm tra quyền truy cập và truy vấn lịch sử.
     */
    private Integer buyerId;

    /**
     * Mã sản phẩm gắn với đơn, worker sử dụng để trừ tồn kho/credential.
     */
    private Integer productId;

    /**
     * Khóa ngoại sang bảng {@code wallet_transactions}, set sau khi worker trừ
     * tiền.
     */
    private Integer paymentTransactionId;

    /**
     * Số lượng mua, phục vụ hiển thị và tính toán tồn kho trong worker.
     */
    private Integer quantity;

    /**
     * Đơn giá tại thời điểm đặt, lưu lại để đối soát.
     */
    private BigDecimal unitPrice;

    /**
     * Mã biến thể sản phẩm (SKU con). Null nếu sản phẩm đơn biến thể.
     */
    private String variantCode;

    /**
     * Tổng số tiền khách phải trả cho đơn hàng.
     */
    private BigDecimal totalAmount;

    /**
     * Trạng thái hiện tại của đơn (Pending/Processing/Completed...).
     */
    private String status;

    /**
     * Khóa idempotent do client cung cấp, giúp tránh tạo đơn trùng khi refresh.
     */
    private String idempotencyKey;

    /**
     * Thời điểm giữ chỗ credential (nếu có cơ chế hold).
     */
    private Date holdUntil;

    /**
     * Thời lượng escrow theo giây được snapshot khi tạo đơn, phục vụ việc trả lại thời gian còn dư khi pause.
     */
    private Integer escrowHoldSeconds;

    /**
     * Mốc giải ngân escrow ban đầu trước khi phát sinh khiếu nại.
     */
    private Date escrowOriginalReleaseAt;

    /**
     * Thời điểm dự kiến giải ngân escrow hiện tại (null nếu đang tạm dừng).
     */
    private Date escrowReleaseAt;

    /**
     * Trạng thái dòng đời escrow: Scheduled/Paused/Released/Cancelled.
     */
    private String escrowStatus;

    /**
     * Thời điểm hệ thống dừng đếm escrow do phát sinh báo cáo.
     */
    private Date escrowPausedAt;

    /**
     * Số giây escrow còn lại tại thời điểm pause để hoàn trả khi resume.
     */
    private Integer escrowRemainingSeconds;

    /**
     * Thời điểm escrow được tiếp tục đếm sau khi dispute được xử lý.
     */
    private Date escrowResumedAt;

    /**
     * Thời điểm escrow thực sự giải ngân về phía seller.
     */
    private Date escrowReleasedAtActual;

    /**
     * Ngày tạo bản ghi, dùng để sắp xếp lịch sử.
     */
    private Date createdAt;

    /**
     * Ngày cập nhật gần nhất (trạng thái/transaction).
     */
    private Date updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(Integer paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getVariantCode() {
        return variantCode;
    }

    public void setVariantCode(String variantCode) {
        this.variantCode = variantCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Date getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(Date holdUntil) {
        this.holdUntil = holdUntil;
    }

    public Integer getEscrowHoldSeconds() {
        return escrowHoldSeconds;
    }

    public void setEscrowHoldSeconds(Integer escrowHoldSeconds) {
        this.escrowHoldSeconds = escrowHoldSeconds;
    }

    public Date getEscrowOriginalReleaseAt() {
        return escrowOriginalReleaseAt;
    }

    public void setEscrowOriginalReleaseAt(Date escrowOriginalReleaseAt) {
        this.escrowOriginalReleaseAt = escrowOriginalReleaseAt;
    }

    public Date getEscrowReleaseAt() {
        return escrowReleaseAt;
    }

    public void setEscrowReleaseAt(Date escrowReleaseAt) {
        this.escrowReleaseAt = escrowReleaseAt;
    }

    public String getEscrowStatus() {
        return escrowStatus;
    }

    public void setEscrowStatus(String escrowStatus) {
        this.escrowStatus = escrowStatus;
    }

    public Date getEscrowPausedAt() {
        return escrowPausedAt;
    }

    public void setEscrowPausedAt(Date escrowPausedAt) {
        this.escrowPausedAt = escrowPausedAt;
    }

    public Integer getEscrowRemainingSeconds() {
        return escrowRemainingSeconds;
    }

    public void setEscrowRemainingSeconds(Integer escrowRemainingSeconds) {
        this.escrowRemainingSeconds = escrowRemainingSeconds;
    }

    public Date getEscrowResumedAt() {
        return escrowResumedAt;
    }

    public void setEscrowResumedAt(Date escrowResumedAt) {
        this.escrowResumedAt = escrowResumedAt;
    }

    public Date getEscrowReleasedAtActual() {
        return escrowReleasedAtActual;
    }

    public void setEscrowReleasedAtActual(Date escrowReleasedAtActual) {
        this.escrowReleasedAtActual = escrowReleasedAtActual;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
