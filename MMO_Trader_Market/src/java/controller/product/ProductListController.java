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
import java.util.List;

/**
 * Public marketplace product list controller.
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
        String keyword = normalize(request.getParameter("q"));
        String productType = normalize(request.getParameter("type"));
        String productSubtype = normalize(request.getParameter("subtype"));
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_SIZE);

        PagedResult<ProductSummaryView> result = productService.searchPublicProducts(
                productType, productSubtype, keyword, page, size);
        List<ProductTypeOption> typeOptions = productService.getTypeOptions();
        List<ProductSubtypeOption> subtypeOptions = productService.getSubtypeOptions(productType);

        request.setAttribute("pageTitle", "Sản phẩm");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("size", result.getSize());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("query", keyword == null ? "" : keyword);
        request.setAttribute("selectedType", productType);
        request.setAttribute("selectedSubtype", productSubtype);
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
