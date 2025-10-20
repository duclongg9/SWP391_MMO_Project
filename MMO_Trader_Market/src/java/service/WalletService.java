package service;

import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import java.util.ArrayList;
import java.util.List;
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
    public List<WalletTransactions> viewUserTransactionList(int walletId, int userId, int currentPage, int pageSize) {
        int page = Math.max(1, currentPage);

        Wallets wallet = wdao.getUserWallet(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Ví không tồn tại");
        }
        if (wallet.getUserId() == null || !wallet.getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền xem ví này");
        }

        List<WalletTransactions> list = wtdao.getListWalletTransactionPaging(walletId, page, pageSize);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Ví này chưa có một giao dịch nào");
        }
        return list;
    }

    public int totalPage(int userId) {
        return wtdao.totalTransaction(userId);
    }
}