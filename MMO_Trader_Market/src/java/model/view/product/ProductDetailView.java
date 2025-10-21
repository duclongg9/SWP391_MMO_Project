package model.view.product;

import model.product.ProductVariantOption;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive product detail view model for the public product detail page.
 */
public class ProductDetailView {

    private final int id;
    private final String name;
    private final String shortDescription;
    private final String description;
    private final String primaryImageUrl;
    private final List<String> galleryImages;
    private final String productType;
    private final String productTypeLabel;
    private final String productSubtype;
    private final String productSubtypeLabel;
    private final String variantSchema;
    private final List<ProductVariantOption> variants;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Integer inventoryCount;
    private final Integer soldCount;
    private final String status;
    private final int shopId;
    private final String shopName;
    private final Integer shopOwnerId;
    private final String variantsJson;

    public ProductDetailView(int id, String name, String shortDescription, String description,
            String primaryImageUrl, List<String> galleryImages, String productType, String productTypeLabel,
            String productSubtype, String productSubtypeLabel, String variantSchema,
            List<ProductVariantOption> variants, BigDecimal minPrice, BigDecimal maxPrice,
            Integer inventoryCount, Integer soldCount, String status, int shopId, String shopName,
            Integer shopOwnerId, String variantsJson) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.primaryImageUrl = primaryImageUrl;
        this.galleryImages = galleryImages == null ? List.of() : List.copyOf(galleryImages);
        this.productType = productType;
        this.productTypeLabel = productTypeLabel;
        this.productSubtype = productSubtype;
        this.productSubtypeLabel = productSubtypeLabel;
        this.variantSchema = variantSchema;
        this.variants = variants == null ? List.of() : List.copyOf(variants);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inventoryCount = inventoryCount;
        this.soldCount = soldCount;
        this.status = status;
        this.shopId = shopId;
        this.shopName = shopName;
        this.shopOwnerId = shopOwnerId;
        this.variantsJson = variantsJson;
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

    public String getDescription() {
        return description;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public List<String> getGalleryImages() {
        return Collections.unmodifiableList(galleryImages);
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

    public String getVariantSchema() {
        return variantSchema;
    }

    public List<ProductVariantOption> getVariants() {
        return variants;
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

    public String getStatus() {
        return status;
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

    public String getVariantsJson() {
        return variantsJson;
    }

    public boolean hasVariants() {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema) && !variants.isEmpty();
    }

    public boolean hasPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return false;
        }
        return minPrice.compareTo(maxPrice) != 0;
    }

    public boolean isAvailable() {
        if (!"Available".equalsIgnoreCase(status)) {
            return false;
        }
        return inventoryCount != null && inventoryCount > 0;
    }
}
