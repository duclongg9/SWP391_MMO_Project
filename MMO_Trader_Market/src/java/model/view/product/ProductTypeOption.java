package model.view.product;

import java.util.List;

/**
 * <p>Một lựa chọn loại sản phẩm bao gồm danh sách subtype con.</p>
 * <p>Dữ liệu cấu trúc cây này giúp JSP render menu chọn loại sản phẩm đồng nhất với rule kinh doanh.</p>
 *
 * @author longpdhe171902
 */
public class ProductTypeOption {

    private final String code;
    private final String label;
    private final List<ProductSubtypeOption> subtypes;

    /**
     * Sao chép danh sách subtype để tránh chỉnh sửa sau khi truyền sang tầng view.
     */
    public ProductTypeOption(String code, String label, List<ProductSubtypeOption> subtypes) {
        this.code = code;
        this.label = label;
        this.subtypes = List.copyOf(subtypes);
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public List<ProductSubtypeOption> getSubtypes() {
        return subtypes;
    }
}
