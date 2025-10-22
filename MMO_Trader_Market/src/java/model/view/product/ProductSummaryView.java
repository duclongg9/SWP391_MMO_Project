package model.view.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Lightweight view model used across homepage, listing and recommendation sections.
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

    public BigDecimal getPriceMin() {
        return getMinPrice();
    }

    public BigDecimal getPriceMax() {
        return getMaxPrice();
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public boolean hasPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return false;
        }
        return minPrice.compareTo(maxPrice) != 0;
    }

    public boolean hasValidPrice() {
        return minPrice != null && maxPrice != null;
    }
}
