package model.product;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a purchasable variant entry parsed from the variants_json column.
 */
public class ProductVariantOption {

    @SerializedName("variant_code")
    private String variantCode;

    private Map<String, String> attributes;

    private BigDecimal price;

    @SerializedName("inventory_count")
    private Integer inventoryCount;

    private String status;

    public String getVariantCode() {
        return variantCode;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyMap();
        }
//        if (attributes instanceof LinkedHashMap<String, String> linkedHashMap) {
//            return Collections.unmodifiableMap(linkedHashMap);
//        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "ProductVariantOption{" +
                "variantCode='" + variantCode + '\'' +
                ", attributes=" + attributes +
                ", price=" + price +
                ", inventoryCount=" + inventoryCount +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantCode, attributes, price, inventoryCount, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProductVariantOption other = (ProductVariantOption) obj;
        return Objects.equals(variantCode, other.variantCode)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(price, other.price)
                && Objects.equals(inventoryCount, other.inventoryCount)
                && Objects.equals(status, other.status);
    }
}
