package dao.user;

import dao.BaseDAO;
import model.RememberMeToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO thao tác với bảng remember_me_tokens để quản lý token "ghi nhớ đăng nhập".
 */
public class RememberMeTokenDAO extends BaseDAO {

    /**
     * Tạo token mới trong database.
     *
     * @param userId ID của người dùng
     * @param selector Selector token (được lưu trong cookie)
     * @param hashedValidator Validator đã được hash (SHA-256)
     * @param expiresAt Thời điểm hết hạn
     * @return RememberMeToken đã được tạo (có ID)
     * @throws SQLException nếu có lỗi khi insert
     */
    public RememberMeToken createToken(int userId, String selector, String hashedValidator, Timestamp expiresAt) throws SQLException {
        final String sql = "INSERT INTO remember_me_tokens (user_id, selector, hashed_validator, expires_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, selector);
            statement.setString(3, hashedValidator);
            statement.setTimestamp(4, expiresAt);
            
            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
            
            RememberMeToken token = new RememberMeToken();
            token.setUserId(userId);
            token.setSelector(selector);
            token.setHashedValidator(hashedValidator);
            token.setExpiresAt(expiresAt);
            
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    token.setId(keys.getInt(1));
                }
            }
            
            return token;
        }
    }

    /**
     * Tìm token theo selector.
     *
     * @param selector Selector token
     * @return RememberMeToken nếu tìm thấy, null nếu không
     */
    public RememberMeToken findBySelector(String selector) {
        final String sql = "SELECT id, user_id, selector, hashed_validator, expires_at FROM remember_me_tokens WHERE selector = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selector);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            // Log error nếu cần
        }
        return null;
    }

    /**
     * Xóa token theo selector.
     *
     * @param selector Selector token cần xóa
     * @throws SQLException nếu có lỗi khi delete
     */
    public void deleteBySelector(String selector) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE selector = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selector);
            statement.executeUpdate();
        }
    }

    /**
     * Xóa token theo ID.
     *
     * @param id ID của token cần xóa
     * @throws SQLException nếu có lỗi khi delete
     */
    public void deleteById(Integer id) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Xóa tất cả token của một user (khi phát hiện token bị compromise).
     *
     * @param userId ID của user
     * @throws SQLException nếu có lỗi khi delete
     */
    public void deleteAllForUser(Integer userId) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    /**
     * Cập nhật validator và thời gian hết hạn của token (token rotation).
     *
     * @param id ID của token
     * @param hashedValidator Validator mới đã được hash
     * @param expiresAt Thời điểm hết hạn mới
     * @throws SQLException nếu có lỗi khi update
     */
    public void updateValidator(Integer id, String hashedValidator, Timestamp expiresAt) throws SQLException {
        final String sql = "UPDATE remember_me_tokens SET hashed_validator = ?, expires_at = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hashedValidator);
            statement.setTimestamp(2, expiresAt);
            statement.setInt(3, id);
            statement.executeUpdate();
        }
    }

    /**
     * Ánh xạ dữ liệu từ ResultSet sang đối tượng RememberMeToken.
     */
    private RememberMeToken mapRow(ResultSet rs) throws SQLException {
        RememberMeToken token = new RememberMeToken();
        token.setId(rs.getInt("id"));
        token.setUserId(rs.getInt("user_id"));
        token.setSelector(rs.getString("selector"));
        token.setHashedValidator(rs.getString("hashed_validator"));
        token.setExpiresAt(rs.getTimestamp("expires_at"));
        return token;
    }
}

