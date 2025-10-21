package model.view.product;

/**
 * Represents a selectable product subtype for filtering.
 */
public class ProductSubtypeOption {

    private final String code;
    private final String label;

    public ProductSubtypeOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
