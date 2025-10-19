package dao.user;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.Users;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO extends BaseDAO {

    // Tên cột trong bảng users
    private static final String COL_ID             = "id";
    private static final String COL_ROLE_ID        = "role_id";
    private static final String COL_EMAIL          = "email";
    private static final String COL_NAME           = "name";
    private static final String COL_AVATAR_URL     = "avatar_url";
    private static final String COL_HASHED_PWD     = "hashed_password";
    private static final String COL_GOOGLE_ID      = "google_id";
    private static final String COL_STATUS         = "status";
    private static final String COL_CREATED_AT     = "created_at";
    private static final String COL_UPDATED_AT     = "updated_at";

    /** Helper: map 1 hàng ResultSet -> Users */
    private Users mapRow(ResultSet rs) throws SQLException {
        Users u = new Users();
        u.setId(rs.getInt(COL_ID));
        u.setRoleId(rs.getInt(COL_ROLE_ID));
        u.setEmail(rs.getString(COL_EMAIL));
        u.setName(rs.getString(COL_NAME));
        u.setAvatarUrl(rs.getString(COL_AVATAR_URL));
        u.setHashedPassword(rs.getString(COL_HASHED_PWD));
        u.setGoogleId(rs.getString(COL_GOOGLE_ID));
        // TINYINT(1) -> boolean
        u.setStatus(rs.getObject(COL_STATUS) == null ? null : rs.getBoolean(COL_STATUS));
        // Timestamp là subclass của java.util.Date, gán trực tiếp ok
        u.setCreatedAt(rs.getTimestamp(COL_CREATED_AT));
        u.setUpdatedAt(rs.getTimestamp(COL_UPDATED_AT));
        return u;
    }

    /** Lấy user theo id (chỉ lấy user đang active) */
    public Users getUserByUserId(int id) {
        final String sql = """
                SELECT * FROM users
                WHERE id = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo id", e);
        }
        return null;
    }

    /** Cập nhật tên hiển thị */
    public int updateUserProfileBasic(int id, String name) throws SQLException {
        final String sql = """
                UPDATE users
                SET name = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /** Cập nhật mật khẩu đã hash */
    public int updateUserPassword(int id, String hashedPassword) throws SQLException {
        final String sql = """
                UPDATE users
                SET hashed_password = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /** (Tuỳ chọn) Lấy user theo email */
    public Users getUserByEmail(String email) {
        final String sql = """
                SELECT * FROM users
                WHERE email = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo email", e);
        }
        return null;
    }
}
