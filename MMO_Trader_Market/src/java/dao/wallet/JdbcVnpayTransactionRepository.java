package dao.wallet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;
import model.VnpayTransaction;

/**
 * JDBC persistence for {@link VnpayTransaction} records.
 */
public class JdbcVnpayTransactionRepository implements VnpayTransactionRepository {

    @Override
    public VnpayTransaction insert(Connection connection, int depositRequestId,
            String linkData) throws SQLException {
        final String sql = """
                INSERT INTO vnpay_transaction (deposit_request_id, link_data, vnpay_status)
                VALUES (?, CAST(? AS JSON), 'pending')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, depositRequestId);
            ps.setString(2, linkData);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return findById(connection, id).orElseThrow(() -> new SQLException("Missing VNPAY transaction after insert"));
                }
            }
        }
        throw new SQLException("Không thể lưu giao dịch VNPAY");
    }

    @Override
    public void updateStatusAndPayload(Connection connection, int depositRequestId,
            String status, String linkData) throws SQLException {
        final String sql = """
                UPDATE vnpay_transaction
                SET vnpay_status = ?, link_data = CAST(? AS JSON)
                WHERE deposit_request_id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, linkData);
            ps.setInt(3, depositRequestId);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<VnpayTransaction> findByDepositRequestId(Connection connection,
            int depositRequestId, boolean forUpdate) throws SQLException {
        final String sql = """
                SELECT * FROM vnpay_transaction
                WHERE deposit_request_id = ?
                """ + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, depositRequestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<VnpayTransaction> findById(Connection connection, int id) throws SQLException {
        final String sql = "SELECT * FROM vnpay_transaction WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private VnpayTransaction mapRow(ResultSet rs) throws SQLException {
        VnpayTransaction tx = new VnpayTransaction();
        tx.setId(rs.getInt("id"));
        tx.setDepositRequestId(rs.getInt("deposit_request_id"));
        tx.setLinkData(rs.getString("link_data"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            tx.setCreatedAt(new java.util.Date(created.getTime()));
        }
        tx.setVnpayStatus(rs.getString("vnpay_status"));
        return tx;
    }
}
