package dao.order;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.OrderStatus;
import model.Orders;
import model.Products;
import model.statistics.ProductStatistics;
import model.statistics.TimeStatistics;
import model.view.OrderDetailView;
import model.view.OrderRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO thao tác với bảng {@code orders}, bao gồm tạo đơn, thống kê và hỗ trợ các
 * giao dịch liên quan tới luồng tiền (gán transaction, ghi nhận inventory log).
 * Tất cả truy vấn đều có chú thích tiếng Việt để người đọc biết dữ liệu nào
 * được kéo lên phục vụ JSP hay worker xử lý hàng đợi.
 *
 * @author longpdhe171902
 */
public class OrderDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    /**
     * Tạo đơn hàng ở trạng thái Pending với khóa idempotency.
     * 
     * Đây là điểm cắm đầu tiên để ghi nhận giao dịch vào DB trước khi worker
     * trừ tiền:
     * 
     * Insert bản ghi đơn với trạng thái Pending, giữ lại variant để worker
     * xử lý tồn kho chuẩn xác.
     * Lưu khóa idempotent để các lần submit lại (do reload) không tạo thêm
     * đơn mới.
     * Trả về {@code order_id} cho controller redirect sang trang chi
     * tiết.
     * 
     *
     * @param buyerId mã người mua
     * @param productId mã sản phẩm
     * @param qty số lượng đặt mua
     * @param unitPrice đơn giá tại thời điểm đặt
     * @param total tổng tiền
     * @param variantCode mã biến thể (có thể null)
     * @param idemKey khóa idempotent để tránh tạo trùng
     * @return mã đơn hàng vừa tạo
     */
    public int createPending(int buyerId, int productId, int qty, BigDecimal unitPrice, BigDecimal total,
            String variantCode, String idemKey) {
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
                // Lưu lại mã biến thể để worker xác định đúng SKU khi trừ tồn kho.
                statement.setString(6, variantCode);
            }
            statement.setString(7, idemKey);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    // Trả về khóa chính của bản ghi vừa insert để controller redirect chi tiết.
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
     * Câu truy vấn join trực tiếp bảng {@code products} để có đủ dữ liệu hiển
     * thị trên JSP chi tiết (tên, mô tả, ảnh...). Controller nhận
     * {@link OrderDetailView} và truyền thẳng xuống view.
     *
     * @param orderId mã đơn hàng
     * @param userId mã người dùng đăng nhập
     * @return thông tin đơn kèm sản phẩm nếu tìm thấy
     */
    public Optional<OrderDetailView> findByIdForUser(int orderId, int userId) {
        final String sql = "SELECT o.id, o.buyer_id, o.product_id, o.quantity, o.unit_price, o.total_amount, o.status, "
                + "o.created_at, o.updated_at, o.payment_transaction_id, o.idempotency_key, o.hold_until, o.variant_code, "
                + "o.escrow_hold_seconds, o.escrow_original_release_at, o.escrow_release_at, o.escrow_status, o.escrow_paused_at, "
                + "o.escrow_remaining_seconds, o.escrow_resumed_at, o.escrow_released_at_actual, "
                + "p.id AS p_id, p.shop_id, p.product_type AS p_product_type, p.product_subtype AS p_product_subtype, "
                + "p.name, p.short_description AS p_short_description, p.description, p.price, p.primary_image_url AS p_primary_image_url, "
                + "p.gallery_json AS p_gallery_json, p.inventory_count, COALESCE(ps.sold_count, 0) AS p_sold_count, p.status AS p_status, "
                + "p.variant_schema AS p_variant_schema, p.variants_json AS p_variants_json, "
                + "p.created_at AS p_created_at, p.updated_at AS p_updated_at "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "LEFT JOIN product_sales_view ps ON ps.product_id = p.id "
                + "WHERE o.id = ? AND o.buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ cả Order lẫn Product để trả ra view model phục vụ JSP chi tiết.
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

    /**
     * Cập nhật trạng thái đơn hàng không ràng buộc giá trị.
     * 
     * Được sử dụng ở các luồng ngoại lệ (worker đánh dấu thất bại) nên không
     * đặt thêm ràng buộc enum.
     *
     * @param orderId mã đơn hàng
     * @param status trạng thái mới ở dạng chuỗi
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean setStatus(int orderId, String status) {
        final String sql = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            // Hàm trả về >0 nếu có bản ghi nào được cập nhật.
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái đơn hàng", ex);
            return false;
        }
    }

    /**
     * Cập nhật đơn hàng sang trạng thái Completed và gán giao dịch thanh toán.
     * 
     * Luồng worker sau khi trừ tiền sẽ gọi hàm này trong transaction để gắn
     * reference tới bảng {@code wallet_transactions}. Nhờ vậy trang chi tiết có
     * thể truy vết nguồn gốc dòng tiền.
     *
     * @param orderId mã đơn hàng
     * @param paymentTxId mã giao dịch thanh toán (có thể null)
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean setCompletedWithTx(int orderId, Integer paymentTxId) {
        final String sql = "UPDATE orders SET status = 'Completed', payment_transaction_id = ?, updated_at = NOW() WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (paymentTxId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                // Khi đã có mã giao dịch ví, gắn trực tiếp để trang chi tiết truy vết số tiền.
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
     * Được worker gọi trước khi xử lý để đọc trạng thái hiện tại, số tiền và
     * thông tin variant.
     *
     * @param orderId mã đơn hàng
     * @return {@link Optional} chứa đơn hàng nếu tồn tại
     */
    public Optional<Orders> findById(int orderId) {
        final String sql = "SELECT id, buyer_id, product_id, quantity, unit_price, payment_transaction_id, total_amount, status, "
                + "variant_code, idempotency_key, hold_until, escrow_hold_seconds, escrow_original_release_at, escrow_release_at, "
                + "escrow_status, escrow_paused_at, escrow_remaining_seconds, escrow_resumed_at, escrow_released_at_actual, "
                + "created_at, updated_at FROM orders WHERE id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Chỉ map và trả về khi tìm thấy bản ghi hợp lệ.
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
     * Dịch vụ gọi phương thức này để phát hiện các lần submit lặp lại từ
     * client.
     *
     * @param idemKey khóa idempotent
     * @return {@link Optional} chứa đơn hàng nếu có
     */
    public Optional<Orders> findByIdemKey(String idemKey) {
        final String sql = "SELECT id, buyer_id, product_id, quantity, unit_price, payment_transaction_id, total_amount, status, "
                + "variant_code, idempotency_key, hold_until, escrow_hold_seconds, escrow_original_release_at, escrow_release_at, "
                + "escrow_status, escrow_paused_at, escrow_remaining_seconds, escrow_resumed_at, escrow_released_at_actual, "
                + "created_at, updated_at FROM orders WHERE idempotency_key = ? LIMIT 1";
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
     * Đánh dấu đơn hàng đang tranh chấp để đóng băng escrow.
     */
    public boolean pauseEscrowForDispute(int orderId, int buyerId, Timestamp pausedAt, Integer remainingSeconds,
            java.util.Date releaseAtSnapshot, Connection connection) throws SQLException {
        final String sql = "UPDATE orders SET status = 'Disputed', escrow_status = 'Paused', escrow_paused_at = ?, "
                + "escrow_remaining_seconds = ?, escrow_resumed_at = NULL, escrow_release_at = NULL, "
                + "escrow_original_release_at = COALESCE(escrow_original_release_at, escrow_release_at, ?), updated_at = NOW()"
                + " WHERE id = ? AND buyer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pausedAt == null) {
                statement.setNull(1, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(1, pausedAt);
            }
            if (remainingSeconds == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, remainingSeconds);
            }
            Timestamp releaseSnapshot;
            if (releaseAtSnapshot != null) {
                releaseSnapshot = new Timestamp(releaseAtSnapshot.getTime());
            } else if (pausedAt != null) {
                releaseSnapshot = pausedAt;
            } else {
                releaseSnapshot = new Timestamp(System.currentTimeMillis());
            }
            statement.setTimestamp(3, releaseSnapshot);
            statement.setInt(4, orderId);
            statement.setInt(5, buyerId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Lên lịch giải ngân escrow tự động cho đơn hàng vừa hoàn tất.
     *
     * @param connection kết nối giao dịch hiện tại
     * @param orderId mã đơn hàng
     * @param releaseAt thời điểm dự kiến giải ngân
     * @param holdSeconds tổng thời gian giữ tiền theo cấu hình (giây)
     * @return {@code true} nếu cập nhật thành công
     * @throws SQLException khi truy vấn lỗi
     */
    public boolean scheduleEscrowRelease(Connection connection, int orderId, Timestamp releaseAt, int holdSeconds)
            throws SQLException {
        final String sql = "UPDATE orders SET escrow_hold_seconds = ?, "
                + "escrow_original_release_at = COALESCE(escrow_original_release_at, ?), "
                + "escrow_release_at = ?, escrow_status = 'Scheduled', escrow_paused_at = NULL, "
                + "escrow_remaining_seconds = NULL, escrow_resumed_at = NULL, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int normalizedHold = Math.max(holdSeconds, 0);
            statement.setInt(1, normalizedHold);
            if (releaseAt == null) {
                statement.setNull(2, java.sql.Types.TIMESTAMP);
                statement.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(2, releaseAt);
                statement.setTimestamp(3, releaseAt);
            }
            statement.setInt(4, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Ghi log sự kiện lên lịch escrow nhằm phục vụ truy vết thay đổi tự động.
     *
     * @param connection kết nối giao dịch
     * @param orderId mã đơn hàng
     * @param releaseAt thời điểm dự kiến giải ngân
     * @param holdSeconds tổng thời gian giữ tiền (giây)
     * @throws SQLException khi thao tác ghi log thất bại
     */
    public void insertEscrowScheduledEvent(Connection connection, int orderId, Timestamp releaseAt, int holdSeconds)
            throws SQLException {
        final String sql = "INSERT INTO order_escrow_events (order_id, event_type, actor_type, release_at_snapshot, "
                + "remaining_seconds_snapshot, metadata, created_at) VALUES (?, 'SCHEDULED', 'SYSTEM', ?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            if (releaseAt == null) {
                statement.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(2, releaseAt);
            }
            if (holdSeconds <= 0) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, holdSeconds);
            }
            String metadata = String.format("{\"holdSeconds\":%d}", Math.max(holdSeconds, 0));
            statement.setString(4, metadata);
            statement.executeUpdate();
        }
    }

    /**
     * Đánh dấu đơn hàng đã giải ngân escrow về phía người bán.
     *
     * @param connection kết nối đang mở transaction
     * @param orderId mã đơn hàng
     * @param releasedAt mốc thời gian thực tế hệ thống giải ngân
     * @return {@code true} nếu có bản ghi được cập nhật
     * @throws SQLException nếu câu lệnh SQL gặp lỗi
     */
    public boolean markEscrowReleased(Connection connection, int orderId, Timestamp releasedAt) throws SQLException {
        final String sql = "UPDATE orders SET escrow_status = 'Released', escrow_remaining_seconds = 0, "
                + "escrow_released_at_actual = ?, updated_at = ? "
                + "WHERE id = ? AND status = 'Completed' AND escrow_status = 'Scheduled'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, releasedAt);
            statement.setTimestamp(2, releasedAt);
            statement.setInt(3, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Ghi log sự kiện escrow được giải ngân để tiện truy vết.
     *
     * @param connection kết nối transaction
     * @param orderId mã đơn hàng
     * @param scheduledRelease mốc giải ngân dự kiến chụp lại tại thời điểm xử lý
     * @param releasedAt mốc thực tế đã giải ngân
     * @param metadataJson chuỗi JSON mô tả ngữ cảnh (có thể null)
     * @throws SQLException nếu thao tác ghi log thất bại
     */
    public void insertEscrowReleasedEvent(Connection connection, int orderId, Timestamp scheduledRelease,
            Timestamp releasedAt, String metadataJson) throws SQLException {
        final String sql = "INSERT INTO order_escrow_events (order_id, event_type, actor_type, release_at_snapshot, "
                + "remaining_seconds_snapshot, metadata, created_at) VALUES (?, 'RELEASED', 'SYSTEM', ?, 0, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            if (scheduledRelease == null) {
                statement.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(2, scheduledRelease);
            }
            if (metadataJson == null || metadataJson.isBlank()) {
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, metadataJson);
            }
            statement.setTimestamp(4, releasedAt);
            statement.executeUpdate();
        }
    }

    /**
     * Lấy danh sách đơn hàng của người mua có phân trang.
     * 
     * Câu truy vấn join sang bảng sản phẩm để lấy tên hiển thị trong bảng lịch
     * sử.
     *
     * @param buyerId mã người mua
     * @param status trạng thái cần lọc (có thể null)
     * @param limit số bản ghi mỗi trang
     * @param offset vị trí bắt đầu
     * @return danh sách đơn hàng rút gọn
     */
    public List<OrderRow> findByBuyerPaged(int buyerId, String status, Integer orderId, String productName,
            LocalDate fromDate, LocalDate toDate, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT o.id, o.total_amount, o.status, o.created_at, p.name AS product_name "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE o.buyer_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(buyerId);
        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        if (orderId != null) {
            sql.append(" AND o.id = ?");
            params.add(orderId);
        }
        if (productName != null) {
            sql.append(" AND LOWER(p.name) LIKE ?");
            params.add('%' + productName.toLowerCase() + '%');
        }
        if (fromDate != null) {
            sql.append(" AND o.created_at >= ?");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sql.append(" AND o.created_at < ?");
            params.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<OrderRow> rows = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                int index = i + 1;
                if (param instanceof Integer) {
                    statement.setInt(index, (Integer) param);
                } else if (param instanceof String) {
                    statement.setString(index, (String) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(index, (Timestamp) param);
                } else {
                    statement.setObject(index, param);
                }
            }
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
     * Đếm số đơn hàng của người mua theo trạng thái để tính phân trang ở tầng
     * dịch vụ.
     *
     * @param buyerId mã người mua
     * @param status trạng thái cần lọc (có thể null)
     * @return tổng số đơn
     */
    public long countByBuyer(int buyerId, String status, Integer orderId, String productName,
            LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE o.buyer_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(buyerId);
        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        if (orderId != null) {
            sql.append(" AND o.id = ?");
            params.add(orderId);
        }
        if (productName != null) {
            sql.append(" AND LOWER(p.name) LIKE ?");
            params.add('%' + productName.toLowerCase() + '%');
        }
        if (fromDate != null) {
            sql.append(" AND o.created_at >= ?");
            params.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sql.append(" AND o.created_at < ?");
            params.add(Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
        }

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                int index = i + 1;
                if (param instanceof Integer) {
                    statement.setInt(index, (Integer) param);
                } else if (param instanceof String) {
                    statement.setString(index, (String) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(index, (Timestamp) param);
                } else {
                    statement.setObject(index, param);
                }
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
        return countByBuyer(buyerId, null, null, null, null, null);
    }

    /**
     * Đếm số đơn của người mua theo trạng thái enum.
     *
     * @param buyerId mã người mua
     * @param status trạng thái enum cần lọc
     * @return tổng số đơn
     */
    public long countByBuyerAndStatus(int buyerId, OrderStatus status) {
        return countByBuyer(buyerId, status == null ? null : status.toDatabaseValue(), null, null, null, null);
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
     * @param orderId mã đơn hàng
     * @param status trạng thái mới
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
     * @param connection kết nối sử dụng
     * @param orderId mã đơn hàng
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
     * @param connection kết nối giao dịch
     * @param productId mã sản phẩm
     * @param orderId mã đơn hàng
     * @param changeAmount số lượng thay đổi
     * @param reason lý do
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
        int qty = rs.getInt("quantity");
        order.setQuantity(rs.wasNull() ? null : qty);
        order.setUnitPrice(rs.getBigDecimal("unit_price"));
        order.setPaymentTransactionId(rs.getObject("payment_transaction_id") == null
                ? null : rs.getInt("payment_transaction_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setVariantCode(rs.getString("variant_code"));
        order.setIdempotencyKey(rs.getString("idempotency_key"));
        order.setHoldUntil(rs.getTimestamp("hold_until"));
        int holdSeconds = rs.getInt("escrow_hold_seconds");
        if (!rs.wasNull()) {
            order.setEscrowHoldSeconds(holdSeconds);
        }
        Timestamp originalRelease = rs.getTimestamp("escrow_original_release_at");
        if (originalRelease != null) {
            order.setEscrowOriginalReleaseAt(new java.util.Date(originalRelease.getTime()));
        }
        Timestamp releaseAt = rs.getTimestamp("escrow_release_at");
        if (releaseAt != null) {
            order.setEscrowReleaseAt(new java.util.Date(releaseAt.getTime()));
        }
        order.setEscrowStatus(rs.getString("escrow_status"));
        Timestamp pausedAt = rs.getTimestamp("escrow_paused_at");
        if (pausedAt != null) {
            order.setEscrowPausedAt(new java.util.Date(pausedAt.getTime()));
        }
        int remainingSeconds = rs.getInt("escrow_remaining_seconds");
        if (!rs.wasNull()) {
            order.setEscrowRemainingSeconds(remainingSeconds);
        }
        Timestamp resumedAt = rs.getTimestamp("escrow_resumed_at");
        if (resumedAt != null) {
            order.setEscrowResumedAt(new java.util.Date(resumedAt.getTime()));
        }
        Timestamp releasedAtActual = rs.getTimestamp("escrow_released_at_actual");
        if (releasedAtActual != null) {
            order.setEscrowReleasedAtActual(new java.util.Date(releasedAtActual.getTime()));
        }
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
        Number soldNumber = (Number) rs.getObject("p_sold_count");
        product.setSoldCount(soldNumber == null ? null : soldNumber.intValue());
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


    /**
     * Tính tổng doanh thu từ các đơn hàng đã hoàn thành của shop trong khoảng thời gian.
     *
     * @param shopId mã shop
     * @param startDate ngày bắt đầu (có thể null)
     * @param endDate ngày kết thúc (có thể null)
     * @return tổng doanh thu
     */
    public BigDecimal getRevenueByShop(int shopId, Timestamp startDate, Timestamp endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(o.total_amount), 0) AS total_revenue "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE p.shop_id = ? AND o.status = 'Completed'");
        List<Object> params = new ArrayList<>();
        params.add(shopId);
        if (startDate != null) {
            sql.append(" AND o.created_at >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND o.created_at < ?");
            params.add(endDate);
        }
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(i + 1, (Timestamp) param);
                }
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal("total_revenue");
                    return result == null ? BigDecimal.ZERO : result;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tính doanh thu theo shop", ex);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Đếm số đơn hàng đã hoàn thành nhưng chưa được giải ngân (chưa có wallet transaction PAYOUT).
     *
     * @param shopId mã shop
     * @return số đơn chờ giải ngân
     */
    public int countPendingDisbursementOrders(int shopId) {
        final String sql = "SELECT COUNT(DISTINCT o.id) AS pending_count "
                + "FROM orders o "
                + "JOIN products p ON p.id = o.product_id "
                + "WHERE p.shop_id = ? AND o.status = 'Completed' "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM wallet_transactions wt "
                + "  WHERE wt.related_entity_id = o.id "
                + "  AND wt.transaction_type = 'Payout'"
                + ")";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("pending_count");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn chờ giải ngân", ex);
        }
        return 0;
    }

    //Tính tổng số đơn hàng theo tháng
    public int gettotalOrderByMonth(int month, int year) {
        String sql = """
        SELECT COUNT(*) AS total_orders
        FROM mmo_schema.orders
        WHERE YEAR(created_at) = ?
          AND MONTH(created_at) = ?
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_orders");
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return 0;
    }


    /**
     * Đếm số đơn hàng đã hoàn thành của shop.
     *
     * @param shopId mã shop
     * @return số đơn đã bán (Completed)
     */
    public int countCompletedOrdersByShop(int shopId) {
        final String sql = "SELECT COUNT(*) FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE p.shop_id = ? AND o.status = 'Completed'";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm đơn đã bán theo shop", ex);
        }
        return 0;
    }

    /**
     * Lấy danh sách đơn hàng của shop để hiển thị trong bảng thu nhập.
     *
     * @param shopId mã shop
     * @param limit số lượng bản ghi
     * @return danh sách đơn hàng
     */
    public List<OrderRow> findByShopId(int shopId, int limit) {
        final String sql = "SELECT o.id, o.total_amount, o.status, o.created_at, p.name AS product_name "
                + "FROM orders o JOIN products p ON p.id = o.product_id "
                + "WHERE p.shop_id = ? AND o.status = 'Completed' "
                + "ORDER BY o.created_at DESC LIMIT ?";
        List<OrderRow> rows = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            statement.setInt(2, limit);
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
            LOGGER.log(Level.SEVERE, "Không thể tải đơn hàng theo shop", ex);
        }
        return rows;
    }
    
    /**
     * Lấy thống kê doanh thu và số lượng bán theo sản phẩm của shop, phân loại theo trạng thái đơn hàng.
     * Chỉ lấy các đơn có trạng thái: Pending (đang chờ), Completed (hoàn thành), Failed (thất bại).
     *
     * @param shopId mã shop
     * @param startDate ngày bắt đầu (có thể null)
     * @param endDate ngày kết thúc (có thể null)
     * @return danh sách thống kê theo sản phẩm và trạng thái
     */
    public List<ProductStatistics> getProductStatistics(int shopId, Timestamp startDate, Timestamp endDate) {
        StringBuilder sql = new StringBuilder("SELECT p.id, p.name, o.status, ");
        sql.append("COALESCE(SUM(o.total_amount), 0) AS revenue, ");
        sql.append("COUNT(o.id) AS order_count, ");
        sql.append("COALESCE(SUM(o.quantity), 0) AS quantity_sold ");
        sql.append("FROM products p ");
        sql.append("INNER JOIN orders o ON o.product_id = p.id ");
        sql.append("WHERE p.shop_id = ? ");
        sql.append("AND o.status IN ('Pending', 'Completed', 'Failed') ");
        if (startDate != null) {
            sql.append("AND o.created_at >= ? ");
        }
        if (endDate != null) {
            sql.append("AND o.created_at < ? ");
        }
        sql.append("GROUP BY p.id, p.name, o.status ");
        sql.append("ORDER BY p.name ASC, o.status ASC, revenue DESC");
        
        List<Object> params = new ArrayList<>();
        params.add(shopId);
        if (startDate != null) {
            params.add(startDate);
        }
        if (endDate != null) {
            params.add(endDate);
        }
        
        List<ProductStatistics> stats = new ArrayList<>();
        try (Connection connection = getConnection(); 
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(i + 1, (Timestamp) param);
                }
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ProductStatistics stat = new ProductStatistics();
                    stat.setProductId(rs.getInt("id"));
                    stat.setProductName(rs.getString("name"));
                    stat.setStatus(rs.getString("status"));
                    stat.setRevenue(rs.getBigDecimal("revenue"));
                    stat.setOrderCount(rs.getInt("order_count"));
                    stat.setQuantitySold(rs.getInt("quantity_sold"));
                    stats.add(stat);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy thống kê theo sản phẩm", ex);
        }
        return stats;
    }
    
    /**
     * Lấy thống kê doanh thu theo thời gian (ngày, tuần, tháng, năm).
     *
     * @param shopId mã shop
     * @param period loại period: "day", "week", "month", "year"
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return danh sách thống kê theo thời gian
     */
    public List<TimeStatistics> getTimeStatistics(int shopId, String period, Timestamp startDate, Timestamp endDate) {
        String dateFormat;
        switch (period.toLowerCase()) {
            case "day":
                dateFormat = "%Y-%m-%d";
                break;
            case "week":
                dateFormat = "%Y-%u"; // Year-Week
                break;
            case "month":
                dateFormat = "%Y-%m";
                break;
            case "year":
                dateFormat = "%Y";
                break;
            default:
                dateFormat = "%Y-%m-%d";
        }
        
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("DATE_FORMAT(o.created_at, ?) AS period_label, ");
        sql.append("COALESCE(SUM(o.total_amount), 0) AS revenue, ");
        sql.append("COUNT(o.id) AS order_count ");
        sql.append("FROM orders o ");
        sql.append("JOIN products p ON p.id = o.product_id ");
        sql.append("WHERE p.shop_id = ? AND o.status = 'Completed' ");
        if (startDate != null) {
            sql.append("AND o.created_at >= ? ");
        }
        if (endDate != null) {
            sql.append("AND o.created_at < ? ");
        }
        sql.append("GROUP BY DATE_FORMAT(o.created_at, ?) ");
        sql.append("ORDER BY period_label ASC");
        
        List<Object> params = new ArrayList<>();
        params.add(dateFormat);
        params.add(shopId);
        if (startDate != null) {
            params.add(startDate);
        }
        if (endDate != null) {
            params.add(endDate);
        }
        params.add(dateFormat); // Thêm lại dateFormat cho GROUP BY
        
        List<TimeStatistics> stats = new ArrayList<>();
        try (Connection connection = getConnection(); 
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    statement.setString(i + 1, (String) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(i + 1, (Timestamp) param);
                }
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    TimeStatistics stat = new TimeStatistics();
                    stat.setPeriodLabel(rs.getString("period_label"));
                    stat.setRevenue(rs.getBigDecimal("revenue"));
                    stat.setOrderCount(rs.getInt("order_count"));
                    stats.add(stat);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy thống kê theo thời gian", ex);
        }
        return stats;
    }


}
