package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>View model chứa toàn bộ trường cần thiết để dựng trang chi tiết sản phẩm công khai.</p>
 * <p>Các giá trị trong lớp này xuất phát từ truy vấn {@code ProductDAO.findDetailById} và được
 * {@link service.ProductService} chuẩn hóa để tránh null ở tầng JSP. Đây là cầu nối giữa dữ liệu
 * trong DB và HTML hiển thị.</p>
 *
 * @author longpdhe171902
 */
public class ProductDetail {

    private final int id;
    private final String productType;
    private final String productSubtype;
    private final String name;
    private final String shortDescription;
    private final String description;
    private final BigDecimal price;
    private final Integer inventoryCount;
    private final Integer soldCount;
    private final String status;
    private final String primaryImageUrl;
    private final String galleryJson;
    private final String variantSchema;
    private final String variantsJson;
    private final int shopId;
    private final String shopName;
    private final Integer shopOwnerId;

    /**
     * Khởi tạo record chi tiết sản phẩm, đồng thời áp dụng thuật toán chuẩn hóa giá trị null:
     * <ol>
     *     <li>Dùng {@link java.util.Objects#requireNonNullElse(Object, Object)} để thay thế null bằng
     *     giá trị mặc định, giúp front-end không phải kiểm tra null nhiều lần.</li>
     *     <li>Giữ nguyên các trường dạng {@link java.math.BigDecimal} hoặc {@link Integer} để service
     *     có thể xác định được tình trạng tồn kho/thông tin giá động.</li>
     * </ol>
     */
    public ProductDetail(int id, String productType, String productSubtype, String name,
            String shortDescription, String description, BigDecimal price,
            Integer inventoryCount, Integer soldCount, String status,
            String primaryImageUrl, String galleryJson, String variantSchema,
            String variantsJson, int shopId, String shopName, Integer shopOwnerId) {
        this.id = id;
        this.productType = Objects.requireNonNullElse(productType, "OTHER");
        this.productSubtype = Objects.requireNonNullElse(productSubtype, "OTHER");
        this.name = Objects.requireNonNullElse(name, "");
        this.shortDescription = Objects.requireNonNullElse(shortDescription, "");
        this.description = Objects.requireNonNullElse(description, "");
        this.price = price;
        this.inventoryCount = inventoryCount;
        this.soldCount = soldCount;
        this.status = status;
        this.primaryImageUrl = primaryImageUrl;
        this.galleryJson = galleryJson;
        this.variantSchema = Objects.requireNonNullElse(variantSchema, "NONE");
        this.variantsJson = variantsJson;
        this.shopId = shopId;
        this.shopName = Objects.requireNonNullElse(shopName, "");
        this.shopOwnerId = shopOwnerId;
    }

    public int getId() {
        return id;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductSubtype() {
        return productSubtype;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public String getStatus() {
        return status;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public String getGalleryJson() {
        return galleryJson;
    }

    public String getVariantSchema() {
        return variantSchema;
    }

    public String getVariantsJson() {
        return variantsJson;
    }

    public int getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public Integer getShopOwnerId() {
        return shopOwnerId;
    }
}
