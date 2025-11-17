package dao.order;

import dao.BaseDAO;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.product.ProductVariantOption;
import service.util.ProductVariantUtils;

/**
 * DAO for product_credentials table.
 */
public class CredentialDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(CredentialDAO.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final char[] PASSWORD_SYMBOLS = "!@#$%&*-_".toCharArray();
    private static final Gson GSON = new Gson();

    public List<Integer> pickFreeCredentialIds(int productId, int qty) {
        return pickFreeCredentialIds(productId, qty, null);
    }

    public List<Integer> pickFreeCredentialIds(int productId, int qty, String variantCode) {
        try (Connection connection = getConnection()) {
            return pickFreeCredentialIds(connection, productId, qty, variantCode);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể khóa credentials", ex);
            return List.of();
        }
    }

    public List<Integer> pickFreeCredentialIds(Connection connection, int productId, int qty) throws SQLException {
        return pickFreeCredentialIds(connection, productId, qty, null);
    }

    public List<Integer> pickFreeCredentialIds(Connection connection, int productId, int qty, String variantCode) throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        // Các bản ghi cũ có thể lưu trữ variant_code rỗng thay vì NULL nên cần gom về chung điều kiện.
        StringBuilder sql = new StringBuilder("SELECT id FROM product_credentials WHERE product_id = ? AND is_sold = 0");
        if (normalized == null) {
            sql.append(" AND (variant_code IS NULL OR TRIM(variant_code) = '')");
        } else {
            sql.append(" AND LOWER(TRIM(variant_code)) = ?");
        }
        sql.append(" ORDER BY id ASC LIMIT ? FOR UPDATE");
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            // Khóa cứng product_id để tránh lấy nhầm credential của sản phẩm khác.
            statement.setInt(index++, productId);
            if (normalized != null) {
                // Biến thể được chuẩn hóa về chữ thường để khớp với dữ liệu đã lưu.
                statement.setString(index++, normalized);
            }
            // LIMIT = số lượng người mua đặt -> đảm bảo lấy đúng số credential cần.
            statement.setInt(index, qty);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }
        return ids;
    }

    public CredentialAvailability fetchAvailability(int productId) {
        try (Connection connection = getConnection()) {
            // Mở kết nối riêng mỗi lần gọi để tái sử dụng được ở cả controller và worker.
            return fetchAvailability(connection, productId);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê credential khả dụng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailability(int productId, String variantCode) {
        String normalized = normalizeVariantCode(variantCode);
        if (normalized == null) {
            return fetchAvailability(productId);
        }
        try (Connection connection = getConnection()) {
            return fetchAvailabilityForVariant(connection, productId, normalized);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thống kê credential khả dụng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailability(Connection connection, int productId) throws SQLException {
        final String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int available = rs.getInt("available");
                    if (rs.wasNull()) {
                        available = 0;
                    }
                    return new CredentialAvailability(total, available);
                }
            }
        }
        return new CredentialAvailability(0, 0);
    }

    public CredentialAvailability fetchAvailabilityForVariant(Connection connection, int productId, String variantCode)
            throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN is_sold = 0 THEN 1 ELSE 0 END) AS available "
                + "FROM product_credentials WHERE product_id = ?");
        boolean filterDefaultVariant = normalized == null;
        if (filterDefaultVariant) {
            sql.append(" AND (variant_code IS NULL OR TRIM(variant_code) = '')");
        } else {
            sql.append(" AND LOWER(TRIM(variant_code)) = ?");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setInt(1, productId);
            if (!filterDefaultVariant) {
                // Nếu có mã biến thể cụ thể thì bind vào vị trí thứ 2 của câu lệnh.
                statement.setString(2, normalized);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int available = rs.getInt("available");
                    if (rs.wasNull()) {
                        available = 0;
                    }
                    return new CredentialAvailability(total, available);
                }
            }
        }
        return new CredentialAvailability(0, 0);
    }

    /**
     * Đảm bảo kho credential của sản phẩm (hoặc biến thể) có đủ số lượng khả
     * dụng để tạo đơn hàng mới.
     * 
     * Nếu tồn kho hiện tại thiếu, phương thức sẽ tự sinh thêm credential ảo và
     * trả về số liệu mới nhất.
     * @param productId
     * @param variantCode
     * @param requiredQuantity
     * @return 
     */
    public CredentialAvailability ensureAvailabilityForOrder(int productId, String variantCode, int requiredQuantity) {
        String normalized = normalizeVariantCode(variantCode);
        if (requiredQuantity <= 0) {
            return normalized == null ? fetchAvailability(productId) : fetchAvailability(productId, variantCode);
        }
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                CredentialAvailability availability = normalized == null
                        ? fetchAvailability(connection, productId)
                        : fetchAvailabilityForVariant(connection, productId, normalized);
                int missing = requiredQuantity - availability.available();
                if (missing > 0) {
                    // Không đủ credential -> sinh thêm credential ảo để worker có dữ liệu bàn giao.
                    generateFakeCredentials(connection, productId, normalized, missing);
                    availability = normalized == null
                            ? fetchAvailability(connection, productId)
                            : fetchAvailabilityForVariant(connection, productId, normalized);
                }
                connection.commit();
                return availability;
            } catch (SQLException ex) {
                try {
                    // Thất bại ở giữa transaction -> rollback để tránh dữ liệu dở dang.
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể đảm bảo credential sẵn sàng", ex);
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đảm bảo credential sẵn sàng", ex);
        }
        return new CredentialAvailability(0, 0);
    }

    public int generateFakeCredentials(int productId, String variantCode, int quantity) {
        if (quantity <= 0) {
            return 0;
        }
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                // Phân nhánh xuống hàm dùng chung để tái sử dụng logic insert/rollback.
                generateFakeCredentials(connection, productId, variantCode, quantity);
                connection.commit();
                return quantity;
            } catch (SQLException ex) {
                try {
                    // Gặp lỗi insert -> hoàn tác để không ghi ra credential rác.
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo", ex);
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo", ex);
        }
        return 0;
    }

    /**
     * Sinh credential ảo cho toàn bộ sản phẩm dựa trên tồn kho đã cấu hình.
     *
     * <p>
     * Với mỗi sản phẩm không có biến thể, phương thức sẽ kiểm tra số credential
     * chưa bán hiện có và sinh thêm nếu thiếu so với {@code inventory_count}.
     * Đối với sản phẩm có biến thể, tồn kho được đọc từ {@code variants_json}
     * và xử lý tương tự cho từng biến thể.</p>
     *
     * @return thống kê số credential đã sinh và số SKU được bổ sung
     */
    public BulkGenerationSummary seedAllProductsFromInventory() {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                List<ProductSeedRow> products = loadSeedableProducts(connection);
                int generated = 0;
                int touchedSkus = 0;
                for (ProductSeedRow product : products) {
                    if (ProductVariantUtils.hasVariants(product.variantSchema())) {
                        List<ProductVariantOption> variants = ProductVariantUtils
                                .parseVariants(product.variantSchema(), product.variantsJson());
                        for (ProductVariantOption variant : variants) {
                            Integer inventory = variant.getInventoryCount();
                            if (inventory == null || inventory <= 0) {
                                continue;
                            }
                            String normalizedCode = ProductVariantUtils.normalizeCode(variant.getVariantCode());
                            if (normalizedCode == null) {
                                continue;
                            }
                            CredentialAvailability availability = fetchAvailabilityForVariant(connection,
                                    product.id(), normalizedCode);
                            int missing = inventory - availability.available();
                            if (missing <= 0) {
                                continue;
                            }
                            generateFakeCredentials(connection, product.id(), normalizedCode, missing);
                            generated += missing;
                            touchedSkus++;
                        }
                    } else {
                        Integer inventory = product.inventoryCount();
                        if (inventory == null || inventory <= 0) {
                            continue;
                        }
                        CredentialAvailability availability = fetchAvailability(connection, product.id());
                        int missing = inventory - availability.available();
                        if (missing <= 0) {
                            continue;
                        }
                        generateFakeCredentials(connection, product.id(), null, missing);
                        generated += missing;
                        touchedSkus++;
                    }
                }
                connection.commit();
                return new BulkGenerationSummary(generated, touchedSkus);
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo hàng loạt", ex);
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể sinh credential ảo hàng loạt", ex);
        }
        return new BulkGenerationSummary(0, 0);
    }

    public void markCredentialsSold(int orderId, List<Integer> ids) {
        try (Connection connection = getConnection()) {
            markCredentialsSold(connection, orderId, ids);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật credentials", ex);
        }
    }

    public void markCredentialsSold(Connection connection, int orderId, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) {
            return;
        }
        final String sql = "UPDATE product_credentials SET is_sold = 1, order_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer id : ids) {
                statement.setInt(1, orderId);
                statement.setInt(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Hoàn trả toàn bộ credential đã gắn với đơn hàng về trạng thái chưa bán.
     *
     * @param orderId mã đơn hàng cần hoàn trả
     * @return số credential được cập nhật
     */
    public int releaseCredentialsForOrder(int orderId) {
        try (Connection connection = getConnection()) {
            return releaseCredentialsForOrder(connection, orderId);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể hoàn trả credential của đơn hàng", ex);
            return 0;
        }
    }

    /**
     * Hoàn trả credential của đơn hàng trong transaction hiện tại.
     *
     * @param connection kết nối đang mở
     * @param orderId    mã đơn hàng cần hoàn trả
     * @return số credential được cập nhật
     * @throws SQLException nếu câu lệnh SQL lỗi
     */
    public int releaseCredentialsForOrder(Connection connection, int orderId) throws SQLException {
        final String sql = "UPDATE product_credentials SET is_sold = 0, order_id = NULL WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            return statement.executeUpdate();
        }
    }

    public List<String> findPlainCredentialsByOrder(int orderId) {
        final String sql = "SELECT encrypted_value FROM product_credentials WHERE order_id = ? ORDER BY id ASC";
        List<String> results = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("encrypted_value"));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải credentials của đơn hàng", ex);
        }
        return results;
    }

    /**
     * Kiểm tra người mua đã từng mở khóa thông tin bàn giao hay chưa.
     * 
     * Admin sử dụng log này để truy vết lượt xem credential; tầng dịch vụ dựa
     * vào đây để quyết định có tải plaintext cho người dùng hay yêu cầu xác
     * nhận lại.
     * @param orderId
     * @param buyerId
     * @return 
     */
    public boolean hasViewLog(int orderId, int buyerId) {
        final String sql = "SELECT 1 FROM credential_view_logs WHERE order_id = ? AND buyer_id = ? LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, buyerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể kiểm tra log xem credential", ex);
            return false;
        }
    }

    /**
     * Ghi nhận hành động mở khóa credential của người mua.
     * 
     * Việc lưu trữ được thực hiện trước khi trả plaintext về cho client nhằm
     * bảo vệ dữ liệu: nếu thao tác insert thất bại hệ thống sẽ ném ngoại lệ để
     * controller có thể báo lỗi và không hiển thị thông tin nhạy cảm.
     * @param orderId
     * @param productId
     * @param buyerId
     * @param variantCode
     * @param viewerIp
     */
    public void logCredentialView(int orderId, int productId, int buyerId, String variantCode, String viewerIp) {
        final String sql = "INSERT INTO credential_view_logs (order_id, product_id, buyer_id, variant_code, viewer_ip) "
                + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE viewer_ip = VALUES(viewer_ip), viewed_at = viewed_at";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setInt(3, buyerId);
            String normalized = normalizeVariantCode(variantCode);
            if (normalized == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, normalized);
            }
            if (viewerIp == null || viewerIp.isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, viewerIp.trim());
            }
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể ghi log mở khóa thông tin bàn giao", ex);
        }
    }

    /**
     * Truy vấn toàn bộ lịch sử mở khóa credential của một đơn hàng để phục vụ
     * giao diện quản trị.
     * 
     * Phương thức trả về danh sách bản ghi giàu thông tin (order, buyer, thời
     * điểm, IP) để admin có thể rà soát khi phát sinh tranh chấp về việc đã xem
     * dữ liệu hay chưa.
     * @param orderId
     * @return 
     */
    public List<CredentialViewLogEntry> findViewLogsByOrder(int orderId) {
        final String sql = "SELECT order_id, product_id, buyer_id, variant_code, viewer_ip, viewed_at "
                + "FROM credential_view_logs WHERE order_id = ? ORDER BY viewed_at ASC";
        List<CredentialViewLogEntry> results = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new CredentialViewLogEntry(
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getInt("buyer_id"),
                            rs.getString("variant_code"),
                            rs.getString("viewer_ip"),
                            rs.getTimestamp("viewed_at")));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải log mở khóa credential", ex);
        }
        return results;
    }

    public record CredentialAvailability(int total, int available) {

    }

    public record CredentialViewLogEntry(int orderId, int productId, int buyerId, String variantCode,
            String viewerIp, Timestamp viewedAt) {

    }

    /**
     * Sinh thêm credential ảo cho sản phẩm/biến thể để bảo đảm mỗi đơn vị hàng
     * hóa đều có dữ liệu bàn giao riêng biệt.
     * @param connection
     * @param productId
     * @param variantCode
     * @param quantity
     * @throws java.sql.SQLException
     */
    public void generateFakeCredentials(Connection connection, int productId, String variantCode, int quantity)
            throws SQLException {
        if (quantity <= 0) {
            return;
        }
        String normalized = normalizeVariantCode(variantCode);
        final String sql = "INSERT INTO product_credentials (product_id, encrypted_value, variant_code, is_sold) "
                + "VALUES (?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < quantity; i++) {
                statement.setInt(1, productId);
                statement.setString(2, buildFakeCredentialPayload(productId, normalized));
                if (normalized == null) {
                    statement.setNull(3, Types.VARCHAR);
                } else {
                    statement.setString(3, normalized);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
        LOGGER.log(Level.INFO,
                "Sinh thêm {0} credential ảo cho sản phẩm {1}{2}",
                new Object[]{quantity, productId, normalized == null ? "" : " - biến thể " + normalized});
    }

    private List<ProductSeedRow> loadSeedableProducts(Connection connection) throws SQLException {
        final String sql = "SELECT id, inventory_count, variant_schema, variants_json FROM products";
        List<ProductSeedRow> products = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                products.add(new ProductSeedRow(
                        rs.getInt("id"),
                        (Integer) rs.getObject("inventory_count"),
                        rs.getString("variant_schema"),
                        rs.getString("variants_json")));
            }
        }
        return products;
    }

    private String normalizeVariantCode(String variantCode) {
        if (variantCode == null) {
            return null;
        }
        String trimmed = variantCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private void restoreAutoCommit(Connection connection, boolean previousAutoCommit) {
        try {
            if (connection.getAutoCommit() != previousAutoCommit) {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể khôi phục chế độ auto-commit", ex);
        }
    }

    private String buildFakeCredentialPayload(int productId, String normalizedVariantCode) {
        String variantSlug = normalizedVariantCode == null ? "prod" + productId
                : normalizedVariantCode.replaceAll("[^a-z0-9]", "");
        if (variantSlug.isEmpty()) {
            variantSlug = "prod" + productId;
        }
        String usernameSuffix = randomAlphaNumeric(6).toLowerCase(Locale.ROOT);
        String username = variantSlug + "_" + usernameSuffix;
        String password = randomAlphaNumeric(8) + randomPasswordSymbol() + randomAlphaNumeric(6);
        return String.format(Locale.ROOT, "{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
    }

    private String randomAlphaNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }
        return builder.toString();
    }

    private char randomPasswordSymbol() {
        return PASSWORD_SYMBOLS[RANDOM.nextInt(PASSWORD_SYMBOLS.length)];
    }

    /**
     * Thêm một credential thực (tài khoản) vào sản phẩm.
     * 
     * @param productId mã sản phẩm
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @param variantCode mã biến thể (có thể null)
     * @return true nếu thêm thành công
     */
    public boolean addRealCredential(int productId, String username, String password, String variantCode) {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                addRealCredential(connection, productId, username, password, variantCode);
                connection.commit();
                return true;
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể thêm credential thực", ex);
                return false;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể thêm credential thực", ex);
            return false;
        }
    }

    /**
     * Thêm một credential thực vào sản phẩm trong transaction.
     */
    public void addRealCredential(Connection connection, int productId, String username, String password, String variantCode) throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        String credentialJson = String.format(Locale.ROOT, "{\"username\":\"%s\",\"password\":\"%s\"}", 
                username != null ? username.replace("\"", "\\\"") : "", 
                password != null ? password.replace("\"", "\\\"") : "");
        
        final String sql = "INSERT INTO product_credentials (product_id, encrypted_value, variant_code, is_sold) "
                + "VALUES (?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, credentialJson);
            if (normalized == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, normalized);
            }
            statement.executeUpdate();
        }
        LOGGER.log(Level.INFO, "Đã thêm credential thực cho sản phẩm {0}", productId);
    }

    public record BulkGenerationSummary(int generatedCredentials, int skuTouched) {

    }

    private record ProductSeedRow(int id, Integer inventoryCount, String variantSchema, String variantsJson) {

    }

    /**
     * Class để lưu thông tin credential với username và password đã parse.
     * Sử dụng class thay vì record để JSP EL có thể truy cập được.
     */
    public static class CredentialInfo {
        private final int id;
        private final int productId;
        private final String variantCode;
        private final boolean isSold;
        private final String username;
        private final String password;
        private final java.util.Date createdAt;
        
        public CredentialInfo(int id, int productId, String variantCode, boolean isSold, 
                             String username, String password, java.util.Date createdAt) {
            this.id = id;
            this.productId = productId;
            this.variantCode = variantCode;
            this.isSold = isSold;
            this.username = username != null ? username : "";
            this.password = password != null ? password : "";
            this.createdAt = createdAt;
        }
        
        public int getId() {
            return id;
        }
        
        public int getProductId() {
            return productId;
        }
        
        public String getVariantCode() {
            return variantCode;
        }
        
        public boolean isSold() {
            return isSold;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public java.util.Date getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * Lấy tất cả credentials của sản phẩm, có thể filter theo variant code.
     * Trả về danh sách credentials với username và password đã được parse từ JSON.
     * 
     * @param productId mã sản phẩm
     * @param variantCode mã biến thể (có thể null để lấy tất cả)
     * @param includeSold true nếu muốn bao gồm cả credentials đã bán
     * @return danh sách credentials
     */
    public List<CredentialInfo> findAllCredentials(int productId, String variantCode, boolean includeSold) {
        try (Connection connection = getConnection()) {
            return findAllCredentials(connection, productId, variantCode, includeSold);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể lấy danh sách credentials", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Lấy tất cả credentials của sản phẩm trong connection.
     */
    public List<CredentialInfo> findAllCredentials(Connection connection, int productId, String variantCode, boolean includeSold) 
            throws SQLException {
        String normalized = normalizeVariantCode(variantCode);
        StringBuilder sql = new StringBuilder(
            "SELECT id, product_id, variant_code, is_sold, encrypted_value, created_at " +
            "FROM product_credentials WHERE product_id = ?"
        );
        
        if (!includeSold) {
            sql.append(" AND is_sold = 0");
        }
        
        if (normalized == null) {
            sql.append(" AND (variant_code IS NULL OR TRIM(variant_code) = '')");
        } else {
            sql.append(" AND LOWER(TRIM(variant_code)) = ?");
        }
        
        sql.append(" ORDER BY created_at DESC, id DESC");
        
        List<CredentialInfo> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, productId);
            if (normalized != null) {
                statement.setString(index++, normalized);
            }
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String encryptedValue = rs.getString("encrypted_value");
                    String username = null;
                    String password = null;
                    
                    // Parse JSON để lấy username và password
                    if (encryptedValue != null && !encryptedValue.trim().isEmpty()) {
                        try {
                            // Parse JSON: {"username":"...","password":"..."}
                            encryptedValue = encryptedValue.trim();
                            if (encryptedValue.startsWith("{") && encryptedValue.endsWith("}")) {
                                JsonObject jsonObject = JsonParser.parseString(encryptedValue).getAsJsonObject();
                                if (jsonObject.has("username")) {
                                    username = jsonObject.get("username").getAsString();
                                }
                                if (jsonObject.has("password")) {
                                    password = jsonObject.get("password").getAsString();
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Không thể parse credential JSON: " + encryptedValue, e);
                        }
                    }
                    
                    results.add(new CredentialInfo(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("variant_code"),
                        rs.getBoolean("is_sold"),
                        username != null ? username : "",
                        password != null ? password : "",
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return results;
    }

    /**
     * Cập nhật credential (username, password và variant code) theo ID.
     * 
     * @param credentialId ID của credential cần cập nhật
     * @param username tên đăng nhập mới
     * @param password mật khẩu mới
     * @param variantCode mã biến thể mới (có thể null)
     * @return true nếu cập nhật thành công
     */
    public boolean updateCredential(int credentialId, String username, String password, String variantCode) {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                updateCredential(connection, credentialId, username, password, variantCode);
                connection.commit();
                return true;
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Không thể rollback giao dịch credential", rollbackEx);
                }
                LOGGER.log(Level.SEVERE, "Không thể cập nhật credential", ex);
                return false;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể cập nhật credential", ex);
            return false;
        }
    }

    /**
     * Cập nhật credential trong transaction.
     */
    public void updateCredential(Connection connection, int credentialId, String username, String password, String variantCode) throws SQLException {
        String credentialJson = String.format(Locale.ROOT, "{\"username\":\"%s\",\"password\":\"%s\"}", 
                username != null ? username.replace("\"", "\\\"") : "", 
                password != null ? password.replace("\"", "\\\"") : "");
        
        String normalized = normalizeVariantCode(variantCode);
        
        final String sql = "UPDATE product_credentials SET encrypted_value = ?, variant_code = ? WHERE id = ? AND is_sold = 0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, credentialJson);
            if (normalized == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, normalized);
            }
            statement.setInt(3, credentialId);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Không tìm thấy credential hoặc credential đã được bán");
            }
        }
        LOGGER.log(Level.INFO, "Đã cập nhật credential {0}", credentialId);
    }
}
