package model.view.product;

/**
 * <p>Một lựa chọn loại con (subtype) trong bộ lọc sản phẩm.</p>
 * <p>Kết hợp cùng {@link ProductTypeOption} để hiển thị cấu trúc danh mục hai tầng ở trang browse.</p>
 *
 * @author longpdhe171902
 */
public class ProductSubtypeOption {

    private final String code;
    private final String label;

    public ProductSubtypeOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
