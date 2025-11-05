/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                    WalletTransactions wt = mapTransaction(rs, true);
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
                    WalletTransactions wt = mapTransaction(rs, true);
                    list.add(wt);
                }
                return list;
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }

    /**
     * Tìm giao dịch ví theo mã đồng thời đảm bảo thuộc sở hữu của người dùng.
     *
     * @param transactionId mã giao dịch ví
     * @param userId mã người dùng cần xác thực
     * @return {@link Optional} chứa giao dịch nếu tìm thấy
     */
    public Optional<WalletTransactions> findByIdForUser(int transactionId, int userId) {
        String sql = """
              SELECT wt.*
              FROM wallet_transactions AS wt
              JOIN wallets AS w ON w.id = wt.wallet_id
              WHERE wt.id = ? AND w.user_id = ?
              LIMIT 1
              """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTransaction(rs, false));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE,
                    "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        WalletTransactionDAO wdao = new WalletTransactionDAO();
        List<WalletTransactions> list = wdao.getListWalletTransactionPaging(1, 1, 5, null, null, null, null, null);
        for (WalletTransactions walletTransactions : list) {
            System.out.println(walletTransactions);
        }
    }

    private WalletTransactions mapTransaction(ResultSet rs, boolean includeWallet) throws SQLException {
        WalletTransactions wt = new WalletTransactions();
        wt.setId(rs.getInt(COL_ID));
        int walletId = rs.getInt(COL_WALLET_ID);
        wt.setWalletId(walletId);
        if (includeWallet) {
            wt.setWallet(wdao.getWalletById(walletId));
        }
        Object relatedEntity = rs.getObject(COL_RELATED_ENTITY);
        wt.setRelatedEntityId(relatedEntity == null ? null : ((Number) relatedEntity).intValue());
        String s = rs.getString(COL_TRANSACTION_TYPE);
        if (s != null) {
            try {
                wt.setTransactionType(TransactionType.fromDbValue(s));
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.WARNING,
                        "Không nhận diện được transaction_type {0}", s);
            }
        }
        wt.setAmount(rs.getBigDecimal(COL_AMOUNT));
        wt.setBalanceBefore(rs.getBigDecimal(COL_BALANCE_BEFORE));
        wt.setBalanceAfter(rs.getBigDecimal(COL_BALANCE_AFTER));
        wt.setNote(rs.getString(COL_NOTE));
        Timestamp c = rs.getTimestamp(COL_CREATED_AT);
        wt.setCreatedAt(c != null ? new java.util.Date(c.getTime()) : null);
        return wt;
    }

    public int insertTransaction(Connection connection, int walletId, Integer relatedEntityId, TransactionType type,
            BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String note) throws SQLException {
        // Ghi lại một giao dịch ví kèm trạng thái số dư trước/sau để phục vụ truy vết.
        final String sql = """
                INSERT INTO wallet_transactions
                    (wallet_id, related_entity_id, transaction_type, amount, balance_before, balance_after, note, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, walletId);
            if (relatedEntityId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                // Ghi nhận khóa ngoại tới đơn hàng/đối tượng liên quan để tiện truy vết.
                ps.setInt(2, relatedEntityId);
            }
            ps.setString(3, type.getDbValue());
            ps.setBigDecimal(4, amount);
            ps.setBigDecimal(5, balanceBefore);
            ps.setBigDecimal(6, balanceAfter);
            if (note == null || note.isBlank()) {
                ps.setNull(7, Types.VARCHAR);
            } else {
                // Ghi chú giúp người dùng nhận biết giao dịch hiển thị ngoài giao diện ví.
                ps.setString(7, note);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    // Trả về ID giao dịch để gắn vào bảng orders/payment log.
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Không thể tạo giao dịch ví mới");
    }
}
