package model.statistics;

import java.math.BigDecimal;

/**
 * View-model mô tả sản phẩm bán chạy nhất của một shop.
 */
public class BestSellerProduct {

    private final int productId;

    private final String productName;

    private final String productImageUrl;

    private final int totalSold;

    private final BigDecimal totalRevenue;

    /**
     * Tạo bản ghi thống kê sản phẩm bán chạy.
     *
     * @param productId      mã sản phẩm
     * @param productName    tên sản phẩm
     * @param productImageUrl ảnh đại diện (có thể null)
     * @param totalSold      tổng số lượng đã bán
     * @param totalRevenue   tổng doanh thu mang lại
     */
    public BestSellerProduct(int productId, String productName, String productImageUrl,
            int totalSold, BigDecimal totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}

