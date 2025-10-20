/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author D E L L
 */
public class WalletTransactions {
    private int id;
    private Wallets walletId;
    private int relatedEntityId;
    private TransactionType transactionType;
    private double amount;
    private double balanceBefore;
    private double balanceAfter;
    private String note;
    private Instant createdAt;

    public WalletTransactions() {
    }

    public WalletTransactions(int id, Wallets walletId, int relatedEntityId, TransactionType transactionType, double amount, double balanceBefore, double balanceAfter, String note, Instant createdAt) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Wallets getWalletId() {
        return walletId;
    }

    public void setWalletId(Wallets walletId) {
        this.walletId = walletId;
    }

    public int getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(int relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAtDate() {
        return createdAt == null ? null : Date.from(createdAt);
    }
    
    @Override
    public String toString() {
return "WalletTransactions{" + "id=" + id + ", walletId=" + walletId + ", relatedEntityId=" + relatedEntityId + ", transactionType=" + transactionType + ", amount=" + amount + ", balanceBefore=" + balanceBefore + ", balanceAfter=" + balanceAfter + ", note=" + note + ", createdAt=" + createdAt + '}';
    }
    
    
}