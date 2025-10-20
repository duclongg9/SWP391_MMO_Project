package model.view;

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

