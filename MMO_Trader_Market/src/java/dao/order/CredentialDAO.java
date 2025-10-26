package dao.order;

import dao.BaseDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for product_credentials table.
 */
public class CredentialDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(CredentialDAO.class.getName());

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
        final String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        final String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ? AND LOWER(TRIM(variant_code)) = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, normalized);
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
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê credential khả dụng", ex);
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
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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

    public record CredentialAvailability(int total, int available) {
    }

    private String normalizeVariantCode(String variantCode) {
        if (variantCode == null) {
            return null;
        }
        String trimmed = variantCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
