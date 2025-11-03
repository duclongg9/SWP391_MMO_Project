package controller.product;

import controller.ViewResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.view.product.ProductSummaryView;
import service.HomepageService;

import java.io.IOException;
import java.util.List;

/**
tách trang chủ thành nhiều “mảnh” độc lập để truy vấn chậm ở một phần không làm chậm cả trang.
 */
@WebServlet(name = "HomepageFragmentController", urlPatterns = {"/fragment/home/*"})
public class HomepageFragmentController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final HomepageService homepageService = new HomepageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null || path.isBlank() || "/".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        switch (path) {
            case "/summary" -> handleSummary(request, response);
            case "/featured" -> handleFeatured(request, response);
            case "/system-notes" -> handleSystemNotes(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleSummary(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("summary", homepageService.loadMarketplaceSummary());
        request.setAttribute("productCategories", homepageService.loadProductCategories());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "public, max-age=60");
        renderFragment(request, response, "product/fragments/home/summary");
    }

    private void handleFeatured(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int limit = parsePositiveInt(request.getParameter("limit"));
        List<ProductSummaryView> featured = limit > 0
                ? homepageService.loadFeaturedProducts(limit)
                : homepageService.loadFeaturedProducts();
        request.setAttribute("featuredProducts", featured);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        renderFragment(request, response, "product/fragments/home/featured");
    }

    private void handleSystemNotes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("systemNotes", homepageService.loadSystemNotes());
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "public, max-age=300");
        renderFragment(request, response, "product/fragments/home/system-notes");
    }

    private void renderFragment(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        String jsp = ViewResolver.resolve(view);
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    private int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
