package dao.wallet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DepositRequests;

/**
 * JDBC implementation of {@link DepositRequestRepository}.
 */
public class JdbcDepositRequestRepository implements DepositRequestRepository {

    private static final Logger LOGGER = Logger.getLogger(JdbcDepositRequestRepository.class.getName());

    @Override
    public DepositRequests insert(Connection connection, int userId, BigDecimal amount,
            String qrContent, String idempotencyKey, Instant expiresAt) throws SQLException {
        final String sql = """
                INSERT INTO deposit_requests (user_id, amount, qr_content, idempotency_key, status, expires_at)
                VALUES (?, ?, ?, ?, 'Pending', ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, qrContent);
            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, idempotencyKey);
            }
            ps.setTimestamp(5, Timestamp.from(expiresAt));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return findById(connection, id).orElseThrow(() -> new SQLException("Missing deposit request after insert"));
                }
            }
        }
        throw new SQLException("Không thể tạo yêu cầu nạp tiền mới");
    }

    @Override
    public Optional<DepositRequests> findByTxnRef(Connection connection, String txnRef,
            boolean forUpdate) throws SQLException {
        String sql = """
                SELECT * FROM deposit_requests
                WHERE idempotency_key = ?
                """ + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, txnRef);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateStatus(Connection connection, int id, String status) throws SQLException {
        final String sql = "UPDATE deposit_requests SET status = ?, admin_note = NULL WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Optional<DepositRequests> findById(Connection connection, int id) {
        final String sql = "SELECT * FROM deposit_requests WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể truy vấn deposit_requests", ex);
        }
        return Optional.empty();
    }

    private DepositRequests mapRow(ResultSet rs) throws SQLException {
        DepositRequests request = new DepositRequests();
        request.setId(rs.getInt("id"));
        request.setUserId(rs.getInt("user_id"));
        request.setAmount(rs.getBigDecimal("amount"));
        request.setQrContent(rs.getString("qr_content"));
        request.setIdempotencyKey(rs.getString("idempotency_key"));
        request.setStatus(rs.getString("status"));
        Timestamp expires = rs.getTimestamp("expires_at");
        if (expires != null) {
            request.setExpiresAt(new java.util.Date(expires.getTime()));
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            request.setCreatedAt(new java.util.Date(created.getTime()));
        }
        request.setAdminNote(rs.getString("admin_note"));
        return request;
    }
}
