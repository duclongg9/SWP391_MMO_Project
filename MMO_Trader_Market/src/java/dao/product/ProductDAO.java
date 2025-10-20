package dao.product;

import dao.BaseDAO;
import model.Products;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access object for the {@code products} table.
 */
public class ProductDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    private static final String PRODUCT_COLUMNS = "id, shop_id, name, description, price, "
            + "inventory_count, status, created_at, updated_at";

    public Optional<Products> findById(int id) {
        final String sql = "SELECT " + PRODUCT_COLUMNS + " FROM products WHERE id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm theo id", ex);
        }
        return Optional.empty();
    }

    public Optional<Products> findAvailableById(int id) {
        final String sql = "SELECT " + PRODUCT_COLUMNS
                + " FROM products WHERE id = ? AND status = 'Available' LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm khả dụng", ex);
        }
        return Optional.empty();
    }

    public Optional<BigDecimal> findPriceById(int productId) {
        final String sql = "SELECT price FROM products WHERE id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getBigDecimal("price"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy giá sản phẩm", ex);
        }
        return Optional.empty();
    }

    public boolean decrementInventory(int productId, int qty) {
        try (Connection connection = getConnection()) {
            return decrementInventory(connection, productId, qty);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể trừ tồn kho", ex);
            return false;
        }
    }

    public boolean decrementInventory(Connection connection, int productId, int qty) throws SQLException {
        final String sql = "UPDATE products SET inventory_count = inventory_count - ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND inventory_count >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, qty);
            statement.setInt(2, productId);
            statement.setInt(3, qty);
            return statement.executeUpdate() > 0;
        }
    }

    public int lockInventoryForUpdate(Connection connection, int productId) throws SQLException {
        final String sql = "SELECT inventory_count FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("inventory_count");
                }
            }
        }
        return 0;
    }

    public int countByKeyword(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products");
        List<String> parameters = new ArrayList<>();
        appendSearchClause(keyword, sql, parameters);
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareSearchStatement(connection, sql.toString(), parameters)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số lượng sản phẩm", ex);
        }
        return 0;
    }

    public List<Products> search(String keyword, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(PRODUCT_COLUMNS)
                .append(" FROM products");
        List<String> parameters = new ArrayList<>();
        appendSearchClause(keyword, sql, parameters);
        sql.append(" ORDER BY updated_at DESC LIMIT ? OFFSET ?");
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareSearchStatement(connection, sql.toString(), parameters)) {
            statement.setInt(parameters.size() + 1, limit);
            statement.setInt(parameters.size() + 2, offset);
            List<Products> products = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
            return products;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm kiếm sản phẩm", ex);
            return List.of();
        }
    }

    public List<Products> findHighlighted(int limit) {
        int resolvedLimit = limit > 0 ? limit : 3;
        final String sql = "SELECT " + PRODUCT_COLUMNS
                + " FROM products ORDER BY updated_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resolvedLimit);
            List<Products> products = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
            return products;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách sản phẩm nổi bật", ex);
            return List.of();
        }
    }

    private void appendSearchClause(String keyword, StringBuilder sql, List<String> parameters) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        sql.append(" WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ?");
        String pattern = '%' + keyword.toLowerCase(Locale.ROOT) + '%';
        parameters.add(pattern);
        parameters.add(pattern);
    }

    private PreparedStatement prepareSearchStatement(Connection connection, String sql, List<String> parameters)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setString(i + 1, parameters.get(i));
        }
        return statement;
    }

    private Products mapRow(ResultSet rs) throws SQLException {
        Products product = new Products();
        product.setId(rs.getInt("id"));
        product.setShopId(rs.getInt("shop_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setInventoryCount(rs.getInt("inventory_count"));
        product.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) {
            product.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        if (updatedAt != null) {
            product.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }
        return product;
    }
}

