package model.statistics;

import java.math.BigDecimal;

/**
 * Model class cho thống kê theo thời gian.
 */
public class TimeStatistics {
    private String periodLabel;
    private BigDecimal revenue;
    private int orderCount;
    
    public TimeStatistics() {
    }
    
    public TimeStatistics(String periodLabel, BigDecimal revenue, int orderCount) {
        this.periodLabel = periodLabel;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }
    
    public String getPeriodLabel() {
        return periodLabel;
    }
    
    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }
    
    public BigDecimal getRevenue() {
        return revenue;
    }
    
    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
    
    public int getOrderCount() {
        return orderCount;
    }
    
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}

