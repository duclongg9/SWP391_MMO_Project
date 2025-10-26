package dao.seller;

import dao.BaseDAO;
import model.Shops;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cho seller quản lý thông tin shop của mình.
 * 
 * @version 1.0
 * @author AI Assistant
 */
public class SellerShopDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SellerShopDAO.class.getName());

    /**
     * Lấy thông tin shop theo owner ID (seller ID).
     *
     * @param ownerId ID của seller
     * @return Optional chứa shop nếu tìm thấy
     */
    public Optional<Shops> findByOwnerId(int ownerId) {
        String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE owner_id = ? LIMIT 1";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải thông tin shop", ex);
        }
        return Optional.empty();
    }

    /**
     * Lấy thông tin shop theo ID và owner ID (đảm bảo seller chỉ truy cập shop của mình).
     *
     * @param shopId  ID của shop
     * @param ownerId ID của seller
     * @return Optional chứa shop nếu tìm thấy
     */
    public Optional<Shops> findByIdAndOwnerId(int shopId, int ownerId) {
        String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE id = ? AND owner_id = ? LIMIT 1";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            statement.setInt(2, ownerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải thông tin shop", ex);
        }
        return Optional.empty();
    }

    /**
     * Tạo shop mới cho seller.
     *
     * @param shop đối tượng shop cần tạo
     * @return ID của shop vừa tạo, hoặc -1 nếu thất bại
     */
    public int create(Shops shop) {
        String sql = "INSERT INTO shops (owner_id, name, description, status, created_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, shop.getOwnerId());
            statement.setString(2, shop.getName());
            statement.setString(3, shop.getDescription());
            statement.setString(4, shop.getStatus() != null ? shop.getStatus() : "Pending");
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tạo shop mới", ex);
        }
        return -1;
    }

    /**
     * Cập nhật thông tin shop.
     *
     * @param shop shop cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean update(Shops shop) {
        String sql = "UPDATE shops SET name = ?, description = ? "
                + "WHERE id = ? AND owner_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, shop.getName());
            statement.setString(2, shop.getDescription());
            statement.setInt(3, shop.getId());
            statement.setInt(4, shop.getOwnerId());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật shop", ex);
            return false;
        }
    }

    /**
     * Kiểm tra xem seller đã có shop chưa.
     *
     * @param ownerId ID của seller
     * @return true nếu đã có shop
     */
    public boolean hasShop(int ownerId) {
        String sql = "SELECT COUNT(*) FROM shops WHERE owner_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể kiểm tra shop", ex);
        }
        return false;
    }

    /**
     * Kiểm tra xem shop có đang hoạt động không.
     *
     * @param shopId ID của shop
     * @return true nếu shop đang active
     */
    public boolean isActive(int shopId) {
        String sql = "SELECT status FROM shops WHERE id = ? LIMIT 1";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return "Active".equals(rs.getString("status"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể kiểm tra trạng thái shop", ex);
        }
        return false;
    }

    /**
     * Ánh xạ ResultSet sang đối tượng Shops.
     */
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
