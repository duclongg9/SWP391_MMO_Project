package controller.dev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.BaseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple development endpoint for checking database connectivity and seed data.
 */
@WebServlet(name = "DevPingDbController", urlPatterns = {"/dev/ping-db"})
public class DevPingDbController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DevPingDbController.class.getName());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ProbeDao probeDao = new ProbeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> payload = new HashMap<>();
        try (Connection connection = probeDao.openConnection()) {
            payload.put("dbName", probeDao.fetchCurrentDatabase(connection));
            payload.put("availableCount", probeDao.fetchAvailableCount(connection));
            payload.put("topProducts", probeDao.fetchTopProducts(connection));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "[DevPingDbController] Unable to execute probe queries", ex);
            payload.put("error", ex.getMessage());
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.write(gson.toJson(payload));
            writer.flush();
        }
    }

    private static final class ProbeDao extends BaseDAO {

        private static final String SQL_DATABASE = "SELECT DATABASE()";
        private static final String SQL_AVAILABLE_COUNT = "SELECT COUNT(*) FROM products "
                + "WHERE status='Available' AND inventory_count > 0";
        private static final String SQL_TOP_PRODUCTS = "SELECT id, name, product_type, product_subtype, "
                + "inventory_count, sold_count FROM products "
                + "WHERE status='Available' AND inventory_count > 0 "
                + "ORDER BY sold_count DESC, created_at DESC LIMIT 3";

        Connection openConnection() throws SQLException {
            return getConnection();
        }

        String fetchCurrentDatabase(Connection connection) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(SQL_DATABASE);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
            return null;
        }

        long fetchAvailableCount(Connection connection) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(SQL_AVAILABLE_COUNT);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0L;
        }

        List<Map<String, Object>> fetchTopProducts(Connection connection) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(SQL_TOP_PRODUCTS);
                 ResultSet rs = statement.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("name", rs.getString("name"));
                    row.put("productType", rs.getString("product_type"));
                    row.put("productSubtype", rs.getString("product_subtype"));
                    row.put("inventoryCount", rs.getObject("inventory_count"));
                    row.put("soldCount", rs.getObject("sold_count"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }
}
