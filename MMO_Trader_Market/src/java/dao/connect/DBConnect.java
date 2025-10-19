package dao.connect;

import conf.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple helper used by legacy DAO classes that still open raw JDBC connections.
 *
 * <p>The original implementation hard-coded the JDBC URL and credentials which made
 * it diverge from {@link conf.AppConfig}. This class now reuses the central
 * configuration file so every DAO reads from the same source of truth.</p>
 */
public final class DBConnect {

    private static final Logger LOGGER = Logger.getLogger(DBConnect.class.getName());

    private static final String URL = AppConfig.get("db.url");
    private static final String USER = AppConfig.get("db.username");
    private static final String PASSWORD = AppConfig.get("db.password");
    private static final String DRIVER = AppConfig.get("db.driver");

    static {
        if (DRIVER.isBlank() || URL.isBlank() || USER.isBlank()) {
            LOGGER.severe("Thiếu cấu hình JDBC. Kiểm tra lại conf/database.properties");
        } else {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Không tìm thấy JDBC driver: {0}", DRIVER);
            }
        }
    }

    private DBConnect() {
    }

    /**
     * Lấy kết nối mới mỗi lần được gọi, sử dụng cấu hình chung của ứng dụng.
     */
    public static Connection getConnection() throws SQLException {
        if (URL.isBlank() || USER.isBlank()) {
            throw new SQLException("Database connection properties are empty");
        }
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi khi kết nối tới database", ex);
            throw ex;
        }
    }

    /**
     * Đóng kết nối sau khi sử dụng.
     */
    public static void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Lỗi khi đóng kết nối database", ex);
        }
    }
}
