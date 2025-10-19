package dao.user;

import dao.connect.DBConnect;
import model.PasswordResetToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO thao tác bảng password_reset_tokens.
 */
public class PasswordResetTokenDAO {

    public void createToken(int userId, String token, Timestamp expiresAt) throws SQLException {
        final String sql = """
                INSERT INTO password_reset_tokens (user_id, token, expires_at)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }
    }

    public PasswordResetToken findActiveToken(String token) throws SQLException {
        final String sql = """
                SELECT id, user_id, token, expires_at, used_at, created_at
                FROM password_reset_tokens
                WHERE token = ? AND used_at IS NULL
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int markUsed(int id) throws SQLException {
        final String sql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE id = ? AND used_at IS NULL
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private PasswordResetToken mapRow(ResultSet rs) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getInt("id"));
        token.setUserId(rs.getInt("user_id"));
        token.setToken(rs.getString("token"));
        token.setExpiresAt(rs.getTimestamp("expires_at"));
        token.setUsedAt(rs.getTimestamp("used_at"));
        token.setCreatedAt(rs.getTimestamp("created_at"));
        return token;
    }
}
