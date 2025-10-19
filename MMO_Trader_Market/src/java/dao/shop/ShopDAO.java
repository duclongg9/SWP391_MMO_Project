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

