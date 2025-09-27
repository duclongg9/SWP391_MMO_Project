package model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a product listing in the marketplace.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final ProductStatus status;

    public Product(int id, String name, String description, BigDecimal price, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
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

    public ProductStatus getStatus() {
        return status;
    }
}
