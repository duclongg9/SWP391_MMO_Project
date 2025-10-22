/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.sql.Types;
import model.TransactionType;
import model.WalletTransactions;

/**
 *
 * @author D E L L
 */
public class WalletTransactionDAO {

    /*Khai báo hàm số*/
    private static final int PAGE_SIZE = 5;

    /*Khai báo các phần model liên quan*/
    WalletsDAO wdao = new WalletsDAO();


    /*Phần mapping database với các biến tự tạo trong java*/
    private static final String COL_ID = "id";
    private static final String COL_WALLET_ID = "wallet_id";
    private static final String COL_RELATED_ENTITY = "related_entity_id";
    private static final String COL_TRANSACTION_TYPE = "transaction_type";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_BALANCE_BEFORE = "balance_before";
    private static final String COL_BALANCE_AFTER = "balance_after";
    private static final String COL_NOTE = "note";
    private static final String COL_CREATED_AT = "created_at";


    /*Hàm lấy danh sách wallet transaction của người dùng*/
    public List<WalletTransactions> getListWalletTransaction(int id) {
        List<WalletTransactions> list = new ArrayList<>();
        String sql;
        sql = """
              SELECT * FROM wallet_transactions
              WHERE id = ?
              """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WalletTransactions wt = new WalletTransactions();
                    wt.setId(rs.getInt(COL_ID));
                    int walletId = rs.getInt(COL_WALLET_ID);
                    wt.setWalletId(walletId);
                    wt.setWallet(wdao.getWalletById(walletId));
                    Object relatedEntity = rs.getObject(COL_RELATED_ENTITY);
                    wt.setRelatedEntityId(relatedEntity == null ? null : ((Number) relatedEntity).intValue());
                    String s = rs.getString(COL_TRANSACTION_TYPE);
                    if (s != null) {
                        wt.setTransactionType(TransactionType.fromDbValue(s));
                    }
                    wt.setAmount(rs.getBigDecimal(COL_AMOUNT));
                    wt.setBalanceBefore(rs.getBigDecimal(COL_BALANCE_BEFORE));
                    wt.setBalanceAfter(rs.getBigDecimal(COL_BALANCE_AFTER));
                    wt.setNote(rs.getString(COL_NOTE));
                    java.sql.Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                    wt.setCreatedAt(c != null ? new java.util.Date(c.getTime()) : null);
                    list.add(wt);
                }
                return list;
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }

    //Tính tổng số transaction của mỗi user
    public int totalTransaction(int id) {
        String sql = """
                     SELECT COUNT(*) FROM wallets w 
                     JOIN wallet_transactions wt ON wt.wallet_id = w.id
                     WHERE user_id = ?
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return 0;
    }

    /*Hàm lấy danh sách transaction và phân trang*/
    public List<WalletTransactions> getListWalletTransactionPaging(int id, int index, int pageSize, String transactionType, Double minAmount, Double maxAmount, Date startDate, Date endDate) {
        List<WalletTransactions> list = new ArrayList<>();

        //Phần tích toán số trang động
        final int page = Math.max(1, index);
        final int limit = pageSize;
        final int offset = (page - 1) * limit;

        String sql;
        sql = """
              SELECT *
              FROM wallet_transactions AS wt
              WHERE
                wt.wallet_id = ?
                AND( ? IS NULL OR ? = '' OR FIND_IN_SET(wt.transaction_type, ?) > 0 )
                AND ( ? IS NULL OR wt.amount >= ?)
                AND ( ? IS NULL OR wt.amount <= ? )
                AND ( ? IS NULL OR wt.created_at >= ? )
                AND ( ? IS NULL OR wt.created_at <  ? )
              ORDER BY wt.created_at DESC, wt.id DESC
              LIMIT ? OFFSET ?;
              """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            String typesCsv = (transactionType == null || transactionType.isBlank())
                    ? null : transactionType.trim();
            if (typesCsv == null) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(2, typesCsv);
                ps.setString(3, typesCsv);
                ps.setString(4, typesCsv);
            }
            if (minAmount == null) {
                ps.setNull(5, Types.DECIMAL);
                ps.setNull(6, Types.DECIMAL);
            } else {
                ps.setDouble(5, minAmount);
                ps.setDouble(6, minAmount);
            }


            if (maxAmount == null) {
                ps.setNull(7, Types.DECIMAL);
                ps.setNull(8, Types.DECIMAL);
            } else {
                ps.setDouble(7, maxAmount);
                ps.setDouble(8, maxAmount);
            }

            // 5) TIME: bind TIMESTAMP an toàn
            if (startDate == null) {
                ps.setNull(9, Types.TIMESTAMP);
                ps.setNull(10, Types.TIMESTAMP);
            } else {
                Timestamp fromTs = new Timestamp(startDate.getTime());
                ps.setTimestamp(9, fromTs);
                ps.setTimestamp(10, fromTs);
            }
            if (endDate == null) {
                ps.setNull(11, Types.TIMESTAMP);
                ps.setNull(12, Types.TIMESTAMP);
            } else {
                Timestamp toTs = new Timestamp(endDate.getTime());
                ps.setTimestamp(11, toTs);
                ps.setTimestamp(12, toTs);
            }
            ps.setInt(13, limit);
            ps.setInt(14, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WalletTransactions wt = new WalletTransactions();
                    wt.setId(rs.getInt(COL_ID));
                    wt.setWalletId(wdao.getWalletById(rs.getInt(COL_WALLET_ID)));
                    wt.setRelatedEntityId(rs.getInt(COL_RELATED_ENTITY));
                    //map từ ENUM java --> enum DB
                    String s = rs.getString(COL_TRANSACTION_TYPE); //"Deposit" | "Purchase" |
                    TransactionType type = TransactionType.fromDbValue(s);

                    wt.setTransactionType(type);
                    wt.setAmount(rs.getDouble(COL_AMOUNT));
                    wt.setBalanceBefore(rs.getDouble(COL_BALANCE_BEFORE));
                    wt.setBalanceAfter(rs.getDouble(COL_BALANCE_AFTER));
                    wt.setNote(rs.getString(COL_NOTE));
                    java.sql.Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                    wt.setCreatedAt(c != null ? new java.util.Date(c.getTime()) : null);
                    list.add(wt);
                }
                return list;
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }

    public static void main(String[] args) {
        WalletTransactionDAO wdao = new WalletTransactionDAO();
        List<WalletTransactions> list = wdao.getListWalletTransactionPaging(1, 1, 5, null, null, null, null, null);
        for (WalletTransactions walletTransactions : list) {
            System.out.println(walletTransactions);
        }
    }
}
