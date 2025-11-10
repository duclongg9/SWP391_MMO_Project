package dao.shop;

import dao.BaseDAO;
import model.Shops;
import model.view.shop.ShopPublicSummary;

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
 * DAO cung cấp dữ liệu liên quan tới bảng {@code shops} cho trang chủ và thống
 * kê.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
public class ShopDAO extends BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(ShopDAO.class.getName());

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
     * Đếm tổng số shop đang ở trạng thái Active (trả về int).
     *
     * @return số lượng shop hoạt động
     */
    public int getTotalActiveShops() {
        final String sql = "SELECT COUNT(*) FROM shops WHERE status = 'Active'";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
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
     * Lấy thông tin công khai của shop kèm tổng số sản phẩm khả dụng.
     *
     * @param shopId mã shop cần tra cứu
     * @return {@link Optional} chứa {@link ShopPublicSummary} nếu shop đang hoạt động
     */
    public Optional<ShopPublicSummary> findPublicSummaryById(int shopId) {
        final String sql = "SELECT s.id, s.name, s.description, COALESCE(pcnt.total_products, 0) AS product_count "
                + "FROM shops s "
                + "LEFT JOIN (SELECT p.shop_id, COUNT(*) AS total_products FROM products p "
                + "WHERE p.status = 'Available' AND p.inventory_count > 0 GROUP BY p.shop_id) pcnt ON pcnt.shop_id = s.id "
                + "WHERE s.id = ? AND s.status = 'Active' LIMIT 1";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    ShopPublicSummary summary = new ShopPublicSummary(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getLong("product_count"));
                    return Optional.of(summary);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải thông tin shop công khai", ex);
        }
        return Optional.empty();
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
	 * Tạo shop mới với trạng thái Active và thời điểm hiện tại.
	 * Shop được tạo sẽ tự động có status = 'Active' và created_at = NOW().
	 *
	 * @param ownerId ID của chủ sở hữu shop
	 * @param name Tên shop (đã được validate ở tầng Service)
	 * @param description Mô tả shop (có thể null)
	 * @return Đối tượng Shops vừa được tạo (bao gồm ID đã được generate)
	 * @throws SQLException nếu có lỗi khi insert hoặc không lấy được generated key
	 */
	public Shops create(int ownerId, String name, String description) throws SQLException {
                final String sql = "INSERT INTO shops (owner_id, name, description, status, created_at, updated_at) VALUES (?, ?, ?, 'Active', NOW(), NOW())";
		try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, ownerId);
			stmt.setString(2, name);
			stmt.setString(3, description);
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
			s.setOwnerId(ownerId);
			s.setName(name);
			s.setDescription(description);
			s.setStatus("Active");
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
	public boolean update(int id, int ownerId, String name, String description) throws SQLException {
                final String sql = "UPDATE shops SET name = ?, description = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
		try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, id);
			stmt.setInt(4, ownerId);
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
	public boolean setStatus(int id, int ownerId, String status) throws SQLException {
                final String sql = "UPDATE shops SET status = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
		try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, status);
			stmt.setInt(2, id);
			stmt.setInt(3, ownerId);
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
        public Optional<Shops> findByIdAndOwner(int id, int ownerId) throws SQLException {
                final String sql = "SELECT id, owner_id, name, description, status, created_at, updated_at FROM shops WHERE id = ? AND owner_id = ?";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setInt(1, id);
                        stmt.setInt(2, ownerId);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        return Optional.of(mapRow(rs));
                                }
                        }
                }
                return Optional.empty();
        }

        /**
         * Lấy chi tiết shop kèm thống kê cơ bản (sản phẩm, lượng bán, tồn kho) theo ID và owner.
         *
         * @param shopId  ID shop cần tìm
         * @param ownerId ID chủ sở hữu
         * @return {@link Optional} chứa {@link model.ShopStatsView} nếu tồn tại
         * @throws SQLException khi truy vấn thất bại
         */
        public Optional<model.ShopStatsView> findDetailByIdAndOwner(int shopId, int ownerId) throws SQLException {
                final String sql = "SELECT\n"
                        + "  s.id, s.name, s.description, s.status, s.created_at, s.updated_at,\n"
                        + "  COALESCE(pcnt.product_count, 0) AS product_count,\n"
                        + "  COALESCE(sales.total_sold, 0)   AS total_sold,\n"
                        + "  COALESCE(inven.total_inventory, 0) AS total_inventory\n"
                        + "FROM shops s\n"
                        + "LEFT JOIN (SELECT p.shop_id, COUNT(*) AS product_count FROM products p GROUP BY p.shop_id) pcnt ON pcnt.shop_id = s.id\n"
                        + "LEFT JOIN (SELECT p.shop_id, SUM(p.sold_count) AS total_sold FROM products p GROUP BY p.shop_id) sales ON sales.shop_id = s.id\n"
                        + "LEFT JOIN (SELECT p.shop_id, SUM(p.inventory_count) AS total_inventory FROM products p GROUP BY p.shop_id) inven ON inven.shop_id = s.id\n"
                        + "WHERE s.id = ? AND s.owner_id = ? LIMIT 1";
                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setInt(1, shopId);
                        stmt.setInt(2, ownerId);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        model.ShopStatsView v = new model.ShopStatsView();
                                        v.setId(rs.getInt("id"));
                                        v.setName(rs.getString("name"));
                                        v.setDescription(rs.getString("description"));
                                        v.setStatus(rs.getString("status"));
                                        v.setCreatedAt(rs.getTimestamp("created_at"));
                                        v.setUpdatedAt(rs.getTimestamp("updated_at"));
                                        v.setProductCount(rs.getInt("product_count"));
                                        v.setTotalSold(rs.getInt("total_sold"));
                                        v.setTotalInventory(rs.getInt("total_inventory"));
                                        return Optional.of(v);
                                }
                        }
                }
                return Optional.empty();
        }

	/**
	 * Lấy danh sách shop của owner kèm thống kê (số sản phẩm, lượng bán, tồn kho).
	 * Sử dụng JOIN và derived tables để tránh N+1 query problem.
	 * Hỗ trợ sắp xếp theo: lượng bán giảm dần, mới nhất, hoặc tên A-Z.
	 *
	 * @param ownerId ID của chủ sở hữu
	 * @param sortBy Cách sắp xếp: 'sales_desc' (lượng bán cao→thấp), 'created_desc' (mới nhất), 'name_asc' (tên A→Z), null mặc định là 'sales_desc'
	 * @return Danh sách ShopStatsView chứa thông tin shop và các thống kê đã được aggregate
	 * @throws SQLException nếu có lỗi khi truy vấn database
	 */
        public List<model.ShopStatsView> findByOwnerWithStats(int ownerId, String sortBy, String searchKeyword) throws SQLException {
                StringBuilder sql = new StringBuilder("SELECT\n"
                                + "  s.id, s.name, s.description, s.status, s.created_at, s.updated_at,\n"
                                + "  COALESCE(pcnt.product_count, 0) AS product_count,\n"
                                + "  COALESCE(sales.total_sold, 0)   AS total_sold,\n"
                                + "  COALESCE(inven.total_inventory, 0) AS total_inventory\n"
                                + "FROM shops s\n"
                                + "LEFT JOIN (SELECT p.shop_id, COUNT(*) AS product_count FROM products p GROUP BY p.shop_id) pcnt ON pcnt.shop_id = s.id\n"
                                + "LEFT JOIN (SELECT p.shop_id, SUM(p.sold_count) AS total_sold FROM products p GROUP BY p.shop_id) sales ON sales.shop_id = s.id\n"
                                + "LEFT JOIN (SELECT p.shop_id, SUM(p.inventory_count) AS total_inventory FROM products p GROUP BY p.shop_id) inven ON inven.shop_id = s.id\n"
                                + "WHERE s.owner_id = ?");

                boolean hasKeyword = searchKeyword != null && !searchKeyword.isBlank();
                if (hasKeyword) {
                        sql.append(" AND LOWER(s.name) LIKE ?");
                }

                sql.append("\nORDER BY\n")
                   .append("  CASE WHEN ? = 'sales_desc'   THEN sales.total_sold END DESC,\n")
                   .append("  CASE WHEN ? = 'created_desc' THEN s.created_at     END DESC,\n")
                   .append("  CASE WHEN ? = 'name_asc'     THEN s.name           END ASC,\n")
                   .append("  s.created_at DESC, s.id DESC");

                try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                        int paramIndex = 1;
                        stmt.setInt(paramIndex++, ownerId);

                        if (hasKeyword) {
                                stmt.setString(paramIndex++, "%" + searchKeyword.trim().toLowerCase(Locale.ROOT) + "%");
                        }

                        String order = (sortBy == null || sortBy.isBlank()) ? "sales_desc" : sortBy;
                        stmt.setString(paramIndex++, order);
                        stmt.setString(paramIndex++, order);
                        stmt.setString(paramIndex++, order);

                        List<model.ShopStatsView> list = new ArrayList<>();
                        try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                        model.ShopStatsView v = new model.ShopStatsView();
                                        v.setId(rs.getInt("id"));
                                        v.setName(rs.getString("name"));
                                        v.setDescription(rs.getString("description"));
                                        v.setStatus(rs.getString("status"));
                                        v.setCreatedAt(rs.getTimestamp("created_at"));
                                        v.setUpdatedAt(rs.getTimestamp("updated_at"));
                                        v.setProductCount(rs.getInt("product_count"));
                                        v.setTotalSold(rs.getInt("total_sold"));
                                        v.setTotalInventory(rs.getInt("total_inventory"));
                                        list.add(v);
                                }
                        }
                        return list;
                }
        }
}
