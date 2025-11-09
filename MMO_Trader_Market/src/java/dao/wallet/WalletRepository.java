package dao.wallet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import model.Wallets;

/**
 * Minimal repository abstraction for wallet operations used during top-up
 * processing.
 */
public interface WalletRepository {

    Wallets lockWalletForUser(Connection connection, int userId) throws SQLException;

    boolean updateBalance(Connection connection, int walletId, BigDecimal newBalance) throws SQLException;
}
