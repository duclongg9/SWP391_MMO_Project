package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>
 * Model hàng rút gọn cho danh sách sản phẩm trong trang browse/checkout.</p>
 * <p>
 * DAO trả về lớp này để {@link service.ProductService} có đủ thông tin render
 * danh sách, đồng thời giúp JSP truy cập trực tiếp các trường cần hiển thị
 * (tên, giá, tồn kho, trạng thái).</p>
 *
 * @author longpdhe171902
 */
public class ProductListRow {

    // ID nội bộ của sản phẩm.
    private final int id;
    // Mã loại sản phẩm.
    private final String productType;
    // Mã phân loại con.
    private final String productSubtype;
    // Tên hiển thị trên danh sách.
    private final String name;
    // Mô tả ngắn gọn trên thẻ sản phẩm.
    private final String shortDescription;
    // Giá hiển thị ở danh sách.
    private final BigDecimal price;
    // Tồn kho còn lại.
    private final Integer inventoryCount;
    // Số lượng đã bán.
    private final Integer soldCount;
    // Trạng thái đăng bán.
    private final String status;
    // URL ảnh đại diện.
    private final String primaryImageUrl;
    // Sơ đồ thuộc tính biến thể.
    private final String variantSchema;
    // Dữ liệu JSON mô tả các biến thể.
    private final String variantsJson;
    // ID shop sở hữu.
    private final int shopId;
    // Tên shop hiển thị.
    private final String shopName;

    /**
     * Chuẩn hóa các giá trị văn bản để tránh {@code null} khi render và giữ
     * nguyên số liệu định lượng (giá, tồn kho) phục vụ tính toán trong service.
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

    // Lấy mã loại sản phẩm.
    public String getProductType() {
        return productType;
    }

    // Lấy mã phân loại con.
    public String getProductSubtype() {
        return productSubtype;
    }

    // Lấy tên hiển thị.
    public String getName() {
        return name;
    }

    // Lấy mô tả ngắn.
    public String getShortDescription() {
        return shortDescription;
    }

    // Lấy giá sản phẩm.
    public BigDecimal getPrice() {
        return price;
    }

    // Lấy số lượng tồn kho.
    public Integer getInventoryCount() {
        return inventoryCount;
    }

    // Lấy số lượng đã bán.
    public Integer getSoldCount() {
        return soldCount;
    }

    // Lấy trạng thái.
    public String getStatus() {
        return status;
    }

    // Lấy ảnh đại diện.
    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    // Lấy sơ đồ biến thể.
    public String getVariantSchema() {
        return variantSchema;
    }

    // Lấy JSON biến thể.
    public String getVariantsJson() {
        return variantsJson;
    }

    // Lấy ID shop.
    public int getShopId() {
        return shopId;
    }

    // Lấy tên shop.
    public String getShopName() {
        return shopName;
    }
}
