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
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    private static final String BASE_SELECT = "SELECT "
            + "o.id AS order_id, o.product_id, o.buyer_id, o.total_amount, o.status AS order_status, o.created_at AS order_created_at, "
            + "p.shop_id, p.name AS product_name, p.description AS product_description, p.price AS product_price, "
            + "p.inventory_count AS product_inventory, p.status AS product_status, p.created_at AS product_created_at, "
            + "p.updated_at AS product_updated_at, u.email AS buyer_email, "
            + "wt.transaction_type AS transaction_type, pc.encrypted_value AS activation_code "
            + "FROM orders o "
            + "JOIN products p ON p.id = o.product_id "
            + "JOIN users u ON u.id = o.buyer_id "
            + "LEFT JOIN wallet_transactions wt ON wt.id = o.payment_transaction_id "
            + "LEFT JOIN product_credentials pc ON pc.order_id = o.id ";

    public int countAll() {
        final String sql = "SELECT COUNT(*) FROM orders";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số đơn hàng", ex);
        }
        return 0;
    }

    public long countByStatus(OrderStatus status) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn hàng theo trạng thái", ex);
        }
        return 0;
    }

    public long countByBuyer(int buyerId) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, buyerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn hàng của người mua", ex);
        }
        return 0;
    }

    public long countByBuyerAndStatus(int buyerId, OrderStatus status) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ? AND status = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, buyerId);
            statement.setString(2, status.toDatabaseValue());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn hàng theo trạng thái và người mua", ex);
        }
        return 0;
    }

    public List<Order> findAll(int limit, int offset) {
        final String sql = BASE_SELECT + "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            List<Order> orders = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
            return orders;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách đơn hàng", ex);
            return List.of();
        }
    }

    public Optional<Order> findById(int orderId) {
        final String sql = BASE_SELECT + "WHERE o.id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải chi tiết đơn hàng", ex);
        }
        return Optional.empty();
    }

    public Order createOrder(Products product, int buyerId, String paymentMethod)
            throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int orderId = insertOrder(connection, product, buyerId);
                boolean credentialAssigned = assignCredential(connection, orderId, product.getId());
                if (credentialAssigned) {
                    updateOrderStatus(connection, orderId, OrderStatus.COMPLETED);
                }
                decrementInventory(connection, product.getId());
                connection.commit();
                return findById(orderId)
                        .map(order -> enrichPaymentMethod(order, paymentMethod))
                        .orElseThrow(() -> new SQLException("Không thể tải lại đơn hàng vừa tạo"));
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private int insertOrder(Connection connection, Products product, int buyerId)
            throws SQLException {
        final String sql = "INSERT INTO orders (buyer_id, product_id, total_amount, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, buyerId);
            statement.setInt(2, product.getId());
            BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            statement.setBigDecimal(3, price);
            statement.setString(4, OrderStatus.PROCESSING.toDatabaseValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Không thể tạo đơn hàng mới");
    }

    private boolean assignCredential(Connection connection, int orderId, Integer productId) throws SQLException {
        final String selectSql = "SELECT id, encrypted_value FROM product_credentials "
                + "WHERE product_id = ? AND is_sold = 0 ORDER BY id ASC LIMIT 1 FOR UPDATE";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setInt(1, productId);
            try (ResultSet rs = select.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int credentialId = rs.getInt("id");
                final String updateSql = "UPDATE product_credentials SET order_id = ?, is_sold = 1 "
                        + "WHERE id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setInt(1, orderId);
                    update.setInt(2, credentialId);
                    update.executeUpdate();
                }
                return true;
            }
        }
    }

    private void updateOrderStatus(Connection connection, int orderId, OrderStatus status) throws SQLException {
        final String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    private void decrementInventory(Connection connection, Integer productId) throws SQLException {
        final String sql = "UPDATE products SET inventory_count = CASE "
                + "WHEN inventory_count > 0 THEN inventory_count - 1 ELSE 0 END, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Products product = new Products();
        product.setId(rs.getInt("product_id"));
        product.setShopId(rs.getInt("shop_id"));
        product.setName(rs.getString("product_name"));
        product.setDescription(rs.getString("product_description"));
        product.setPrice(rs.getBigDecimal("product_price"));
        product.setInventoryCount(rs.getInt("product_inventory"));
        product.setStatus(rs.getString("product_status"));
        Timestamp productCreated = rs.getTimestamp("product_created_at");
        Timestamp productUpdated = rs.getTimestamp("product_updated_at");
        if (productCreated != null) {
            product.setCreatedAt(new java.util.Date(productCreated.getTime()));
        }
        if (productUpdated != null) {
            product.setUpdatedAt(new java.util.Date(productUpdated.getTime()));
        }

        OrderStatus status = OrderStatus.fromDatabaseValue(rs.getString("order_status"));
        Timestamp orderCreated = rs.getTimestamp("order_created_at");
        LocalDateTime createdAt = orderCreated == null ? LocalDateTime.now() : orderCreated.toLocalDateTime();
        String activationCode = rs.getString("activation_code");
        String paymentMethod = resolvePaymentMethod(rs.getString("transaction_type"));

        return new Order(rs.getInt("order_id"), product, rs.getString("buyer_email"), paymentMethod,
                status, createdAt, activationCode, null);
    }

    private String resolvePaymentMethod(String transactionType) {
        if (transactionType == null) {
            return null;
        }
        return switch (transactionType) {
            case "Purchase" -> "Ví ký quỹ MMO";
            case "Deposit" -> "Nạp ví";
            case "Withdrawal" -> "Rút ví";
            default -> transactionType;
        };
    }

    private Order enrichPaymentMethod(Order order, String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return order;
        }
        return new Order(order.getId(), order.getProduct(), order.getBuyerEmail(), paymentMethod,
                order.getStatus(), order.getCreatedAt(), order.getActivationCode(), order.getDeliveryLink());
    }
}

