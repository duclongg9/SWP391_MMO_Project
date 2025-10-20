package dao.user;

import dao.BaseDAO;
import model.OrderStatus;
import model.Users;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuyerDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(BuyerDAO.class.getName());
    private static final String BUYER_ROLE = "BUYER";
    private static final SecureRandom RANDOM = new SecureRandom();

    public Optional<Users> findActiveBuyerByEmail(String email) {
        final String sql = "SELECT id, role_id, email, name, avatar_url, hashed_password, google_id, status, created_at, updated_at "
                + "FROM users WHERE email = ? AND status = 1 LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm người mua theo email", ex);
        }
        return Optional.empty();
    }

    public Optional<Users> findTopBuyerByCompletedOrders() {
        final String sql = "SELECT u.id, u.role_id, u.email, u.name, u.avatar_url, u.hashed_password, u.google_id, u.status, "
                + "u.created_at, u.updated_at "
                + "FROM users u "
                + "JOIN roles r ON r.id = u.role_id "
                + "LEFT JOIN orders o ON o.buyer_id = u.id AND o.status = ? "
                + "WHERE u.status = 1 AND r.name = ? "
                + "GROUP BY u.id, u.role_id, u.email, u.name, u.avatar_url, u.hashed_password, u.google_id, u.status, u.created_at, u.updated_at "
                + "ORDER BY COUNT(o.id) DESC, u.created_at ASC LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, OrderStatus.COMPLETED.toDatabaseValue());
            statement.setString(2, BUYER_ROLE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm buyer có nhiều đơn hoàn thành nhất", ex);
        }
        return Optional.empty();
    }

    public Users createBuyer(String email) throws SQLException {
        try (Connection connection = getConnection()) {
            int roleId = resolveBuyerRoleId(connection);
            final String sql = "INSERT INTO users (role_id, email, name, hashed_password, status, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, 1, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, roleId);
                statement.setString(2, email);
                statement.setString(3, deriveDisplayName(email));
                statement.setString(4, generateHashedPassword());
                Timestamp now = Timestamp.from(Instant.now());
                statement.setTimestamp(5, now);
                statement.setTimestamp(6, now);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        Users user = new Users();
                        user.setId(keys.getInt(1));
                        user.setRoleId(roleId);
                        user.setEmail(email);
                        user.setName(deriveDisplayName(email));
                        user.setStatus(true);
                        user.setCreatedAt(now);
                        user.setUpdatedAt(now);
                        return user;
                    }
                }
            }
        }
        throw new SQLException("Không thể tạo người mua mới");
    }

    public long countActiveBuyers() {
        final String sql = "SELECT COUNT(*) FROM users u "
                + "JOIN roles r ON r.id = u.role_id WHERE u.status = 1 AND r.name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, BUYER_ROLE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số lượng buyer đang hoạt động", ex);
        }
        return 0;
    }

    private Users mapRow(ResultSet rs) throws SQLException {
        Users user = new Users();
        user.setId(rs.getInt("id"));
        user.setRoleId(rs.getInt("role_id"));
        user.setEmail(rs.getString("email"));
        user.setName(rs.getString("name"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setHashedPassword(rs.getString("hashed_password"));
        user.setGoogleId(rs.getString("google_id"));
        user.setStatus(rs.getObject("status") == null ? null : rs.getBoolean("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }

    private int resolveBuyerRoleId(Connection connection) throws SQLException {
        final String sql = "SELECT id FROM roles WHERE name = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, BUYER_ROLE);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Không tìm thấy vai trò BUYER trong hệ thống");
    }

    private String deriveDisplayName(String email) {
        String localPart = email == null ? "" : email.split("@", 2)[0];
        if (localPart.isBlank()) {
            return "Buyer";
        }
        return localPart.substring(0, 1).toUpperCase(Locale.ROOT) + localPart.substring(1);
    }

    private String generateHashedPassword() {
        byte[] randomBytes = new byte[16];
        RANDOM.nextBytes(randomBytes);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(randomBytes);
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.WARNING, "Không thể khởi tạo SHA-256, sử dụng fallback", ex);
            StringBuilder builder = new StringBuilder();
            for (byte b : randomBytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
    }
}

