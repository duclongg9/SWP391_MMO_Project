package dao.product;

import dao.BaseDAO;
import dao.connect.DBConnect;
import model.Products;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO extends BaseDAO {

    // Tên cột trong bảng products
    private static final String COL_ID              = "id";
    private static final String COL_SHOP_ID         = "shop_id";
    private static final String COL_NAME            = "name";
    private static final String COL_PRICE           = "price";
    private static final String COL_INVENTORY_COUNT = "inventory_count";
    private static final String COL_STATUS          = "status";
    private static final String COL_DESCRIPTION     = "description";
    private static final String COL_CREATED_AT      = "created_at";
    private static final String COL_UPDATED_AT      = "updated_at";

    // Chọn đúng các cột cần dùng (tránh SELECT *)
    private static final String BASE_COLUMNS = String.join(", ",
            COL_ID, COL_SHOP_ID, COL_NAME, COL_PRICE, COL_INVENTORY_COUNT,
            COL_STATUS, COL_DESCRIPTION, COL_CREATED_AT, COL_UPDATED_AT
    );

    private Products mapRow(ResultSet rs) throws SQLException {
        Products p = new Products();
        p.setId(rs.getInt(COL_ID));
        p.setShopId(rs.getInt(COL_SHOP_ID));
        p.setName(rs.getString(COL_NAME));
        p.setPrice(rs.getBigDecimal(COL_PRICE));
        p.setInventoryCount(rs.getInt(COL_INVENTORY_COUNT));
        p.setStatus(rs.getString(COL_STATUS));
        p.setDescription(rs.getString(COL_DESCRIPTION));
        p.setCreatedAt(rs.getTimestamp(COL_CREATED_AT));   // Timestamp extends Date
        p.setUpdatedAt(rs.getTimestamp(COL_UPDATED_AT));
        return p;
    }

    /** Lấy toàn bộ sản phẩm (có thể dùng cho admin) */
    public List<Products> findAll() {
        String sql = "SELECT " + BASE_COLUMNS + " FROM products ORDER BY updated_at DESC, id DESC";
        List<Products> result = new ArrayList<>();
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách sản phẩm", e);
        }
        return result;
    }

    /** Sản phẩm nổi bật: trạng thái APPROVED (đủ dữ liệu hiển thị) */
    public List<Products> findHighlighted() {
        String sql = "SELECT " + BASE_COLUMNS + " FROM products " +
                "WHERE status = ? ORDER BY updated_at DESC, id DESC LIMIT 30";
        List<Products> result = new ArrayList<>();
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "APPROVED");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy sản phẩm nổi bật", e);
        }
        return result;
    }

    /** Lấy sản phẩm theo id (chỉ trả về khi đang active) */
    public Products findById(int id) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM products WHERE id=? AND status <> 'DELETED' LIMIT 1";
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy sản phẩm theo id", e);
        }
        return null;
    }

    /** Tìm theo tên (phục vụ search đơn giản) */
    public List<Products> searchByName(String keyword) {
        String sql = "SELECT " + BASE_COLUMNS + " FROM products " +
                "WHERE status = 'APPROVED' AND name LIKE ? ORDER BY updated_at DESC";
        List<Products> result = new ArrayList<>();
        try (Connection con = DBConnect.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi search sản phẩm theo tên", e);
        }
        return result;
    }
}
