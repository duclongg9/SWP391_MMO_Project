// model/CashTxn.java
package model;

import java.math.BigDecimal;
import java.util.Date;

public class CashTxn {

    private String type; // Deposit | Withdrawal
    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private String status;
    private Date createdAt;
    private Date processedAt;
    private String userName;
    // Chỉ dùng cho Withdrawal
    private String bankAccountInfo;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String adminProofUrl;

    // Chỉ dùng cho Deposit
    private String qrContent;
    private String idempotencyKey;
    private String adminNote;

    public CashTxn() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public String getBankAccountInfo() {
        return bankAccountInfo;
    }

    public void setBankAccountInfo(String bankAccountInfo) {
        this.bankAccountInfo = bankAccountInfo;
    }

    public String getAdminProofUrl() {
        return adminProofUrl;
    }

    public void setAdminProofUrl(String adminProofUrl) {
        this.adminProofUrl = adminProofUrl;
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

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
// Getters/Setters ...
    // (bạn sinh bằng IDE cho gọn)
}
