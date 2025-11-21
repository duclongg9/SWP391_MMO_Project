package dao.user;

import dao.connect.DBConnect;
import model.EmailVerificationToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO thao tác với bảng lưu trữ mã xác thực email.
 */
public class EmailVerificationTokenDAO {

    /**
     * Tạo mới hoặc thay thế mã xác thực cho người dùng, đồng thời làm mới thời gian khởi tạo.
     *
     * @param userId ID người dùng
     * @param code   mã xác thực cần lưu trữ
     * @throws SQLException nếu truy vấn thất bại hoặc vi phạm unique key
     */
    public void upsertToken(int userId, String code) throws SQLException {
        final String sql = """
                INSERT INTO email_verification_tokens (user_id, code, created_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE code = VALUES(code), created_at = VALUES(created_at)
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.executeUpdate();
        }
    }

    public String findCodeByUserId(int userId) throws SQLException {
        EmailVerificationToken token = findTokenByUserId(userId);
        return token == null ? null : token.getCode();
    }

    /**
     * Truy vấn token gần nhất của người dùng (nếu có).
     *
     * @param userId ID người dùng
     * @return {@link EmailVerificationToken} hoặc null nếu không có
     * @throws SQLException nếu truy vấn thất bại
     */
    public EmailVerificationToken findTokenByUserId(int userId) throws SQLException {
        final String sql = """
                SELECT user_id, code, created_at
                FROM email_verification_tokens
                WHERE user_id = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmailVerificationToken token = new EmailVerificationToken();
                    token.setUserId(rs.getInt("user_id"));
                    token.setCode(rs.getString("code"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    token.setCreatedAt(createdAt == null ? null : new java.util.Date(createdAt.getTime()));
                    return token;
                }
            }
        }
        return null;
    }

    public boolean hasToken(int userId) throws SQLException {
        final String sql = """
                SELECT 1
                FROM email_verification_tokens
                WHERE user_id = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    //xóa userid trong bảng email_very..
    public int deleteByUserId(int userId) throws SQLException {
        final String sql = """
                DELETE FROM email_verification_tokens
                WHERE user_id = ?
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }
}
