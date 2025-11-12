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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author D E L L
 */
public class KYCRequestDAO {

    /*Mapping với DB*/
    private static final String COL_ID = "id";
    private static final String COL_FRONT_IMAGE_URL = "front_image_url";
    private static final String COL_BACK_IMAGE_URL = "back_image_url";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_STATUS_ID = "status_id";
    private static final String COL_SELFIE_IMAGE_URL = "selfie_image_url";
    private static final String COL_ID_NUMBER = "id_number";
    private static final String COL_ADMIN_FEED_BACK = "admin_feedback";
    private static final String COL_REVIEWD_AT = "reviewed_at";
    private static final String COL_CREATED_AT = "created_at";

    //Tạo yêu cầu KYC
    public int createdKycRequest(int id, String frontImage, String backImage, String selfieImage, String idNumber) {
        String sql = """
                         INSERT INTO mmo_schema.kyc_requests (user_id, front_image_url, back_image_url, selfie_image_url, id_number, status_id, created_at)
                                 VALUES (?, ?, ?, ?, ?, 1, NOW())
                         """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, frontImage);
            ps.setString(3, backImage);
            ps.setString(4, selfieImage);
            ps.setString(5, idNumber);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Creating KYC request failed, no rows affected.");
            }
            return rows;
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return 0;
    }

    //Phản hồi từ ADMIN
    public int kycRespones(int id, String adminNote, int statusId) {
        String sql = """
                         UPDATE mmo_schema.kyc_requests
                         SET status_id = ?,admin_feedback = ?,reviewed_at = NOW()
                         WHERE id = ?
                         """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(3, id);
            ps.setString(2, adminNote);
            ps.setInt(1, statusId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Updating KYC request failed,no rows affected");
            }
            return rows;
        } catch (SQLException e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return 0;
    }

    public int checkKycRequest(int userId) {
        String sql = """
                         SELECT COUNT(*) FROM mmo_schema.kyc_requests
                         WHERE user_id = ? AND status_id = 1;
                         """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
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

    public static void main(String[] args) {
        KYCRequestDAO krdao = new KYCRequestDAO();
        System.out.println(krdao.checkKycRequest(1));
    }
}
