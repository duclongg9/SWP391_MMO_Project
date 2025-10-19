package model;

import java.math.BigDecimal;
import java.util.Date;

public class WalletTransactions {
    private Integer id;
    private Integer walletId;
    private Integer relatedEntityId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String note;

    public WalletTransactions(Integer id, Integer walletId, Integer relatedEntityId, String transactionType, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String note, Date createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.relatedEntityId = relatedEntityId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.note = note;
        this.createdAt = createdAt;
    }

    private Date createdAt;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getWalletId() {
        return walletId;
    }
    public void setWalletId(Integer walletId) {
        this.walletId = walletId;
    }
    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }
    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}