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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Điều phối luồng "Danh sách sản phẩm" trên marketplace công khai.
 * <p>
 * - Liệt kê sản phẩm theo loại với phân trang và bộ lọc phân nhóm con.
 * - Hỗ trợ chọn nhiều phân loại chi tiết cho từng loại sản phẩm.
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
        String productTypeParam = normalize(request.getParameter("type"));
        String[] subtypeParams = request.getParameterValues("subtype");
        List<String> requestedSubtypes = new ArrayList<>();
        if (subtypeParams != null) {
            for (String value : subtypeParams) {
                String normalized = normalize(value);
                if (normalized != null) {
                    requestedSubtypes.add(normalized);
                }
            }
        }
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("pageSize"), -1);
        if (size == -1) {
            size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_SIZE);
        }

        List<ProductTypeOption> typeOptions = productService.getTypeOptions();
        Optional<ProductTypeOption> selectedTypeOption = productService.findTypeOption(productTypeParam);
        if (selectedTypeOption.isEmpty()) {
            if (typeOptions.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            ProductTypeOption defaultOption = typeOptions.get(0);
            response.sendRedirect(request.getContextPath() + "/products?type=" + defaultOption.getCode());
            return;
        }

        String resolvedType = selectedTypeOption.get().getCode();
        List<String> validSubtypes = productService.filterValidSubtypes(resolvedType, requestedSubtypes);

        PagedResult<ProductSummaryView> result = productService.searchPublicProducts(
                resolvedType, validSubtypes, page, size);
        List<ProductSubtypeOption> subtypeOptions = selectedTypeOption.get().getSubtypes();

        request.setAttribute("pageTitle", "Sản phẩm");
        request.setAttribute("headerTitle", selectedTypeOption.get().getLabel());
        request.setAttribute("headerSubtitle",
                "Chọn phân loại chi tiết để thu hẹp danh sách sản phẩm.");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("pageSize", result.getSize());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("selectedType", resolvedType);
        request.setAttribute("selectedTypeLabel", selectedTypeOption.get().getLabel());
        request.setAttribute("selectedSubtypes", validSubtypes);
        request.setAttribute("typeOptions", typeOptions);
        request.setAttribute("subtypeOptions", subtypeOptions);

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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
