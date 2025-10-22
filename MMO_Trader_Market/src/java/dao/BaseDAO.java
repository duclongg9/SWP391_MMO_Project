package dao;

import conf.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Common DAO functionality that centralizes how JDBC connections are created.
 */
public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        String driver = AppConfig.get("db.driver").trim();
        if (!driver.isEmpty()) {
            // Explicit driver loading remains configurable for legacy containers.
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                throw new SQLException("JDBC driver not found", ex);
            }
        }
        try {
            return DriverManager.getConnection(
                    AppConfig.get("db.url"),
                    AppConfig.get("db.username"),
                    AppConfig.get("db.password"));
        } catch (SQLException ex) {
            throw new SQLException("Failed to obtain JDBC connection using configuration keys db.url, db.username, and db.password", ex);
        }
    }
}
