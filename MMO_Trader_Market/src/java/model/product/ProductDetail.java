package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>
 * View model chứa toàn bộ trường cần thiết để dựng trang chi tiết sản phẩm công
 * khai.</p>
 * <p>
 * Các giá trị trong lớp này xuất phát từ truy vấn
 * {@code ProductDAO.findDetailById} và được {@link service.ProductService}
 * chuẩn hóa để tránh null ở tầng JSP. Đây là cầu nối giữa dữ liệu trong DB và
 * HTML hiển thị.</p>
 *
 * @author longpdhe171902
 */
public class ProductDetail {

    // ID nội bộ của sản phẩm.
    private final int id;
    // Mã loại sản phẩm chính (ví dụ ACC, SERVICE...).
    private final String productType;
    // Mã phân loại con của sản phẩm.
    private final String productSubtype;
    // Tên hiển thị của sản phẩm.
    private final String name;
    // Mô tả ngắn gọn hiển thị ở phần đầu trang.
    private final String shortDescription;
    // Mô tả chi tiết đầy đủ.
    private final String description;
    // Giá niêm yết (có thể là giá tham chiếu).
    private final BigDecimal price;
    // Số lượng tồn kho hiện tại (nếu có).
    private final Integer inventoryCount;
    // Số lượng đã bán.
    private final Integer soldCount;
    // Trạng thái hiển thị của sản phẩm (ACTIVE, SOLD_OUT...).
    private final String status;
    // Ảnh đại diện chính của sản phẩm.
    private final String primaryImageUrl;
    // Dữ liệu JSON chứa danh sách ảnh trong thư viện.
    private final String galleryJson;
    // Mô tả cấu trúc thuộc tính biến thể (ví dụ: Rank, Server...).
    private final String variantSchema;
    // Danh sách biến thể và giá tương ứng dưới dạng JSON.
    private final String variantsJson;
    // ID của shop đang quản lý sản phẩm.
    private final int shopId;
    // Tên shop hiển thị cho người mua.
    private final String shopName;
    // ID chủ shop (để xác định quyền thao tác).
    private final Integer shopOwnerId;

    /**
     * Khởi tạo record chi tiết sản phẩm, đồng thời áp dụng thuật toán chuẩn hóa
     * giá trị null:
     * <ol>
     * <li>Dùng {@link java.util.Objects#requireNonNullElse(Object, Object)} để
     * thay thế null bằng giá trị mặc định, giúp front-end không phải kiểm tra
     * null nhiều lần.</li>
     * <li>Giữ nguyên các trường dạng {@link java.math.BigDecimal} hoặc
     * {@link Integer} để service có thể xác định được tình trạng tồn kho/thông
     * tin giá động.</li>
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

    // Lấy mã loại sản phẩm chính.
    public String getProductType() {
        return productType;
    }

    // Lấy mã phân loại con.
    public String getProductSubtype() {
        return productSubtype;
    }

    // Lấy tên sản phẩm.
    public String getName() {
        return name;
    }

    // Lấy mô tả ngắn.
    public String getShortDescription() {
        return shortDescription;
    }

    // Lấy mô tả chi tiết.
    public String getDescription() {
        return description;
    }

    // Lấy giá niêm yết.
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

    // Lấy trạng thái sản phẩm.
    public String getStatus() {
        return status;
    }

    // Lấy URL ảnh chính.
    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    // Lấy dữ liệu JSON của thư viện ảnh.
    public String getGalleryJson() {
        return galleryJson;
    }

    // Lấy cấu trúc biến thể.
    public String getVariantSchema() {
        return variantSchema;
    }

    // Lấy dữ liệu JSON của biến thể.
    public String getVariantsJson() {
        return variantsJson;
    }

    // Lấy ID shop sở hữu sản phẩm.
    public int getShopId() {
        return shopId;
    }

    // Lấy tên shop hiển thị.
    public String getShopName() {
        return shopName;
    }

    // Lấy ID chủ shop.
    public Integer getShopOwnerId() {
        return shopOwnerId;
    }
}
