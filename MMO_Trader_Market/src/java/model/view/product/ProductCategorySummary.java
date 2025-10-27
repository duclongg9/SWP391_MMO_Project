package model.view.product;

/**
 * <p>
 * Tóm tắt số lượng sản phẩm theo từng loại để hiển thị ở điều hướng trang
 * chủ.</p>
 * <p>
 * Được {@link service.ProductService#getHomepageCategories()} tính toán dựa
 * trên dữ liệu DB và truyền thẳng xuống JSP giúp người dùng chọn danh mục
 * nhanh.</p>
 *
 * @author longpdhe171902
 */
public class ProductCategorySummary {

    private final String typeCode;
    private final String typeLabel;
    private final long availableProducts;

    public ProductCategorySummary(String typeCode, String typeLabel, long availableProducts) {
        this.typeCode = typeCode;
        this.typeLabel = typeLabel;
        this.availableProducts = availableProducts;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public long getAvailableProducts() {
        return availableProducts;
    }
}
