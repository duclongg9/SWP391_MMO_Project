package dao.user;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.RememberMeToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;

/**
 * DAO for managing remember-me persistent login tokens.
 */
public class RememberMeTokenDAO extends BaseDAO {

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_SELECTOR = "selector";
    private static final String COL_HASHED_VALIDATOR = "hashed_validator";
    private static final String COL_EXPIRES_AT = "expires_at";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_LAST_USED_AT = "last_used_at";

    public RememberMeToken createToken(int userId, String selector, String hashedValidator, Timestamp expiresAt)
            throws SQLException {
        final String sql = """
                INSERT INTO remember_me_tokens (user_id, selector, hashed_validator, expires_at, last_used_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public RememberMeToken findBySelector(String selector) {
        final String sql = """
                SELECT * FROM remember_me_tokens
                WHERE selector = ?
                LIMIT 1
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public void updateValidator(int id, String hashedValidator, Timestamp expiresAt) throws SQLException {
        final String sql = """
                UPDATE remember_me_tokens
                SET hashed_validator = ?, expires_at = ?, last_used_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedValidator);
            ps.setTimestamp(2, expiresAt);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteBySelector(String selector) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE selector = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selector);
            ps.executeUpdate();
        }
    }

    public void deleteAllForUser(int userId) throws SQLException {
        final String sql = "DELETE FROM remember_me_tokens WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

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
}
