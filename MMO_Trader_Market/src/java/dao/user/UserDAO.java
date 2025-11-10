package dao.user;

import dao.BaseDAO;
import dao.connect.DBConnect;
import java.math.BigDecimal;
import model.Users;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO thao tác với bảng {@code users}, phục vụ các nghiệp vụ xác thực và hồ sơ
 * người dùng.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class UserDAO extends BaseDAO {

    // Tên cột trong bảng users
    private static final String COL_ID = "id";
    private static final String COL_ROLE_ID = "role_id";
    private static final String COL_EMAIL = "email";
    private static final String COL_NAME = "name";
    private static final String COL_AVATAR_URL = "avatar_url";
    private static final String COL_HASHED_PWD = "hashed_password";
    private static final String COL_GOOGLE_ID = "google_id";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";

    /**
     * Chuyển đổi một hàng dữ liệu sang đối tượng {@link Users}.
     *
     * @param rs hàng dữ liệu cần ánh xạ
     * @return thực thể người dùng tương ứng
     * @throws SQLException nếu đọc dữ liệu gặp lỗi
     */
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
        u.setStatus(rs.getObject(COL_STATUS) == null ? null : rs.getInt(COL_STATUS));
        // Timestamp là subclass của java.util.Date, gán trực tiếp ok
        u.setCreatedAt(rs.getTimestamp(COL_CREATED_AT));
        u.setUpdatedAt(rs.getTimestamp(COL_UPDATED_AT));
        return u;
    }

    /**
     * Lấy người dùng hoạt động theo mã định danh.
     *
     * @param id mã người dùng cần truy vấn
     * @return người dùng nếu tồn tại, hoặc {@code null} khi không tìm thấy
     */
    public Users getUserByUserId(int id) {
        final String sql = """
                SELECT * FROM users
                WHERE id = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo id", e);
        }
        return null;
    }

    /**
     * Cập nhật tên hiển thị cơ bản của người dùng.
     *
     * @param id mã người dùng cần cập nhật
     * @param name tên mới
     * @return số hàng bị ảnh hưởng
     * @throws SQLException khi câu lệnh SQL lỗi
     */
    public int updateUserProfileBasic(int id, String name, String avata) throws SQLException {
        final String sql = """
                UPDATE users
                SET name = ?, avatar_url = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, avata);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    /**
     * Cập nhật mật khẩu đã băm cho người dùng.
     *
     * @param id mã người dùng
     * @param hashedPassword mật khẩu đã được băm
     * @return số hàng bị ảnh hưởng
     * @throws SQLException khi thao tác thất bại
     */
    public int updateUserPassword(int id, String hashedPassword) throws SQLException {
        final String sql = """
                UPDATE users
                SET hashed_password = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /**
     * Tạo tài khoản mới dựa trên vai trò chỉ định.
     *
     * @param roleName tên vai trò (ví dụ {@code BUYER})
     * @param email email đăng ký
     * @param hashedPassword mật khẩu đã băm
     * @param createdAt thời điểm tạo tài khoản
     * @return số hàng được chèn
     * @throws SQLException khi thao tác thất bại
     */
    public int createBuyerAccount(String roleName, String email, String hashedPassword, Timestamp createdAt) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, hashed_password, status, created_at, updated_at)
                SELECT id, ?, ?, 1, ?, ?
                FROM roles
                WHERE name = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.setTimestamp(3, createdAt);
            ps.setTimestamp(4, createdAt);
            ps.setString(5, roleName);
            return ps.executeUpdate();
        }
    }

    /**
     * lấy người dùng theo email
     */
    public Users getUserByEmail(String email) {
        final String sql = """
                SELECT * FROM users
                WHERE email = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo email", e);
        }
        return null;
    }
// tìm user theo email bất kể stt là gì

    public Users getUserByEmailAnyStatus(String email) {
        final String sql = """
                SELECT * FROM users
                WHERE email = ?
                LIMIT 1 
                """;//chỉ lấy 1 dòng đầu tiên
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email); //ind tham số thứ nhất bằng biến email
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // có bản ghi trả về user
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo email bất kể trạng thái", e);
        }
        return null;
    }

    /**
     * Tìm người dùng đã liên kết với Google theo mã định danh.
     *
     * @param googleId mã Google cần truy vấn
     * @return người dùng nếu tồn tại, hoặc {@code null}
     */
    public Users getUserByGoogleId(String googleId) {
        final String sql = """
                SELECT * FROM users
                WHERE google_id = ? AND status = 1
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName())
                    .log(Level.SEVERE, "Lỗi lấy user theo google id", e);
        }
        return null;
    }

    /**
     * Kiểm tra email đã tồn tại trong hệ thống.
     *
     * @param email email cần kiểm tra
     * @return {@code true} nếu đã tồn tại
     * @throws SQLException khi truy vấn lỗi
     */
    public boolean emailExists(String email) throws SQLException {
        final String sql = """
                SELECT 1 FROM users
                WHERE email = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tạo người dùng nội bộ với mật khẩu được cung cấp.
     *
     * @param email email đăng ký
     * @param name tên hiển thị
     * @param hashedPassword mật khẩu đã băm
     * @param roleId mã vai trò
     * @param status goi ve trang thai nguoi dung.
     * @return người dùng vừa tạo hoặc {@code null} nếu thất bại
     * @throws SQLException khi thao tác chèn lỗi
     */
    public Users createUser(String email, String name, String hashedPassword, int roleId,
            int status) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, name, hashed_password, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roleId);
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, hashedPassword);
            ps.setInt(5, status);
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
                    created.setStatus(2);
                    return created;
                }
            }
            return null;
        }
    }

    /**
     * Tạo tài khoản mới cho hình thức đăng nhập Google.
     *
     * @param email email Google trả về
     * @param name tên hiển thị
     * @param googleId mã liên kết Google
     * @param hashedPassword mật khẩu dự phòng đã băm
     * @param roleId vai trò hệ thống
     * @return người dùng vừa tạo hoặc {@code null}
     * @throws SQLException khi thao tác chèn lỗi
     */
    public Users createUserWithGoogle(String email, String name, String googleId,
            String hashedPassword, int roleId) throws SQLException {
        final String sql = """
                INSERT INTO users (role_id, email, name, hashed_password, google_id, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                    created.setStatus(1);
                    return created;
                }
            }
            return null;
        }
    }

    public int activateUser(int userId) throws SQLException {
        final String sql = """
                UPDATE users
                SET status = 1, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    /**
     * Gắn hoặc thay đổi mã Google cho người dùng hiện hữu.
     *
     * @param userId mã người dùng
     * @param googleId mã Google cần liên kết
     * @return số hàng được cập nhật
     * @throws SQLException khi truy vấn lỗi
     */
    public int updateGoogleId(int userId, String googleId) throws SQLException {
        final String sql = """
                UPDATE users
                SET google_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    public int getTotalUserByMonth(int month, int year) throws SQLException {
        String sql = """
        SELECT 
                COUNT(*) AS total_user
            FROM mmo_schema.users
            WHERE status = 1
              AND YEAR(created_at) = ?
              AND MONTH(created_at) = ?;
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_order");
            }
        }
        return 0;
    }

    public int getTotalActive() throws SQLException {
        String sql = """
                        SELECT 
                       COUNT(*) AS total_user
                       FROM mmo_schema.users
                       WHERE status = 1;
                        """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_user");
            }
        }
        return 0;
    }
}
