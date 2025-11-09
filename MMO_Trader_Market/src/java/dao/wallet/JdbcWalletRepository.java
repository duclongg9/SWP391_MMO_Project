package dao.wallet;

import dao.user.WalletsDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import model.Wallets;

/**
 * Adapter bridging {@link WalletsDAO} with the {@link WalletRepository}
 * contract.
 */
public class JdbcWalletRepository implements WalletRepository {

    private final WalletsDAO delegate;

    public JdbcWalletRepository(WalletsDAO delegate) {
        this.delegate = delegate;
    }

    @Override
    public Wallets lockWalletForUser(Connection connection, int userId) throws SQLException {
        return delegate.lockWalletForUpdate(connection, userId);
    }

    @Override
    public boolean updateBalance(Connection connection, int walletId, BigDecimal newBalance) throws SQLException {
        return delegate.updateBalance(connection, walletId, newBalance);
    }
}
