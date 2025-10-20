package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.PagedResult;
import model.product.ProductListRow;
import model.product.ShopOption;
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
        String query = normalize(request.getParameter("q"));
        Integer shopId = parseShopId(request.getParameter("shopId"));
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_SIZE);

        PagedResult<ProductListRow> result = productService.list(query, shopId, page, size);
        List<ShopOption> shops = productService.listAvailableShops();

        request.setAttribute("pageTitle", "Sản phẩm");
        request.setAttribute("headerTitle", "Kho sản phẩm");
        request.setAttribute("headerSubtitle", "Tìm kiếm và mua ngay các tài nguyên số");
        request.setAttribute("items", result.getItems());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("page", result.getPage());
        request.setAttribute("currentPage", result.getPage());
        request.setAttribute("size", result.getSize());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("query", query == null ? "" : query);
        request.setAttribute("selectedShopId", shopId);
        request.setAttribute("shops", shops);

        forward(request, response, "product/list");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseShopId(String value) {
        int parsed = parsePositiveIntOrDefault(value, -1);
        return parsed > 0 ? parsed : null;
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
}
