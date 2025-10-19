package dao.product;

import dao.BaseDAO;
import model.Products;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access object for product records.
 */
public class ProductDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    private static final String PRODUCT_COLUMNS = "p.id, p.shop_id, p.name, p.description, p.price, "
            + "p.inventory_count, p.status, p.is_featured, p.created_at, p.updated_at";

    public long countAllByOwner(int ownerId) {
        final String sql = "SELECT COUNT(*) FROM products p "
                + "JOIN shops s ON s.id = p.shop_id "
                + "WHERE s.owner_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "countAllByOwner failed", ex);
        }
        return 0L;
    }

    public List<Products> findFeaturedByOwner(int ownerId, int limit) {
        int resolvedLimit = limit > 0 ? limit : 4;
        final String sql = "SELECT " + PRODUCT_COLUMNS
                + " FROM products p "
                + "JOIN shops s ON s.id = p.shop_id "
                + "WHERE s.owner_id = ? AND p.is_featured = 1 AND p.status = 'APPROVED' "
                + "ORDER BY p.updated_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            statement.setInt(2, resolvedLimit);
            try (ResultSet rs = statement.executeQuery()) {
                List<Products> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
                return products;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findFeaturedByOwner failed", ex);
            return List.of();
        }
    }

    public List<Products> search(int ownerId, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        StringBuilder sql = new StringBuilder("SELECT ").append(PRODUCT_COLUMNS)
                .append(" FROM products p JOIN shops s ON s.id = p.shop_id WHERE s.owner_id = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(ownerId);
        appendKeywordFilter(keyword, sql, parameters);
        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        parameters.add(safeSize);
        parameters.add(offset);

        try (Connection connection = getConnection();
             PreparedStatement statement = prepareStatement(connection, sql.toString(), parameters)) {
            try (ResultSet rs = statement.executeQuery()) {
                List<Products> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
                return products;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "search failed", ex);
            return List.of();
        }
    }

    public long countSearch(int ownerId, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p "
                + "JOIN shops s ON s.id = p.shop_id WHERE s.owner_id = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(ownerId);
        appendKeywordFilter(keyword, sql, parameters);

        try (Connection connection = getConnection();
             PreparedStatement statement = prepareStatement(connection, sql.toString(), parameters);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "countSearch failed", ex);
        }
        return 0L;
    }

    public Optional<Products> findById(int id) {
        final String sql = "SELECT " + PRODUCT_COLUMNS + " FROM products p WHERE p.id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findById failed", ex);
        }
        return Optional.empty();
    }

    public List<Products> findHighlighted(int limit) {
        int resolvedLimit = limit > 0 ? limit : 3;
        final String sql = "SELECT " + PRODUCT_COLUMNS
                + " FROM products p ORDER BY p.updated_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resolvedLimit);
            try (ResultSet rs = statement.executeQuery()) {
                List<Products> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
                return products;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "findHighlighted failed", ex);
            return List.of();
        }
    }

    private void appendKeywordFilter(String keyword, StringBuilder sql, List<Object> parameters) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?)");
        String pattern = '%' + keyword.trim().toLowerCase(Locale.ROOT) + '%';
        parameters.add(pattern);
        parameters.add(pattern);
    }

    private PreparedStatement prepareStatement(Connection connection, String sql, List<Object> parameters)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        int index = 1;
        for (Object param : parameters) {
            if (param instanceof Integer value) {
                statement.setInt(index++, value);
            } else if (param instanceof Long value) {
                statement.setLong(index++, value);
            } else if (param instanceof String value) {
                statement.setString(index++, value);
            } else {
                statement.setObject(index++, param);
            }
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
        if (hasColumn(rs, "is_featured")) {
            boolean featured = rs.getBoolean("is_featured");
            product.setFeatured(rs.wasNull() ? null : featured);
        }
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

    private boolean hasColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
