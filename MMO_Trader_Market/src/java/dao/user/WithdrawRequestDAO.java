/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import model.WithdrawalRequests;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Types;

/**
 *
 * @author D E L L
 */
public class WithdrawRequestDAO {

    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_ACCOUNT_INFO = "bank_account_info";
    private static final String COL_STATUS = "status";
    private static final String COL_PROOF_URL = "admin_proof_url";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_PROCESSED_AT = "processed_at";

    //Tạo một yêu cầu rút tiền mới 
    public int createWithDrawRequest(int userId, BigDecimal amount, String bankAccountInfo) {
        String sql = """
                     INSERT INTO mmo_schema.withdrawal_requests
                     (user_id, amount, bank_account_info, status, admin_proof_url, created_at, processed_at)
                     VALUES (?, ?, ?,'Pending', NULL, NOW(), NOW())
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, bankAccountInfo);
            int rows = ps.executeUpdate();
            return rows;
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }

        return 0;
    }

    //Gọi tổng số tiền rút theo tháng
    public BigDecimal getTotalWithdrawByMonth(int month, int year) throws SQLException {
        String sql = """
        SELECT 
              IFNULL(SUM(amount), 0) AS total_withdraw
          FROM mmo_schema.withdrawal_requests
          WHERE status = 'Completed'
            AND YEAR(created_at) = ?
            AND MONTH(created_at) = ?;
    """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total_withdraw");
            }
        }
        return BigDecimal.ZERO;
    }

    public WithdrawalRequests findById(int id) throws SQLException {
        String sql = """
            SELECT id, user_id, amount, bank_account_info, status
            FROM mmo_schema.withdrawal_requests
            WHERE id = ?
        """;
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WithdrawalRequests wr = new WithdrawalRequests();
                    wr.setId(rs.getInt("id"));
                    wr.setUserId(rs.getInt("user_id"));
                    wr.setAmount(rs.getBigDecimal("amount"));
                    wr.setBankAccountInfo(rs.getString("bank_account_info"));
                    wr.setStatus(rs.getString("status"));
                    return wr;
                }
            }
        }
        return null;

    }
}
