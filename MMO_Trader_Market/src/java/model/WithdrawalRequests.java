package model;

import java.math.BigDecimal;
import java.util.Date;

public class WithdrawalRequests {

    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private String status;
    private String adminProofUrl;
    private Date createdAt;
    private Date processedAt;
    private String bankAccountInfo;

    public Integer getId() {
        return id;
    }

    public WithdrawalRequests(Integer id, Integer userId, BigDecimal amount, String status, String adminProofUrl, Date createdAt, Date processedAt, String bankAccountInfo) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.adminProofUrl = adminProofUrl;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.bankAccountInfo = bankAccountInfo;
    }

    public WithdrawalRequests() {

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

    public String getAdminProofUrl() {
        return adminProofUrl;
    }

    public void setAdminProofUrl(String adminProofUrl) {
        this.adminProofUrl = adminProofUrl;
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
}
