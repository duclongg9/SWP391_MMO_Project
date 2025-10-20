package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * View model for product detail page.
 */
public class ProductDetail {

    private final int id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer inventoryCount;
    private final String status;
    private final int shopId;
    private final String shopName;
    private final Integer shopOwnerId;

    public ProductDetail(int id, String name, String description, BigDecimal price,
            Integer inventoryCount, String status, int shopId, String shopName, Integer shopOwnerId) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
        this.description = Objects.requireNonNullElse(description, "");
        this.price = price;
        this.inventoryCount = inventoryCount;
        this.status = status;
        this.shopId = shopId;
        this.shopName = Objects.requireNonNullElse(shopName, "");
        this.shopOwnerId = shopOwnerId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public String getStatus() {
        return status;
    }

    public int getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public Integer getShopOwnerId() {
        return shopOwnerId;
    }
}
