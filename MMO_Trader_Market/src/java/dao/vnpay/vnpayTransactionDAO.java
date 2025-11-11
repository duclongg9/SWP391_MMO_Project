/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.vnpay;


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
public class vnpayTransactionDAO {
    private static final String COL_ID = "id";
    private static final String COL_DEPOSIT_REQUEST_ID = "deposit_request_id";
    private static final String COL_LINK_DATA = "link_data";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_VNPAY_STATUS = "vnpay_status";
    
    public int createVnpayTransaction(int depositId, String data, String status) throws SQLException  {
    final String sql = """
        INSERT INTO vnpay_transaction (deposit_request_id, link_data, vnpay_status)
        VALUES (?, CAST(? AS JSON), ?)
    """;

    final String safeStatus = (status == null || status.isBlank()) ? "pending" : status;

    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, depositId);
        ps.setString(2, data);       // JSON hợp lệ
        ps.setString(3, safeStatus);
        return ps.executeUpdate();   
    }
}

}
