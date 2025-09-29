package model;

import java.io.Serializable;

/**
 * Represents a product category displayed on the public homepage.
 */
public class ProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String name;
    private final String description;
    private final String icon;

    public ProductCategory(String code, String name, String description, String icon) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
