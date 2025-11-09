package service.wallet;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Allows {@link service.wallet.WalletTopUpService} to obtain JDBC connections
 * in a testable way.
 */
@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
