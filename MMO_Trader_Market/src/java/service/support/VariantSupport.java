package service.support;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import model.product.ProductVariantOption;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared helpers for parsing and working with product variant definitions.
 */
public final class VariantSupport {

    private static final Gson GSON = new Gson();
    private static final Type VARIANT_LIST_TYPE = new TypeToken<List<ProductVariantOption>>() { }.getType();

    private VariantSupport() {
    }

    public static boolean hasVariants(String variantSchema) {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema);
    }

    public static List<ProductVariantOption> parseVariants(String variantSchema, String variantsJson) {
        if (!hasVariants(variantSchema) || variantsJson == null || variantsJson.isBlank()) {
            return List.of();
        }
        try {
            List<ProductVariantOption> variants = GSON.fromJson(variantsJson, VARIANT_LIST_TYPE);
            if (variants == null) {
                return List.of();
            }
            List<ProductVariantOption> normalized = new ArrayList<>();
            for (ProductVariantOption option : variants) {
                if (option != null) {
                    normalized.add(option);
                }
            }
            return List.copyOf(normalized);
        } catch (JsonSyntaxException ex) {
            return List.of();
        }
    }

    public static Optional<ProductVariantOption> findVariant(List<ProductVariantOption> variants, String variantCode) {
        if (variants == null || variants.isEmpty() || variantCode == null) {
            return Optional.empty();
        }
        String normalized = variantCode.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        for (ProductVariantOption option : variants) {
            if (option == null || option.getVariantCode() == null) {
                continue;
            }
            if (option.getVariantCode().equalsIgnoreCase(normalized)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }
}
