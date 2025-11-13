package model.view.shop;

import units.IdObfuscator;

/**
 * Thông tin công khai của shop phục vụ trang xem sản phẩm của người mua.
 */
public class ShopPublicSummary {

    private final int id;
    private final String name;
    private final String description;
    private final long productCount;

    public ShopPublicSummary(int id, String name, String description, long productCount) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.description = description == null ? "" : description;
        this.productCount = Math.max(productCount, 0);
    }

    /**
     * @return mã shop nội bộ.
     */
    public int getId() {
        return id;
    }

    /**
     * @return tên hiển thị của shop.
     */
    public String getName() {
        return name;
    }

    /**
     * @return mô tả ngắn của shop.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return tổng số sản phẩm đang mở bán của shop.
     */
    public long getProductCount() {
        return productCount;
    }

    /**
     * Sinh token mã hóa để tạo URL thân thiện.
     *
     * @return chuỗi token đại diện cho shop
     */
    public String getEncodedId() {
        return IdObfuscator.encode(id);
    }
}
