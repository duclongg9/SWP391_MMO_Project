package dao.user;

import dao.BaseDAO;
import model.Users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
        try (Connection conn = getConnection();
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
        try (Connection con = getConnection();
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
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /**
     lưu tài khoản mới
     */
    public int createBuyerAccount(String roleName, String email, String hashedPassword, Timestamp createdAt) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, hashed_password, status, created_at, updated_at)
                SELECT id, ?, ?, 1, ?, ?
                FROM roles
                WHERE name = ?
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.setTimestamp(3, createdAt);
            ps.setTimestamp(4, createdAt);
            ps.setString(5, roleName);
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
        try (Connection conn = getConnection();
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

    /** Tìm user theo google_id */
    public Users getUserByGoogleId(String googleId) {
        final String sql = """
                SELECT * FROM users
                WHERE google_id = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo google id", e);
        }
        return null;
    }
        /** Kiểm tra email đã tồn tại hay chưa */
    public boolean emailExists(String email) throws SQLException {
        final String sql = """
                SELECT 1 FROM users
                WHERE email = ?
                LIMIT 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Tạo user mới */
    public Users createUser(String email, String name, String hashedPassword, int roleId) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, name, hashed_password, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roleId);
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, hashedPassword);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    Users created = new Users();
                    created.setId(id);
                    created.setRoleId(roleId);
                    created.setEmail(email);
                    created.setName(name);
                    created.setHashedPassword(hashedPassword);
                    created.setStatus(true);
                    return created;
                }
            }
            return null;
        }
    }

    /**
     * Tạo user mới đăng nhập bằng Google.
     */
    public Users createUserWithGoogle(String email, String name, String googleId,
            String hashedPassword, int roleId) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, name, hashed_password, google_id, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roleId);
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, hashedPassword);
            ps.setString(5, googleId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return null;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Users created = new Users();
                    created.setId(rs.getInt(1));
                    created.setRoleId(roleId);
                    created.setEmail(email);
                    created.setName(name);
                    created.setGoogleId(googleId);
                    created.setHashedPassword(hashedPassword);
                    created.setStatus(true);
                    return created;
                }
            }
            return null;
        }
    }

    /**
     * Liên kết tài khoản Google với user hiện tại.
     */
    public int updateGoogleId(int userId, String googleId) throws SQLException {
        final String sql = """
                UPDATE users
                SET google_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }
}
