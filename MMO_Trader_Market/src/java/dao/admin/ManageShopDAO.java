package dao.admin;

import model.Shops;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageShopDAO {

    private final Connection con;

    public ManageShopDAO(Connection con) {
        this.con = con;
    }

    /**
     * Lấy danh sách tất cả cửa hàng kèm tên chủ sở hữu
     */
    public List<Shops> getAllShops() throws SQLException {
        List<Shops> list = new ArrayList<>();

        String sql = """
            SELECT s.id, s.owner_id, s.name, s.status, s.description, s.created_at,
                   u.name AS owner_name
            FROM shops s
            LEFT JOIN users u ON s.owner_id = u.id
        
            ORDER BY s.created_at DESC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Shops s = new Shops();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("owner_id"));
                s.setName(rs.getString("name"));
                s.setStatus(rs.getString("status"));
                s.setDescription(rs.getString("description"));
                Timestamp cAt = rs.getTimestamp("created_at");
                s.setCreatedAt(cAt != null ? new java.util.Date(cAt.getTime()) : null);
                s.setOwnerName(rs.getString("owner_name"));
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái Ban / Unban
     */
    public boolean updateStatus(int id, String newStatus) throws SQLException {
        String sql = "UPDATE shops SET status=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
