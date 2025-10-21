package model.view.product;

/**
 * Summary information about a product type category for homepage navigation.
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
