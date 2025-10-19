/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import model.Wallets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import java.time.Instant;
import model.WalletTransactions;

/**
 *
 * @author D E L L
 */
public class WalletsDAO {

    /*Khai báo các phần model liên quan*/
    UserDAO udao = new UserDAO();

    /*Phần mapping database với các biến tự tạo trong java*/
    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_BALANCE = "balance";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";
    
     /*Hàm lấy thông tin wallet theo id*/
    public Wallets getWalletById(int id) {
        String sql = """
                     SELECT * FROM wallets
                     WHERE id = ?
                     LIMIT 1
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Wallets wallet = new Wallets();
                    User user = new User();
                    wallet.setId(rs.getInt(COL_ID));
                    wallet.setUserId(udao.getUserByUserId(rs.getInt(COL_USER_ID)));
                    wallet.setBalance(rs.getDouble(COL_BALANCE));
                    wallet.setStatus(rs.getInt(COL_STATUS));

                    //Lấy thời gian TimeStamp ở DB --> Instant trong java
                    java.sql.Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                    java.sql.Timestamp u = rs.getTimestamp(COL_UPDATED_AT);
                    wallet.setCreatedAt(c != null ? c.toInstant() : null);
                    wallet.setUpdatedAt(u != null ? u.toInstant() : null);

                    return wallet;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }
    
    /*Hàm lấy thông tin wallet theo id người dùng*/
    public Wallets getUserWallet(int id) {
        String sql = """
                     SELECT * FROM wallets
                     WHERE user_id = ?
                     LIMIT 1
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Wallets wallet = new Wallets();
                    User user = new User();
                    wallet.setId(rs.getInt(COL_ID));
                    wallet.setUserId(udao.getUserByUserId(rs.getInt(COL_USER_ID)));
                    wallet.setBalance(rs.getDouble(COL_BALANCE));
                    wallet.setStatus(rs.getInt(COL_STATUS));

                    //Lấy thời gian TimeStamp ở DB --> Instant trong java
                    java.sql.Timestamp c = rs.getTimestamp(COL_CREATED_AT);
                    java.sql.Timestamp u = rs.getTimestamp(COL_UPDATED_AT);
                    wallet.setCreatedAt(c != null ? c.toInstant() : null);
                    wallet.setUpdatedAt(u != null ? u.toInstant() : null);

                    return wallet;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }

}
