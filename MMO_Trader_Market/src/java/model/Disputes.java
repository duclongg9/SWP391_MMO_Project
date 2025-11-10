package model;

import java.util.Date;

/**
 * POJO ánh xạ bảng {@code disputes} lưu báo cáo đơn hàng của người mua.
 */
public class Disputes {

    private Integer id;
    private Integer orderId;
    private String orderReferenceCode;
    private Integer reporterId;
    private Integer resolvedByAdminId;
    private String issueType;
    private String customIssueTitle;
    private String reason;
    private String status;
    private Date escrowPausedAt;
    private Integer escrowRemainingSeconds;
    private Date resolvedAt;
    private String resolutionNote;
    private Date createdAt;
    private Date updatedAt;

    public Disputes() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }

    public Integer getResolvedByAdminId() {
        return resolvedByAdminId;
    }

    public void setResolvedByAdminId(Integer resolvedByAdminId) {
        this.resolvedByAdminId = resolvedByAdminId;
    }

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
