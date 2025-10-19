package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ProductService;
import service.dto.ProductSearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller hiển thị danh sách sản phẩm của chủ shop.
 */
@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final String BODY_CLASS = "layout";
    private static final String HEADER_MODIFIER = "layout__header--split";

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer ownerId = resolveOwnerId(request);
        if (ownerId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String keyword = normalizeKeyword(request.getParameter("q"));
        int page = resolvePage(request.getParameter("page"));
        int pageSize = resolveSize(request.getParameter("size"));

        ProductSearchResult result = productService.search(ownerId, keyword, page, pageSize);

        prepareLayout(request);
        request.setAttribute("items", result.items());
        request.setAttribute("total", result.total());
        request.setAttribute("page", result.page());
        request.setAttribute("totalPages", result.totalPages());
        request.setAttribute("pageSize", result.pageSize());
        request.setAttribute("keyword", keyword == null ? "" : keyword);

        forward(request, response, "product/list");
    }

    private Integer resolveOwnerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId != null) {
            return ownerId;
        }
        return (Integer) session.getAttribute("userId");
    }

    private void prepareLayout(HttpServletRequest request) {
        request.setAttribute("pageTitle", "Danh sách sản phẩm - MMO Trader Market");
        request.setAttribute("bodyClass", BODY_CLASS);
        request.setAttribute("headerTitle", "Danh sách sản phẩm");
        request.setAttribute("headerSubtitle", "Quản lý sản phẩm theo mô hình MVC");
        request.setAttribute("headerModifier", HEADER_MODIFIER);
        request.setAttribute("navItems", buildNavigation(request.getContextPath()));
    }

    private List<Map<String, String>> buildNavigation(String contextPath) {
        List<Map<String, String>> items = new ArrayList<>();
        items.add(createNavItem(contextPath + "/dashboard", "Bảng điều khiển"));
        items.add(createNavItem(contextPath + "/products", "Sản phẩm"));
        items.add(createNavItem(contextPath + "/orders", "Đơn đã mua"));
        items.add(createNavItem(contextPath + "/styleguide", "Styleguide"));
        return items;
    }

    private Map<String, String> createNavItem(String href, String label) {
        Map<String, String> item = new HashMap<>();
        item.put("href", href);
        item.put("label", label);
        return item;
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
            int parsed = Integer.parseInt(pageParam);
            return parsed >= 1 ? parsed : 1;
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private int resolveSize(String sizeParam) {
        if (sizeParam == null) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int parsed = Integer.parseInt(sizeParam);
            return parsed > 0 ? parsed : DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException ignored) {
            return DEFAULT_PAGE_SIZE;
        }
    }
}
