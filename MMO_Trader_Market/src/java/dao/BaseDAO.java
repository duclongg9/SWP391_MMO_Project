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
        String driver = AppConfig.get("db.driver");
        if (driver.isBlank()) {
            throw new SQLException("Database driver class is not configured. Update conf/database.properties");
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("JDBC driver not found", ex);
        }
        return DriverManager.getConnection(
                AppConfig.get("db.url"),
                AppConfig.get("db.username"),
                AppConfig.get("db.password"));
    }
}
