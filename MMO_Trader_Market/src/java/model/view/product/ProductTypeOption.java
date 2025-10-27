package model.view.product;

import java.util.List;

/**
 * <p>
 * Một lựa chọn loại sản phẩm bao gồm danh sách subtype con.</p>
 * <p>
 * Dữ liệu cấu trúc cây này giúp JSP render menu chọn loại sản phẩm đồng nhất
 * với rule kinh doanh.</p>
 *
 * @author longpdhe171902
 */
public class ProductTypeOption {

    // Mã loại sản phẩm cấp cao.
    private final String code;
    // Nhãn hiển thị của loại sản phẩm.
    private final String label;
    // Các loại con thuộc loại sản phẩm này.
    private final List<ProductSubtypeOption> subtypes;

    /**
     * Sao chép danh sách subtype để tránh chỉnh sửa sau khi truyền sang tầng
     * view.
     */
    public ProductTypeOption(String code, String label, List<ProductSubtypeOption> subtypes) {
        this.code = code;
        this.label = label;
        this.subtypes = List.copyOf(subtypes);
    }

    // Lấy mã loại sản phẩm.
    public String getCode() {
        return code;
    }

    // Lấy nhãn loại sản phẩm.
    public String getLabel() {
        return label;
    }

    // Lấy danh sách loại con.
    public List<ProductSubtypeOption> getSubtypes() {
        return subtypes;
    }
}
