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
 * Tập hàm tiện ích xử lý dữ liệu biến thể sản phẩm được lưu dưới dạng JSON.
 */
public final class ProductVariantUtils {

    // Gson dùng chung để parse/ghi JSON biến thể.
    private static final Gson GSON = new Gson();
    // Kiểu danh sách biến thể phục vụ quá trình parse JSON.
    private static final Type VARIANT_LIST_TYPE = new TypeToken<List<ProductVariantOption>>() {
    }.getType();

    private ProductVariantUtils() {
    }

    // Chuẩn hóa mã biến thể về chữ thường, loại bỏ khoảng trắng thừa.
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

    // Kiểm tra xem sản phẩm có cấu hình biến thể hay không.
    public static boolean hasVariants(String variantSchema) {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema);
    }

    // Parse JSON biến thể thành danh sách đối tượng, đồng thời loại bỏ phần tử null.
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
                if (option != null && option.getVariantCode() != null && !option.getVariantCode().isBlank()) {
                    cleaned.add(option);
                }
            }
            return cleaned;
        } catch (JsonSyntaxException ex) {
            return Collections.emptyList();
        }
    }

    // Tìm biến thể theo mã đã chuẩn hóa.
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

    // Xác định đơn giá áp dụng: ưu tiên giá của biến thể nếu có.
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

    // Trừ tồn kho biến thể sau khi đặt hàng, kèm kiểm tra an toàn.
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

    /**
     * Tăng tồn kho cho biến thể khi hoàn trả credential về kho.
     *
     * @param variant  biến thể cần cập nhật
     * @param quantity số lượng hoàn trả
     */
    public static void increaseInventory(ProductVariantOption variant, int quantity) {
        if (variant == null || quantity <= 0) {
            return;
        }
        Integer current = variant.getInventoryCount();
        int next = (current == null ? 0 : current) + quantity;
        variant.setInventoryCount(Math.max(next, 0));
    }

    // Chuyển danh sách biến thể thành JSON để lưu trữ.
    public static String toJson(List<ProductVariantOption> variants) {
        if (variants == null || variants.isEmpty()) {
            return "[]";
        }
        return GSON.toJson(variants, VARIANT_LIST_TYPE);
    }
}
