package dao.shop;

import dao.BaseDAO;
import model.Shops;
import model.view.ShopListItem;
import service.dto.ShopFilters;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO cung cấp dữ liệu liên quan tới bảng {@code shops} cho trang chủ và thống
 * kê.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
    public class ShopDAO extends BaseDAO {

        private static final Logger LOGGER = Logger.getLogger(ShopDAO.class.getName());

        public Connection openConnection() throws SQLException {
                return getConnection();
        }

    /**
     * Lấy danh sách shop đang hoạt động gần nhất.
     *
     * @param limit số lượng shop cần trả về
     * @return danh sách shop hoạt động
     */
    public List<Shops> findActive(int limit) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at, updated_at "
                + "FROM shops WHERE status = 'Active' ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            List<Shops> shops = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    shops.add(mapRow(rs));
                }
            }
            return shops;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải danh sách shop đang hoạt động", ex);
            return List.of();
        }
    }

    /**
     * Đếm tổng số shop đang ở trạng thái Active.
     *
     * @return số lượng shop hoạt động
     */
    public long countActive() {
        final String sql = "SELECT COUNT(*) FROM shops WHERE status = 'Active'";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể đếm số lượng shop đang hoạt động", ex);
        }
        return 0;
    }

    /**
     * Tìm shop theo owner_id.
     *
     * @param ownerId mã người dùng sở hữu shop
     * @return shop nếu tìm thấy, null nếu không tìm thấy
     */
    public Shops findByOwnerId(int ownerId) {
        final String sql = "SELECT id, owner_id, name, description, status, created_at, updated_at "
                + "FROM shops WHERE owner_id = ? LIMIT 1";
        try (Connection connection = getConnection(); 
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải shop theo owner_id", ex);
        }
        return null;
    }

    /**
     * Ánh xạ dữ liệu kết quả sang đối tượng {@link Shops}.
     */
    private Shops mapRow(ResultSet rs) throws SQLException {
        Shops shop = new Shops();
        shop.setId(rs.getInt("id"));
        shop.setOwnerId(rs.getInt("owner_id"));
        shop.setName(rs.getString("name"));
        shop.setDescription(rs.getString("description"));
        shop.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            shop.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            shop.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }
        return shop;
    }

    private ShopListItem mapListItem(ResultSet rs) throws SQLException {
        ShopListItem item = new ShopListItem();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(new java.util.Date(updatedAt.getTime()));
        }
        item.setProductCount(rs.getInt("product_count"));
        item.setTotalSold(rs.getLong("total_sold"));
        item.setTotalStock(rs.getLong("total_stock"));
        return item;
    }

	/**
	 * Đếm số lượng shop hiện có của một chủ sở hữu (owner).
	 * Dùng để kiểm tra giới hạn tối đa 5 shop trước khi cho phép tạo mới.
	 *
	 * @param ownerId ID của chủ sở hữu shop
	 * @return Số lượng shop thuộc về owner này
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public int countByOwner(int ownerId) throws SQLException {
                final String sql = "SELECT COUNT(*) FROM shops WHERE owner_id = ?";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setInt(1, ownerId);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        return rs.getInt(1);
                                }
                        }
                }
                return 0;
        }

        /**
         * Kiểm tra tên shop đã tồn tại trong phạm vi một owner hay chưa.
         *
         * @param ownerId       chủ shop
         * @param normalizedName tên đã chuẩn hóa (collapse space, lower-case)
         * @param excludeShopId  id shop cần bỏ qua khi kiểm tra (dùng khi update)
         * @return true nếu đã tồn tại shop trùng tên
         * @throws SQLException khi truy vấn lỗi
         */
        public boolean existsNameInOwner(long ownerId, String normalizedName, Long excludeShopId) throws SQLException {
                final StringBuilder sql = new StringBuilder("SELECT 1 FROM shops WHERE owner_id = ? AND LOWER(name) = ?");
                if (excludeShopId != null) {
                        sql.append(" AND id <> ?");
                }
                sql.append(" LIMIT 1");
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                        stmt.setLong(1, ownerId);
                        stmt.setString(2, normalizedName.toLowerCase());
                        if (excludeShopId != null) {
                                stmt.setLong(3, excludeShopId);
                        }
                        try (ResultSet rs = stmt.executeQuery()) {
                                return rs.next();
                        }
                }
        }

	/**
	 * Tạo shop mới với trạng thái Active và thời điểm hiện tại.
	 * Shop được tạo sẽ tự động có status = 'Active' và created_at = NOW().
	 *
	 * @param ownerId ID của chủ sở hữu shop
	 * @param name Tên shop (đã được validate ở tầng Service)
	 * @param description Mô tả shop (có thể null)
	 * @return Đối tượng Shops vừa được tạo (bao gồm ID đã được generate)
	 * @throws SQLException nếu có lỗi khi insert hoặc không lấy được generated key
	 */
        public Shops insert(long ownerId, String name, String description, String status) throws SQLException {
                final String sql = "INSERT INTO shops (owner_id, name, description, status, created_at, updated_at) "
                                + "VALUES (?, ?, ?, ?, NOW(), NOW())";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setLong(1, ownerId);
                        stmt.setString(2, name);
                        stmt.setString(3, description);
                        stmt.setString(4, status);
                        int affected = stmt.executeUpdate();
                        if (affected == 0) {
                                throw new SQLException("Không tạo được shop (0 rows)");
                        }
                        Integer newId = null;
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                                if (keys.next()) {
                                        newId = keys.getInt(1);
                                }
                        }
                        Shops s = new Shops();
                        s.setId(newId);
                        s.setOwnerId((int) ownerId);
                        s.setName(name);
                        s.setDescription(description);
                        s.setStatus(status);
                        java.util.Date now = new java.util.Date();
                        s.setCreatedAt(now);
                        s.setUpdatedAt(now);
                        return s;
                }
        }

	/**
	 * Cập nhật tên và mô tả của shop, chỉ cho phép nếu shop thuộc về owner.
	 * Đảm bảo quyền truy cập bằng cách kiểm tra owner_id trong WHERE clause.
	 *
	 * @param id ID của shop cần cập nhật
	 * @param ownerId ID của chủ sở hữu (để xác thực quyền)
	 * @param name Tên mới của shop (đã được validate)
	 * @param description Mô tả mới (có thể null)
	 * @return true nếu cập nhật thành công (shop tồn tại và thuộc về owner), false nếu không tìm thấy hoặc không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public boolean update(long id, long ownerId, String name, String description) throws SQLException {
                final String sql = "UPDATE shops SET name = ?, description = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setString(2, description);
                        stmt.setLong(3, id);
                        stmt.setLong(4, ownerId);
                        return stmt.executeUpdate() > 0;
                }
        }

	/**
	 * Cập nhật trạng thái shop (Active hoặc Suspended), chỉ cho phép nếu shop thuộc về owner.
	 * Dùng cho chức năng ẩn shop (Suspended) hoặc khôi phục shop (Active).
	 *
	 * @param id ID của shop cần thay đổi trạng thái
	 * @param ownerId ID của chủ sở hữu (để xác thực quyền)
	 * @param status Trạng thái mới ('Active' hoặc 'Suspended')
	 * @return true nếu cập nhật thành công, false nếu không tìm thấy hoặc không có quyền
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public boolean updateStatus(long id, long ownerId, String status) throws SQLException {
                final String sql = "UPDATE shops SET status = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, status);
                        stmt.setLong(2, id);
                        stmt.setLong(3, ownerId);
                        return stmt.executeUpdate() > 0;
                }
        }

        public boolean updateStatus(Connection connection, long id, long ownerId, String status) throws SQLException {
                final String sql = "UPDATE shops SET status = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, status);
                        stmt.setLong(2, id);
                        stmt.setLong(3, ownerId);
                        return stmt.executeUpdate() > 0;
                }
        }

	/**
	 * Tìm shop theo ID và owner, đảm bảo chỉ trả về shop thuộc về owner.
	 * Dùng để kiểm tra quyền trước khi cho phép chỉnh sửa hoặc thao tác trên shop.
	 *
	 * @param id ID của shop cần tìm
	 * @param ownerId ID của chủ sở hữu (để xác thực quyền)
	 * @return Optional chứa Shops nếu tìm thấy và thuộc về owner, Optional.empty() nếu không
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public Optional<Shops> findByIdAndOwner(long id, long ownerId) throws SQLException {
                final String sql = "SELECT id, owner_id, name, description, status, created_at, updated_at FROM shops WHERE id = ? AND owner_id = ?";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setLong(1, id);
                        stmt.setLong(2, ownerId);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        return Optional.of(mapRow(rs));
                                }
                        }
                }
                return Optional.empty();
        }

        /**
         * Lấy danh sách shop của owner kèm thống kê tổng quan.
         *
         * @param ownerId ID của chủ sở hữu
         * @param filters bộ lọc tên và khoảng thời gian tạo
         * @return danh sách {@link ShopListItem} đã sắp xếp theo thời gian cập nhật giảm dần
         * @throws SQLException nếu có lỗi khi truy vấn database
         */
        public List<ShopListItem> findByOwnerWithFilters(long ownerId, ShopFilters filters) throws SQLException {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT s.id, s.name, s.description, s.status, s.created_at, s.updated_at, ")
                        .append("COALESCE(agg.product_count, 0) AS product_count, ")
                        .append("COALESCE(agg.total_sold, 0) AS total_sold, ")
                        .append("COALESCE(agg.total_stock, 0) AS total_stock ")
                        .append("FROM shops s ")
                        .append("LEFT JOIN (SELECT shop_id, COUNT(*) AS product_count, ")
                        .append("SUM(p.sold_count) AS total_sold, SUM(p.inventory_count) AS total_stock ")
                        .append("FROM products p GROUP BY shop_id) agg ON agg.shop_id = s.id ")
                        .append("WHERE s.owner_id = ? ");

                List<Object> params = new ArrayList<>();
                params.add(ownerId);

                if (filters != null) {
                        String keyword = filters.getKeyword();
                        if (keyword != null && !keyword.isBlank()) {
                                sql.append("AND LOWER(s.name) LIKE ? ");
                                params.add('%' + keyword.toLowerCase() + '%');
                        }
                        LocalDate from = filters.getFromDate();
                        if (from != null) {
                                sql.append("AND s.created_at >= ? ");
                                params.add(Date.valueOf(from));
                        }
                        LocalDate to = filters.getToDate();
                        if (to != null) {
                                sql.append("AND s.created_at < ? ");
                                params.add(Date.valueOf(to.plusDays(1)));
                        }
                }

                sql.append("ORDER BY s.updated_at DESC, s.created_at DESC");

                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                        for (int i = 0; i < params.size(); i++) {
                                Object value = params.get(i);
                                if (value instanceof String) {
                                        stmt.setString(i + 1, (String) value);
                                } else if (value instanceof Date) {
                                        stmt.setDate(i + 1, (Date) value);
                                } else if (value instanceof Long) {
                                        stmt.setLong(i + 1, (Long) value);
                                } else if (value instanceof Integer) {
                                        stmt.setInt(i + 1, (Integer) value);
                                } else {
                                        stmt.setObject(i + 1, value);
                                }
                        }

                        List<ShopListItem> items = new ArrayList<>();
                        try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                        items.add(mapListItem(rs));
                                }
                        }
                        return items;
                }
        }
}
