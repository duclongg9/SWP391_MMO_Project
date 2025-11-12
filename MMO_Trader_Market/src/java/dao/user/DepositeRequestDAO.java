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
import java.sql.Statement;
import java.util.UUID;
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
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = DBConnect.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("year"));
            }
        }
        return years;
    }
    
    //tạo 1 bản ghi DepositRequest để đối chiếu
    public int createDepositRequest(int userId, double amount, String message) throws SQLException {
    final String sql = """
        INSERT INTO deposit_requests (user_id, amount, qr_content, idempotency_key,status, expires_at,admin_note,created_at)
        VALUES (?, ?, ?, ?,'Pending', DATE_ADD(NOW(), INTERVAL 15 MINUTE),'',NOW())
    """;

    try (Connection con = DBConnect.getConnection();
         PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, userId);
        ps.setDouble(2, amount);                 
        ps.setString(3, message);
        ps.setString(4, java.util.UUID.randomUUID().toString());

        int affected = ps.executeUpdate();
        if (affected == 0) throw new SQLException("Insert deposit_requests failed, no rows affected.");

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);                 // chính là cột AUTO_INCREMENT id
            } else {
                throw new SQLException("Insert deposit_requests failed, no ID obtained.");
            }
        }
    }
}
    //Status = succsess
    public int UpdateDepositStatus(int id , int status) throws SQLException{
        String statusString;
        if(status == 2){
            statusString = "Complete";
        }else if(status == 3){
            statusString = "Failed";
        }else if(status == 4){
            statusString ="Exprired";
        }else{
            statusString = "Pending";
        }
        String sql="""
                   UPDATE deposit_requests
                   SET status = ?   
                   WHERE id = ?;
                   """;
        try(Connection con = DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, statusString);
            ps.setInt(2,id);
            return ps.executeUpdate();
        }
    }
    public int getUserIdBydeposit(int depositId) throws SQLException{
        String sql ="""
                  SELECT user_id 
                  FROM mmo_schema.deposit_requests
                  WHERE id =?;
                  """;
        try(Connection con =DBConnect.getConnection();PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, depositId);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) return rs.getInt("user_id");
            }
        }
        return 0;
    }

//    public static void main(String[] args) throws SQLException {
//        DepositeRequestDAO drdao = new DepositeRequestDAO();
//        int a = drdao.createDepositRequest(3, 1000, "chat");
//        System.out.println(a);
//    }
}
