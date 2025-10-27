package dao.user;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.RememberMeToken;

import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;

/**
 * DAO quản lý bảng {@code remember_me_tokens} phục vụ tính năng đăng nhập ghi
 * nhớ.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class RememberMeTokenDAO extends BaseDAO {

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_SELECTOR = "selector";
    private static final String COL_HASHED_VALIDATOR = "hashed_validator";
    private static final String COL_EXPIRES_AT = "expires_at";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_LAST_USED_AT = "last_used_at";

    private static final Logger LOGGER = Logger.getLogger(RememberMeTokenDAO.class.getName());

    /**
     * Tạo mã ghi nhớ đăng nhập mới cho người dùng.
     *
     * @param userId mã người dùng sở hữu token
     * @param selector mã định danh công khai lưu trên cookie
     * @param hashedValidator giá trị validator đã được băm
     * @param expiresAt thời điểm hết hạn
     * @return token vừa tạo hoặc {@code null} nếu thất bại
     * @throws SQLException khi thao tác chèn lỗi
     */
    public RememberMeToken createToken(int userId, String selector, String hashedValidator, Timestamp expiresAt)
            throws SQLException {
        final String sql = """
                INSERT INTO remember_me_tokens (user_id, selector, hashed_validator, expires_at, last_used_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, selector);
            ps.setString(3, hashedValidator);
            ps.setTimestamp(4, expiresAt);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    RememberMeToken token = new RememberMeToken();
                    token.setId(id);
                    token.setUserId(userId);
                    token.setSelector(selector);
                    token.setHashedValidator(hashedValidator);
                    token.setExpiresAt(expiresAt);
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    token.setCreatedAt(now);
                    token.setLastUsedAt(new Timestamp(System.currentTimeMillis()));
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * Tìm token dựa trên trường selector được gửi từ cookie.
     *
     * @param selector giá trị selector cần tìm
     * @return token tương ứng hoặc {@code null}
     */
    public RememberMeToken findBySelector(String selector) {
        final String sql = """
                SELECT * FROM remember_me_tokens
                WHERE selector = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selector);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logSqlError("findBySelector", e);
        }
        return null;
    }

    /**
     * Cập nhật validator và hạn dùng của token hiện tại.
     *
     * @param id mã token
     * @param hashedValidator validator mới đã băm
     * @param expiresAt thời điểm hết hạn mới
     * @throws SQLException khi truy vấn lỗi
     */
    public void updateValidator(int id, String hashedValidator, Timestamp expiresAt) throws SQLException {
        final String sql = """
                UPDATE remember_me_tokens
                SET hashed_validator = ?, expires_at = ?, last_used_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedValidator);
            ps.setTimestamp(2, expiresAt);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa token theo mã định danh chính.
     *
     * @param id mã token
     * @throws SQLException khi thao tác xóa lỗi
     */
    public void deleteById(int id) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE id = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa token dựa trên selector (ví dụ khi người dùng đăng xuất từ cookie).
     *
     * @param selector giá trị selector cần xóa
     * @throws SQLException khi truy vấn lỗi
     */
    public void deleteBySelector(String selector) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE selector = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selector);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa toàn bộ token ghi nhớ của một người dùng.
     *
     * @param userId mã người dùng
     * @throws SQLException khi câu lệnh SQL lỗi
     */
    public void deleteAllForUser(int userId) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Ánh xạ dữ liệu ResultSet thành đối tượng {@link RememberMeToken}.
     *
     * @param rs hàng dữ liệu đang đọc
     * @return token tương ứng
     * @throws SQLException khi đọc dữ liệu lỗi
     */
    private RememberMeToken mapRow(ResultSet rs) throws SQLException {
        RememberMeToken token = new RememberMeToken();
        token.setId(rs.getInt(COL_ID));
        token.setUserId(rs.getInt(COL_USER_ID));
        token.setSelector(rs.getString(COL_SELECTOR));
        token.setHashedValidator(rs.getString(COL_HASHED_VALIDATOR));
        token.setExpiresAt(rs.getTimestamp(COL_EXPIRES_AT));
        token.setCreatedAt(rs.getTimestamp(COL_CREATED_AT));
        token.setLastUsedAt(rs.getTimestamp(COL_LAST_USED_AT));
        return token;
    }

    /**
     * Ghi log chi tiết khi xảy ra lỗi truy vấn để dễ dàng truy vết.
     *
     * @param operation tên thao tác đang thực hiện
     * @param e ngoại lệ SQL bắt được
     */
    private void logSqlError(String operation, SQLException e) {
        LOGGER.log(java.util.logging.Level.SEVERE,
                String.format("[RememberMeTokenDAO] SQL error during %s: %s (SQLState=%s, ErrorCode=%d)",
                        operation, e.getMessage(), e.getSQLState(), e.getErrorCode()),
                e);

        for (SQLException next = e.getNextException(); next != null; next = next.getNextException()) {
            LOGGER.log(java.util.logging.Level.SEVERE,
                    String.format("[RememberMeTokenDAO] NextException during %s: %s (SQLState=%s, ErrorCode=%d)",
                            operation, next.getMessage(), next.getSQLState(), next.getErrorCode()),
                    next);
        }
    }
}
