package model.product;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Lightweight row used for product listing page.
 */
public class ProductListRow {

    private final int id;
    private final String name;
    private final BigDecimal price;
    private final Integer inventoryCount;
    private final String status;
    private final int shopId;
    private final String shopName;

    public ProductListRow(int id, String name, BigDecimal price, Integer inventoryCount,
            String status, int shopId, String shopName) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
        this.price = price;
        this.inventoryCount = inventoryCount;
        this.status = status;
        this.shopId = shopId;
        this.shopName = Objects.requireNonNullElse(shopName, "");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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
}
