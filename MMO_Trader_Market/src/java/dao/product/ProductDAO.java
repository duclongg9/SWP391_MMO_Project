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

    /**
     * Tạo sản phẩm mới cho shop
     */
    public Products createProduct(int shopId, String name, String description, 
                                 java.math.BigDecimal price, Integer inventoryCount) throws SQLException {
        final String sql = "INSERT INTO products (shop_id, name, description, price, inventory_count, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, 'Pending', NOW(), NOW())";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, shopId);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setBigDecimal(4, price);
            if (inventoryCount == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, inventoryCount);
            }
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo sản phẩm thất bại");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Products product = new Products();
                    product.setId(generatedKeys.getInt(1));
                    product.setShopId(shopId);
                    product.setName(name);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setInventoryCount(inventoryCount);
                    product.setStatus("Pending");
                    product.setCreatedAt(new java.util.Date());
                    product.setUpdatedAt(new java.util.Date());
                    return product;
                }
                throw new SQLException("Tạo sản phẩm thất bại, không lấy được ID");
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm của một shop
     */
    public List<Products> findByShopId(int shopId, int limit, int offset) {
        final String sql = "SELECT id, shop_id, name, description, price, inventory_count, status, created_at, updated_at "
                + "FROM products WHERE shop_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        List<Products> products = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm theo shop ID", ex);
        }
        return products;
    }

    /**
     * Đếm số sản phẩm của shop
     */
    public long countByShopId(int shopId) {
        final String sql = "SELECT COUNT(*) FROM products WHERE shop_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm sản phẩm theo shop ID", ex);
        }
        return 0;
    }

    /**
     * Cập nhật sản phẩm
     */
    public boolean updateProduct(int productId, String name, String description, 
                               java.math.BigDecimal price, Integer inventoryCount) {
        final String sql = "UPDATE products SET name = ?, description = ?, price = ?, inventory_count = ?, updated_at = NOW() "
                + "WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setBigDecimal(3, price);
            if (inventoryCount == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, inventoryCount);
            }
            statement.setInt(5, productId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật sản phẩm", ex);
            return false;
        }
    }

    /**
     * Xóa sản phẩm (chỉ chủ shop mới được xóa)
     */
    public boolean deleteProduct(int productId, int shopId) {
        final String sql = "DELETE FROM products WHERE id = ? AND shop_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, shopId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể xóa sản phẩm", ex);
            return false;
        }
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

