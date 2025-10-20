package dao.wallet;

import dao.BaseDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO responsible for wallet balance mutations.
 */
public class WalletDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(WalletDAO.class.getName());

    public Optional<WalletSnapshot> findWalletForUpdate(Connection connection, int userId) throws SQLException {
        String sql = "SELECT id, balance, hold_balance FROM wallets WHERE user_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    BigDecimal holdBalance = rs.getBigDecimal("hold_balance");
                    return Optional.of(new WalletSnapshot(rs.getInt("id"), balance, holdBalance));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<WalletSnapshot> findWallet(int userId) {
        String sql = "SELECT id, balance, hold_balance FROM wallets WHERE user_id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    BigDecimal holdBalance = rs.getBigDecimal("hold_balance");
                    return Optional.of(new WalletSnapshot(rs.getInt("id"), balance, holdBalance));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findWallet failed", ex);
        }
        return Optional.empty();
    }

    public List<WalletTransactionItem> findRecentTransactions(int walletId, int limit) {
        String sql = "SELECT id, transaction_type, amount, balance_before, balance_after, note, created_at "
                + "FROM wallet_transactions WHERE wallet_id = ? ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, walletId);
            statement.setInt(2, Math.max(limit, 1));
            try (ResultSet rs = statement.executeQuery()) {
                List<WalletTransactionItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new WalletTransactionItem(
                            rs.getLong("id"),
                            rs.getString("transaction_type"),
                            rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance_before"),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("note"),
                            rs.getTimestamp("created_at")
                    ));
                }
                return items;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findRecentTransactions failed", ex);
            return List.of();
        }
    }

    public WalletHoldRecord createHold(int buyerId, int sellerId, BigDecimal amount, int orderId, String orderToken)
            throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                WalletSnapshot wallet = findWalletForUpdate(connection, buyerId)
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví khách hàng"));

                BigDecimal available = wallet.balance().subtract(wallet.holdBalance());
                if (available.compareTo(amount) < 0) {
                    throw new IllegalStateException("Số dư ví không đủ");
                }

                updateHoldBalance(connection, wallet.id(), amount, true);
                long transactionId = insertTransaction(connection, wallet.id(), orderId, "HOLD", amount,
                        wallet.balance(), wallet.balance(), orderToken);
                long holdId = insertHold(connection, wallet.id(), orderId, orderToken, amount, "HOLD", transactionId);

                connection.commit();
                return new WalletHoldRecord(holdId, wallet.id(), buyerId, sellerId, amount, orderId, orderToken,
                        transactionId, WalletHoldStatus.HOLD);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                if (ex instanceof RuntimeException runtime && !(runtime instanceof IllegalStateException)) {
                    LOGGER.log(Level.SEVERE, "createHold failed", ex);
                }
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public WalletHoldRecord capture(String orderToken) throws SQLException {
        return transition(orderToken, WalletHoldStatus.CAPTURED, true, false);
    }

    public WalletHoldRecord release(String orderToken) throws SQLException {
        return transition(orderToken, WalletHoldStatus.RELEASED, false, true);
    }

    public WalletHoldRecord refund(String orderToken) throws SQLException {
        return transition(orderToken, WalletHoldStatus.REFUNDED, false, false);
    }

    public Optional<WalletHoldRecord> findHold(String orderToken) {
        String sql = "SELECT id, wallet_id, order_id, order_token, amount, status, transaction_id "
                + "FROM wallet_holds WHERE order_token = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapHold(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findHold failed", ex);
        }
        return Optional.empty();
    }

    private WalletHoldRecord transition(String orderToken, WalletHoldStatus targetStatus,
            boolean subtractBalance, boolean releaseBalance) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                HoldSnapshot hold = lockHold(connection, orderToken);
                if (hold.status() != WalletHoldStatus.HOLD) {
                    return mapRecord(hold);
                }
                WalletSnapshot wallet = findWalletForUpdate(connection, hold.buyerId())
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví khách hàng"));

                if (subtractBalance) {
                    BigDecimal newBalance = wallet.balance().subtract(hold.amount());
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalStateException("Ví không đủ để ghi nhận thanh toán");
                    }
                    updateBalance(connection, wallet.id(), newBalance);
                }
                if (releaseBalance) {
                    updateHoldBalance(connection, wallet.id(), hold.amount(), false);
                } else {
                    updateHoldBalance(connection, wallet.id(), hold.amount(), false);
                }

                WalletSnapshot refreshed = findWalletForUpdate(connection, hold.buyerId())
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy ví khách hàng"));

                BigDecimal balanceBefore = wallet.balance();
                BigDecimal balanceAfter = refreshed.balance();
                if (!subtractBalance) {
                    balanceAfter = balanceBefore;
                }
                long txId = insertTransaction(connection, wallet.id(), hold.orderId(), targetStatus.name(), hold.amount(),
                        balanceBefore, balanceAfter, orderToken);
                updateHoldStatus(connection, hold.id(), targetStatus, txId);

                connection.commit();
                return new WalletHoldRecord(hold.id(), wallet.id(), hold.buyerId(), hold.sellerId(), hold.amount(),
                        hold.orderId(), orderToken, txId, targetStatus);
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                if (ex instanceof RuntimeException runtime && !(runtime instanceof IllegalStateException)) {
                    LOGGER.log(Level.SEVERE, "transition hold failed", ex);
                }
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void updateHoldBalance(Connection connection, int walletId, BigDecimal amount, boolean increase)
            throws SQLException {
        String sql = increase
                ? "UPDATE wallets SET hold_balance = hold_balance + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?"
                : "UPDATE wallets SET hold_balance = hold_balance - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, walletId);
            statement.executeUpdate();
        }
    }

    private void updateBalance(Connection connection, int walletId, BigDecimal balance) throws SQLException {
        String sql = "UPDATE wallets SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, walletId);
            statement.executeUpdate();
        }
    }

    private long insertTransaction(Connection connection, int walletId, int orderId, String type,
            BigDecimal amount, BigDecimal before, BigDecimal after, String orderToken) throws SQLException {
        String sql = "INSERT INTO wallet_transactions (wallet_id, related_entity_id, transaction_type, amount, "
                + "balance_before, balance_after, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, walletId);
            statement.setInt(2, orderId);
            statement.setString(3, type);
            statement.setBigDecimal(4, amount);
            statement.setBigDecimal(5, before);
            statement.setBigDecimal(6, after);
            statement.setString(7, orderToken);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Không thể tạo giao dịch ví");
    }

    private long insertHold(Connection connection, int walletId, int orderId, String orderToken,
            BigDecimal amount, String status, long transactionId) throws SQLException {
        String sql = "INSERT INTO wallet_holds (wallet_id, order_id, order_token, amount, status, transaction_id, "
                + "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, walletId);
            statement.setInt(2, orderId);
            statement.setString(3, orderToken);
            statement.setBigDecimal(4, amount);
            statement.setString(5, status);
            statement.setLong(6, transactionId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Không thể tạo hold ví");
    }

    private HoldSnapshot lockHold(Connection connection, String orderToken) throws SQLException {
        String sql = "SELECT h.id, h.wallet_id, w.user_id AS buyer_id, h.amount, h.status, h.transaction_id, h.order_id, "
                + "h.created_at, h.updated_at, s.owner_id AS seller_id "
                + "FROM wallet_holds h "
                + "JOIN wallets w ON w.id = h.wallet_id "
                + "JOIN orders o ON o.id = h.order_id "
                + "JOIN products p ON p.id = o.product_id "
                + "JOIN shops s ON s.id = p.shop_id "
                + "WHERE h.order_token = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Không tìm thấy giao dịch hold");
                }
                return new HoldSnapshot(
                        rs.getLong("id"),
                        rs.getInt("wallet_id"),
                        rs.getInt("buyer_id"),
                        rs.getInt("seller_id"),
                        rs.getInt("order_id"),
                        rs.getBigDecimal("amount"),
                        WalletHoldStatus.valueOf(rs.getString("status")),
                        rs.getLong("transaction_id"));
            }
        }
    }

    private void updateHoldStatus(Connection connection, long holdId, WalletHoldStatus status, long transactionId)
            throws SQLException {
        String sql = "UPDATE wallet_holds SET status = ?, transaction_id = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, transactionId);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.setLong(4, holdId);
            statement.executeUpdate();
        }
    }

    private WalletHoldRecord mapHold(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        int walletId = rs.getInt("wallet_id");
        int orderId = rs.getInt("order_id");
        String token = rs.getString("order_token");
        BigDecimal amount = rs.getBigDecimal("amount");
        WalletHoldStatus status = WalletHoldStatus.valueOf(rs.getString("status"));
        long transactionId = rs.getLong("transaction_id");
        return new WalletHoldRecord(id, walletId, 0, 0, amount, orderId, token, transactionId, status);
    }

    private WalletHoldRecord mapRecord(HoldSnapshot snapshot) {
        return new WalletHoldRecord(snapshot.id(), snapshot.walletId(), snapshot.buyerId(), snapshot.sellerId(),
                snapshot.amount(), snapshot.orderId(), null, snapshot.transactionId(), snapshot.status());
    }

    public record WalletSnapshot(int id, BigDecimal balance, BigDecimal holdBalance) {
    }

    public record WalletTransactionItem(long id, String type, BigDecimal amount,
            BigDecimal balanceBefore, BigDecimal balanceAfter, String note, Timestamp createdAt) {
    }

    public enum WalletHoldStatus {
        HOLD,
        CAPTURED,
        RELEASED,
        REFUNDED
    }

    public record WalletHoldRecord(long id, int walletId, int buyerId, int sellerId, BigDecimal amount,
            int orderId, String orderToken, long transactionId, WalletHoldStatus status) {
    }

    private record HoldSnapshot(long id, int walletId, int buyerId, int sellerId, int orderId, BigDecimal amount,
            WalletHoldStatus status, long transactionId) {
    }
}
