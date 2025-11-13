package controller.shop;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.PagedResult;
import model.view.product.ProductSubtypeOption;
import model.view.product.ProductSummaryView;
import model.view.product.ProductTypeOption;
import model.view.shop.ShopPublicSummary;
import service.ProductService;
import service.ShopService;
import units.IdObfuscator;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Hiển thị trang công khai của shop bao gồm danh sách sản phẩm và các bộ lọc.
 */
@WebServlet(name = "ShopCatalogController", urlPatterns = {"/shops/*"})
public class ShopCatalogController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 4;
    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(4, 8, 12, 16);
    private static final String SORT_NEWEST = "newest";
    private static final String SORT_BEST_SELLER = "best_seller";

    private final ShopService shopService = new ShopService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = extractTokenFromPath(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int shopId;
        try {
            shopId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Optional<ShopPublicSummary> shopOpt = shopService.findPublicSummary(shopId);
        if (shopOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ShopPublicSummary shop = shopOpt.get();

        String requestedType = request.getParameter("type");
        String normalizedType = productService.normalizeTypeCode(requestedType);
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int sizeCandidate = parsePositiveIntOrDefault(resolveSizeParam(request), DEFAULT_SIZE);
        int pageSize = PAGE_SIZE_OPTIONS.contains(sizeCandidate) ? sizeCandidate : DEFAULT_SIZE;

        String rawKeyword = request.getParameter("keyword");
        String normalizedKeyword = rawKeyword == null ? null : rawKeyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }

        List<String> subtypeFilters = productService.normalizeSubtypeCodes(normalizedType,
                request.getParameterValues("subtype"));
        Set<String> selectedSubtypes = new LinkedHashSet<>(subtypeFilters);

        String sortOrder = normalizeSort(request.getParameter("sort"));
        String serviceSort = SORT_BEST_SELLER.equals(sortOrder) ? SORT_BEST_SELLER : null;

        PagedResult<ProductSummaryView> result = productService.browseByShop(shopId, normalizedType, subtypeFilters,
                normalizedKeyword, serviceSort, page, pageSize);

        List<ProductTypeOption> typeOptions = productService.getTypeOptionsForShop(shopId);
        List<ProductSubtypeOption> subtypeOptions = normalizedType == null
                ? List.of()
                : productService.getSubtypeOptionsForShop(shopId, normalizedType);
        String currentTypeLabel = normalizedType == null
                ? "Tất cả sản phẩm"
                : productService.getTypeLabel(normalizedType);

        request.setAttribute("shopSummary", shop);
        request.setAttribute("pageTitle", shop.getName() + " - Shop");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("pageSize", result.getSize());
        request.setAttribute("pageSizeOptions", PAGE_SIZE_OPTIONS);
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("selectedType", normalizedType == null ? "" : normalizedType);
        request.setAttribute("selectedSubtypes", selectedSubtypes);
        request.setAttribute("keyword", normalizedKeyword == null ? "" : normalizedKeyword);
        request.setAttribute("sortOrder", sortOrder == null ? SORT_NEWEST : sortOrder);
        request.setAttribute("typeOptions", typeOptions);
        request.setAttribute("subtypeOptions", subtypeOptions);
        request.setAttribute("currentTypeLabel", currentTypeLabel);
        request.setAttribute("shopToken", shop.getEncodedId());

        forward(request, response, "shop/catalog");
    }

    /**
     * Chuẩn hóa số nguyên dương từ query string.
     */
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String resolveSizeParam(HttpServletRequest request) {
        String pageSizeParam = request.getParameter("pageSize");
        if (pageSizeParam != null && !pageSizeParam.isBlank()) {
            return pageSizeParam;
        }
        return request.getParameter("size");
    }

    /**
     * Chuẩn hóa tham số sắp xếp để tránh truyền giá trị không hợp lệ xuống DAO.
     */
    private String normalizeSort(String sortParam) {
        if (sortParam == null) {
            return null;
        }
        String normalized = sortParam.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        if (SORT_BEST_SELLER.equals(normalized) || "best-seller".equals(normalized) || "bestseller".equals(normalized)) {
            return SORT_BEST_SELLER;
        }
        if (SORT_NEWEST.equals(normalized) || "latest".equals(normalized)) {
            return SORT_NEWEST;
        }
        return null;
    }

    /**
     * Tách token shop từ phần path của URL thân thiện.
     */
    private String extractTokenFromPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String token = pathInfo.charAt(0) == '/' ? pathInfo.substring(1) : pathInfo;
        int slashIndex = token.indexOf('/');
        if (slashIndex >= 0) {
            token = token.substring(0, slashIndex);
        }
        return token.isBlank() ? null : token;
    }
}
