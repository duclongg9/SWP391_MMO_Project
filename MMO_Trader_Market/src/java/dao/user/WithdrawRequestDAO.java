/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
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
    public int createWithDrawRequest(int userId, double amount, String bankAccountInfo){
        String sql = """
                     INSERT INTO mmo_schema.withdrawal_requests
                     (user_id, amount, bank_account_info, status, admin_proof_url, created_at, processed_at)
                     VALUES (?, ?, ?,pending, ?, NOW(), NOW())
                     """;   
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, userId);
            ps.setDouble(2, amount);
            ps.setString(3, bankAccountInfo);
            ps.setNull(4,Types.LONGNVARCHAR);
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        
        return 0;
    }
}
