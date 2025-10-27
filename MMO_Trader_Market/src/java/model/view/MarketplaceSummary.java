package model.view;

/**
 * <p>
 * View model thống kê tổng quan marketplace hiển thị ở đầu trang chủ.</p>
 * <p>
 * Số liệu được {@link service.HomepageService#loadMarketplaceSummary()} tổng
 * hợp từ nhiều DAO và đẩy thẳng xuống JSP để giúp người dùng hiểu quy mô hệ
 * thống.</p>
 *
 * @author longpdhe171902
 */
public class MarketplaceSummary {

    private final long totalCompletedOrders;
    private final long activeShopCount;
    private final long activeBuyerCount;

    public MarketplaceSummary(long totalCompletedOrders, long activeShopCount, long activeBuyerCount) {
        this.totalCompletedOrders = totalCompletedOrders;
        this.activeShopCount = activeShopCount;
        this.activeBuyerCount = activeBuyerCount;
    }

    public long getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public long getActiveShopCount() {
        return activeShopCount;
    }

    public long getActiveBuyerCount() {
        return activeBuyerCount;
    }
}
