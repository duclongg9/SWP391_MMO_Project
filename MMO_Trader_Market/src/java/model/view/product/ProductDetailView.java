package model.view.product;

import model.product.ProductVariantOption;
import units.IdObfuscator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 
 * View model đầy đủ cho trang chi tiết sản phẩm công khai.
 * 
 * Dữ liệu được {@link service.ProductService#getPublicDetail(int)} chuyển đổi
 * từ {@link model.product.ProductDetail} sang định dạng phù hợp với JSP: tách
 * gallery, map biến thể, nhãn loại sản phẩm và thông tin shop.
 *
 * @author longpdhe171902
 */
public class ProductDetailView {

    // ID sản phẩm nội bộ.
    private final int id;
    // Tên sản phẩm.
    private final String name;
    // Mô tả ngắn.
    private final String shortDescription;
    // Mô tả chi tiết.
    private final String description;
    // Ảnh đại diện chính.
    private final String primaryImageUrl;
    // Danh sách ảnh thư viện.
    private final List<String> galleryImages;
    // Mã loại sản phẩm.
    private final String productType;
    // Nhãn hiển thị của loại sản phẩm.
    private final String productTypeLabel;
    // Mã phân loại con.
    private final String productSubtype;
    // Nhãn hiển thị của phân loại con.
    private final String productSubtypeLabel;
    // Sơ đồ biến thể (NONE, JSON...)
    private final String variantSchema;
    // Danh sách biến thể cụ thể.
    private final List<ProductVariantOption> variants;
    // Giá thấp nhất trong các biến thể.
    private final BigDecimal minPrice;
    // Giá cao nhất trong các biến thể.
    private final BigDecimal maxPrice;
    // Tổng tồn kho.
    private final Integer inventoryCount;
    // Tổng số lượng đã bán.
    private final Integer soldCount;
    // Trạng thái hiển thị của sản phẩm.
    private final String status;
    // ID shop sở hữu.
    private final int shopId;
    // Tên shop.
    private final String shopName;
    // Chủ shop.
    private final Integer shopOwnerId;
    // Chuỗi JSON biến thể phục vụ client-side.
    private final String variantsJson;

    /**
     * Khởi tạo đối tượng bất biến phục vụ hiển thị:
     *
     * Sao chép danh sách hình ảnh/biến thể để tránh bị sửa đổi ngoài ý
     * muốn.
     * Lưu cả nhãn hiển thị và mã định danh của loại/nhóm sản phẩm.
     * Giữ {@code variantsJson} nhằm phục vụ các action AJAX (nếu cần).
     * 
     */
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

    // Lấy ảnh đại diện.
    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    // Lấy danh sách ảnh thư viện ở dạng bất biến.
    public List<String> getGalleryImages() {
        return Collections.unmodifiableList(galleryImages);
    }

    // Lấy mã loại sản phẩm.
    public String getProductType() {
        return productType;
    }

    // Lấy nhãn loại sản phẩm.
    public String getProductTypeLabel() {
        return productTypeLabel;
    }

    // Lấy mã phân loại con.
    public String getProductSubtype() {
        return productSubtype;
    }

    // Lấy nhãn phân loại con.
    public String getProductSubtypeLabel() {
        return productSubtypeLabel;
    }

    // Lấy schema biến thể.
    public String getVariantSchema() {
        return variantSchema;
    }

    // Lấy danh sách biến thể.
    public List<ProductVariantOption> getVariants() {
        return variants;
    }

    // Lấy giá thấp nhất.
    public BigDecimal getMinPrice() {
        return minPrice;
    }

    // Lấy giá cao nhất.
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    // Lấy tồn kho tổng.
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

    // Lấy ID shop sở hữu.
    public int getShopId() {
        return shopId;
    }

    // Lấy tên shop.
    public String getShopName() {
        return shopName;
    }

    /**
     * Lấy mã shop đã được mã hóa phục vụ định tuyến thân thiện.
     *
     * @return chuỗi mã hóa ID shop.
     */
    public String getShopEncodedId() {
        return IdObfuscator.encode(shopId);
    }

    // Lấy ID chủ shop.
    public Integer getShopOwnerId() {
        return shopOwnerId;
    }

    // Lấy dữ liệu JSON biến thể.
    public String getVariantsJson() {
        return variantsJson;
    }

    // Sinh token mã hóa ID phục vụ router thân thiện.
    public String getEncodedId() {
        return IdObfuscator.encode(id);
    }

    /**
     * Cho biết sản phẩm có cấu hình biến thể hay không dựa trên schema và danh
     * sách đã parse.
     */
    public boolean isHasVariants() {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema) && !variants.isEmpty();
    }

    // Alias hỗ trợ JSP kiểm tra nhanh.
    public boolean hasVariants() {
        return isHasVariants();
    }

    /**
     * Kiểm tra xem sản phẩm có khoảng giá (biến thể ảnh hưởng giá) hay không.
     */
    // Cho biết sản phẩm có khoảng giá khác nhau hay không.
    public boolean hasPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return false;
        }
        return minPrice.compareTo(maxPrice) != 0;
    }

    /**
     * Đánh giá trạng thái "Available" kết hợp với tồn kho tổng và tồn kho từng
     * biến thể. Thuật toán:
     * <ol>
     * <li>Loại bỏ ngay nếu trạng thái khác Available.</li>
     * <li>Nếu trường inventoryCount tổng lớn hơn 0 thì coi như còn hàng.</li>
     * <li>Nếu có biến thể, duyệt stream để tìm biến thể còn hoạt động và tồn
     * kho &gt; 0 hoặc không giới hạn.</li>
     * </ol>
     */
    public boolean isAvailable() {
        if (!"Available".equalsIgnoreCase(status)) {
            return false;
        }
        if (inventoryCount != null && inventoryCount > 0) {
            return true;
        }
        if (hasVariants()) {
            return variants.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(variant -> variant.isAvailable()
                    && (variant.getInventoryCount() == null || variant.getInventoryCount() > 0));
        }
        return false;
    }
}
