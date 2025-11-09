package dao.wallet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import model.TransactionType;

/**
 * Abstraction around persistence of {@code wallet_transactions}.
 */
public interface WalletTransactionRepository {

    int insertTransaction(Connection connection, int walletId, Integer relatedEntityId,
            TransactionType type, BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String note) throws SQLException;
}
