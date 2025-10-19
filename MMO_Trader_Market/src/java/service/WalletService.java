package service;

import dao.wallet.WalletDAO;
import dao.wallet.WalletDAO.WalletHoldRecord;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Encapsulates wallet operations used during the checkout flow. The service
 * delegates to {@link WalletDAO} which currently stores the hold state in
 * memory, keeping the API similar to a real implementation backed by a
 * database.
 */
public class WalletService {

    private final WalletDAO walletDAO;

    public WalletService() {
        this(new WalletDAO());
    }

    public WalletService(WalletDAO walletDAO) {
        this.walletDAO = Objects.requireNonNull(walletDAO, "walletDAO");
    }

    public WalletHoldRecord hold(int buyerId, int sellerId, BigDecimal amount, int orderId, String orderToken) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Số tiền giữ phải không âm");
        }
        return walletDAO.createHold(buyerId, sellerId, amount, orderId, orderToken);
    }

    public void capture(String orderToken) {
        walletDAO.capture(orderToken);
    }

    public void release(String orderToken) {
        walletDAO.release(orderToken);
    }

    public void refund(String orderToken) {
        walletDAO.refund(orderToken);
    }

    public boolean hasHold(String orderToken) {
        return walletDAO.findHold(orderToken).isPresent();
    }
}
