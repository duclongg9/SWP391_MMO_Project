package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>Model hàng rút gọn cho danh sách sản phẩm trong trang browse/checkout.</p>
 * <p>DAO trả về lớp này để {@link service.ProductService} có đủ thông tin render danh sách, đồng thời
 * giúp JSP truy cập trực tiếp các trường cần hiển thị (tên, giá, tồn kho, trạng thái).</p>
 *
 * @author longpdhe171902
 */
public class ProductListRow {

    private final int id;
    private final String productType;
    private final String productSubtype;
    private final String name;
    private final String shortDescription;
    private final BigDecimal price;
    private final Integer inventoryCount;
    private final Integer soldCount;
    private final String status;
    private final String primaryImageUrl;
    private final String variantSchema;
    private final String variantsJson;
    private final int shopId;
    private final String shopName;

    /**
     * Chuẩn hóa các giá trị văn bản để tránh {@code null} khi render và giữ nguyên số liệu định lượng
     * (giá, tồn kho) phục vụ tính toán trong service.
     */
    public ProductListRow(int id, String productType, String productSubtype, String name,
            String shortDescription, BigDecimal price, Integer inventoryCount,
            Integer soldCount, String status, String primaryImageUrl,
            String variantSchema, String variantsJson, int shopId, String shopName) {
        this.id = id;
        this.productType = Objects.requireNonNullElse(productType, "OTHER");
        this.productSubtype = Objects.requireNonNullElse(productSubtype, "OTHER");
        this.name = Objects.requireNonNullElse(name, "");
        this.shortDescription = Objects.requireNonNullElse(shortDescription, "");
        this.price = price;
        this.inventoryCount = inventoryCount;
        this.soldCount = soldCount;
        this.status = status;
        this.primaryImageUrl = primaryImageUrl;
        this.variantSchema = variantSchema;
        this.variantsJson = variantsJson;
        this.shopId = shopId;
        this.shopName = Objects.requireNonNullElse(shopName, "");
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
}
