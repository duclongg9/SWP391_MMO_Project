package service;

import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import java.sql.Date;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import model.TransactionType;
import model.WalletTransactions;
import model.Wallets;

public class WalletService {

    private final WalletsDAO wdao;
    private final WalletTransactionDAO wtdao;

    public WalletService(WalletsDAO wdao, WalletTransactionDAO wtdao) {
        this.wdao = wdao;
        this.wtdao = wtdao;
    }

    /* Xem ví của mình */
    public Wallets viewUserWallet(int userId) {
        Wallets wallet = wdao.getUserWallet(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Hệ thống ví đang gặp vấn đề");
        }
        // userId trong model là Integer, so sánh trực tiếp
        if (wallet.getUserId() == null || !wallet.getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền xem ví này");
        }
        return wallet;
    }

    /* Xem lịch sử giao dịch trong ví */
    public List<WalletTransactions> viewUserTransactionList(int walletId, int userId, int currentPage, int pageSize, String transactionType, Double minAmount, Double maxAmount, Date startDate, Date endDate) {
        int page = Math.max(1, currentPage);

        //Vadidate dữ liệu truyền vào
        String typeNorm = null;
        if (transactionType != null && !transactionType.isBlank()) {
            typeNorm = transactionType.trim().toUpperCase(Locale.ROOT);
            if (!EnumSet.allOf(TransactionType.class)
                    .contains(TransactionType.valueOf(typeNorm))) {
                throw new IllegalArgumentException("Loại giao dịch không hợp lệ");
            }
        }

        if (minAmount != null && !Double.isFinite(minAmount)) {
            throw new IllegalArgumentException("Min không hợp lệ");
        }
        if (maxAmount != null && !Double.isFinite(maxAmount)) {
            throw new IllegalArgumentException("Max không hợp lệ");
        }
        if (minAmount != null && maxAmount != null && (minAmount - maxAmount) > 1e-9) {
            throw new IllegalArgumentException("min > max");
        }
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ (start > end)");
        }

        Wallets wallet = wdao.getUserWallet(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Ví không tồn tại");
        }
        if (wallet.getUserId() == null || !wallet.getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền xem ví này");
        }

        List<WalletTransactions> list = wtdao.getListWalletTransactionPaging(walletId, page, pageSize, typeNorm, minAmount, maxAmount, startDate, endDate);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Ví này chưa có một giao dịch nào");
        }
        return list;
    }

    public int totalPage(int userId) {
        return wtdao.totalTransaction(userId);
    }
}
