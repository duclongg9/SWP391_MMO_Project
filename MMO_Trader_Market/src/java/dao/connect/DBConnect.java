package dao.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    private static final String URL = "jdbc:mysql://localhost:3306/cocoland_schema";
    private static final String USER = "root"; 
    private static final String PASSWORD = "123456"; 

    // Hàm lấy kết nối mới mỗi lần được gọi
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối thành công đến database!");
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy driver JDBC!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi: Không thể kết nối đến database!");
            e.printStackTrace();
        }
        return conn;
    }

    // Đóng kết nối sau khi sử dụng
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Đã đóng kết nối database.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đóng kết nối database!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Connection con = DBConnect.getConnection();
        if (con != null) {
            System.out.println("Database connection is active!");
            DBConnect.closeConnection(con); // Đóng kết nối sau khi kiểm tra
        } else {
            System.out.println("Database connection failed!");
        }
    }
}