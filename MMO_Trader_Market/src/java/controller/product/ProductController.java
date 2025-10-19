package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PaginatedResult;
import model.Products;
import service.ProductService;

import java.io.IOException;

/**
 * Controller hiển thị danh sách sản phẩm trong marketplace.
 */
@WebServlet(name = "ProductController", urlPatterns = {"/products"})
public class ProductController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final String BODY_CLASS = "layout";
    private static final String HEADER_MODIFIER = "layout__header--split";

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = normalizeKeyword(request.getParameter("keyword"));
        int page = resolvePage(request.getParameter("page"));

        PaginatedResult<Products> result = productService.search(keyword, page, DEFAULT_PAGE_SIZE);

        prepareLayout(request);
        request.setAttribute("products", result.getItems());
        request.setAttribute("currentPage", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("pageSize", result.getPageSize());
        request.setAttribute("totalItems", result.getTotalItems());
        request.setAttribute("searchKeyword", keyword == null ? "" : keyword);

        forward(request, response, "product/list");
    }

    private void prepareLayout(HttpServletRequest request) {
        request.setAttribute("pageTitle", "Danh sách sản phẩm - MMO Trader Market");
        request.setAttribute("bodyClass", BODY_CLASS);
        request.setAttribute("headerTitle", "Danh sách sản phẩm");
        request.setAttribute("headerSubtitle", "Quản lý sản phẩm theo mô hình MVC");
        request.setAttribute("headerModifier", HEADER_MODIFIER);
    }

    private String normalizeKeyword(String keywordParam) {
        if (keywordParam == null) {
            return null;
        }
        String trimmed = keywordParam.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int resolvePage(String pageParam) {
        if (pageParam == null) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return page >= 1 ? page : 1;
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
