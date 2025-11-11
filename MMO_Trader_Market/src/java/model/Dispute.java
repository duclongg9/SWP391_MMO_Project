package model;

import java.util.Date;

public class Dispute {
    private int id;
    private int orderId;
    private String orderReferenceCode;

    private int reporterId;
    private String reporterName;
    private String reporterEmail;

    private Integer resolvedByAdminId;
    private String resolvedByAdminName;

    // enum: ACCOUNT_NOT_WORKING, ACCOUNT_DUPLICATED, ACCOUNT_EXPIRED, ACCOUNT_MISSING, OTHER
    private String issueType;
    private String customIssueTitle;
    private String reason;

    // JSON trong DB -> lưu chuỗi để hiển thị
    private String orderSnapshotJson;

    // enum:
    // Open, InReview, ResolvedWithRefund, ResolvedWithoutRefund, Closed, Cancelled
    private String status;

    private Date escrowPausedAt;
    private Date escrowResolvedAt;

    private String resolutionNote;
    private Date createdAt;
    private Date updatedAt;

    // ==== getters & setters ====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getOrderReferenceCode() { return orderReferenceCode; }
    public void setOrderReferenceCode(String orderReferenceCode) { this.orderReferenceCode = orderReferenceCode; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public Integer getResolvedByAdminId() { return resolvedByAdminId; }
    public void setResolvedByAdminId(Integer resolvedByAdminId) { this.resolvedByAdminId = resolvedByAdminId; }

    public String getResolvedByAdminName() { return resolvedByAdminName; }
    public void setResolvedByAdminName(String resolvedByAdminName) { this.resolvedByAdminName = resolvedByAdminName; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getCustomIssueTitle() { return customIssueTitle; }
    public void setCustomIssueTitle(String customIssueTitle) { this.customIssueTitle = customIssueTitle; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getOrderSnapshotJson() { return orderSnapshotJson; }
    public void setOrderSnapshotJson(String orderSnapshotJson) { this.orderSnapshotJson = orderSnapshotJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getEscrowPausedAt() { return escrowPausedAt; }
    public void setEscrowPausedAt(Date escrowPausedAt) { this.escrowPausedAt = escrowPausedAt; }

    public Date getEscrowResolvedAt() { return escrowResolvedAt; }
    public void setEscrowResolvedAt(Date escrowResolvedAt) { this.escrowResolvedAt = escrowResolvedAt; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
