package model;

import java.math.BigDecimal;
import java.util.Date;

public class Products {

    private Integer id;
    private Integer shopId;
    private String name;
    private BigDecimal price;
    private Integer inventoryCount;
    private String status;
    private Boolean featured;
    private Date createdAt;
    private Date updatedAt;
    private String description;

    public Products() {}  // no-args constructor cần cho JDBC/MBG

    public Products(Integer id, Integer shopId, String name, BigDecimal price,
                    Integer inventoryCount, String status, Date createdAt,
                    Date updatedAt, String description) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.price = price;
        this.inventoryCount = inventoryCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = description;
        this.featured = null;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(Integer inventoryCount) { this.inventoryCount = inventoryCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Products{" +
                "id=" + id +
                ", shopId=" + shopId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", inventoryCount=" + inventoryCount +
                ", status='" + status + '\'' +
                ", featured=" + featured +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", description='" + description + '\'' +
                '}';
    }
}
