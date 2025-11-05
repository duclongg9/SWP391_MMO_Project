package model.product;

import java.util.Objects;

/**
 * <p>
 * Đại diện cho một lựa chọn cửa hàng trong bộ lọc sản phẩm.</p>
 * <p>
 * Danh sách này được tạo ở tầng service để đổ vào combobox tìm kiếm, đảm bảo
 * tên shop không null.</p>
 *
 * @author longpdhe171902
 */
public class ShopOption {

    // ID của shop dùng trong bộ lọc.
    private final int id;
    // Tên hiển thị của shop.
    private final String name;

    // Khởi tạo lựa chọn shop, đảm bảo tên không null.
    public ShopOption(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
    }

    // Lấy ID shop.
    public int getId() {
        return id;
    }

    // Lấy tên shop.
    public String getName() {
        return name;
    }
}
