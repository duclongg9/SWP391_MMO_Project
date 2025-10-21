package model;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Domain model mapping cho bảng {@code wallet_transactions}.
 */
public class WalletTransactions {

    private Integer id;
    private Integer walletId;
    private Wallets wallet;
    private Integer relatedEntityId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String note;
    private Date createdAt;

    public WalletTransactions() {
    }

    public WalletTransactions(Integer id, Integer walletId, Integer relatedEntityId,
            String transactionType, BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String note, Date createdAt) {
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

    public Wallets getWallet() {
        return wallet;
    }

    public void setWallet(Wallets wallet) {
        this.wallet = wallet;
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

    public TransactionType getTransactionTypeEnum() {
        return transactionType == null ? null : TransactionType.fromDbValue(transactionType);
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType == null ? null : transactionType.getDbValue();
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

    @Override
    public String toString() {
        return "WalletTransactions{" +
                "id=" + id +
                ", walletId=" + walletId +
                ", relatedEntityId=" + relatedEntityId +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
