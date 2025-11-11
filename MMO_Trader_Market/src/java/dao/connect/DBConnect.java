package dao.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    private static final String URL = "jdbc:mysql://localhost:3306/mmo_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "123";

    // Ham lay ket noi moi moi lan duoc goi
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Ket noi thanh cong den database!");
        } catch (ClassNotFoundException e) {
            System.err.println("Loi: Khong tim thay driver JDBC!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Loi: Khong the ket noi den database!");
            e.printStackTrace();
        }
        return conn;
    }

    // Dong ket noi sau khi su dung
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Da dong ket noi database.");
            }
        } catch (SQLException e) {
            System.err.println("Loi khi dong ket noi database!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Connection con = DBConnect.getConnection();
        if (con != null) {
            System.out.println("Database connection is active!");
            DBConnect.closeConnection(con); // Dong ket noi sau khi kiem tra
        } else {
            System.out.println("Database connection failed!");
        }
    }
}