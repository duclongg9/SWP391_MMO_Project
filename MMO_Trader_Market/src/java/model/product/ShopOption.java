package model.product;

import java.util.Objects;

/**
 * Simple option entry for shop filters.
 */
public class ShopOption {

    private final int id;
    private final String name;

    public ShopOption(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, "");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
