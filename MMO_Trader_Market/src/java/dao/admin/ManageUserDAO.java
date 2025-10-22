package dao.admin;

import dao.connect.DBConnect;
import model.Users;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUserDAO {

    private Connection con;

    public ManageUserDAO(Connection con) {
        this.con = con;
    }

    public ManageUserDAO() {
    }

    /** 🔹 Lấy toàn bộ user */
    public List<Users> getAllUsers() {
        List<Users> list = new ArrayList<>();
        String sql = """
                SELECT 
                    u.id, u.name, u.email, u.status, u.created_at, u.updated_at,
                    r.name AS role_name
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                ORDER BY u.created_at DESC
        """;

        try (Connection c = (this.con != null ? this.con : DBConnect.getConnection());
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 🔹 Hàm tìm kiếm có lọc */
    public List<Users> searchUsers(String keyword, String role, Timestamp fromAt, Timestamp toAt) throws SQLException {
        List<Users> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder("""
            SELECT 
                u.id, u.name, u.email, u.status, u.created_at, u.updated_at,
                r.name AS role_name
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        // 🔸 Tìm theo keyword (name/email LIKE %keyword%)
        if (keyword != null && !keyword.isEmpty()) {
            sb.append(" AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String like = "%" + keyword.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }

        // 🔸 Lọc theo role
        if (role != null && !role.isEmpty()) {
            sb.append(" AND UPPER(r.name) = UPPER(?) ");
            params.add(role);
        }

        // 🔸 Lọc theo ngày tạo
        if (fromAt != null && toAt != null) {
            sb.append(" AND u.created_at BETWEEN ? AND ? ");
            params.add(fromAt);
            params.add(toAt);
        } else if (fromAt != null) {
            sb.append(" AND u.created_at >= ? ");
            params.add(fromAt);
        } else if (toAt != null) {
            sb.append(" AND u.created_at <= ? ");
            params.add(toAt);
        }

        sb.append(" ORDER BY u.created_at DESC ");

        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Timestamp ts)
                    ps.setTimestamp(i + 1, ts);
                else
                    ps.setString(i + 1, p.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        return list;
    }

    /** 🔹 Xóa user theo ID */
    public int deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("DELETE users id=" + id + " -> rows=" + rows);
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** 🔹 Map từ ResultSet sang model */
    private Users mapRow(ResultSet rs) throws SQLException {
        Users u = new Users();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        Object statusObj = rs.getObject("status");
        Boolean active = null;
        if(statusObj != null) {
            int val = (statusObj instanceof Boolean) ? ((Boolean) statusObj ? 1 : 0)  : ((Number) statusObj).intValue();
            active = val == 1;
        }
        u.setStatus(active);
        u.setRoleName(rs.getString("role_name"));
        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        u.setCreatedAt(cAt != null ? new java.util.Date(cAt.getTime()) : null);
        u.setUpdatedAt(uAt != null ? new java.util.Date(uAt.getTime()) : null);
        return u;
    }
}
