package model;

import java.util.Date;

public class Disputes {
    private Integer id;
    private Integer orderId;
    private Integer reporterId;
    private Integer resolvedByAdminId;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String reason;
    public Integer getId() {
        return id;
    }

    public Disputes(Integer id, Integer orderId, Integer reporterId, Integer resolvedByAdminId, String status, Date createdAt, Date updatedAt, String reason) {
        this.id = id;
        this.orderId = orderId;
        this.reporterId = reporterId;
        this.resolvedByAdminId = resolvedByAdminId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reason = reason;
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
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
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}