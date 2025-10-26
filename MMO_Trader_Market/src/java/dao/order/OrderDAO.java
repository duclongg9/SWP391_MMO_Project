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
 * DAO thao tác với bảng {@code orders}, bao gồm tạo đơn, thống kê và hỗ trợ giao dịch.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class OrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    /**
     * Tạo đơn hàng ở trạng thái Pending với khóa idempotency.
     *
     * @param buyerId  mã người mua
     * @param productId mã sản phẩm
     * @param qty       số lượng đặt mua
     * @param total     tổng tiền
     * @param idemKey   khóa idempotent để tránh tạo trùng
     * @return mã đơn hàng vừa tạo
     */
    public int createPending(int buyerId, int productId, int qty, BigDecimal unitPrice, BigDecimal total, String idemKey, String variantCode) {
        final String sql = "INSERT INTO orders (buyer_id, product_id, quantity, unit_price, total_amount, status, variant_code, idempotency_key, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, 'Pending', ?, ?, NOW(), NOW())";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, buyerId);
            statement.setInt(2, productId);
            statement.setInt(3, qty);
            statement.setBigDecimal(4, unitPrice);
            statement.setBigDecimal(5, total);
            if (variantCode == null || variantCode.isBlank()) {
                statement.setNull(6, java.sql.Types.VARCHAR);
            } else {
                statement.setString(6, variantCode);
            }
            statement.setString(7, idemKey);
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

    /**
     * Lấy chi tiết đơn hàng dành cho người mua sở hữu.
     *
     * @param orderId mã đơn hàng
     * @param userId  mã người dùng đăng nhập
     * @return thông tin đơn kèm sản phẩm nếu tìm thấy
     */
    public Optional<OrderDetailView> findByIdForUser(int orderId, int userId) {
        final String sql = "SELECT o.id, o.buyer_id, o.product_id, o.quantity, o.unit_price, o.total_amount, o.status, o.created_at, o.updated_at, "
                + "o.payment_transaction_id, o.idempotency_key, o.hold_until, "
                + "o.variant_code, "
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
                    return Optional.of(new OrderDetailView(order, product, List.of(), null));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy chi tiết đơn hàng", ex);
        }
        return Optional.empty();
    }

    /**
     * Cập nhật trạng thái đơn hàng không ràng buộc giá trị.
     *
     * @param orderId mã đơn hàng
     * @param status  trạng thái mới ở dạng chuỗi
     * @return {@code true} nếu cập nhật thành công
     */
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

    /**
     * Cập nhật đơn hàng sang trạng thái Completed và gán giao dịch thanh toán.
     *
     * @param orderId     mã đơn hàng
     * @param paymentTxId mã giao dịch thanh toán (có thể null)
     * @return {@code true} nếu cập nhật thành công
     */
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

    /**
     * Tìm đơn hàng theo mã.
     *
     * @param orderId mã đơn hàng
     * @return {@link Optional} chứa đơn hàng nếu tồn tại
     */
    public Optional<Orders> findById(int orderId) {
        final String sql = "SELECT id, buyer_id, product_id, quantity, unit_price, payment_transaction_id, total_amount, status, variant_code, idempotency_key, "
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

    /**
     * Tìm đơn hàng thông qua khóa idempotency.
     *
     * @param idemKey khóa idempotent
     * @return {@link Optional} chứa đơn hàng nếu có
     */
    public Optional<Orders> findByIdemKey(String idemKey) {
        final String sql = "SELECT id, buyer_id, product_id, quantity, unit_price, payment_transaction_id, total_amount, status, variant_code, idempotency_key, "
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

    /**
     * Lấy danh sách đơn hàng của người mua có phân trang.
     *
     * @param buyerId mã người mua
     * @param status  trạng thái cần lọc (có thể null)
     * @param limit   số bản ghi mỗi trang
     * @param offset  vị trí bắt đầu
     * @return danh sách đơn hàng rút gọn
     */
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

    /**
     * Đếm số đơn hàng của người mua theo trạng thái.
     *
     * @param buyerId mã người mua
     * @param status  trạng thái cần lọc (có thể null)
     * @return tổng số đơn
     */
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

    /**
     * Đếm tổng số đơn hàng của người mua.
     *
     * @param buyerId mã người mua
     * @return tổng số đơn
     */
    public long countByBuyer(int buyerId) {
        return countByBuyer(buyerId, (String) null);
    }

    /**
     * Đếm số đơn của người mua theo trạng thái enum.
     *
     * @param buyerId mã người mua
     * @param status  trạng thái enum cần lọc
     * @return tổng số đơn
     */
    public long countByBuyerAndStatus(int buyerId, OrderStatus status) {
        return countByBuyer(buyerId, status == null ? null : status.toDatabaseValue());
    }

    /**
     * Đếm số đơn hàng theo trạng thái toàn hệ thống.
     *
     * @param status trạng thái cần thống kê
     * @return tổng số đơn
     */
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

    /**
     * Cập nhật trạng thái đơn hàng trong giao dịch hiện tại.
     *
     * @param connection kết nối đang dùng
     * @param orderId    mã đơn hàng
     * @param status     trạng thái mới
     * @throws SQLException khi câu lệnh SQL lỗi
     */
    public void updateStatus(Connection connection, int orderId, OrderStatus status) throws SQLException {
        final String sql = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.toDatabaseValue());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    /**
     * Gán mã giao dịch thanh toán cho đơn hàng trong giao dịch hiện tại.
     *
     * @param connection           kết nối sử dụng
     * @param orderId              mã đơn hàng
     * @param paymentTransactionId mã giao dịch (có thể null)
     * @throws SQLException khi truy vấn lỗi
     */
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

    /**
     * Ghi nhận lịch sử thay đổi tồn kho liên quan tới đơn hàng.
     *
     * @param connection  kết nối giao dịch
     * @param productId   mã sản phẩm
     * @param orderId     mã đơn hàng
     * @param changeAmount số lượng thay đổi
     * @param reason      lý do
     * @throws SQLException khi chèn bản ghi lỗi
     */
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

    /**
     * Mở kết nối thủ công để tái sử dụng bên ngoài DAO.
     *
     * @return kết nối mới
     * @throws SQLException khi không thể kết nối
     */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }

    /**
     * Ánh xạ dữ liệu {@code orders} từ ResultSet sang thực thể {@link Orders}.
     */
    private Orders mapOrder(ResultSet rs) throws SQLException {
        Orders order = new Orders();
        order.setId(rs.getInt("id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setProductId(rs.getInt("product_id"));
        order.setQuantity(rs.getInt("quantity"));
        order.setUnitPrice(rs.getBigDecimal("unit_price"));
        order.setPaymentTransactionId(rs.getObject("payment_transaction_id") == null
                ? null : rs.getInt("payment_transaction_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setVariantCode(rs.getString("variant_code"));
        order.setIdempotencyKey(rs.getString("idempotency_key"));
        order.setHoldUntil(rs.getTimestamp("hold_until"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        return order;
    }

    /**
     * Ánh xạ dữ liệu sản phẩm phục vụ chi tiết đơn hàng.
     */
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
