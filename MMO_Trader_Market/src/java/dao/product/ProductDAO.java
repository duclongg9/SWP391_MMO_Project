package dao.product;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.BaseDAO;
import model.Products;
import model.product.ProductDetail;
import model.product.ProductListRow;
import model.product.ProductVariantOption;
import model.product.ShopOption;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * DAO tương tác với bảng {@code products} để phục vụ tra cứu, thống kê và cập nhật hàng hóa.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class ProductDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());

    private static final String INVENTORY_JOIN =
            " LEFT JOIN (" +
                    "SELECT product_id, " +
                    "       SUM(available_quantity) AS available_quantity, " +
                    "       SUM(sold_quantity) AS sold_quantity " +
                    "FROM product_quantities GROUP BY product_id" +
                    ") q ON q.product_id = p.id";

    private static final String LIST_SELECT = "SELECT p.id, p.product_type, p.product_subtype, p.name, "
            + "p.short_description, p.price, COALESCE(q.available_quantity, 0) AS inventory_count, "
            + "COALESCE(q.sold_quantity, 0) AS sold_count, p.status, "
            + "p.primary_image_url, p.variant_schema, s.id AS shop_id, s.name AS shop_name "
            + "FROM products p JOIN shops s ON s.id = p.shop_id" + INVENTORY_JOIN;

    private static final String DETAIL_SELECT = "SELECT p.id, p.product_type, p.product_subtype, p.name, "
            + "p.short_description, p.description, p.price, COALESCE(q.available_quantity, 0) AS inventory_count, "
            + "COALESCE(q.sold_quantity, 0) AS sold_count, p.status, "
            + "p.primary_image_url, p.gallery_json, p.variant_schema, "
            + "s.id AS shop_id, s.name AS shop_name, s.owner_id AS shop_owner_id "
            + "FROM products p JOIN shops s ON s.id = p.shop_id" + INVENTORY_JOIN
            + " WHERE p.id = ? LIMIT 1";

    private static final String PRODUCT_BASE_SELECT = "SELECT p.id, p.shop_id, p.product_type, p.product_subtype, p.name, "
            + "p.short_description, p.description, p.price, p.primary_image_url, p.gallery_json, "
            + "COALESCE(q.available_quantity, 0) AS inventory_count, COALESCE(q.sold_quantity, 0) AS sold_count, "
            + "p.status, p.variant_schema, p.created_at, p.updated_at "
            + "FROM products p" + INVENTORY_JOIN;

    private static final String SHOP_FILTER_SELECT = "SELECT DISTINCT s.id AS shop_id, s.name AS shop_name "
            + "FROM products p JOIN shops s ON s.id = p.shop_id" + INVENTORY_JOIN
            + " WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0 ORDER BY s.name ASC";

    private final Gson gson = new Gson();
    private final Type variantListType = new TypeToken<List<ProductVariantOption>>() { }.getType();
    private final Type attributeMapType = new TypeToken<Map<String, String>>() { }.getType();

    /**
     * Lấy danh sách sản phẩm đang mở bán theo trang và bộ lọc cơ bản.
     *
     * @param keyword        từ khóa tìm kiếm theo tên/mô tả
     * @param productType    loại sản phẩm lọc theo mã
     * @param productSubtype phân loại con
     * @param limit          số bản ghi mỗi trang
     * @param offset         vị trí bắt đầu lấy dữ liệu
     * @return danh sách sản phẩm kèm thông tin shop
     */
    public List<ProductListRow> findAvailablePaged(String keyword, String productType, String productSubtype,
            int limit, int offset) {
        StringBuilder sql = new StringBuilder(LIST_SELECT)
                .append(" WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0");
        List<Object> params = new ArrayList<>();
        if (hasText(productType)) {
            sql.append(" AND p.product_type = ?");
            params.add(productType);
        }
        if (hasText(productSubtype)) {
            sql.append(" AND p.product_subtype = ?");
            params.add(productSubtype);
        }
        if (hasText(keyword)) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.short_description) LIKE ?)");
            String pattern = buildLikePattern(keyword);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);
            List<RawProductListRow> rawRows = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rawRows.add(mapRawListRow(rs));
                }
            }
            Map<Integer, String> variantsJson = loadVariantsJson(connection, rawRows);
            List<ProductListRow> rows = new ArrayList<>(rawRows.size());
            for (RawProductListRow raw : rawRows) {
                rows.add(raw.toProductListRow(variantsJson.get(raw.id())));
            }
            return rows;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách sản phẩm khả dụng", ex);
            return List.of();
        }
    }

    /**
     * Đếm số sản phẩm đang mở bán phù hợp với bộ lọc.
     *
     * @param keyword        từ khóa tìm kiếm
     * @param productType    loại sản phẩm
     * @param productSubtype phân loại con
     * @return tổng số sản phẩm thỏa điều kiện
     */
    public long countAvailable(String keyword, String productType, String productSubtype) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (")
                .append(LIST_SELECT)
                .append(" WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0");
        List<Object> params = new ArrayList<>();
        if (hasText(productType)) {
            sql.append(" AND p.product_type = ?");
            params.add(productType);
        }
        if (hasText(productSubtype)) {
            sql.append(" AND p.product_subtype = ?");
            params.add(productSubtype);
        }
        if (hasText(keyword)) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.short_description) LIKE ?)");
            String pattern = buildLikePattern(keyword);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(") AS available_products");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm sản phẩm khả dụng", ex);
        }
        return 0;
    }

    /**
     * Lấy chi tiết công khai của sản phẩm theo mã số.
     *
     * @param productId mã sản phẩm
     * @return thông tin chi tiết nếu tìm thấy
     */
    public Optional<ProductDetail> findDetailById(int productId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DETAIL_SELECT)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    RawProductDetailRow raw = mapRawDetailRow(rs);
                    String variantsJson = null;
                    if (hasVariantSchema(raw.variantSchema())) {
                        variantsJson = loadVariantsJson(connection, Collections.singletonList(raw.id()))
                                .get(raw.id());
                    }
                    return Optional.of(raw.toProductDetail(variantsJson));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải chi tiết sản phẩm", ex);
        }
        return Optional.empty();
    }

    /**
     * Lấy danh sách sản phẩm bán chạy nhất trong trạng thái khả dụng.
     *
     * @param limit giới hạn số sản phẩm trả về
     * @return danh sách sản phẩm nổi bật
     */
    public List<ProductListRow> findTopAvailable(int limit) {
        int resolvedLimit = Math.max(limit, 1);
        String sql = LIST_SELECT
                + " WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0"
                + " ORDER BY COALESCE(q.sold_quantity, 0) DESC, p.created_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resolvedLimit);
            List<RawProductListRow> rawRows = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rawRows.add(mapRawListRow(rs));
                }
            }
            Map<Integer, String> variantsJson = loadVariantsJson(connection, rawRows);
            List<ProductListRow> rows = new ArrayList<>(rawRows.size());
            for (RawProductListRow raw : rawRows) {
                rows.add(raw.toProductListRow(variantsJson.get(raw.id())));
            }
            return rows;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm nổi bật", ex);
            return List.of();
        }
    }

    /**
     * Thống kê số lượng sản phẩm đang bán theo từng loại chính.
     *
     * @return bản đồ mã loại và tổng số sản phẩm
     */
    public Map<String, Long> countAvailableByType() {
        final String sql = "SELECT p.product_type, COUNT(*) AS total "
                + "FROM products p" + INVENTORY_JOIN
                + " WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0 "
                + "GROUP BY p.product_type";
        Map<String, Long> result = new HashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("product_type");
                long total = rs.getLong("total");
                if (type != null) {
                    result.put(type, total);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm sản phẩm theo loại", ex);
        }
        return result;
    }

    /**
     * Tìm sản phẩm tương tự theo loại, bỏ qua một sản phẩm cụ thể.
     *
     * @param productType     loại sản phẩm
     * @param excludeProductId mã sản phẩm cần loại trừ
     * @param limit           số lượng gợi ý mong muốn
     * @return danh sách sản phẩm tương tự
     */
    public List<ProductListRow> findSimilarByType(String productType, int excludeProductId, int limit) {
        if (!hasText(productType)) {
            return List.of();
        }
        String sql = LIST_SELECT
                + " WHERE p.status = 'Available' AND COALESCE(q.available_quantity, 0) > 0"
                + " AND p.product_type = ? AND p.id <> ?"
                + " ORDER BY COALESCE(q.sold_quantity, 0) DESC, p.created_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productType);
            statement.setInt(2, excludeProductId);
            statement.setInt(3, Math.max(limit, 1));
            List<RawProductListRow> rawRows = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rawRows.add(mapRawListRow(rs));
                }
            }
            Map<Integer, String> variantsJson = loadVariantsJson(connection, rawRows);
            List<ProductListRow> rows = new ArrayList<>(rawRows.size());
            for (RawProductListRow raw : rawRows) {
                rows.add(raw.toProductListRow(variantsJson.get(raw.id())));
            }
            return rows;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm tương tự", ex);
            return List.of();
        }
    }

    /**
     * Lấy danh sách shop có sản phẩm khả dụng để hiển thị bộ lọc.
     *
     * @return danh sách shop và tên tương ứng
     */
    public List<ShopOption> findShopsWithAvailableProducts() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SHOP_FILTER_SELECT);
             ResultSet rs = statement.executeQuery()) {
            List<ShopOption> shops = new ArrayList<>();
            while (rs.next()) {
                shops.add(mapShopOption(rs));
            }
            return shops;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách shop cho bộ lọc sản phẩm", ex);
            return List.of();
        }
    }

    /**
     * Tìm sản phẩm theo mã không phụ thuộc trạng thái.
     *
     * @param id mã sản phẩm
     * @return {@link Optional} chứa sản phẩm nếu tồn tại
     */
    public Optional<Products> findById(int id) {
        final String sql = PRODUCT_BASE_SELECT + " WHERE p.id = ? LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int productId = rs.getInt("id");
                    String variantSchema = rs.getString("variant_schema");
                    String variantsJson = null;
                    if (hasVariantSchema(variantSchema)) {
                        variantsJson = loadVariantsJson(connection, Collections.singletonList(productId))
                                .get(productId);
                    }
                    return Optional.of(mapRow(rs, variantsJson));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm theo id", ex);
        }
        return Optional.empty();
    }

    /**
     * Tìm sản phẩm đang khả dụng theo mã số.
     *
     * @param id mã sản phẩm
     * @return {@link Optional} chứa sản phẩm nếu còn bán
     */
    public Optional<Products> findAvailableById(int id) {
        final String sql = PRODUCT_BASE_SELECT
                + " WHERE p.id = ? AND p.status = 'Available' LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int productId = rs.getInt("id");
                    String variantSchema = rs.getString("variant_schema");
                    String variantsJson = null;
                    if (hasVariantSchema(variantSchema)) {
                        variantsJson = loadVariantsJson(connection, Collections.singletonList(productId))
                                .get(productId);
                    }
                    return Optional.of(mapRow(rs, variantsJson));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải sản phẩm khả dụng", ex);
        }
        return Optional.empty();
    }

    public Map<Integer, String> findVariantsJson(Connection connection, List<Integer> productIds) throws SQLException {
        return loadVariantsJson(connection, productIds);
    }

    /**
     * Lấy giá niêm yết của sản phẩm.
     *
     * @param productId mã sản phẩm
     * @return giá nếu có
     */
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

    /**
     * Trừ tồn kho sản phẩm bằng câu lệnh độc lập.
     *
     * @param productId mã sản phẩm
     * @param qty       số lượng cần trừ
     * @return {@code true} nếu cập nhật thành công
     */
    public boolean decrementInventory(int productId, int qty) {
        return decrementInventory(productId, null, qty);
    }

    public boolean decrementInventory(int productId, Integer variantId, int qty) {
        try (Connection connection = getConnection()) {
            return decrementInventory(connection, productId, variantId, qty);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể trừ tồn kho", ex);
            return false;
        }
    }

    /**
     * Trừ tồn kho trong bối cảnh giao dịch đã có sẵn kết nối.
     *
     * @param connection kết nối dùng chung
     * @param productId  mã sản phẩm
     * @param variantId  mã biến thể (có thể null nếu sản phẩm không có biến thể)
     * @param qty        số lượng cần trừ
     * @return {@code true} nếu tồn kho đủ và trừ thành công
     * @throws SQLException khi câu lệnh SQL lỗi
     */
    public boolean decrementInventory(Connection connection, int productId, Integer variantId, int qty) throws SQLException {
        String sql;
        if (variantId == null) {
            sql = "UPDATE product_quantities SET available_quantity = available_quantity - ?, "
                    + "sold_quantity = sold_quantity + ?, updated_at = CURRENT_TIMESTAMP "
                    + "WHERE product_id = ? AND variant_id IS NULL AND available_quantity >= ?";
        } else {
            sql = "UPDATE product_quantities SET available_quantity = available_quantity - ?, "
                    + "sold_quantity = sold_quantity + ?, updated_at = CURRENT_TIMESTAMP "
                    + "WHERE product_id = ? AND variant_id = ? AND available_quantity >= ?";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setInt(index++, qty);
            statement.setInt(index++, qty);
            statement.setInt(index++, productId);
            if (variantId != null) {
                statement.setInt(index++, variantId);
            }
            statement.setInt(index, qty);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Khóa hàng tồn kho để tránh race-condition trước khi cập nhật.
     *
     * @param connection kết nối hiện hành
     * @param productId  mã sản phẩm
     * @return tồn kho hiện tại
     * @throws SQLException khi truy vấn lỗi
     */
    public int lockInventoryForUpdate(Connection connection, int productId) throws SQLException {
        ProductInventoryLock lock = lockProductForUpdate(connection, productId);
        return lock.inventoryCount() == null ? 0 : lock.inventoryCount();
    }

    public ProductInventoryLock lockProductForUpdate(Connection connection, int productId) throws SQLException {
        String variantSchema = null;
        final String productSql = "SELECT variant_schema FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(productSql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    variantSchema = rs.getString("variant_schema");
                }
            }
        }
        int totalInventory = 0;
        List<ProductVariantOption> variants = new ArrayList<>();
        final String quantitySql = "SELECT pq.available_quantity, pv.id AS variant_id, pv.variant_code, "
                + "pv.attributes_json, pv.price, pv.status "
                + "FROM product_quantities pq "
                + "LEFT JOIN product_variants pv ON pq.variant_id = pv.id "
                + "WHERE pq.product_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(quantitySql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Integer available = (Integer) rs.getObject("available_quantity");
                    if (available != null) {
                        totalInventory += available;
                    }
                    Integer variantId = (Integer) rs.getObject("variant_id");
                    if (variantId != null && hasVariantSchema(variantSchema)) {
                        variants.add(mapVariantOption(rs));
                    }
                }
            }
        }
        String variantsJson = variants.isEmpty() ? null : gson.toJson(variants, variantListType);
        return new ProductInventoryLock(totalInventory, variantSchema, variantsJson);
    }

    /**
     * Đếm số sản phẩm theo từ khóa phục vụ trang quản trị.
     *
     * @param keyword từ khóa tìm kiếm
     * @return tổng số sản phẩm khớp
     */
    public int countByKeyword(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p");
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

    /**
     * Tìm kiếm sản phẩm với phân trang cho giao diện quản trị.
     *
     * @param keyword từ khóa tìm kiếm
     * @param limit   số bản ghi tối đa
     * @param offset  vị trí bắt đầu
     * @return danh sách sản phẩm phù hợp
     */
    public List<Products> search(String keyword, int limit, int offset) {
        StringBuilder sql = new StringBuilder(PRODUCT_BASE_SELECT);
        List<String> parameters = new ArrayList<>();
        appendSearchClause(keyword, sql, parameters);
        sql.append(" ORDER BY p.updated_at DESC LIMIT ? OFFSET ?");
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareSearchStatement(connection, sql.toString(), parameters)) {
            statement.setInt(parameters.size() + 1, limit);
            statement.setInt(parameters.size() + 2, offset);
            List<Products> products = new ArrayList<>();
            List<Integer> variantProductIds = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Products product = mapRow(rs, null);
                    products.add(product);
                    if (hasVariantSchema(product.getVariantSchema())) {
                        variantProductIds.add(product.getId());
                    }
                }
            }
            Map<Integer, String> variantsJson = loadVariantsJson(connection, variantProductIds);
            for (Products product : products) {
                if (hasVariantSchema(product.getVariantSchema())) {
                    product.setVariantsJson(variantsJson.get(product.getId()));
                }
            }
            return products;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tìm kiếm sản phẩm", ex);
            return List.of();
        }
    }

    /**
     * Lấy danh sách sản phẩm nổi bật dựa vào thời gian cập nhật.
     *
     * @param limit số lượng cần lấy
     * @return danh sách sản phẩm nổi bật
     */
    public List<Products> findHighlighted(int limit) {
        int resolvedLimit = limit > 0 ? limit : 3;
        final String sql = PRODUCT_BASE_SELECT + " ORDER BY p.updated_at DESC LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resolvedLimit);
            List<Products> products = new ArrayList<>();
            List<Integer> variantProductIds = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Products product = mapRow(rs, null);
                    products.add(product);
                    if (hasVariantSchema(product.getVariantSchema())) {
                        variantProductIds.add(product.getId());
                    }
                }
            }
            Map<Integer, String> variantsJson = loadVariantsJson(connection, variantProductIds);
            for (Products product : products) {
                if (hasVariantSchema(product.getVariantSchema())) {
                    product.setVariantsJson(variantsJson.get(product.getId()));
                }
            }
            return products;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách sản phẩm nổi bật", ex);
            return List.of();
        }
    }

    /**
     * Kiểm tra chuỗi có chứa ký tự khác trắng hay không.
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Chuẩn hóa từ khóa thành biểu thức LIKE.
     */
    private String buildLikePattern(String keyword) {
        return "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
    }

    /**
     * Gán danh sách tham số vào {@link PreparedStatement} theo thứ tự.
     */
    private void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;
            if (value instanceof Integer intValue) {
                statement.setInt(index, intValue);
            } else if (value instanceof Long longValue) {
                statement.setLong(index, longValue);
            } else if (value instanceof String stringValue) {
                statement.setString(index, stringValue);
            } else if (value instanceof BigDecimal decimalValue) {
                statement.setBigDecimal(index, decimalValue);
            } else {
                statement.setObject(index, value);
            }
        }
    }

    /**
     * Bổ sung điều kiện tìm kiếm cho câu truy vấn nếu có từ khóa.
     */
    private void appendSearchClause(String keyword, StringBuilder sql, List<String> parameters) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        sql.append(" WHERE LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?");
        String pattern = '%' + keyword.toLowerCase(Locale.ROOT) + '%';
        parameters.add(pattern);
        parameters.add(pattern);
    }

    /**
     * Tạo {@link PreparedStatement} và gán tham số chuỗi cho truy vấn tìm kiếm.
     */
    private PreparedStatement prepareSearchStatement(Connection connection, String sql, List<String> parameters)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            statement.setString(i + 1, parameters.get(i));
        }
        return statement;
    }

    private RawProductListRow mapRawListRow(ResultSet rs) throws SQLException {
        Integer inventory = (Integer) rs.getObject("inventory_count");
        Integer sold = (Integer) rs.getObject("sold_count");
        return new RawProductListRow(
                rs.getInt("id"),
                rs.getString("product_type"),
                rs.getString("product_subtype"),
                rs.getString("name"),
                rs.getString("short_description"),
                rs.getBigDecimal("price"),
                inventory,
                sold,
                rs.getString("status"),
                rs.getString("primary_image_url"),
                rs.getString("variant_schema"),
                rs.getInt("shop_id"),
                rs.getString("shop_name")
        );
    }

    private RawProductDetailRow mapRawDetailRow(ResultSet rs) throws SQLException {
        Integer inventory = (Integer) rs.getObject("inventory_count");
        Integer sold = (Integer) rs.getObject("sold_count");
        Integer ownerId = (Integer) rs.getObject("shop_owner_id");
        return new RawProductDetailRow(
                rs.getInt("id"),
                rs.getString("product_type"),
                rs.getString("product_subtype"),
                rs.getString("name"),
                rs.getString("short_description"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                inventory,
                sold,
                rs.getString("status"),
                rs.getString("primary_image_url"),
                rs.getString("gallery_json"),
                rs.getString("variant_schema"),
                rs.getInt("shop_id"),
                rs.getString("shop_name"),
                ownerId
        );
    }

    private Products mapRow(ResultSet rs, String variantsJson) throws SQLException {
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
        product.setVariantsJson(variantsJson);
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

    private Map<Integer, String> loadVariantsJson(Connection connection, List<RawProductListRow> rows) throws SQLException {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        List<Integer> productIds = rows.stream()
                .filter(row -> hasVariantSchema(row.variantSchema()))
                .map(RawProductListRow::id)
                .collect(Collectors.toList());
        return loadVariantsJson(connection, productIds);
    }

    private Map<Integer, String> loadVariantsJson(Connection connection, List<Integer> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT pv.product_id, pv.id AS variant_id, pv.variant_code, pv.attributes_json, pv.price, pv.status, "
                + "COALESCE(pq.available_quantity, 0) AS available_quantity "
                + "FROM product_variants pv "
                + "LEFT JOIN product_quantities pq ON pq.variant_id = pv.id "
                + "WHERE pv.product_id IN (" + placeholders + ") "
                + "ORDER BY pv.product_id ASC, pv.id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                statement.setInt(i + 1, productIds.get(i));
            }
            Map<Integer, List<ProductVariantOption>> variantsByProduct = new LinkedHashMap<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    variantsByProduct.computeIfAbsent(productId, key -> new ArrayList<>())
                            .add(mapVariantOption(rs));
                }
            }
            Map<Integer, String> jsonMap = new HashMap<>();
            for (Map.Entry<Integer, List<ProductVariantOption>> entry : variantsByProduct.entrySet()) {
                String json = gson.toJson(entry.getValue(), variantListType);
                jsonMap.put(entry.getKey(), json);
            }
            return jsonMap;
        }
    }

    private ProductVariantOption mapVariantOption(ResultSet rs) throws SQLException {
        Integer variantId = (Integer) rs.getObject("variant_id");
        String variantCode = rs.getString("variant_code");
        String attributesJson = rs.getString("attributes_json");
        Map<String, String> attributes = parseAttributes(attributesJson);
        BigDecimal price = rs.getBigDecimal("price");
        Integer available = (Integer) rs.getObject("available_quantity");
        String status = rs.getString("status");
        return new ProductVariantOption(variantId, variantCode, attributes, price, available, status);
    }

    private Map<String, String> parseAttributes(String attributesJson) {
        if (attributesJson == null || attributesJson.isBlank()) {
            return Map.of();
        }
        Map<String, String> attributes = gson.fromJson(attributesJson, attributeMapType);
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        return new LinkedHashMap<>(attributes);
    }

    private boolean hasVariantSchema(String variantSchema) {
        if (variantSchema == null) {
            return false;
        }
        String normalized = variantSchema.trim();
        return !normalized.isEmpty() && !"NONE".equalsIgnoreCase(normalized);
    }

    /**
     * Ánh xạ dữ liệu shop sang đối tượng {@link ShopOption}.
     */
    private ShopOption mapShopOption(ResultSet rs) throws SQLException {
        return new ShopOption(rs.getInt("shop_id"), rs.getString("shop_name"));
    }

    private Products mapRow(ResultSet rs) throws SQLException {
        return mapRow(rs, null);
    }

    public record ProductInventoryLock(Integer inventoryCount, String variantSchema, String variantsJson) {
    }

    private record RawProductListRow(int id, String productType, String productSubtype, String name,
                                     String shortDescription, BigDecimal price, Integer inventoryCount,
                                     Integer soldCount, String status, String primaryImageUrl,
                                     String variantSchema, int shopId, String shopName) {

        private ProductListRow toProductListRow(String variantsJson) {
            return new ProductListRow(id, productType, productSubtype, name, shortDescription, price,
                    inventoryCount, soldCount, status, primaryImageUrl, variantSchema, variantsJson, shopId, shopName);
        }
    }

    private record RawProductDetailRow(int id, String productType, String productSubtype, String name,
                                       String shortDescription, String description, BigDecimal price,
                                       Integer inventoryCount, Integer soldCount, String status,
                                       String primaryImageUrl, String galleryJson, String variantSchema,
                                       int shopId, String shopName, Integer shopOwnerId) {

        private ProductDetail toProductDetail(String variantsJson) {
            return new ProductDetail(id, productType, productSubtype, name, shortDescription, description,
                    price, inventoryCount, soldCount, status, primaryImageUrl, galleryJson, variantSchema,
                    variantsJson, shopId, shopName, shopOwnerId);
        }
    }
}

