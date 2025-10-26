/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author D E L L
 */
public class KYCRequestStatusDAO {

    //Mapping database
    private static final String COL_ID = "id";
    private static final String COL_STATUS = "status_name";

    /*Lấy trạng thái*/
    public String getKYCStatus(int id) {
        String sql = """
                     SELECT *
                     FROM kyc_request_statuses
                     LIMIT 1;
                     """;
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql);) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(COL_STATUS);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(WalletTransactionDAO.class.getName()).log(Level.SEVERE, "Lỗi liên quan đến lấy dữ liệu từ DB", e);
        }
        return null;
    }
}
