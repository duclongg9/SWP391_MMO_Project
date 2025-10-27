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

    private final int id;
    private final String name;

    public ShopOption(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
