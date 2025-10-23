package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dao.product.ProductDAO;
import model.PaginatedResult;
import model.Products;
import model.product.PagedResult;
import model.product.ProductDetail;
import model.product.ProductListRow;
import model.product.ProductVariantOption;
import model.view.product.ProductCategorySummary;
import model.view.product.ProductDetailView;
import model.view.product.ProductSubtypeOption;
import model.view.product.ProductSummaryView;
import model.view.product.ProductTypeOption;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private static final int DEFAULT_HOMEPAGE_LIMIT = 8;
    private static final int DEFAULT_SIMILAR_LIMIT = 4;

    private static final Map<String, String> TYPE_LABELS;
    private static final Map<String, String> SUBTYPE_LABELS;
    private static final List<ProductTypeOption> TYPE_OPTIONS;

    static {
        Map<String, String> typeLabels = new LinkedHashMap<>();
        typeLabels.put("EMAIL", "Tài khoản Mail");
        typeLabels.put("SOCIAL", "Tài khoản MXH");
        typeLabels.put("SOFTWARE", "Tài khoản phần mềm");
        typeLabels.put("GAME", "Tài khoản Game");
        typeLabels.put("OTHER", "Khác");
        TYPE_LABELS = Collections.unmodifiableMap(typeLabels);

        Map<String, String> subtypeLabels = new LinkedHashMap<>();
        subtypeLabels.put("GMAIL", "Gmail");
        subtypeLabels.put("FACEBOOK", "Facebook");
        subtypeLabels.put("TIKTOK", "TikTok");
        subtypeLabels.put("CANVA", "Canva");
        subtypeLabels.put("VALORANT", "Valorant");
        subtypeLabels.put("OTHER", "Khác");
        SUBTYPE_LABELS = Collections.unmodifiableMap(subtypeLabels);

        List<ProductTypeOption> options = new ArrayList<>();
        options.add(new ProductTypeOption("EMAIL", TYPE_LABELS.get("EMAIL"),
                List.of(new ProductSubtypeOption("GMAIL", SUBTYPE_LABELS.get("GMAIL")))));
        options.add(new ProductTypeOption("SOCIAL", TYPE_LABELS.get("SOCIAL"),
                List.of(
                        new ProductSubtypeOption("FACEBOOK", SUBTYPE_LABELS.get("FACEBOOK")),
                        new ProductSubtypeOption("TIKTOK", SUBTYPE_LABELS.get("TIKTOK"))
                )));
        options.add(new ProductTypeOption("SOFTWARE", TYPE_LABELS.get("SOFTWARE"),
                List.of(new ProductSubtypeOption("CANVA", SUBTYPE_LABELS.get("CANVA")))));
        options.add(new ProductTypeOption("GAME", TYPE_LABELS.get("GAME"),
                List.of(new ProductSubtypeOption("VALORANT", SUBTYPE_LABELS.get("VALORANT")))));
        options.add(new ProductTypeOption("OTHER", TYPE_LABELS.get("OTHER"),
                List.of(new ProductSubtypeOption("OTHER", SUBTYPE_LABELS.get("OTHER")))));
        TYPE_OPTIONS = List.copyOf(options);
    }

    private final ProductDAO productDAO = new ProductDAO();
    private final Gson gson = new Gson();
    private final Type stringListType = new TypeToken<List<String>>() { }.getType();
    private final Type variantListType = new TypeToken<List<ProductVariantOption>>() { }.getType();

    public List<ProductSummaryView> getHomepageHighlights() {
        List<ProductListRow> rows = productDAO.findTopAvailable(DEFAULT_HOMEPAGE_LIMIT);
        return rows.stream().map(this::toSummaryView).toList();
    }

    public List<ProductCategorySummary> getHomepageCategories() {
        Map<String, Long> counts = productDAO.countAvailableByType();
        List<ProductCategorySummary> summaries = new ArrayList<>();
        for (ProductTypeOption option : TYPE_OPTIONS) {
            long total = counts.getOrDefault(option.getCode(), 0L);
            summaries.add(new ProductCategorySummary(option.getCode(), option.getLabel(), total));
        }
        return summaries;
    }

    public PagedResult<ProductSummaryView> searchPublicProducts(String productType, String productSubtype,
            String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        String normalizedKeyword = normalize(keyword);
        String normalizedType = normalizeFilter(productType);
        String normalizedSubtype = normalizeSubtype(normalizedType, productSubtype);

        long totalItems = productDAO.countAvailable(normalizedKeyword, normalizedType, normalizedSubtype);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;

        List<ProductSummaryView> items = totalItems == 0
                ? List.of()
                : productDAO.findAvailablePaged(normalizedKeyword, normalizedType, normalizedSubtype, safeSize, offset)
                .stream()
                .map(this::toSummaryView)
                .toList();

        return new PagedResult<>(items, currentPage, safeSize, totalPages, totalItems);
    }

    public ProductDetailView getPublicDetail(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }
        ProductDetail detail = productDAO.findDetailById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        if (!"Available".equalsIgnoreCase(detail.getStatus())) {
            throw new IllegalArgumentException("Sản phẩm không khả dụng");
        }
        List<ProductVariantOption> variants = parseVariants(detail.getVariantSchema(), detail.getVariantsJson());
        PriceRange priceRange = determinePriceRange(detail.getPrice(), detail.getVariantSchema(), variants);
        List<String> gallery = parseGallery(detail.getGalleryJson(), detail.getPrimaryImageUrl());
        return new ProductDetailView(
                detail.getId(),
                detail.getName(),
                detail.getShortDescription(),
                detail.getDescription(),
                detail.getPrimaryImageUrl(),
                gallery,
                detail.getProductType(),
                resolveTypeLabel(detail.getProductType()),
                detail.getProductSubtype(),
                resolveSubtypeLabel(detail.getProductSubtype()),
                detail.getVariantSchema(),
                variants,
                priceRange.min(),
                priceRange.max(),
                detail.getInventoryCount(),
                detail.getSoldCount(),
                detail.getStatus(),
                detail.getShopId(),
                detail.getShopName(),
                detail.getShopOwnerId(),
                detail.getVariantsJson()
        );
    }

    public List<ProductSummaryView> findSimilarProducts(String productType, int excludeProductId) {
        String normalizedType = normalizeFilter(productType);
        if (normalizedType == null) {
            return List.of();
        }
        return productDAO.findSimilarByType(normalizedType, excludeProductId, DEFAULT_SIMILAR_LIMIT)
                .stream()
                .map(this::toSummaryView)
                .toList();
    }

    public List<ProductTypeOption> getTypeOptions() {
        return TYPE_OPTIONS;
    }

    public List<ProductSubtypeOption> getSubtypeOptions(String productType) {
        String normalizedType = normalizeFilter(productType);
        return TYPE_OPTIONS.stream()
                .filter(option -> Objects.equals(option.getCode(), normalizedType))
                .findFirst()
                .map(ProductTypeOption::getSubtypes)
                .orElse(List.of());
    }

    public List<Products> homepageHighlights() {
        return productDAO.findHighlighted(DEFAULT_HOMEPAGE_LIMIT);
    }

    public Products detail(int id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xoá"));
    }

    public Optional<Products> findOptionalById(int id) {
        return productDAO.findById(id);
    }

    public PaginatedResult<Products> search(String keyword, int page, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Số lượng mỗi trang phải lớn hơn 0.");
        }
        if (page < 1) {
            throw new IllegalArgumentException("Số trang phải lớn hơn hoặc bằng 1.");
        }

        String normalizedKeyword = keyword == null ? null : keyword.trim();
        int totalItems = productDAO.countByKeyword(normalizedKeyword);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(page, totalPages);
        int offset = (currentPage - 1) * pageSize;

        List<Products> items = totalItems == 0
                ? List.of()
                : productDAO.search(normalizedKeyword, pageSize, offset);

        return new PaginatedResult<>(items, currentPage, totalPages, pageSize, totalItems);
    }

    private ProductSummaryView toSummaryView(ProductListRow row) {
        List<ProductVariantOption> variants = parseVariants(row.getVariantSchema(), row.getVariantsJson());
        PriceRange priceRange = determinePriceRange(row.getPrice(), row.getVariantSchema(), variants);
        return new ProductSummaryView(
                row.getId(),
                row.getName(),
                row.getShortDescription(),
                row.getPrimaryImageUrl(),
                row.getProductType(),
                resolveTypeLabel(row.getProductType()),
                row.getProductSubtype(),
                resolveSubtypeLabel(row.getProductSubtype()),
                row.getShopName(),
                priceRange.min(),
                priceRange.max(),
                row.getInventoryCount(),
                row.getSoldCount()
        );
    }

    private List<ProductVariantOption> parseVariants(String variantSchema, String variantsJson) {
        if (!hasVariants(variantSchema) || variantsJson == null || variantsJson.isBlank()) {
            return List.of();
        }
        try {
            List<ProductVariantOption> variants = gson.fromJson(variantsJson, variantListType);
            if (variants == null) {
                return List.of();
            }
            List<ProductVariantOption> normalized = new ArrayList<>();
            for (ProductVariantOption option : variants) {
                if (option == null) {
                    continue;
                }
                normalized.add(option);
            }
            return List.copyOf(normalized);
        } catch (JsonSyntaxException ex) {
            return List.of();
        }
    }

    private List<String> parseGallery(String galleryJson, String fallback) {
        if (galleryJson == null || galleryJson.isBlank()) {
            return fallback == null || fallback.isBlank() ? List.of() : List.of(fallback);
        }
        try {
            List<String> images = gson.fromJson(galleryJson, stringListType);
            if (images == null || images.isEmpty()) {
                return fallback == null || fallback.isBlank() ? List.of() : List.of(fallback);
            }
            List<String> filtered = new ArrayList<>();
            for (String image : images) {
                if (image != null && !image.isBlank()) {
                    filtered.add(image);
                }
            }
            if (filtered.isEmpty() && fallback != null && !fallback.isBlank()) {
                filtered.add(fallback);
            }
            return List.copyOf(filtered);
        } catch (JsonSyntaxException ex) {
            return fallback == null || fallback.isBlank() ? List.of() : List.of(fallback);
        }
    }

    private PriceRange determinePriceRange(BigDecimal basePrice, String variantSchema, List<ProductVariantOption> variants) {
        if (!hasVariants(variantSchema) || variants.isEmpty()) {
            BigDecimal price = basePrice == null ? BigDecimal.ZERO : basePrice;
            return new PriceRange(price, price);
        }
        BigDecimal min = null;
        BigDecimal max = null;
        for (ProductVariantOption variant : variants) {
            if (variant == null || !variant.isAvailable()) {
                continue;
            }
            BigDecimal price = variant.getPrice();
            if (price == null) {
                continue;
            }
            min = min == null || price.compareTo(min) < 0 ? price : min;
            max = max == null || price.compareTo(max) > 0 ? price : max;
        }
        if (min == null || max == null) {
            BigDecimal price = basePrice == null ? BigDecimal.ZERO : basePrice;
            return new PriceRange(price, price);
        }
        return new PriceRange(min, max);
    }

    private String resolveTypeLabel(String type) {
        if (type == null) {
            return TYPE_LABELS.get("OTHER");
        }
        return TYPE_LABELS.getOrDefault(type.toUpperCase(Locale.ROOT), TYPE_LABELS.get("OTHER"));
    }

    private String resolveSubtypeLabel(String subtype) {
        if (subtype == null) {
            return SUBTYPE_LABELS.get("OTHER");
        }
        return SUBTYPE_LABELS.getOrDefault(subtype.toUpperCase(Locale.ROOT), SUBTYPE_LABELS.get("OTHER"));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeFilter(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return TYPE_LABELS.containsKey(upper) ? upper : null;
    }

    private String normalizeSubtype(String type, String subtype) {
        String normalizedSubtype = normalize(subtype);
        if (normalizedSubtype == null) {
            return null;
        }
        String upper = normalizedSubtype.toUpperCase(Locale.ROOT);
        boolean exists = TYPE_OPTIONS.stream()
                .filter(option -> type == null || option.getCode().equals(type))
                .flatMap(option -> option.getSubtypes().stream())
                .anyMatch(option -> option.getCode().equals(upper));
        return exists ? upper : null;
    }

    private boolean hasVariants(String variantSchema) {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema);
    }

    private record PriceRange(BigDecimal min, BigDecimal max) {
    }
}
