package model.view.product;

/**
 * Một lựa chọn loại con (subtype) trong bộ lọc sản phẩm.
 * Kết hợp cùng {@link ProductTypeOption} để hiển thị cấu trúc danh mục hai tầng
 * ở trang browse.
 *
 * @author longpdhe171902
 */
public class ProductSubtypeOption {

    // Mã loại con.
    private final String code;
    // Nhãn hiển thị của loại con.
    private final String label;

    // Khởi tạo lựa chọn loại con.
    public ProductSubtypeOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    // Lấy mã loại con.
    public String getCode() {
        return code;
    }

    // Lấy nhãn hiển thị.
    public String getLabel() {
        return label;
    }
}
