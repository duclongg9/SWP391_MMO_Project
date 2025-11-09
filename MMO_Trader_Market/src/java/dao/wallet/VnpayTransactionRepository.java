package dao.wallet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import model.VnpayTransaction;

/**
 * Repository abstraction to keep track of payload exchanges with VNPAY.
 */
public interface VnpayTransactionRepository {

    VnpayTransaction insert(Connection connection, int depositRequestId,
            String linkData) throws SQLException;

    void updateStatusAndPayload(Connection connection, int depositRequestId,
            String status, String linkData) throws SQLException;

    Optional<VnpayTransaction> findByDepositRequestId(Connection connection,
            int depositRequestId, boolean forUpdate) throws SQLException;
}
