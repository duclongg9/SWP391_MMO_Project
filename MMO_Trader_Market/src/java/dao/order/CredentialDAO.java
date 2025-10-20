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
        try (Connection connection = getConnection()) {
            return pickFreeCredentialIds(connection, productId, qty);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể khóa credentials", ex);
            return List.of();
        }
    }

    public List<Integer> pickFreeCredentialIds(Connection connection, int productId, int qty) throws SQLException {
        final String sql = "SELECT id FROM product_credentials WHERE product_id = ? AND is_sold = 0 "
                + "ORDER BY id ASC LIMIT ? FOR UPDATE";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, qty);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }
        return ids;
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
}
