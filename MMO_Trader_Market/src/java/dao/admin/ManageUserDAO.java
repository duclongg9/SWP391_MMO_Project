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

    public int createUser(String name, String email, String rawPassword, Integer roleId, Integer status01) throws SQLException {
        if (name == null || name.isBlank() || email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new SQLException("Thiếu dữ liệu bắt buộc");
        }

        // Hash password (nếu bạn có BCrypt, bỏ comment 2 dòng dưới và thay passHash).
        // String passHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        String passHash = rawPassword; // TODO: thay bằng hash thật trong môi trường production

        if (roleId == null) {
            roleId = 3;   // mặc định BUYER
        }
        if (status01 == null) {
            status01 = 1; // mặc định active
        }
        String sql = """
            INSERT INTO users (name, email, password_hash, role_id, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, passHash);
            ps.setInt(4, roleId);
            ps.setInt(5, status01);
            return ps.executeUpdate();
        }
    }

    /**
     * 🔹 Lấy toàn bộ user
     */
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

        try (Connection c = (this.con != null ? this.con : DBConnect.getConnection()); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 🔹 Hàm tìm kiếm có lọc
     */
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
                if (p instanceof Timestamp ts) {
                    ps.setTimestamp(i + 1, ts);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    /**
     * 🔹 Xóa user theo ID
     */
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

    // Chỉ đổi trạng thái nếu user thuộc role BUYER hoặc SELLER
    public int updateStatus(int userId, int status01) throws SQLException {
        String sql = """
        UPDATE users u
        JOIN roles r ON u.role_id = r.id
        SET u.status = ?, u.updated_at = CURRENT_TIMESTAMP
        WHERE u.id = ? AND UPPER(r.name) IN ('BUYER','SELLER')
    """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, status01);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    /**
     * 🔹 Map từ ResultSet sang model
     */
    private Users mapRow(ResultSet rs) throws SQLException {
        Users u = new Users();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        Object statusObj = rs.getObject("status");
        Boolean active = null;
        if (statusObj != null) {
            int val = (statusObj instanceof Boolean) ? ((Boolean) statusObj ? 1 : 0) : ((Number) statusObj).intValue();
            active = val == 1;
        }
        u.setStatus(1);
        u.setRoleName(rs.getString("role_name"));
        Timestamp cAt = rs.getTimestamp("created_at");
        Timestamp uAt = rs.getTimestamp("updated_at");
        u.setCreatedAt(cAt != null ? new java.util.Date(cAt.getTime()) : null);
        u.setUpdatedAt(uAt != null ? new java.util.Date(uAt.getTime()) : null);
        return u;
    }
}
