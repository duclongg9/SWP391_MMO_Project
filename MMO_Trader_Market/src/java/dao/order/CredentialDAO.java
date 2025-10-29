package dao.order;

import dao.BaseDAO;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for product_credentials table.
 */
public class CredentialDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(CredentialDAO.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final char[] PASSWORD_SYMBOLS = "!@#$%&*-_".toCharArray();

    public List<Integer> pickFreeCredentialIds(int productId, int qty) {
        return pickFreeCredentialIds(productId, qty, null);
    }

    public List<Integer> pickFreeCredentialIds(int productId, int qty, String variantCode) {
        try (Connection connection = getConnection()) {
            return pickFreeCredentialIds(connection, productId, qty, variantCode);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể khóa credentials", ex);
            return List.of();
        }
    }

    public List<Integer> pickFreeCredentialIds(Connection connection, int productId, int qty) throws SQLException {
        return pickFreeCredentialIds(connection, productId, qty, null);
    }

    public List<Integer> pickFreeCredentialIds(Connection connection, int productId, int qty, String variantCode) throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        // Các bản ghi cũ có thể lưu trữ variant_code rỗng thay vì NULL nên cần gom về chung điều kiện.
        StringBuilder sql = new StringBuilder("SELECT id FROM product_credentials WHERE product_id = ? AND is_sold = 0");
        if (normalized == null) {
            sql.append(" AND (variant_code IS NULL OR TRIM(variant_code) = '')");
        } else {
            sql.append(" AND LOWER(TRIM(variant_code)) = ?");
        }
        sql.append(" ORDER BY id ASC LIMIT ? FOR UPDATE");
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, productId);
            if (normalized != null) {
                statement.setString(index++, normalized);
            }
            statement.setInt(index, qty);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }
        return ids;
    }

    public CredentialAvailability fetchAvailability(int productId) {
        try (Connection connection = getConnection()) {
            return fetchAvailability(connection, productId);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê credential khả dụng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailability(int productId, String variantCode) {
        String normalized = normalizeVariantCode(variantCode);
        if (normalized == null) {
            return fetchAvailability(productId);
        }
        try (Connection connection = getConnection()) {
            return fetchAvailabilityForVariant(connection, productId, normalized);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê credential khả dụng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailability(Connection connection, int productId) throws SQLException {
        final String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int available = rs.getInt("available");
                    if (rs.wasNull()) {
                        available = 0;
                    }
                    return new CredentialAvailability(total, available);
                }
            }
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailabilityForVariant(Connection connection, int productId, String variantCode)
            throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ?");
        boolean filterDefaultVariant = normalized == null;
        if (filterDefaultVariant) {
            sql.append(" AND (variant_code IS NULL OR TRIM(variant_code) = '')");
        } else {
            sql.append(" AND LOWER(TRIM(variant_code)) = ?");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, productId);
            if (!filterDefaultVariant) {
                statement.setString(2, normalized);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int available = rs.getInt("available");
                    if (rs.wasNull()) {
                        available = 0;
                    }
                    return new CredentialAvailability(total, available);
                }
            }
        }
        return new CredentialAvailability(0, 0);
    }

    /**
     * Đảm bảo kho credential của sản phẩm (hoặc biến thể) có đủ số lượng khả
     * dụng để tạo đơn hàng mới.
     * <p>
     * Nếu tồn kho hiện tại thiếu, phương thức sẽ tự sinh thêm credential ảo và
     * trả về số liệu mới nhất.</p>
     */
    public CredentialAvailability ensureAvailabilityForOrder(int productId, String variantCode, int requiredQuantity) {
        String normalized = normalizeVariantCode(variantCode);
        if (requiredQuantity <= 0) {
            return normalized == null ? fetchAvailability(productId) : fetchAvailability(productId, variantCode);
        }
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                CredentialAvailability availability = normalized == null
                        ? fetchAvailability(connection, productId)
                        : fetchAvailabilityForVariant(connection, productId, normalized);
                int missing = requiredQuantity - availability.available();
                if (missing > 0) {
                    generateFakeCredentials(connection, productId, normalized, missing);
                    availability = normalized == null
                            ? fetchAvailability(connection, productId)
                            : fetchAvailabilityForVariant(connection, productId, normalized);
                }
                connection.commit();
                return availability;
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể đảm bảo credential sẵn sàng", ex);
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đảm bảo credential sẵn sàng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public void markCredentialsSold(int orderId, List<Integer> ids) {
        try (Connection connection = getConnection()) {
            markCredentialsSold(connection, orderId, ids);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật credentials", ex);
        }
    }

    public void markCredentialsSold(Connection connection, int orderId, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) {
            return;
        }
        final String sql = "UPDATE product_credentials SET is_sold = 1, order_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer id : ids) {
                statement.setInt(1, orderId);
                statement.setInt(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public List<String> findPlainCredentialsByOrder(int orderId) {
        final String sql = "SELECT encrypted_value FROM product_credentials WHERE order_id = ? ORDER BY id ASC";
        List<String> results = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("encrypted_value"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải credentials của đơn hàng", ex);
        }
        return results;
    }

    /**
     * Kiểm tra người mua đã từng mở khóa thông tin bàn giao hay chưa.
     * <p>
     * Admin sử dụng log này để truy vết lượt xem credential; tầng dịch vụ dựa
     * vào đây để quyết định có tải plaintext cho người dùng hay yêu cầu xác
     * nhận lại.</p>
     */
    public boolean hasViewLog(int orderId, int buyerId) {
        final String sql = "SELECT 1 FROM credential_view_logs WHERE order_id = ? AND buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, buyerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể kiểm tra log xem credential", ex);
            return false;
        }
    }

    /**
     * Ghi nhận hành động mở khóa credential của người mua.
     * <p>
     * Việc lưu trữ được thực hiện trước khi trả plaintext về cho client nhằm
     * bảo vệ dữ liệu: nếu thao tác insert thất bại hệ thống sẽ ném ngoại lệ để
     * controller có thể báo lỗi và không hiển thị thông tin nhạy cảm.</p>
     */
    public void logCredentialView(int orderId, int productId, int buyerId, String variantCode, String viewerIp) {
        final String sql = "INSERT INTO credential_view_logs (order_id, product_id, buyer_id, variant_code, viewer_ip) "
                + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE viewer_ip = VALUES(viewer_ip), viewed_at = viewed_at";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setInt(3, buyerId);
            String normalized = normalizeVariantCode(variantCode);
            if (normalized == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, normalized);
            }
            if (viewerIp == null || viewerIp.isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, viewerIp.trim());
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể ghi log mở khóa thông tin bàn giao", ex);
        }
    }

    /**
     * Truy vấn toàn bộ lịch sử mở khóa credential của một đơn hàng để phục vụ
     * giao diện quản trị.
     * <p>
     * Phương thức trả về danh sách bản ghi giàu thông tin (order, buyer, thời
     * điểm, IP) để admin có thể rà soát khi phát sinh tranh chấp về việc đã xem
     * dữ liệu hay chưa.</p>
     */
    public List<CredentialViewLogEntry> findViewLogsByOrder(int orderId) {
        final String sql = "SELECT order_id, product_id, buyer_id, variant_code, viewer_ip, viewed_at "
                + "FROM credential_view_logs WHERE order_id = ? ORDER BY viewed_at ASC";
        List<CredentialViewLogEntry> results = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new CredentialViewLogEntry(
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getInt("buyer_id"),
                            rs.getString("variant_code"),
                            rs.getString("viewer_ip"),
                            rs.getTimestamp("viewed_at")));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải log mở khóa credential", ex);
        }
        return results;
    }

    public record CredentialAvailability(int total, int available) {

    }

    public record CredentialViewLogEntry(int orderId, int productId, int buyerId, String variantCode,
            String viewerIp, Timestamp viewedAt) {

    }

    /**
     * Sinh thêm credential ảo cho sản phẩm/biến thể để bảo đảm mỗi đơn vị hàng
     * hóa đều có dữ liệu bàn giao riêng biệt.
     */
    public void generateFakeCredentials(Connection connection, int productId, String variantCode, int quantity)
            throws SQLException {
        if (quantity <= 0) {
            return;
        }
        String normalized = normalizeVariantCode(variantCode);
        final String sql = "INSERT INTO product_credentials (product_id, encrypted_value, variant_code, is_sold) "
                + "VALUES (?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < quantity; i++) {
                statement.setInt(1, productId);
                statement.setString(2, buildFakeCredentialPayload(productId, normalized));
                if (normalized == null) {
                    statement.setNull(3, Types.VARCHAR);
                } else {
                    statement.setString(3, normalized);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
        LOGGER.log(Level.INFO,
                "Sinh thêm {0} credential ảo cho sản phẩm {1}{2}",
                new Object[]{quantity, productId, normalized == null ? "" : " - biến thể " + normalized});
    }

    private String normalizeVariantCode(String variantCode) {
        if (variantCode == null) {
            return null;
        }
        String trimmed = variantCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private void restoreAutoCommit(Connection connection, boolean previousAutoCommit) {
        try {
            if (connection.getAutoCommit() != previousAutoCommit) {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể khôi phục chế độ auto-commit", ex);
        }
    }

    private String buildFakeCredentialPayload(int productId, String normalizedVariantCode) {
        String variantSlug = normalizedVariantCode == null ? "prod" + productId
                : normalizedVariantCode.replaceAll("[^a-z0-9]", "");
        if (variantSlug.isEmpty()) {
            variantSlug = "prod" + productId;
        }
        String usernameSuffix = randomAlphaNumeric(6).toLowerCase(Locale.ROOT);
        String username = variantSlug + "_" + usernameSuffix;
        String password = randomAlphaNumeric(8) + randomPasswordSymbol() + randomAlphaNumeric(6);
        return String.format(Locale.ROOT, "{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
    }

    private String randomAlphaNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }
        return builder.toString();
    }

    private char randomPasswordSymbol() {
        return PASSWORD_SYMBOLS[RANDOM.nextInt(PASSWORD_SYMBOLS.length)];
    }
}
