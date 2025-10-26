package service.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import model.Products;
import model.product.ProductVariantOption;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility helpers for working with product variants stored as JSON definitions.
 */
public final class ProductVariantUtils {

    private static final Gson GSON = new Gson();
    private static final Type VARIANT_LIST_TYPE = new TypeToken<List<ProductVariantOption>>() { }.getType();

    private ProductVariantUtils() {
    }

    public static String normalizeCode(String variantCode) {
        if (variantCode == null) {
            return null;
        }
        String trimmed = variantCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    public static boolean hasVariants(String variantSchema) {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema);
    }

    public static List<ProductVariantOption> parseVariants(String variantSchema, String variantsJson) {
        if (!hasVariants(variantSchema) || variantsJson == null || variantsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<ProductVariantOption> variants = GSON.fromJson(variantsJson, VARIANT_LIST_TYPE);
            if (variants == null || variants.isEmpty()) {
                return Collections.emptyList();
            }
            List<ProductVariantOption> cleaned = new ArrayList<>();
            for (ProductVariantOption option : variants) {
                if (option == null) {
                    continue;
                }
                String code = option.getVariantCode();
                if (code == null) {
                    continue;
                }
                String trimmed = code.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                option.setVariantCode(trimmed);
                cleaned.add(option);
            }
            return cleaned;
        } catch (JsonSyntaxException ex) {
            return Collections.emptyList();
        }
    }

    public static Optional<ProductVariantOption> findVariant(List<ProductVariantOption> variants, String normalizedCode) {
        if (normalizedCode == null || variants == null || variants.isEmpty()) {
            return Optional.empty();
        }
        return variants.stream()
                .filter(Objects::nonNull)
                .filter(variant -> {
                    String code = variant.getVariantCode();
                    if (code == null) {
                        return false;
                    }
                    return normalizedCode.equalsIgnoreCase(code.trim());
                })
                .findFirst();
    }

    public static BigDecimal resolveUnitPrice(Products product, Optional<ProductVariantOption> variantOpt) {
        if (variantOpt.isPresent()) {
            BigDecimal price = variantOpt.get().getPrice();
            if (price == null) {
                throw new IllegalArgumentException("Không thể xác định giá sản phẩm.");
            }
            return price;
        }
        BigDecimal basePrice = product.getPrice();
        if (basePrice == null) {
            throw new IllegalArgumentException("Không thể xác định giá sản phẩm.");
        }
        return basePrice;
    }

    public static void decreaseInventory(ProductVariantOption variant, int quantity) {
        if (variant == null || quantity <= 0) {
            return;
        }
        Integer current = variant.getInventoryCount();
        if (current == null) {
            throw new IllegalStateException("Biến thể sản phẩm không đủ tồn kho.");
        }
        if (current < quantity) {
            throw new IllegalStateException("Biến thể sản phẩm không đủ tồn kho.");
        }
        int remaining = current - quantity;
        variant.setInventoryCount(Math.max(remaining, 0));
    }

    public static String toJson(List<ProductVariantOption> variants) {
        if (variants == null || variants.isEmpty()) {
            return "[]";
        }
        return GSON.toJson(variants, VARIANT_LIST_TYPE);
    }
}

