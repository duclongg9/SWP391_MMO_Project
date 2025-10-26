package dao.seller;

import dao.BaseDAO;
import model.Products;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cho seller quản lý sản phẩm của shop riêng.
 * Cung cấp các thao tác CRUD và thống kê sản phẩm theo shop.
 * 
 * @version 1.0
 * @author AI Assistant
 */
public class SellerProductDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(SellerProductDAO.class.getName());

    private static final String PRODUCT_COLUMNS = String.join(", ",
            "id", "shop_id", "product_type", "product_subtype", "name",
            "short_description", "description", "price", "primary_image_url",
            "gallery_json", "inventory_count", "sold_count", "status",
            "variant_schema", "variants_json", "created_at", "updated_at");

    /**
     * Lấy danh sách sản phẩm theo shop ID với phân trang.
     *
     * @param shopId ID của shop
     * @param limit  số lượng sản phẩm trên mỗi trang
     * @param offset vị trí bắt đầu
     * @return danh sách sản phẩm
     */
    public List<Products> findByShopId(int shopId, int limit, int offset) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM products "
                + "WHERE shop_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            
            List<Products> products = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
            return products;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm theo shop", ex);
            return List.of();
        }
    }

    /**
     * Đếm tổng số sản phẩm của shop.
     *
     * @param shopId ID của shop
     * @return tổng số sản phẩm
     */
    public long countByShopId(int shopId) {
        String sql = "SELECT COUNT(*) FROM products WHERE shop_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm sản phẩm theo shop", ex);
        }
        return 0;
    }

    /**
     * Tìm sản phẩm theo ID và shop ID (đảm bảo seller chỉ truy cập sản phẩm của mình).
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @return Optional chứa sản phẩm nếu tìm thấy
     */
    public Optional<Products> findByIdAndShopId(int productId, int shopId) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM products "
                + "WHERE id = ? AND shop_id = ? LIMIT 1";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, shopId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm", ex);
        }
        return Optional.empty();
    }

    /**
     * Tạo sản phẩm mới.
     *
     * @param product đối tượng sản phẩm cần tạo
     * @return ID của sản phẩm vừa tạo, hoặc -1 nếu thất bại
     */
    public int create(Products product) {
        String sql = "INSERT INTO products (shop_id, product_type, product_subtype, name, "
                + "short_description, description, price, primary_image_url, gallery_json, "
                + "inventory_count, sold_count, status, variant_schema, variants_json, "
                + "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, product.getShopId());
            statement.setString(2, product.getProductType());
            
            // Handle product_subtype - use ENUM default if empty (ENUM column, case-sensitive)
            String productSubtype = product.getProductSubtype();
            if (productSubtype == null || productSubtype.trim().isEmpty()) {
                statement.setString(3, "OTHER"); // ENUM default value (uppercase)
            } else {
                statement.setString(3, productSubtype.trim().toUpperCase());
            }
            
            statement.setString(4, product.getName());
            statement.setString(5, product.getShortDescription());
            statement.setString(6, product.getDescription());
            statement.setBigDecimal(7, product.getPrice());
            statement.setString(8, product.getPrimaryImageUrl());
            
            // Handle JSON fields - default to empty array if null or empty
            String galleryJson = product.getGalleryJson();
            statement.setString(9, (galleryJson == null || galleryJson.trim().isEmpty()) ? "[]" : galleryJson.trim());
            
            statement.setObject(10, product.getInventoryCount());
            statement.setObject(11, product.getSoldCount() != null ? product.getSoldCount() : 0);
            
            // Handle status - ensure it's exactly "Available" or "Unavailable"
            String status = product.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Available";
            } else {
                status = status.trim();
                if (!"Available".equals(status) && !"Unavailable".equals(status)) {
                    status = "Available";
                }
            }
            statement.setString(12, status);
            
            // Handle variant_schema - ENUM column with default 'NONE'
            String variantSchema = product.getVariantSchema();
            if (variantSchema == null || variantSchema.trim().isEmpty()) {
                statement.setString(13, "NONE"); // ENUM default value
            } else {
                statement.setString(13, variantSchema.trim().toUpperCase());
            }
            
            // Handle variants_json - JSON column, can be NULL
            String variantsJson = product.getVariantsJson();
            if (variantsJson == null || variantsJson.trim().isEmpty()) {
                statement.setNull(14, java.sql.Types.VARCHAR);
            } else {
                statement.setString(14, variantsJson.trim());
            }
            
            LOGGER.log(Level.INFO, "Attempting to create product: " + product.getName());
            int affectedRows = statement.executeUpdate();
            LOGGER.log(Level.INFO, "Affected rows: " + affectedRows);
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        LOGGER.log(Level.INFO, "Product created successfully with ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tạo sản phẩm mới. SQL Error: " + ex.getMessage(), ex);
        }
        return -1;
    }

    /**
     * Cập nhật thông tin sản phẩm.
     *
     * @param product sản phẩm cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean update(Products product) {
        String sql = "UPDATE products SET product_type = ?, product_subtype = ?, name = ?, "
                + "short_description = ?, description = ?, price = ?, primary_image_url = ?, "
                + "gallery_json = ?, inventory_count = ?, status = ?, variant_schema = ?, "
                + "variants_json = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND shop_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, product.getProductType());
            
            // Handle product_subtype - use ENUM default if empty (ENUM column, case-sensitive)
            String productSubtype = product.getProductSubtype();
            if (productSubtype == null || productSubtype.trim().isEmpty()) {
                statement.setString(2, "OTHER"); // ENUM default value (uppercase)
            } else {
                statement.setString(2, productSubtype.trim().toUpperCase());
            }
            
            statement.setString(3, product.getName());
            statement.setString(4, product.getShortDescription());
            statement.setString(5, product.getDescription());
            statement.setBigDecimal(6, product.getPrice());
            statement.setString(7, product.getPrimaryImageUrl());
            
            // Handle JSON fields - default to empty array if null or empty
            String galleryJson = product.getGalleryJson();
            statement.setString(8, (galleryJson == null || galleryJson.trim().isEmpty()) ? "[]" : galleryJson.trim());
            
            statement.setObject(9, product.getInventoryCount());
            
            // Handle status - ensure it's exactly "Available" or "Unavailable"
            String status = product.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Available";
            } else {
                status = status.trim();
                if (!"Available".equals(status) && !"Unavailable".equals(status)) {
                    status = "Available";
                }
            }
            statement.setString(10, status);
            
            // Handle variant_schema - ENUM column with default 'NONE'
            String variantSchema = product.getVariantSchema();
            if (variantSchema == null || variantSchema.trim().isEmpty()) {
                statement.setString(11, "NONE"); // ENUM default value
            } else {
                statement.setString(11, variantSchema.trim().toUpperCase());
            }
            
            // Handle variants_json - JSON column, can be NULL
            String variantsJson = product.getVariantsJson();
            if (variantsJson == null || variantsJson.trim().isEmpty()) {
                statement.setNull(12, java.sql.Types.VARCHAR);
            } else {
                statement.setString(12, variantsJson.trim());
            }
            
            statement.setInt(13, product.getId());
            statement.setInt(14, product.getShopId());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật sản phẩm", ex);
            return false;
        }
    }

    /**
     * Cập nhật trạng thái sản phẩm.
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @param status    trạng thái mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateStatus(int productId, int shopId, String status) {
        String sql = "UPDATE products SET status = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND shop_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, productId);
            statement.setInt(3, shopId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật trạng thái sản phẩm", ex);
            return false;
        }
    }

    /**
     * Xóa sản phẩm (soft delete bằng cách đổi status thành Unlisted).
     * Sử dụng 'Unlisted' thay vì 'Deleted' vì database ENUM chỉ có: Available, OutOfStock, Unlisted.
     *
     * @param productId ID của sản phẩm
     * @param shopId    ID của shop
     * @return true nếu xóa thành công
     */
    public boolean delete(int productId, int shopId) {
        return updateStatus(productId, shopId, "Unlisted");
    }

    /**
     * Cập nhật số lượng tồn kho.
     *
     * @param productId      ID của sản phẩm
     * @param shopId         ID của shop
     * @param inventoryCount số lượng tồn kho mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateInventory(int productId, int shopId, int inventoryCount) {
        String sql = "UPDATE products SET inventory_count = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND shop_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, inventoryCount);
            statement.setInt(2, productId);
            statement.setInt(3, shopId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật tồn kho", ex);
            return false;
        }
    }

    /**
     * Thống kê tổng số sản phẩm theo trạng thái.
     *
     * @param shopId ID của shop
     * @return mảng [Available, Unavailable, Deleted]
     */
    public long[] countByStatus(int shopId) {
        String sql = "SELECT "
                + "SUM(CASE WHEN status = 'Available' THEN 1 ELSE 0 END) AS available, "
                + "SUM(CASE WHEN status = 'Unavailable' THEN 1 ELSE 0 END) AS unavailable, "
                + "SUM(CASE WHEN status = 'Deleted' THEN 1 ELSE 0 END) AS deleted "
                + "FROM products WHERE shop_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new long[]{
                        rs.getLong("available"),
                        rs.getLong("unavailable"),
                        rs.getLong("deleted")
                    };
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê sản phẩm theo trạng thái", ex);
        }
        return new long[]{0, 0, 0};
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa trong shop.
     *
     * @param shopId  ID của shop
     * @param keyword từ khóa tìm kiếm
     * @param limit   số lượng kết quả
     * @param offset  vị trí bắt đầu
     * @return danh sách sản phẩm
     */
    public List<Products> searchInShop(int shopId, String keyword, int limit, int offset) {
        String sql = "SELECT " + PRODUCT_COLUMNS + " FROM products "
                + "WHERE shop_id = ? AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) "
                + "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            statement.setInt(1, shopId);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setInt(4, limit);
            statement.setInt(5, offset);
            
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

    /**
     * Ánh xạ ResultSet sang đối tượng Products.
     */
    private Products mapRow(ResultSet rs) throws SQLException {
        Products product = new Products();
        product.setId(rs.getInt("id"));
        product.setShopId(rs.getInt("shop_id"));
        product.setProductType(rs.getString("product_type"));
        product.setProductSubtype(rs.getString("product_subtype"));
        product.setName(rs.getString("name"));
        product.setShortDescription(rs.getString("short_description"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setPrimaryImageUrl(rs.getString("primary_image_url"));
        product.setGalleryJson(rs.getString("gallery_json"));
        
        Integer inventory = (Integer) rs.getObject("inventory_count");
        product.setInventoryCount(inventory);
        
        Integer sold = (Integer) rs.getObject("sold_count");
        product.setSoldCount(sold);
        
        product.setStatus(rs.getString("status"));
        product.setVariantSchema(rs.getString("variant_schema"));
        product.setVariantsJson(rs.getString("variants_json"));
        
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
