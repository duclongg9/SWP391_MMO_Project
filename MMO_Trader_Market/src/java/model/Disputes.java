package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * POJO ánh xạ bảng disputes (khiếu nại đơn hàng).
 * Kèm thêm thông tin hiển thị (reporterName, reporterEmail, resolvedByAdminName)
 * và danh sách ảnh đính kèm (DisputeAttachment).
 */
public class Disputes {

    private Integer id;
    private Integer orderId;
    private String orderReferenceCode;
    private String orderSnapshotJson; // map với order_snapshot_json

    private Integer reporterId;
    private String reporterName;
    private String reporterEmail;

    private Integer resolvedByAdminId;
    private String resolvedByAdminName;

    private Integer productId;
    private String productName;
    private Integer shopId;
    private String shopName;

    private Integer orderQuantity;
    private String orderStatus;

    private String issueType;          // ACCOUNT_NOT_WORKING / OTHER / ...
    private String customIssueTitle;   // tiêu đề tùy chọn khi chọn OTHER

    private String reason;             // nội dung khiếu nại của user
    private String status;             // Open / InReview / Resolved... / Closed...

    private Date escrowPausedAt;
    private Integer escrowRemainingSeconds;

    private Date resolvedAt;
    private String resolutionNote;

    private Date createdAt;
    private Date updatedAt;

    // Danh sách ảnh đính kèm (từ bảng dispute_attachments)
    private List<DisputeAttachment> attachments = new ArrayList<>();

    public Disputes() {
    }

    // ===== ID =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // ===== Order =====
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderReferenceCode() {
        return orderReferenceCode;
    }

    public void setOrderReferenceCode(String orderReferenceCode) {
        this.orderReferenceCode = orderReferenceCode;
    }

    public String getOrderSnapshotJson() {
        return orderSnapshotJson;
    }

    public void setOrderSnapshotJson(String orderSnapshotJson) {
        this.orderSnapshotJson = orderSnapshotJson;
    }

    // ===== Reporter =====
    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    // ===== Admin xử lý =====
    public Integer getResolvedByAdminId() {
        return resolvedByAdminId;
    }

    public void setResolvedByAdminId(Integer resolvedByAdminId) {
        this.resolvedByAdminId = resolvedByAdminId;
    }

    public String getResolvedByAdminName() {
        return resolvedByAdminName;
    }

    public void setResolvedByAdminName(String resolvedByAdminName) {
        this.resolvedByAdminName = resolvedByAdminName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Integer orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    // ===== Issue / Reason / Status =====
    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getCustomIssueTitle() {
        return customIssueTitle;
    }

    public void setCustomIssueTitle(String customIssueTitle) {
        this.customIssueTitle = customIssueTitle;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ===== Escrow =====
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

    // ===== Resolve info =====
    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    // ===== Timestamps =====
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

    // ===== Attachments =====
    public List<DisputeAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<DisputeAttachment> attachments) {
        this.attachments = (attachments != null) ? attachments : new ArrayList<>();
    }

    public void addAttachment(DisputeAttachment att) {
        if (att != null) {
            if (this.attachments == null) {
                this.attachments = new ArrayList<>();
            }
            this.attachments.add(att);
        }
    }


}
