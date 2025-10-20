package service;

import dao.wallet.WalletDAO;
import dao.wallet.WalletDAO.WalletHoldRecord;
import dao.wallet.WalletDAO.WalletHoldStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Encapsulates wallet operations used during the checkout flow.
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
        try {
            return walletDAO.createHold(buyerId, sellerId, amount, orderId, orderToken);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể giữ tiền trong ví. Vui lòng nạp thêm hoặc thử lại.", ex);
        }
    }

    public void capture(String orderToken) {
        try {
            walletDAO.capture(orderToken);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể ghi nhận thanh toán từ ví.", ex);
        }
    }

    public void release(String orderToken) {
        try {
            walletDAO.release(orderToken);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể giải phóng số tiền tạm giữ.", ex);
        }
    }

    public void refund(String orderToken) {
        try {
            walletDAO.refund(orderToken);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể hoàn tiền cho ví.", ex);
        }
    }

    public boolean hasActiveHold(String orderToken) {
        return walletDAO.findHold(orderToken)
                .map(record -> record.status() == WalletHoldStatus.HOLD)
                .orElse(false);
    }
}
