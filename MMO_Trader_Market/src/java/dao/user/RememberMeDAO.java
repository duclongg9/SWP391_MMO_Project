package dao.user;

import dao.BaseDAO;
import model.RememberMeToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO quản lý bảng {@code remember_me_tokens} dùng cho chức năng ghi nhớ đăng nhập.
 */
public class RememberMeDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(RememberMeDAO.class.getName());

    private static final String COLUMNS = "id, user_id, selector, validator_hash, expires_at, created_at, updated_at";

    public RememberMeToken insert(int userId, String selector, String validatorHash, Instant expiresAt) throws SQLException {
        String sql = "INSERT INTO remember_me_tokens (user_id, selector, validator_hash, expires_at, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, selector);
            statement.setString(3, validatorHash);
            statement.setTimestamp(4, Timestamp.from(expiresAt));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1)).orElseThrow(() -> new SQLException("Không thể load token mới tạo"));
                }
            }
        }
        throw new SQLException("Không thể tạo remember-me token mới");
    }

    public Optional<RememberMeToken> findBySelector(String selector) {
        String sql = "SELECT " + COLUMNS + " FROM remember_me_tokens WHERE selector = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selector);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findBySelector failed", ex);
        }
        return Optional.empty();
    }

    public boolean updateValidator(int tokenId, String validatorHash, Instant expiresAt) throws SQLException {
        String sql = "UPDATE remember_me_tokens SET validator_hash = ?, expires_at = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, validatorHash);
            statement.setTimestamp(2, Timestamp.from(expiresAt));
            statement.setInt(3, tokenId);
            return statement.executeUpdate() > 0;
        }
    }

    public void deleteByUser(int userId) throws SQLException {
        String sql = "DELETE FROM remember_me_tokens WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public void deleteBySelector(String selector) throws SQLException {
        String sql = "DELETE FROM remember_me_tokens WHERE selector = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selector);
            statement.executeUpdate();
        }
    }

    public int purgeExpired(Instant now) {
        String sql = "DELETE FROM remember_me_tokens WHERE expires_at < ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(now));
            return statement.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "purgeExpired failed", ex);
            return 0;
        }
    }

    public List<RememberMeToken> findByUser(int userId) {
        String sql = "SELECT " + COLUMNS + " FROM remember_me_tokens WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<RememberMeToken> tokens = new ArrayList<>();
                while (rs.next()) {
                    tokens.add(mapRow(rs));
                }
                return tokens;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findByUser failed", ex);
            return List.of();
        }
    }

    private Optional<RememberMeToken> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM remember_me_tokens WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findById failed", ex);
        }
        return Optional.empty();
    }

    private RememberMeToken mapRow(ResultSet rs) throws SQLException {
        RememberMeToken token = new RememberMeToken();
        token.setId(rs.getInt("id"));
        token.setUserId(rs.getInt("user_id"));
        token.setSelector(rs.getString("selector"));
        token.setValidatorHash(rs.getString("validator_hash"));
        Timestamp expires = rs.getTimestamp("expires_at");
        if (expires != null) {
            token.setExpiresAt(expires.toInstant());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            token.setCreatedAt(created.toInstant());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            token.setUpdatedAt(updated.toInstant());
        }
        return token;
    }
}
