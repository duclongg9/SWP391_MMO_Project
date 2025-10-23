package dao.order;

import dao.BaseDAO;
import model.OrderStatus;
import model.Orders;
import model.Products;
import model.view.OrderDetailView;
import model.view.OrderRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access layer for orders table, encapsulating CRUD and transactional
 * helpers.
 */
public class OrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    public int createPending(int buyerId, int productId, int qty, BigDecimal total, String idemKey) {
        final String sql = "INSERT INTO orders (buyer_id, product_id, total_amount, status, idempotency_key, created_at, updated_at) "
                + "VALUES (?, ?, ?, 'Pending', ?, NOW(), NOW())";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, buyerId);
            statement.setInt(2, productId);
            statement.setBigDecimal(3, total);
            statement.setString(4, idemKey);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo đơn hàng mới", ex);
        }
        throw new IllegalStateException("Không thể tạo đơn hàng mới");
    }

    public Optional<OrderDetailView> findByIdForUser(int orderId, int userId) {
        final String sql = "SELECT o.id, o.buyer_id, o.product_id, o.total_amount, o.status, o.created_at, o.updated_at, "
                + "o.payment_transaction_id, o.idempotency_key, o.hold_until, "
                + "p.id AS p_id, p.shop_id, p.product_type AS p_product_type, p.product_subtype AS p_product_subtype, "
                + "p.name, p.short_description AS p_short_description, p.description, p.price, p.primary_image_url AS p_primary_image_url, "
                + "p.gallery_json AS p_gallery_json, p.inventory_count, p.sold_count AS p_sold_count, p.status AS p_status, "
                + "p.variant_schema AS p_variant_schema, p.variants_json AS p_variants_json, "
                + "p.created_at AS p_created_at, p.updated_at AS p_updated_at "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE o.id = ? AND o.buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Orders order = mapOrder(rs);
                    Products product = mapProduct(rs);
                    return Optional.of(new OrderDetailView(order, product, List.of()));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy chi tiết đơn hàng", ex);
        }
        return Optional.empty();
    }

    public boolean setStatus(int orderId, String status) {
        final String sql = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái đơn hàng", ex);
            return false;
        }
    }

    public boolean setCompletedWithTx(int orderId, Integer paymentTxId) {
        final String sql = "UPDATE orders SET status = 'Completed', payment_transaction_id = ?, updated_at = NOW() WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (paymentTxId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, paymentTxId);
            }
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể hoàn tất đơn hàng", ex);
            return false;
        }
    }

    public Optional<Orders> findById(int orderId) {
        final String sql = "SELECT id, buyer_id, product_id, payment_transaction_id, total_amount, status, idempotency_key, "
                + "hold_until, created_at, updated_at FROM orders WHERE id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOrder(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm đơn hàng", ex);
        }
        return Optional.empty();
    }

    public Optional<Orders> findByIdemKey(String idemKey) {
        final String sql = "SELECT id, buyer_id, product_id, payment_transaction_id, total_amount, status, idempotency_key, "
                + "hold_until, created_at, updated_at FROM orders WHERE idempotency_key = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, idemKey);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOrder(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm đơn hàng theo khóa idempotency", ex);
        }
        return Optional.empty();
    }

    public List<OrderRow> findByBuyerPaged(int buyerId, String status, int limit, int offset) {
        final String sql = "SELECT o.id, o.total_amount, o.status, o.created_at, p.name AS product_name "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE o.buyer_id = ? "
                + "AND (? IS NULL OR o.status = ?) "
                + "ORDER BY o.created_at DESC LIMIT ? OFFSET ?";
        List<OrderRow> rows = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, buyerId);
            if (status == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, status);
                statement.setString(3, status);
            }
            statement.setInt(4, limit);
            statement.setInt(5, offset);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp created = rs.getTimestamp("created_at");
                    rows.add(new OrderRow(
                            rs.getInt("id"),
                            rs.getString("product_name"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status"),
                            created == null ? null : new java.util.Date(created.getTime())
                    ));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách đơn hàng", ex);
        }
        return rows;
    }

    public long countByBuyer(int buyerId, String status) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ? AND (? IS NULL OR status = ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, buyerId);
            if (status == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, status);
                statement.setString(3, status);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn hàng", ex);
        }
        return 0;
    }

    public long countByBuyer(int buyerId) {
        return countByBuyer(buyerId, (String) null);
    }

    public long countByBuyerAndStatus(int buyerId, OrderStatus status) {
        return countByBuyer(buyerId, status == null ? null : status.toDatabaseValue());
    }

    public long countByStatus(OrderStatus status) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE (? IS NULL OR status = ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (status == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                String dbStatus = status.toDatabaseValue();
                statement.setString(1, dbStatus);
                statement.setString(2, dbStatus);
            }
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

    public void updateStatus(Connection connection, int orderId, OrderStatus status) throws SQLException {
        final String sql = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void assignPaymentTransaction(Connection connection, int orderId, Integer paymentTransactionId) throws SQLException {
        final String sql = "UPDATE orders SET payment_transaction_id = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (paymentTransactionId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, paymentTransactionId);
            }
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void insertInventoryLog(Connection connection, int productId, int orderId, int changeAmount, String reason)
            throws SQLException {
        final String sql = "INSERT INTO inventory_logs (product_id, related_order_id, change_amount, reason, created_at) "
                + "VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, orderId);
            statement.setInt(3, changeAmount);
            statement.setString(4, reason);
            statement.executeUpdate();
        }
    }

    public Connection openConnection() throws SQLException {
        return getConnection();
    }

    private Orders mapOrder(ResultSet rs) throws SQLException {
        Orders order = new Orders();
        order.setId(rs.getInt("id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setProductId(rs.getInt("product_id"));
        order.setPaymentTransactionId(rs.getObject("payment_transaction_id") == null
                ? null : rs.getInt("payment_transaction_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setIdempotencyKey(rs.getString("idempotency_key"));
        order.setHoldUntil(rs.getTimestamp("hold_until"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        return order;
    }

    private Products mapProduct(ResultSet rs) throws SQLException {
        Products product = new Products();
        product.setId(rs.getInt("p_id"));
        product.setShopId(rs.getInt("shop_id"));
        product.setProductType(rs.getString("p_product_type"));
        product.setProductSubtype(rs.getString("p_product_subtype"));
        product.setName(rs.getString("name"));
        product.setShortDescription(rs.getString("p_short_description"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setPrimaryImageUrl(rs.getString("p_primary_image_url"));
        product.setGalleryJson(rs.getString("p_gallery_json"));
        int inventory = rs.getInt("inventory_count");
        product.setInventoryCount(rs.wasNull() ? null : inventory);
        int sold = rs.getInt("p_sold_count");
        product.setSoldCount(rs.wasNull() ? null : sold);
        product.setStatus(rs.getString("p_status"));
        product.setVariantSchema(rs.getString("p_variant_schema"));
        product.setVariantsJson(rs.getString("p_variants_json"));
        Timestamp created = rs.getTimestamp("p_created_at");
        Timestamp updated = rs.getTimestamp("p_updated_at");
        if (created != null) {
            product.setCreatedAt(new java.util.Date(created.getTime()));
        }
        if (updated != null) {
            product.setUpdatedAt(new java.util.Date(updated.getTime()));
        }
        return product;
    }

}
