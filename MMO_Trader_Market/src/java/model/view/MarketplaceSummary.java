package model.view;

import java.math.BigDecimal;

public class MarketplaceSummary {

    private final long availableProductCount;
    private final long pendingOrderCount;
    private final long completedOrderCount;
    private final BigDecimal totalRevenue;

    public MarketplaceSummary(long availableProductCount, long pendingOrderCount,
            long completedOrderCount, BigDecimal totalRevenue) {
        this.availableProductCount = availableProductCount;
        this.pendingOrderCount = pendingOrderCount;
        this.completedOrderCount = completedOrderCount;
        this.totalRevenue = totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
    }

    public long getAvailableProductCount() {
        return availableProductCount;
    }

    public long getPendingOrderCount() {
        return pendingOrderCount;
    }

    public long getCompletedOrderCount() {
        return completedOrderCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}

