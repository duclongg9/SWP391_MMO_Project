package model.statistics;

import java.math.BigDecimal;

/**
 * Model class cho thống kê theo sản phẩm.
 */
public class ProductStatistics {
    private int productId;
    private String productName;
    private String status;
    private BigDecimal revenue;
    private int orderCount;
    private int quantitySold;
    
    public ProductStatistics() {
    }
    
    public ProductStatistics(int productId, String productName, String status, BigDecimal revenue, int orderCount, int quantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.status = status;
        this.revenue = revenue;
        this.orderCount = orderCount;
        this.quantitySold = quantitySold;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
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
    
    public int getQuantitySold() {
        return quantitySold;
    }
    
    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

