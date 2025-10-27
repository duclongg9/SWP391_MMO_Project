package listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ensures JDBC resources that are registered by the application are released
 * when the web application is stopped to prevent memory leaks.
 */
@WebListener
public class ApplicationShutdownListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(ApplicationShutdownListener.class.getName());

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        shutdownAbandonedCleanupThread();
        deregisterJdbcDrivers();
    }

    private void shutdownAbandonedCleanupThread() {
        AbandonedConnectionCleanupThread.uncheckedShutdown(); // hoặc checkedShutdown() và bỏ catch
    }

    private void deregisterJdbcDrivers() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                    LOGGER.log(Level.FINE, "Deregistered JDBC driver {0}", driver);
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Unable to deregister JDBC driver " + driver, ex);
                }
            }
        }
    }
}
