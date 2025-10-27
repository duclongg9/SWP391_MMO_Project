package model.view.product;

import units.IdObfuscator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>
 * View model gọn nhẹ dùng chung cho trang chủ, trang duyệt và khối gợi ý sản
 * phẩm.</p>
 * <p>
 * {@link service.ProductService} chuyển đổi từ
 * {@link model.product.ProductListRow} sang lớp này để cung cấp đủ thông tin
 * hiển thị (ảnh đại diện, giá min/max, nhãn loại, shop) cho nhiều JSP.</p>
 *
 * @author longpdhe171902
 */
public class ProductSummaryView {

    private final int id;
    private final String name;
    private final String shortDescription;
    private final String primaryImageUrl;
    private final String productType;
    private final String productTypeLabel;
    private final String productSubtype;
    private final String productSubtypeLabel;
    private final String shopName;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Integer inventoryCount;
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

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductTypeLabel() {
        return productTypeLabel;
    }

    public String getProductSubtype() {
        return productSubtype;
    }

    public String getProductSubtypeLabel() {
        return productSubtypeLabel;
    }

    public String getShopName() {
        return shopName;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

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
