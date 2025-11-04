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

    // Mã biến thể duy nhất, lấy từ JSON.
    @SerializedName("variant_code")
    private String variantCode;

    // Tập thuộc tính mô tả biến thể (ví dụ Rank=Challenger, Region=NA).
    private Map<String, String> attributes;

    // Giá dành riêng cho biến thể này.
    private BigDecimal price;

    // Số lượng tồn kho của biến thể.
    @SerializedName("inventory_count")
    private Integer inventoryCount;

    // URL ảnh đại diện cho biến thể (nếu có).
    @SerializedName("image_url")
    private String imageUrl;

    // Trạng thái khả dụng của biến thể.
    private String status;

    // Lấy mã biến thể.
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

    // Lấy giá biến thể.
    public BigDecimal getPrice() {
        return price;
    }

    // Lấy số lượng tồn.
    public Integer getInventoryCount() {
        return inventoryCount;
    }

    // Cập nhật số lượng tồn (dùng khi đồng bộ kho).
    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    // Lấy trạng thái biến thể.
    public String getStatus() {
        return status;
    }

    // Thiết lập trạng thái biến thể.
    public void setStatus(String status) {
        this.status = status;
    }

    // Lấy URL ảnh biến thể.
    public String getImageUrl() {
        return imageUrl;
    }

    // Gán URL ảnh biến thể sau khi chuẩn hóa.
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
                + ", imageUrl='" + imageUrl + '\''
                + ", status='" + status + '\''
                + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantCode, attributes, price, inventoryCount, imageUrl, status);
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
                && Objects.equals(imageUrl, other.imageUrl)
                && Objects.equals(status, other.status);
    }
}
