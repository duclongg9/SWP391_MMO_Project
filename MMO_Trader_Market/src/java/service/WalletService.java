package service;

import dao.wallet.WalletDAO;
import dao.wallet.WalletDAO.WalletHoldRecord;
import dao.wallet.WalletDAO.WalletHoldStatus;
import dao.wallet.WalletDAO.WalletSnapshot;
import dao.wallet.WalletDAO.WalletTransactionItem;
import service.dto.WalletOverview;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public WalletOverview loadOverview(int userId, int transactionLimit) {
        Optional<WalletSnapshot> snapshot = walletDAO.findWallet(userId);
        if (snapshot.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy ví của người dùng");
        }
        WalletSnapshot wallet = snapshot.get();
        var transactions = walletDAO.findRecentTransactions(wallet.id(), transactionLimit)
                .stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
        return new WalletOverview(wallet.balance(), wallet.holdBalance(), "VND", transactions);
    }

    private WalletOverview.Transaction mapTransaction(WalletTransactionItem item) {
        java.util.Date createdAt = item.createdAt() == null ? null : new java.util.Date(item.createdAt().getTime());
        return new WalletOverview.Transaction(item.id(), item.type(), item.amount(),
                item.balanceBefore(), item.balanceAfter(), item.note(), createdAt);
    }
}
