package dao.user;

import dao.connect.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO thao tác với bảng lưu trữ mã xác thực email.
 */
public class EmailVerificationTokenDAO {

    public void createToken(int userId, String code) throws SQLException {
        final String sql = """
                INSERT INTO email_verification_tokens (user_id, code)
                VALUES (?, ?)
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.executeUpdate();
        }
    }

    public String findCodeByUserId(int userId) throws SQLException {
        final String sql = """
                SELECT code
                FROM email_verification_tokens
                WHERE user_id = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("code");
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
