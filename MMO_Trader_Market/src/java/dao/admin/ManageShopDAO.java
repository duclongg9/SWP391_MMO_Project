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

    // Lấy danh sách tất cả cửa hàng kèm tên chủ và admin_note
    public List<Shops> getAllShops() throws SQLException {
        List<Shops> list = new ArrayList<>();

        String sql = """
            SELECT s.id,
                   s.owner_id,
                   s.name,
                   s.status,
                   s.description,
                   s.admin_note,
                   s.created_at,
                   u.name AS owner_name
            FROM shops s
            LEFT JOIN users u ON s.owner_id = u.id
            ORDER BY s.created_at DESC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Shops s = new Shops();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("owner_id"));
                s.setName(rs.getString("name"));
                s.setStatus(rs.getString("status"));
                s.setDescription(rs.getString("description"));
                s.setAdminNote(rs.getString("admin_note"));

                Timestamp cAt = rs.getTimestamp("created_at");
                s.setCreatedAt(cAt != null ? new java.util.Date(cAt.getTime()) : null);

                s.setOwnerName(rs.getString("owner_name"));
                list.add(s);
            }
        }
        return list;
    }

    // Lấy 1 shop theo id (dùng cho handleShopStatus)
    public Shops findById(int id) throws SQLException {
        String sql = """
        SELECT s.id, s.owner_id, s.name, s.status, s.description,
               s.admin_note, s.created_at,
               u.name AS owner_name
        FROM shops s
        LEFT JOIN users u ON s.owner_id = u.id
        WHERE s.id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shops s = new Shops();
                    s.setId(rs.getInt("id"));
                    s.setOwnerId(rs.getInt("owner_id"));
                    s.setName(rs.getString("name"));
                    s.setStatus(rs.getString("status"));
                    s.setDescription(rs.getString("description"));
                    s.setAdminNote(rs.getString("admin_note"));
                    Timestamp cAt = rs.getTimestamp("created_at");
                    s.setCreatedAt(cAt != null ? new java.util.Date(cAt.getTime()) : null);
                    s.setOwnerName(rs.getString("owner_name"));
                    return s;
                }
            }
        }
        return null;
    }

    public boolean updateStatusAndNote(int id, String status, String note) throws SQLException {
        String sql = "UPDATE shops SET status = ?, admin_note = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }


    // (Tuỳ: giữ lại nếu nơi khác còn dùng)
    public boolean updateStatus(int id, String newStatus) throws SQLException {
        String sql = "UPDATE shops SET status=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
