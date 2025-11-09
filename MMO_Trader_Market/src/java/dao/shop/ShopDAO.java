package dao.shop;

import dao.BaseDAO;
import dao.connect.DBConnect;
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

/**
 * DAO cung cấp dữ liệu liên quan tới bảng {@code shops} cho trang chủ và thống
 * kê.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class ShopDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ShopDAO.class.getName());

    /**
     * Lấy danh sách shop đang hoạt động gần nhất.
     *
     * @param limit số lượng shop cần trả về
     * @return danh sách shop hoạt động
     */
    public List<Shops> findActive(int limit) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at "
                + "FROM shops WHERE status = 'Active' ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
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

    /**
     * Đếm tổng số shop đang ở trạng thái Active.
     *
     * @return số lượng shop hoạt động
     */
    public long countActive() {
        final String sql = "SELECT COUNT(*) FROM shops WHERE status = 'Active'";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số lượng shop đang hoạt động", ex);
        }
        return 0;
    }

    /**
     * Tìm shop theo owner_id.
     *
     * @param ownerId mã người dùng sở hữu shop
     * @return shop nếu tìm thấy, null nếu không tìm thấy
     */
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
            LOGGER.log(Level.SEVERE, "Không thể tải shop theo owner_id", ex);
        }
        return null;
    }

    /**
     * Ánh xạ dữ liệu kết quả sang đối tượng {@link Shops}.
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

    //Lấy tổng số shop đang hoạt động
    public int getTotalActiveShops() {
        String sql = """
        SELECT COUNT(*) AS total_active_shops
        FROM mmo_schema.shops
        WHERE status = 'Active'
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_active_shops");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Nếu có lỗi hoặc không có kết quả
    }
    
    public int getTotalPendingShops() {
        String sql = """
        SELECT COUNT(*) AS total_active_shops
        FROM mmo_schema.shops
        WHERE status = 'Pending'
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_active_shops");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Nếu có lỗi hoặc không có kết quả
    }
    
    public int getTotalSuspendedShops() {
        String sql = """
        SELECT COUNT(*) AS total_active_shops
        FROM mmo_schema.shops
        WHERE status = 'Suspended'
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_active_shops");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Nếu có lỗi hoặc không có kết quả
    }
}
