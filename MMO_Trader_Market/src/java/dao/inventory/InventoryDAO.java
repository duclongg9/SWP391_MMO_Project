package dao.inventory;

import dao.BaseDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides low level inventory operations that run in the database to ensure
 * atomic updates to the remaining stock of a product.
 */
public class InventoryDAO extends BaseDAO {

    public boolean reserveStock(int productId, int quantity) throws SQLException {
        final String sql = "UPDATE products SET inventory_count = inventory_count - ? "
                + "WHERE id = ? AND inventory_count >= ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            return statement.executeUpdate() > 0;
        }
    }

    public void releaseStock(int productId, int quantity) throws SQLException {
        final String sql = "UPDATE products SET inventory_count = inventory_count + ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }
}
