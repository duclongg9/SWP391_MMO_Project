package model.view.product;

import units.IdObfuscator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * View model gọn nhẹ dùng chung cho trang chủ, trang duyệt và khối gợi ý sản
 * phẩm.
 * {@link service.ProductService} chuyển đổi từ
 * {@link model.product.ProductListRow} sang lớp này để cung cấp đủ thông tin
 * hiển thị (ảnh đại diện, giá min/max, nhãn loại, shop) cho nhiều JSP.
 * @author longpdhe171902
 */
public class ProductSummaryView {

    // ID sản phẩm.
    private final int id;
    // Tên sản phẩm.
    private final String name;
    // Mô tả ngắn.
    private final String shortDescription;
    // Ảnh đại diện hiển thị trong danh sách.
    private final String primaryImageUrl;
    // Mã loại sản phẩm.
    private final String productType;
    // Nhãn loại sản phẩm.
    private final String productTypeLabel;
    // Mã phân loại con.
    private final String productSubtype;
    // Nhãn phân loại con.
    private final String productSubtypeLabel;
    // Tên shop đăng bán.
    private final String shopName;
    // Giá thấp nhất.
    private final BigDecimal minPrice;
    // Giá cao nhất.
    private final BigDecimal maxPrice;
    // Tồn kho.
    private final Integer inventoryCount;
    // Đã bán.
    private final Integer soldCount;

    public ProductSummaryView(int id, String name, String shortDescription, String primaryImageUrl,
            String productType, String productTypeLabel, String productSubtype, String productSubtypeLabel,
            String shopName, BigDecimal minPrice, BigDecimal maxPrice, Integer inventoryCount, Integer soldCount) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
        this.shortDescription = Objects.requireNonNullElse(shortDescription, "");
        this.primaryImageUrl = primaryImageUrl;
        this.productType = productType;
        this.productTypeLabel = productTypeLabel;
        this.productSubtype = productSubtype;
        this.productSubtypeLabel = productSubtypeLabel;
        this.shopName = Objects.requireNonNullElse(shopName, "");
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inventoryCount = inventoryCount;
        this.soldCount = soldCount;
    }

    public int getId() {
        return id;
    }

    // Lấy tên sản phẩm.
    public String getName() {
        return name;
    }

    // Lấy mô tả ngắn.
    public String getShortDescription() {
        return shortDescription;
    }

    // Lấy ảnh đại diện.
    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    // Lấy mã loại sản phẩm.
    public String getProductType() {
        return productType;
    }

    // Lấy nhãn loại sản phẩm.
    public String getProductTypeLabel() {
        return productTypeLabel;
    }

    // Lấy mã phân loại con.
    public String getProductSubtype() {
        return productSubtype;
    }

    // Lấy nhãn phân loại con.
    public String getProductSubtypeLabel() {
        return productSubtypeLabel;
    }

    // Lấy tên shop.
    public String getShopName() {
        return shopName;
    }

    // Lấy giá thấp nhất.
    public BigDecimal getMinPrice() {
        return minPrice;
    }

    // Lấy giá cao nhất.
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    // Lấy tồn kho.
    public Integer getInventoryCount() {
        return inventoryCount;
    }

    // Lấy số lượng đã bán.
    public Integer getSoldCount() {
        return soldCount;
    }

    // Sinh token mã hóa ID để tạo đường dẫn thân thiện.
    public String getEncodedId() {
        return IdObfuscator.encode(id);
    }

    /**
     * Kiểm tra xem giá hiển thị có dạng khoảng (min khác max) để JSP quyết định
     * hiển thị "từ...".
     */
    public boolean hasPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return false;
        }
        return minPrice.compareTo(maxPrice) != 0;
    }

    /**
     * Đảm bảo cả giá min và max đã được tính toán trước khi render.
     */
    public boolean hasValidPrice() {
        return minPrice != null && maxPrice != null;
    }
}
