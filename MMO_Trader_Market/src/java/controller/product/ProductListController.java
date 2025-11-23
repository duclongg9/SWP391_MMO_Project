package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.PagedResult;
import model.view.product.ProductSubtypeOption;
import model.view.product.ProductSummaryView;
import model.view.product.ProductTypeOption;
import service.ProductService;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 4; //mỗi trang 4 sản phẩm nếu không chọn gì
    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(4, 8, 12, 16); //chỉ cho phép các giá trị này (4/8/12/16).
    private static final String SORT_NEWEST = "newest";
    private static final String SORT_BEST_SELLER = "best_seller";

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProductTypeOption> typeOptions = productService.getTypeOptions(); //Lấy danh sách type khả dụng
        String requestedType = request.getParameter("type"); //Lấy type từ query, normalize về code hợp lệ
        String normalizedType = productService.normalizeTypeCode(requestedType);
        boolean hasRawType = requestedType != null && !requestedType.trim().isEmpty(); //Chuyển về code chuẩn (lowercase, bỏ space, validate…)Nếu không hợp lệ → trả về null.
        if (normalizedType == null && hasRawType) {
            Optional<String> fallbackType = typeOptions.stream()
                    .map(ProductTypeOption::getCode)
                    .findFirst();
            if (fallbackType.isPresent()) {
                // Nếu không truyền type hợp lệ thì chuyển sang loại đầu tiên.
                response.sendRedirect(request.getContextPath() + "/products?type=" + fallbackType.get());
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
//Lấy trang (mặc định 1) và kích thước trang (mặc định 5).
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE); 
        int sizeCandidate = parsePositiveIntOrDefault(resolveSizeParam(request), DEFAULT_SIZE);
        int size = PAGE_SIZE_OPTIONS.contains(sizeCandidate) ? sizeCandidate : DEFAULT_SIZE;
//Chuẩn hoá keyword: trim, rỗng ⇒ coi như không có.
        String rawKeyword = request.getParameter("keyword");
        String normalizedKeyword = rawKeyword == null ? null : rawKeyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }
//Lấy danh sách subtype được chọn (có thể nhiều), chuẩn hoá theo type hiện tại.
        List<String> subtypeFilters = productService.normalizeSubtypeCodes(normalizedType,
                request.getParameterValues("subtype"));
        Set<String> selectedSubtypeSet = new LinkedHashSet<>(subtypeFilters);
//Gọi service lấy kết quả trang (items, page, size, total, totalPages).
        String sortOrder = normalizeSort(request.getParameter("sort"));
        String serviceSort = SORT_BEST_SELLER.equals(sortOrder) ? SORT_BEST_SELLER : null;

        PagedResult<ProductSummaryView> result = productService.browseByType(normalizedType, subtypeFilters,
                normalizedKeyword, serviceSort, page, size);
        List<ProductSubtypeOption> subtypeOptions = normalizedType == null
                ? List.of()
                : productService.getSubtypeOptions(normalizedType);
        String currentTypeLabel = normalizedType == null
                ? "Tất cả sản phẩm"
                : typeOptions.stream()
                        .filter(option -> option.getCode().equals(normalizedType))
                        .map(ProductTypeOption::getLabel)
                        .findFirst()
                        .orElse(productService.getTypeLabel(normalizedType));

        request.setAttribute("pageTitle", currentTypeLabel + " - Sản phẩm");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("pageSize", result.getSize());
        request.setAttribute("pageSizeOptions", PAGE_SIZE_OPTIONS);
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("selectedType", normalizedType == null ? "" : normalizedType);
        request.setAttribute("selectedSubtypes", selectedSubtypeSet);
        request.setAttribute("keyword", normalizedKeyword == null ? "" : normalizedKeyword);
        request.setAttribute("sortOrder", sortOrder == null ? SORT_NEWEST : sortOrder);
        request.setAttribute("typeOptions", typeOptions);
        request.setAttribute("subtypeOptions", subtypeOptions);
        request.setAttribute("currentTypeLabel", currentTypeLabel);

        forward(request, response, "product/list");
    }

    // Phân tích số nguyên dương, trả về mặc định nếu đầu vào không hợp lệ.
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

    // Ưu tiên tham số pageSize nếu có, nếu không thì lấy size (tương thích cũ).
    private String resolveSizeParam(HttpServletRequest request) {
        String pageSizeParam = request.getParameter("pageSize");
        if (pageSizeParam != null && !pageSizeParam.isBlank()) {
            return pageSizeParam;
        }
        return request.getParameter("size");
    }

    /**
     * Chuẩn hóa tham số sắp xếp từ query string để tránh giá trị không hợp lệ.
     *
     * @param sortParam giá trị raw từ request
     * @return mã sắp xếp hợp lệ hoặc {@code null} nếu mặc định
     */
    private String normalizeSort(String sortParam) {
        if (sortParam == null) {
            return null;
        }
        String normalized = sortParam.trim().toLowerCase(java.util.Locale.ROOT);
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
}
