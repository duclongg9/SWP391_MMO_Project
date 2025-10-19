package dao.wallet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified DAO that keeps track of wallet holds. In a production system this
 * would be persisted inside the database but for the purposes of this
 * application we keep the state in-memory to focus on the orchestration logic.
 */
public class WalletDAO {

    private static final Map<String, WalletHoldRecord> HOLDS = new ConcurrentHashMap<>();

    public WalletHoldRecord createHold(int buyerId, int sellerId, BigDecimal amount, int orderId, String orderToken) {
        WalletHoldRecord record = new WalletHoldRecord(buyerId, sellerId, amount, orderId, orderToken);
        WalletHoldRecord existing = HOLDS.putIfAbsent(orderToken, record);
        if (existing != null) {
            throw new IllegalStateException("Hold already exists for token " + orderToken);
        }
        return record;
    }

    public Optional<WalletHoldRecord> findHold(String orderToken) {
        return Optional.ofNullable(HOLDS.get(orderToken));
    }

    public WalletHoldRecord capture(String orderToken) {
        WalletHoldRecord record = HOLDS.get(orderToken);
        if (record == null) {
            throw new IllegalStateException("Hold not found for token " + orderToken);
        }
        record.setCaptured(true);
        return record;
    }

    public WalletHoldRecord release(String orderToken) {
        WalletHoldRecord record = HOLDS.remove(orderToken);
        if (record == null) {
            throw new IllegalStateException("Hold not found for token " + orderToken);
        }
        record.setCaptured(false);
        record.setReleased(true);
        return record;
    }

    public WalletHoldRecord refund(String orderToken) {
        WalletHoldRecord record = HOLDS.remove(orderToken);
        if (record == null) {
            throw new IllegalStateException("Hold not found for token " + orderToken);
        }
        record.setCaptured(false);
        record.setRefunded(true);
        return record;
    }

    public static final class WalletHoldRecord {
        private final int buyerId;
        private final int sellerId;
        private final BigDecimal amount;
        private final int orderId;
        private final String orderToken;
        private volatile boolean captured;
        private volatile boolean released;
        private volatile boolean refunded;

        private WalletHoldRecord(int buyerId, int sellerId, BigDecimal amount, int orderId, String orderToken) {
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.amount = amount;
            this.orderId = orderId;
            this.orderToken = orderToken;
        }

        public int getBuyerId() {
            return buyerId;
        }

        public int getSellerId() {
            return sellerId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public int getOrderId() {
            return orderId;
        }

        public String getOrderToken() {
            return orderToken;
        }

        public boolean isCaptured() {
            return captured;
        }

        public void setCaptured(boolean captured) {
            this.captured = captured;
        }

        public boolean isReleased() {
            return released;
        }

        public void setReleased(boolean released) {
            this.released = released;
        }

        public boolean isRefunded() {
            return refunded;
        }

        public void setRefunded(boolean refunded) {
            this.refunded = refunded;
        }
    }
}
