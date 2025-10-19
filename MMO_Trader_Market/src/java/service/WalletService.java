/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import java.util.ArrayList;
import java.util.List;
import model.WalletTransactions;
import model.Wallets;

/**
 *
 * @author D E L L
 */
public class WalletService {

    //Khai báo các model liên quan
    private final WalletsDAO wdao;
    private final WalletTransactionDAO wtdao;

    public WalletService(WalletsDAO wdao, WalletTransactionDAO wtdao) {
        this.wdao = wdao;
        this.wtdao = wtdao;
    }

    /*Xem ví của mình*/
    public Wallets viewUserWallet(int userId) {

        // Quyền Isở hữu ví
        Wallets wallet = wdao.getUserWallet(userId);
        if (wallet.getUserId().getId() != userId) {
            throw new SecurityException("Không có quyền xem ví này");
        }
        if (wallet == null) {
            throw new IllegalArgumentException("Hệ thống ví đang gặp vấn đề");
        }
        return wallet;
    }

    /*Xem lịch sử giao dịch trong ví*/
    public List<WalletTransactions> viewUserTransactionList(int userId, int index) {
        int page = Math.max(1, index);

        // Kiểm quyền trước khi đọc giao dịch (chặn IDOR)
        Wallets wallet = wdao.getUserWallet(index);
        if (wallet == null) {
            throw new IllegalArgumentException("Ví không tồn tại");
        }
        if (wallet.getUserId() == null || wallet.getUserId().getId() != userId) {
            throw new SecurityException("Không có quyền xem ví này");
        }
        List<WalletTransactions> list = new ArrayList<>();
        list = wtdao.getListWalletTransactionPaging(userId, index);
        if (list.isEmpty() || list == null) {
            throw new IllegalArgumentException("Ví này chưa có một giao dịch nào");
        }
        return list;
    }
    
    public int totalPage(int userId){
        return wtdao.totalTransaction(userId);
    }
}
