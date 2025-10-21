package model.view.product;

import java.util.List;

/**
 * Represents a selectable product type along with its subtypes.
 */
public class ProductTypeOption {

    private final String code;
    private final String label;
    private final List<ProductSubtypeOption> subtypes;

    public ProductTypeOption(String code, String label, List<ProductSubtypeOption> subtypes) {
        this.code = code;
        this.label = label;
        this.subtypes = List.copyOf(subtypes);
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public List<ProductSubtypeOption> getSubtypes() {
        return subtypes;
    }
}
