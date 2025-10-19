package dao.product;

import com.sun.jdi.connect.spi.Connection;
import dao.BaseDAO;
import dao.connect.DBConnect;
import model.Products;
import model.ProductStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<ProductDAO> findAll() {
        return new ArrayList<>(SAMPLE_PRODUCTS);
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

    public Optional<Products> findById(int id) {
        return SAMPLE_PRODUCTS.stream()
                .filter(product -> product.getId() == id)
                .findFirst();
    }
}
