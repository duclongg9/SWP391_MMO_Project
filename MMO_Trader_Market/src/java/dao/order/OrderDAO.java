package dao.order;

import dao.BaseDAO;
import model.Order;
import model.OrderStatus;
import model.Products;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for reading and writing order data.
 */
public class OrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());
    private static final String ORDER_SELECT = "SELECT "
            + "o.id AS order_id, o.buyer_id, o.product_id, o.total_amount, o.status, o.idempotency_key, "
            + "o.created_at, o.updated_at, o.payment_transaction_id, u.email AS buyer_email, oi.quantity AS order_quantity, "
            + "p.shop_id, p.name AS product_name, p.description AS product_description, p.price AS product_price, "
            + "p.inventory_count, p.status AS product_status "
            + "FROM orders o "
            + "JOIN products p ON p.id = o.product_id "
            + "JOIN users u ON u.id = o.buyer_id "
            + "LEFT JOIN order_items oi ON oi.order_id = o.id ";

    public int countByBuyer(int buyerId, OrderStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE buyer_id = ?");
        if (status != null) {
            sql.append(" AND status = ?");
        }
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, buyerId);
            if (status != null) {
                statement.setString(2, status.toDatabaseValue());
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "countByBuyer failed", ex);
        }
        return 0;
    }

    public long countByBuyer(int buyerId) {
        return countByBuyer(buyerId, null);
    }

    public long countByBuyerAndStatus(int buyerId, OrderStatus status) {
        return countByBuyer(buyerId, status);
    }

    public long countByStatus(OrderStatus status) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "countByStatus failed", ex);
        }
        return 0L;
    }

    public long countPendingByOwner(int ownerId) {
        final String sql = "SELECT COUNT(*) FROM orders o "
                + "JOIN products p ON p.id = o.product_id "
                + "JOIN shops s ON s.id = p.shop_id "
                + "WHERE s.owner_id = ? AND o.status = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setString(2, OrderStatus.PENDING.toDatabaseValue());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "countPendingByOwner failed", ex);
        }
        return 0L;
    }

    public List<Order> findByBuyer(int buyerId, OrderStatus status, int limit, int offset) {
        StringBuilder sql = new StringBuilder(ORDER_SELECT)
                .append("WHERE o.buyer_id = ? ");
        if (status != null) {
            sql.append("AND o.status = ? ");
        }
        sql.append("ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, buyerId);
            if (status != null) {
                statement.setString(index++, status.toDatabaseValue());
            }
            statement.setInt(index++, limit);
            statement.setInt(index, offset);
            List<Order> orders = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
            return orders;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findByBuyer failed", ex);
            return List.of();
        }
    }

    public Optional<Order> findByIdAndToken(int orderId, String orderToken) {
        String sql = ORDER_SELECT + "WHERE o.id = ? AND o.idempotency_key = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, orderToken);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findByIdAndToken failed", ex);
        }
        return Optional.empty();
    }

    public Optional<Order> findByTokenAndBuyer(String orderToken, int buyerId) {
        String sql = ORDER_SELECT + "WHERE o.idempotency_key = ? AND o.buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderToken);
            statement.setInt(2, buyerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findByTokenAndBuyer failed", ex);
        }
        return Optional.empty();
    }

    public Optional<Order> findByIdAndBuyer(int orderId, int buyerId) {
        String sql = ORDER_SELECT + "WHERE o.id = ? AND o.buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, buyerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findByIdAndBuyer failed", ex);
        }
        return Optional.empty();
    }

    public Order insertPendingOrder(int buyerId, int productId, int quantity, BigDecimal totalAmount, String orderToken)
            throws SQLException {
        String sql = "INSERT INTO orders (buyer_id, product_id, total_amount, status, idempotency_key, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            try {
                statement.setInt(1, buyerId);
                statement.setInt(2, productId);
                statement.setBigDecimal(3, totalAmount);
                statement.setString(4, OrderStatus.PENDING.toDatabaseValue());
                statement.setString(5, orderToken);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        int orderId = keys.getInt(1);
                        insertOrderItem(connection, orderId, productId, quantity);
                        connection.commit();
                        return findById(orderId).orElseThrow(() -> new SQLException("Không thể tải lại đơn hàng"));
                    }
                }
                connection.rollback();
                throw new SQLException("Không tạo được đơn hàng mới");
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Optional<Order> findById(int orderId) {
        String sql = ORDER_SELECT + "WHERE o.id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findById failed", ex);
        }
        return Optional.empty();
    }

    public BigDecimal sumMonthlyRevenueByOwner(int ownerId, int year, int month) {
        int safeYear = Math.max(year, 1970);
        int safeMonth = Math.min(Math.max(month, 1), 12);
        final String sql = "SELECT SUM(o.total_amount) FROM orders o "
                + "JOIN products p ON p.id = o.product_id "
                + "JOIN shops s ON s.id = p.shop_id "
                + "WHERE s.owner_id = ? AND o.status IN (?, ?) "
                + "AND YEAR(o.created_at) = ? AND MONTH(o.created_at) = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setString(2, OrderStatus.CONFIRMED.toDatabaseValue());
            statement.setString(3, OrderStatus.DELIVERED.toDatabaseValue());
            statement.setInt(4, safeYear);
            statement.setInt(5, safeMonth);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return total;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "sumMonthlyRevenueByOwner failed", ex);
        }
        return null;
    }

    public boolean updateStatus(int orderId, String orderToken, OrderStatus status) {
        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND idempotency_key = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            statement.setInt(2, orderId);
            statement.setString(3, orderToken);
            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "updateStatus failed", ex);
            return false;
        }
    }

    public boolean updateStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            statement.setInt(2, orderId);
            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "updateStatus by id failed", ex);
            return false;
        }
    }

    public boolean updatePaymentTransaction(int orderId, long transactionId) {
        String sql = "UPDATE orders SET payment_transaction_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, transactionId);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "updatePaymentTransaction failed", ex);
            return false;
        }
    }

    public List<String> findCredentials(int orderId) {
        String sql = "SELECT key_or_link FROM order_item_credentials WHERE order_id = ? ORDER BY id ASC";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> credentials = new ArrayList<>();
                while (rs.next()) {
                    credentials.add(rs.getString("key_or_link"));
                }
                return credentials;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "findCredentials failed", ex);
            return Collections.emptyList();
        }
    }

    public boolean appendCredential(int orderId, String keyOrLink, String note) {
        try (Connection connection = getConnection()) {
            insertCredential(connection, orderId, keyOrLink, note);
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "appendCredential failed", ex);
            return false;
        }
    }

    public Optional<String> assignCredentialToOrder(int orderId, int productId) {
        String selectSql = "SELECT id, encrypted_value FROM product_credentials "
                + "WHERE product_id = ? AND is_sold = 0 ORDER BY id ASC LIMIT 1 FOR UPDATE";
        String updateSql = "UPDATE product_credentials SET order_id = ?, is_sold = 1 WHERE id = ?";
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, productId);
                try (ResultSet rs = select.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return Optional.empty();
                    }
                    int credentialId = rs.getInt("id");
                    String credentialValue = rs.getString("encrypted_value");
                    try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                        update.setInt(1, orderId);
                        update.setInt(2, credentialId);
                        update.executeUpdate();
                    }
                    insertCredential(connection, orderId, credentialValue, "AUTO");
                    connection.commit();
                    return Optional.ofNullable(credentialValue);
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "assignCredentialToOrder failed", ex);
            return Optional.empty();
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Products product = new Products();
        product.setId(rs.getInt("product_id"));
        product.setShopId(rs.getInt("shop_id"));
        product.setName(rs.getString("product_name"));
        product.setDescription(rs.getString("product_description"));
        product.setPrice(rs.getBigDecimal("product_price"));
        product.setInventoryCount(rs.getInt("inventory_count"));
        product.setStatus(rs.getString("product_status"));

        OrderStatus status = OrderStatus.fromDatabaseValue(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        LocalDateTime createdAt = created == null ? LocalDateTime.now() : created.toLocalDateTime();
        BigDecimal total = rs.getBigDecimal("total_amount");
        Integer buyerId = rs.getInt("buyer_id");
        if (rs.wasNull()) {
            buyerId = null;
        }
        int orderId = rs.getInt("order_id");
        String orderToken = rs.getString("idempotency_key");
        Integer quantity = rs.getInt("order_quantity");
        if (rs.wasNull()) {
            quantity = null;
        }
        return new Order(orderId, product, rs.getString("buyer_email"), total, status,
                createdAt, null, null, buyerId, orderToken, quantity);
    }

    private void insertOrderItem(Connection connection, int orderId, int productId, int quantity) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, created_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        }
    }

    private void insertCredential(Connection connection, int orderId, String value, String note) throws SQLException {
        String sql = "INSERT INTO order_item_credentials (order_id, key_or_link, note, created_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, value);
            statement.setString(3, note);
            statement.executeUpdate();
        }
    }
}
