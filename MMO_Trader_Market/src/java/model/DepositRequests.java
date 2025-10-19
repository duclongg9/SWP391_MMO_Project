package model;

import java.math.BigDecimal;
import java.util.Date;

public class DepositRequests {
    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private String qrContent;
    private String idempotencyKey;
    private String status;
    private Date expiresAt;
    private Date createdAt;
    private String adminNote;

    public DepositRequests(Integer id, Integer userId, BigDecimal amount, String qrContent, String idempotencyKey, String status, Date expiresAt, Date createdAt, String adminNote) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.qrContent = qrContent;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.adminNote = adminNote;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getQrContent() {
        return qrContent;
    }
    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public String getAdminNote() {
        return adminNote;
    }
    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}