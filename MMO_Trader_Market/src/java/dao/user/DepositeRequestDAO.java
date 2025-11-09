/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.user;

import dao.connect.DBConnect;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author D E L L
 */
public class DepositeRequestDAO {
    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_QR_CONTENT = "qr_content";
    private static final String COL_IDEMOTENCY_KEY = "idempotency_key";
    private static final String COL_STATUS = "status";
    private static final String COL_EXPIRES_AT = "expires_at";
    private static final String COL_ADMIN_NOTE = "admin_note";
    private static final String COL_CREATED_AT = "created_at";
    
    //Gọi tổng só tiền nạp theo tháng
    public BigDecimal getTotalDepositByMonth(int month, int year) throws SQLException {
    String sql = """
        SELECT IFNULL(SUM(amount), 0) AS total_deposit
        FROM mmo_schema.deposit_requests
        WHERE status = 'Completed'
          AND YEAR(created_at) = ?
          AND MONTH(created_at) = ?
    """;
    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, year);
        ps.setInt(2, month);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getBigDecimal("total_deposit");
        }
    }
    return BigDecimal.ZERO;
}
    
    public List<Integer> getAvailableYears() throws SQLException {
    String sql = """
        SELECT DISTINCT YEAR(created_at) AS year
        FROM (
            SELECT created_at FROM mmo_schema.deposit_requests WHERE status = 'Completed'
            UNION
            SELECT created_at FROM mmo_schema.withdrawal_requests WHERE status = 'Completed'
        ) AS combined
        ORDER BY year DESC;
    """;

    List<Integer> years = new ArrayList<>();
    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            years.add(rs.getInt("year"));
        }
    }
    return years;
}

}
