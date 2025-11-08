package model.view;

import java.util.Date;

/**
 * View-model thể hiện một shop trong danh sách quản lý của seller.
 * <p>
 * Gồm các thông tin mô tả cơ bản, trạng thái, thống kê sản phẩm và dấu thời
 * gian tạo/cập nhật giúp JSP render bảng quản lý.
 */
public class ShopListItem {

    private long id;
    private String name;
    private String description;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private int productCount;
    private long totalSold;
    private long totalStock;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(long totalSold) {
        this.totalSold = totalSold;
    }

    public long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(long totalStock) {
        this.totalStock = totalStock;
    }
}
