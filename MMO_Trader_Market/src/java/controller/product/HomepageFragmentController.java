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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * tách trang chủ thành nhiều “mảnh” độc lập để truy vấn chậm ở một phần không
 * làm chậm cả trang.
 */
@WebServlet(name = "HomepageFragmentController", urlPatterns = {"/fragment/home/*"})
public class HomepageFragmentController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(HomepageFragmentController.class.getName());

    private final HomepageService homepageService = new HomepageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // "/summary", "/featured", "/system-notes"
        if (path == null || path.isBlank() || "/".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        switch (path) { //Tuỳ path mà gọi từng handler.
            case "/summary" ->
                handleSummary(request, response);
            case "/featured" ->
                handleFeatured(request, response);
            case "/system-notes" ->
                handleSystemNotes(request, response);
            default ->
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleSummary(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("summary", homepageService.loadMarketplaceSummary()); //tổng số shop, tổng số sản phẩm, số giao dịch
            request.setAttribute("productCategories", homepageService.loadProductCategories()); //danh sách category
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading homepage summary", e);
            // Set empty data to avoid breaking the page
            request.setAttribute("summary", null);
            request.setAttribute("productCategories", new ArrayList<>());
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "public, max-age=60");
        renderFragment(request, response, "product/fragments/home/summary");
    }

    private void handleFeatured(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Nếu hợp lệ >0 thì chỉ lấy bấy nhiêu sản phẩm.
            //Nếu không có/không hợp lệ thì lấy default.
            int limit = parsePositiveInt(request.getParameter("limit"));
            List<ProductSummaryView> featured = limit > 0
                    ? homepageService.loadFeaturedProducts(limit)
                    : homepageService.loadFeaturedProducts();
            request.setAttribute("featuredProducts", featured);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading featured products", e);
            // Set empty list to avoid breaking the page
            request.setAttribute("featuredProducts", new ArrayList<>());
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //Set header không cache (vì sản phẩm nổi bật có thể thay đổi liên tục).
        renderFragment(request, response, "product/fragments/home/featured");
    }

    private void handleSystemNotes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("systemNotes", homepageService.loadSystemNotes());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading system notes", e);
            // Set empty list to avoid breaking the page
            request.setAttribute("systemNotes", new ArrayList<>());
        }
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
