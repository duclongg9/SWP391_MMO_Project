package dao.connect;

import conf.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    // Ham lay ket noi moi moi lan duoc goi
    public static Connection getConnection() throws SQLException {
        String driver = AppConfig.get("db.driver").trim();
        if (!driver.isEmpty()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver not found: " + driver, e);
            }
        }
        String url = AppConfig.get("db.url");
        String user = AppConfig.get("db.username");
        String password = AppConfig.get("db.password");
        
        if (url.isEmpty() || user.isEmpty() || password.isEmpty()) {
            throw new SQLException("Database configuration is incomplete. Check db.url, db.username, and db.password in database.properties");
        }
        
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Ket noi thanh cong den database!");
            return conn;
        } catch (SQLException e) {
            System.err.println("Loi: Khong the ket noi den database!");
            System.err.println("URL: " + url);
            System.err.println("User: " + user);
            throw new SQLException("Failed to connect to database. Check your database configuration and ensure MySQL is running.", e);
        }
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
        try {
            Connection con = DBConnect.getConnection();
            if (con != null) {
                System.out.println("Database connection is active!");
                DBConnect.closeConnection(con); // Dong ket noi sau khi kiem tra
            } else {
                System.out.println("Database connection failed!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}