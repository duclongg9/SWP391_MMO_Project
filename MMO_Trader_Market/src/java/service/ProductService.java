package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dao.order.CredentialDAO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <p>Dịch vụ nghiệp vụ cho toàn bộ luồng hiển thị sản phẩm: trang chủ, trang duyệt, trang chi tiết.</p>
 * <p>Lớp này nhận dữ liệu thô từ {@link dao.product.ProductDAO} và {@link dao.order.CredentialDAO}, chuẩn hóa,
 * ghép nhãn tiếng Việt, parse JSON biến thể bằng Gson rồi chuyển thành các view model sử dụng trực tiếp ở JSP.</p>
 * <p>Các phương thức chính mô tả rõ quy trình lọc theo danh mục, phân trang, xác định khoảng giá, kiểm tra tồn kho
 * và tính toán số lượng credential còn bàn giao được.</p>
 *
 * @author longpdhe171902
 */
public class ProductService {

    private static final int DEFAULT_HOMEPAGE_LIMIT = 6;
    private static final int DEFAULT_SIMILAR_LIMIT = 4;
    private static final String PRODUCT_IMAGE_BASE_PATH = "/assets/images/products/";

    private static final Map<String, String> TYPE_LABELS;
    private static final Map<String, String> SUBTYPE_LABELS;
    private static final List<ProductTypeOption> TYPE_OPTIONS;

    static {
        // Khởi tạo bảng ánh xạ mã loại -> nhãn hiển thị để sử dụng xuyên suốt các view.
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
    private final CredentialDAO credentialDAO = new CredentialDAO();
    private final Gson gson = new Gson();
    private final Type stringListType = new TypeToken<List<String>>() { }.getType();
    private final Type variantListType = new TypeToken<List<ProductVariantOption>>() { }.getType();

    /**
     * Lấy danh sách sản phẩm nổi bật hiển thị tại trang chủ.
     * DAO trả về {@link model.product.ProductListRow}, sau đó convert thành {@link ProductSummaryView}.
     */
    public List<ProductSummaryView> getHomepageHighlights() {
        List<ProductListRow> rows = productDAO.findTopAvailable(DEFAULT_HOMEPAGE_LIMIT);
        return rows.stream().map(this::toSummaryView).toList();
    }

    /**
     * Tính toán số lượng sản phẩm theo loại để render menu danh mục.
     * Quy trình: gọi {@link dao.product.ProductDAO#countAvailableByType()}, ghép nhãn hiển thị rồi
     * truyền xuống view.
     */
    public List<ProductCategorySummary> getHomepageCategories() {
        Map<String, Long> counts = productDAO.countAvailableByType();
        List<ProductCategorySummary> summaries = new ArrayList<>();
        for (ProductTypeOption option : TYPE_OPTIONS) {
            long total = counts.getOrDefault(option.getCode(), 0L);
            summaries.add(new ProductCategorySummary(option.getCode(), option.getLabel(), total));
        }
        return summaries;
    }

    /**
     * Phân trang sản phẩm theo loại, subtype và từ khóa.
     * <ol>
     *     <li>Chuẩn hóa input (mã loại, danh sách subtype, keyword) để tránh SQL injection và dữ liệu bẩn.</li>
     *     <li>Đếm tổng số bản ghi thỏa điều kiện để tính tổng trang.</li>
     *     <li>Query danh sách sản phẩm khả dụng và ánh xạ sang {@link ProductSummaryView}.</li>
     * </ol>
     */
    public PagedResult<ProductSummaryView> browseByType(String productType, List<String> productSubtypes,
            String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        String normalizedType = normalizeFilter(productType);
        if (normalizedType == null) {
            throw new IllegalArgumentException("Loại sản phẩm không hợp lệ");
        }
        List<String> normalizedSubtypes = normalizeSubtypeList(normalizedType, productSubtypes);
        String normalizedKeyword = normalize(keyword);

        long totalItems = productDAO.countAvailableByType(normalizedType, normalizedSubtypes, normalizedKeyword);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;

        List<ProductSummaryView> items = totalItems == 0
                ? List.of()
                : productDAO.findAvailableByType(normalizedType, normalizedSubtypes, normalizedKeyword, safeSize, offset)
                .stream()
                .map(this::toSummaryView)
                .toList();

        return new PagedResult<>(items, currentPage, safeSize, totalPages, totalItems);
    }

    /**
     * Lấy thông tin chi tiết sản phẩm cho trang công khai.
     * Thuật toán:
     * <ol>
     *     <li>Xác thực {@code productId} và trạng thái {@code Available}.</li>
     *     <li>Parse JSON biến thể/gallery bằng Gson, chuyển đổi đường dẫn ảnh về base path chuẩn.</li>
     *     <li>Tính giá min/max, ghép nhãn loại sản phẩm, tổng hợp dữ liệu shop.</li>
     * </ol>
     */
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
        String primaryImage = resolveImagePath(detail.getPrimaryImageUrl());
        List<String> gallery = parseGallery(detail.getGalleryJson(), detail.getPrimaryImageUrl());
        if (primaryImage == null && !gallery.isEmpty()) {
            primaryImage = gallery.get(0);
        }
        return new ProductDetailView(
                detail.getId(),
                detail.getName(),
                detail.getShortDescription(),
                detail.getDescription(),
                primaryImage,
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

    /**
     * Lấy danh sách sản phẩm tương tự theo cùng loại để hiển thị trong khối gợi ý.
     */
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

    /**
     * Trả về danh sách loại sản phẩm (bao gồm subtype con) phục vụ form lọc.
     */
    public List<ProductTypeOption> getTypeOptions() {
        return TYPE_OPTIONS;
    }

    /**
     * Lọc danh sách subtype có hàng dựa theo loại, đồng thời fallback về cấu hình mặc định nếu DB rỗng.
     */
    public List<ProductSubtypeOption> getSubtypeOptions(String productType) {
        String normalizedType = normalizeFilter(productType);
        List<String> availableSubtypes = productDAO.findAvailableSubtypeCodes(normalizedType);
        if (availableSubtypes.isEmpty()) {
            return TYPE_OPTIONS.stream()
                    .filter(option -> Objects.equals(option.getCode(), normalizedType))
                    .findFirst()
                    .map(ProductTypeOption::getSubtypes)
                    .orElse(List.of());
        }

        List<ProductSubtypeOption> options = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String code : availableSubtypes) {
            String normalized = normalizeSubtypeCode(code);
            if (normalized == null || !seen.add(normalized)) {
                continue;
            }
            options.add(new ProductSubtypeOption(normalized, resolveSubtypeLabel(normalized)));
        }
        if (options.isEmpty()) {
            return TYPE_OPTIONS.stream()
                    .filter(option -> Objects.equals(option.getCode(), normalizedType))
                    .findFirst()
                    .map(ProductTypeOption::getSubtypes)
                    .orElse(List.of());
        }
        return List.copyOf(options);
    }

    /**
     * Chuẩn hóa mã loại trước khi lưu/so sánh.
     */
    public String normalizeTypeCode(String value) {
        return normalizeFilter(value);
    }

    /**
     * Chuẩn hóa danh sách subtype theo cùng loại, tránh duplicate và giá trị sai.
     */
    public List<String> normalizeSubtypeCodes(String productType, String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        String normalizedType = normalizeFilter(productType);
        if (normalizedType == null) {
            return List.of();
        }
        return normalizeSubtypeList(normalizedType, Arrays.asList(values));
    }

    /**
     * Map mã loại sang nhãn tiếng Việt.
     */
    public String getTypeLabel(String typeCode) {
        return resolveTypeLabel(typeCode);
    }

    /**
     * API nội bộ để lấy bản ghi sản phẩm gốc phục vụ dashboard quản trị.
     */
    public List<Products> homepageHighlights() {
        return productDAO.findHighlighted(DEFAULT_HOMEPAGE_LIMIT);
    }

    /**
     * Truy xuất bản ghi {@link model.Products} theo id, dùng cho trang quản trị.
     */
    public Products detail(int id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xoá"));
    }

    /**
     * Trả về Optional để các luồng xử lý khác tự quyết định thông báo lỗi.
     */
    public Optional<Products> findOptionalById(int id) {
        return productDAO.findById(id);
    }

    /**
     * Kiểm tra số lượng credential có thể bàn giao cho sản phẩm khi không truyền biến thể cụ thể.
     */
    public boolean hasDeliverableCredentials(int productId) {
        return hasDeliverableCredentials(productId, List.of());
    }

    /**
     * Đánh giá khả năng bàn giao credential cho sản phẩm hoặc từng biến thể.
     * <ol>
     *     <li>Nếu danh sách biến thể được truyền vào, kiểm tra từng biến thể khả dụng và tồn kho.</li>
     *     <li>Gọi {@link CredentialDAO#fetchAvailability(int, String)} để biết số lượng credential còn lại.</li>
     *     <li>Fallback về kiểm tra tổng (không theo biến thể) khi chưa có dữ liệu.</li>
     * </ol>
     */
    public boolean hasDeliverableCredentials(int productId, List<ProductVariantOption> variants) {
        boolean hasVariantData = variants != null && !variants.isEmpty();
        if (hasVariantData) {
            for (ProductVariantOption variant : variants) {
                if (variant == null || !variant.isAvailable()) {
                    continue;
                }
                Integer variantInventory = variant.getInventoryCount();
                if (variantInventory != null && variantInventory <= 0) {
                    continue;
                }
                CredentialDAO.CredentialAvailability variantAvailability =
                        credentialDAO.fetchAvailability(productId, variant.getVariantCode());
                if (variantAvailability.total() == 0 || variantAvailability.available() > 0) {
                    return true;
                }
            }
        }
        CredentialDAO.CredentialAvailability availability = credentialDAO.fetchAvailability(productId);
        if (availability.total() == 0) {
            return true;
        }
        return availability.available() > 0;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa cho trang quản trị (trả về entity gốc và phân trang).
     */
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

    /**
     * Chuyển đổi dữ liệu hàng sản phẩm sang view model gọn nhẹ.
     */
    private ProductSummaryView toSummaryView(ProductListRow row) {
        List<ProductVariantOption> variants = parseVariants(row.getVariantSchema(), row.getVariantsJson());
        PriceRange priceRange = determinePriceRange(row.getPrice(), row.getVariantSchema(), variants);
        String primaryImage = resolveImagePath(row.getPrimaryImageUrl());
        return new ProductSummaryView(
                row.getId(),
                row.getName(),
                row.getShortDescription(),
                primaryImage,
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

    /**
     * Parse JSON biến thể dựa trên schema và loại bỏ phần tử null.
     */
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

    /**
     * Parse danh sách ảnh gallery, đảm bảo luôn có ảnh fallback nếu dữ liệu rỗng.
     */
    private List<String> parseGallery(String galleryJson, String fallback) {
        String normalizedFallback = resolveImagePath(fallback);
        if (galleryJson == null || galleryJson.isBlank()) {
            return normalizedFallback == null ? List.of() : List.of(normalizedFallback);
        }
        try {
            List<String> images = gson.fromJson(galleryJson, stringListType);
            if (images == null || images.isEmpty()) {
                return normalizedFallback == null ? List.of() : List.of(normalizedFallback);
            }
            List<String> filtered = new ArrayList<>();
            for (String image : images) {
                String normalizedImage = resolveImagePath(image);
                if (normalizedImage != null) {
                    filtered.add(normalizedImage);
                }
            }
            if (filtered.isEmpty() && normalizedFallback != null) {
                filtered.add(normalizedFallback);
            }
            return List.copyOf(filtered);
        } catch (JsonSyntaxException ex) {
            return normalizedFallback == null ? List.of() : List.of(normalizedFallback);
        }
    }

    /**
     * Chuẩn hóa đường dẫn ảnh: trả về URL tuyệt đối hoặc thêm prefix thư mục tài nguyên.
     */
    private String resolveImagePath(String path) {
        String normalized = normalize(path);
        if (normalized == null) {
            return null;
        }
        String sanitized = normalized.replace('\\', '/');
        String lower = sanitized.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("data:")) {
            return sanitized;
        }
        if (sanitized.startsWith("//")) {
            return sanitized;
        }
        if (sanitized.startsWith("/")) {
            return sanitized;
        }
        if (sanitized.startsWith("assets/")) {
            return "/" + sanitized;
        }
        if (sanitized.startsWith("./")) {
            sanitized = sanitized.substring(2);
        }
        return PRODUCT_IMAGE_BASE_PATH + sanitized;
    }

    /**
     * Tính khoảng giá dựa trên giá base và giá từng biến thể khả dụng.
     */
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

    /**
     * Lấy nhãn hiển thị cho mã loại, fallback về "Khác" nếu không khớp.
     */
    private String resolveTypeLabel(String type) {
        if (type == null) {
            return TYPE_LABELS.get("OTHER");
        }
        return TYPE_LABELS.getOrDefault(type.toUpperCase(Locale.ROOT), TYPE_LABELS.get("OTHER"));
    }

    /**
     * Lấy nhãn hiển thị cho subtype, fallback về "Khác" khi cần.
     */
    private String resolveSubtypeLabel(String subtype) {
        if (subtype == null) {
            return SUBTYPE_LABELS.get("OTHER");
        }
        return SUBTYPE_LABELS.getOrDefault(subtype.toUpperCase(Locale.ROOT), SUBTYPE_LABELS.get("OTHER"));
    }

    /**
     * Chuẩn hóa chuỗi: trim và chuyển null nếu rỗng.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Chuẩn hóa mã loại trước khi query, chỉ nhận giá trị nằm trong bảng cấu hình.
     */
    private String normalizeFilter(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return TYPE_LABELS.containsKey(upper) ? upper : null;
    }

    /**
     * Chuẩn hóa subtype theo loại, đảm bảo chỉ giữ giá trị hợp lệ trong cấu hình.
     */
    private String normalizeSubtype(String type, String subtype) {
        String normalizedSubtype = normalize(subtype);
        if (normalizedSubtype == null) {
            return null;
        }
        String upper = normalizedSubtype.toUpperCase(Locale.ROOT);
        if ("OTHER".equals(upper)) {
            return upper;
        }
        boolean exists = TYPE_OPTIONS.stream()
                .filter(option -> type == null || option.getCode().equals(type))
                .flatMap(option -> option.getSubtypes().stream())
                .anyMatch(option -> option.getCode().equals(upper));
        return exists ? upper : null;
    }

    /**
     * Chuẩn hóa danh sách subtype: bỏ null, bỏ trùng và cố định thứ tự lựa chọn.
     */
    private List<String> normalizeSubtypeList(String type, List<String> subtypes) {
        if (subtypes == null || subtypes.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : subtypes) {
            String resolved = normalizeSubtype(type, value);
            if (resolved != null) {
                normalized.add(resolved);
            }
        }
        return normalized.isEmpty() ? List.of() : List.copyOf(normalized);
    }

    /**
     * Kiểm tra schema có khai báo biến thể hay không.
     */
    private boolean hasVariants(String variantSchema) {
        return variantSchema != null && !"NONE".equalsIgnoreCase(variantSchema);
    }

    private record PriceRange(BigDecimal min, BigDecimal max) {
    }

    /**
     * Chuẩn hóa mã subtype lấy từ DB (trim, upper case).
     */
    private String normalizeSubtypeCode(String subtype) {
        if (subtype == null) {
            return null;
        }
        String trimmed = subtype.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
