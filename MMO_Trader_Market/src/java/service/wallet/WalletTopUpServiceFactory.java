package service.wallet;

import conf.VnpayConfig;
import dao.connect.DBConnect;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import dao.wallet.JdbcDepositRequestRepository;
import dao.wallet.JdbcVnpayTransactionRepository;
import dao.wallet.JdbcWalletRepository;
import dao.wallet.JdbcWalletTransactionRepository;

/**
 * Centralised factory used by servlets to build the {@link WalletTopUpService}
 * with concrete JDBC-based dependencies.
 */
public final class WalletTopUpServiceFactory {

    private WalletTopUpServiceFactory() {
    }

    public static WalletTopUpService createDefault() {
        VnpayConfig config = VnpayConfig.fromAppConfig();
        return new WalletTopUpService(
                config,
                new JdbcDepositRequestRepository(),
                new JdbcVnpayTransactionRepository(),
                new JdbcWalletRepository(new WalletsDAO()),
                new JdbcWalletTransactionRepository(new WalletTransactionDAO()),
                DBConnect::getConnection);
    }
}
