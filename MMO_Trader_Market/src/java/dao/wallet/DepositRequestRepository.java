package dao.wallet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import model.DepositRequests;

/**
 * Data access abstraction dedicated to {@code deposit_requests}. Separating the
 * repository contract allows the service layer to be unit-tested without
 * touching the real database.
 */
public interface DepositRequestRepository {

    DepositRequests insert(Connection connection, int userId, BigDecimal amount,
            String qrContent, String idempotencyKey, Instant expiresAt) throws SQLException;

    Optional<DepositRequests> findByTxnRef(Connection connection, String txnRef,
            boolean forUpdate) throws SQLException;

    void updateStatus(Connection connection, int id, String status) throws SQLException;
}
