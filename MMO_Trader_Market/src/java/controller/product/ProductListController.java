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

/**
 * Điều phối luồng "Danh sách sản phẩm" trên marketplace công khai.
 * <p>
 * - Liệt kê sản phẩm theo từng loại chính với phân trang phía server.
 * - Hỗ trợ lọc theo nhiều phân loại con của cùng một loại sản phẩm.
 * - Chuẩn hóa tham số đầu vào để đảm bảo trải nghiệm duyệt sản phẩm mượt mà.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 12;

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProductTypeOption> typeOptions = productService.getTypeOptions();
        String requestedType = request.getParameter("type");
        String normalizedType = productService.normalizeTypeCode(requestedType);
        if (normalizedType == null) {
            Optional<String> fallbackType = typeOptions.stream()
                    .map(ProductTypeOption::getCode)
                    .findFirst();
            if (fallbackType.isPresent()) {
                response.sendRedirect(request.getContextPath() + "/products?type=" + fallbackType.get());
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(resolveSizeParam(request), DEFAULT_SIZE);

        String rawKeyword = request.getParameter("keyword");
        String normalizedKeyword = rawKeyword == null ? null : rawKeyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isBlank()) {
            normalizedKeyword = null;
        }

        List<String> subtypeFilters = productService.normalizeSubtypeCodes(normalizedType,
                request.getParameterValues("subtype"));
        Set<String> selectedSubtypeSet = new LinkedHashSet<>(subtypeFilters);

        PagedResult<ProductSummaryView> result = productService.browseByType(normalizedType, subtypeFilters,
                normalizedKeyword, page, size);
        List<ProductSubtypeOption> subtypeOptions = productService.getSubtypeOptions(normalizedType);
        String currentTypeLabel = typeOptions.stream()
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
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("selectedType", normalizedType);
        request.setAttribute("selectedSubtypes", selectedSubtypeSet);
        request.setAttribute("keyword", normalizedKeyword == null ? "" : normalizedKeyword);
        request.setAttribute("typeOptions", typeOptions);
        request.setAttribute("subtypeOptions", subtypeOptions);
        request.setAttribute("currentTypeLabel", currentTypeLabel);

        forward(request, response, "product/list");
    }

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
}
