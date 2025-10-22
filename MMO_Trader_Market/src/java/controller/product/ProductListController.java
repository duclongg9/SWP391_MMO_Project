package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.PagedResult;
import model.view.product.ProductSummaryView;
import service.ProductService;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public marketplace product list controller.
 */
@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 12;

    private static final Set<String> ALLOWED_TYPES = Set.of("EMAIL", "SOCIAL", "SOFTWARE", "GAME");
    private static final Set<String> ALLOWED_SUBTYPES = Set.of("GMAIL", "FACEBOOK", "TIKTOK", "CANVA", "VALORANT", "OTHER");

    private static final Logger LOGGER = Logger.getLogger(ProductListController.class.getName());
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String rawKeyword = request.getParameter("q");
        String rawType = request.getParameter("type");
        String rawSubtype = request.getParameter("subtype");
        String rawPage = request.getParameter("page");
        String rawPageSize = firstNonBlank(request.getParameter("pageSize"), request.getParameter("size"));

        String keyword = trimToNull(rawKeyword);
        String productType = normalizeEnum(rawType, ALLOWED_TYPES);
        String productSubtype = normalizeEnum(rawSubtype, ALLOWED_SUBTYPES);
        int page = parsePositiveIntOrDefault(rawPage, DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(rawPageSize, DEFAULT_SIZE);

        LOGGER.log(Level.INFO, "[ProductListController] q={0}, type={1}, subtype={2}, page={3}, pageSize={4}",
                new Object[]{keyword, productType, productSubtype, page, size});

        PagedResult<ProductSummaryView> result = productService.searchPublicProducts(
                productType, productSubtype, keyword, page, size);

        request.setAttribute("pageTitle", "Sản phẩm");
        request.setAttribute("headerTitle", "Kho sản phẩm");
        request.setAttribute("headerSubtitle", "Tìm kiếm, lọc theo shop và mua ngay những sản phẩm bạn cần.");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("pageSize", result.getSize());
        request.setAttribute("size", result.getSize());
        request.setAttribute("totalPages", result.getTotalPages());
        String resolvedQuery = keyword == null ? "" : keyword;
        request.setAttribute("query", resolvedQuery);
        request.setAttribute("q", resolvedQuery);
        request.setAttribute("selectedType", productType);
        request.setAttribute("selectedSubtype", productSubtype);

        forward(request, response, "product/product-list");
    }

    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEnum(String value, Set<String> allowed) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        return allowed.contains(upper) ? upper : null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }
}
