package dao.shop;

import dao.BaseDAO;
import model.Shops;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ShopDAO.class.getName());

    public List<Shops> findActive(int limit) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE status = 'Active' ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            List<Shops> shops = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    shops.add(mapRow(rs));
                }
            }
            return shops;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách shop đang hoạt động", ex);
            return List.of();
        }
    }

    public long countActive() {
        final String sql = "SELECT COUNT(*) FROM shops WHERE status = 'Active'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số lượng shop đang hoạt động", ex);
        }
        return 0;
    }

    public Shops findByOwnerId(int ownerId) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE owner_id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm shop theo owner ID", ex);
        }
        return null;
    }

    public Shops findById(int shopId) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm shop theo ID", ex);
        }
        return null;
    }

    public Shops createShop(int ownerId, String name, String description) throws SQLException {
        final String sql = "INSERT INTO shops (owner_id, name, description, status, created_at) "
                + "VALUES (?, ?, ?, 'Pending', NOW())";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, 
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, ownerId);
            statement.setString(2, name);
            statement.setString(3, description);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo shop thất bại, không có dòng nào được thêm");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Shops shop = new Shops();
                    shop.setId(generatedKeys.getInt(1));
                    shop.setOwnerId(ownerId);
                    shop.setName(name);
                    shop.setDescription(description);
                    shop.setStatus("Pending");
                    shop.setCreatedAt(new java.util.Date());
                    return shop;
                }
                throw new SQLException("Tạo shop thất bại, không lấy được ID");
            }
        }
    }

    public boolean updateShopStatus(int shopId, String status) {
        final String sql = "UPDATE shops SET status = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, shopId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái shop", ex);
            return false;
        }
    }

    public boolean updateShop(int shopId, String name, String description) {
        final String sql = "UPDATE shops SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, shopId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật thông tin shop", ex);
            return false;
        }
    }

    private Shops mapRow(ResultSet rs) throws SQLException {
        Shops shop = new Shops();
        shop.setId(rs.getInt("id"));
        shop.setOwnerId(rs.getInt("owner_id"));
        shop.setName(rs.getString("name"));
        shop.setDescription(rs.getString("description"));
        shop.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            shop.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        return shop;
    }
}

