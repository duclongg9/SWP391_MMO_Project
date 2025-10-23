package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * View model for product detail page.
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
