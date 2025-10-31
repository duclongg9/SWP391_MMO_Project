package controller.product;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.SystemConfigs;
import model.view.MarketplaceSummary;
import model.view.product.ProductCategorySummary;
import model.view.product.ProductSummaryView;
import service.HomepageService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API trả về từng khối dữ liệu trang chủ dưới dạng JSON để giao diện nạp rời.
 */
@WebServlet(name = "HomepageFragmentController", urlPatterns = {"/api/home/*"})
public class HomepageFragmentController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(HomepageFragmentController.class.getName());

    private static final int DEFAULT_FEATURED_LIMIT = 6;

    private final HomepageService homepageService = new HomepageService();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String fragment = resolveFragment(request.getPathInfo());
        if (fragment.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu fragment cần lấy dữ liệu");
            return;
        }

        try {
            switch (fragment) {
                case "summary" -> renderSummary(response);
                case "categories" -> renderCategories(response);
                case "highlights" -> renderHighlights(request, response);
                case "system-notes" -> renderSystemNotes(response);
                default -> writeError(response, HttpServletResponse.SC_NOT_FOUND, "Fragment không tồn tại");
            }
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Không thể tải fragment homepage: " + fragment, ex);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tải dữ liệu, vui lòng thử lại sau.");
        }
    }

    private void renderSummary(HttpServletResponse response) throws IOException {
        MarketplaceSummary summary = homepageService.loadMarketplaceSummary();
        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", summary);
        writeJson(response, payload);
    }

    private void renderCategories(HttpServletResponse response) throws IOException {
        List<ProductCategorySummary> categories = homepageService.loadProductCategories();
        Map<String, Object> payload = new HashMap<>();
        payload.put("categories", categories);
        writeJson(response, payload);
    }

    private void renderHighlights(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int limit = parsePositiveInt(request.getParameter("limit"), DEFAULT_FEATURED_LIMIT);
        List<ProductSummaryView> products = homepageService.loadFeaturedProducts(limit);
        List<ProductHighlight> highlights = new ArrayList<>();
        for (ProductSummaryView product : products) {
            highlights.add(ProductHighlight.from(product));
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("products", highlights);
        writeJson(response, payload);
    }

    private void renderSystemNotes(HttpServletResponse response) throws IOException {
        List<SystemConfigs> configs = homepageService.loadSystemNotes();
        List<SystemNote> notes = new ArrayList<>();
        for (SystemConfigs config : configs) {
            notes.add(new SystemNote(config.getId(), config.getConfigKey(), config.getDescription()));
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("notes", notes);
        writeJson(response, payload);
    }

    private String resolveFragment(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return "";
        }
        String normalized = pathInfo.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private int parsePositiveInt(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void writeJson(HttpServletResponse response, Object payload) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.write(gson.toJson(payload));
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("error", message);
        writeJson(response, payload);
    }

    private static final class ProductHighlight {
        private final int id;
        private final String encodedId;
        private final String name;
        private final String shortDescription;
        private final String primaryImageUrl;
        private final String productTypeLabel;
        private final String productSubtypeLabel;
        private final String shopName;
        private final BigDecimal minPrice;
        private final BigDecimal maxPrice;
        private final Integer inventoryCount;
        private final Integer soldCount;

        private ProductHighlight(int id, String encodedId, String name, String shortDescription, String primaryImageUrl,
                String productTypeLabel, String productSubtypeLabel, String shopName, BigDecimal minPrice,
                BigDecimal maxPrice, Integer inventoryCount, Integer soldCount) {
            this.id = id;
            this.encodedId = encodedId;
            this.name = name;
            this.shortDescription = shortDescription;
            this.primaryImageUrl = primaryImageUrl;
            this.productTypeLabel = productTypeLabel;
            this.productSubtypeLabel = productSubtypeLabel;
            this.shopName = shopName;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.inventoryCount = inventoryCount;
            this.soldCount = soldCount;
        }

        private static ProductHighlight from(ProductSummaryView view) {
            return new ProductHighlight(
                    view.getId(),
                    view.getEncodedId(),
                    view.getName(),
                    view.getShortDescription(),
                    view.getPrimaryImageUrl(),
                    view.getProductTypeLabel(),
                    view.getProductSubtypeLabel(),
                    view.getShopName(),
                    view.getMinPrice(),
                    view.getMaxPrice(),
                    view.getInventoryCount(),
                    view.getSoldCount()
            );
        }
    }

    private static final class SystemNote {
        private final Integer id;
        private final String key;
        private final String description;

        private SystemNote(Integer id, String key, String description) {
            this.id = id;
            this.key = key;
            this.description = Optional.ofNullable(description).orElse("");
        }
    }
}
