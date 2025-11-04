package model.view.product;

/**
 * 
 * Tóm tắt số lượng sản phẩm theo từng loại để hiển thị ở điều hướng trang
 * chủ.
 * 
 * Được {@link service.ProductService#getHomepageCategories()} tính toán dựa
 * trên dữ liệu DB và truyền thẳng xuống JSP giúp người dùng chọn danh mục
 * nhanh.
 *
 * @author longpdhe171902
 */
public class ProductCategorySummary {

    // Mã loại sản phẩm (dùng trong URL, filter).
    private final String typeCode;
    // Tên hiển thị của loại sản phẩm.
    private final String typeLabel;
    // Số lượng sản phẩm đang mở bán thuộc loại này.
    private final long availableProducts;

    // Khởi tạo bản tóm tắt danh mục.
    public ProductCategorySummary(String typeCode, String typeLabel, long availableProducts) {
        this.typeCode = typeCode;
        this.typeLabel = typeLabel;
        this.availableProducts = availableProducts;
    }

    // Lấy mã loại sản phẩm.
    public String getTypeCode() {
        return typeCode;
    }

    // Lấy nhãn hiển thị.
    public String getTypeLabel() {
        return typeLabel;
    }

    // Lấy tổng sản phẩm khả dụng.
    public long getAvailableProducts() {
        return availableProducts;
    }
}
