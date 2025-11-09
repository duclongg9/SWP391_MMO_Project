package dao.wallet;

import dao.user.WalletTransactionDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import model.TransactionType;

/**
 * JDBC backed implementation for writing wallet transaction records.
 */
public class JdbcWalletTransactionRepository implements WalletTransactionRepository {

    private final WalletTransactionDAO delegate;

    public JdbcWalletTransactionRepository(WalletTransactionDAO delegate) {
        this.delegate = delegate;
    }

    @Override
    public int insertTransaction(Connection connection, int walletId, Integer relatedEntityId,
            TransactionType type, BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String note) throws SQLException {
        return delegate.insertTransaction(connection, walletId, relatedEntityId, type,
                amount, balanceBefore, balanceAfter, note);
    }
}
