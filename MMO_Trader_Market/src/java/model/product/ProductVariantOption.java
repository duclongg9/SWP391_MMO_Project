package model.product;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Đại diện cho từng lựa chọn biến thể (SKU con) được parse từ cột
 * {@code variants_json}.</p>
 * <p>
 * Được {@link service.ProductService} dựng từ JSON của sản phẩm và truyền xuống
 * JSP để hiển thị dropdown lựa chọn. Thông tin tồn kho/giá tùy biến theo từng
 * biến thể.</p>
 *
 * @author longpdhe171902
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

    /**
     * Trả về map thuộc tính theo thứ tự ban đầu. Nếu JSON rỗng thì trả về
     * {@link Collections#emptyMap()} để front-end không phải kiểm tra null. Khi
     * có dữ liệu, tạo bản sao {@link LinkedHashMap} nhằm khóa thứ tự hiển thị
     * trên giao diện.
     */
    public Map<String, String> getAttributes() {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Cho biết biến thể có thể bán hay không dựa trên trạng thái.
     */
    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "ProductVariantOption{"
                + "variantCode='" + variantCode + '\''
                + ", attributes=" + attributes
                + ", price=" + price
                + ", inventoryCount=" + inventoryCount
                + ", status='" + status + '\''
                + '}';
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
