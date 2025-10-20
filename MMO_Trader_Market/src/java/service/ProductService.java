package service;

import dao.product.ProductDAO;
import model.PaginatedResult;
import model.Products;
import model.product.PagedResult;
import model.product.ProductDetail;
import model.product.ProductListRow;
import model.product.ShopOption;

import java.util.List;
import java.util.Optional;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private static final int HIGHLIGHT_LIMIT = 3;

    private final ProductDAO productDAO = new ProductDAO();

    public PagedResult<ProductListRow> list(String q, Integer shopId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        String keyword = normalizeFilter(q);
        Integer normalizedShopId = normalizeShopId(shopId);

        long totalItems = productDAO.countAvailable(keyword, normalizedShopId);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;

        List<ProductListRow> items = totalItems == 0
                ? List.of()
                : productDAO.findAvailablePaged(keyword, normalizedShopId, safeSize, offset);

        return new PagedResult<>(items, currentPage, safeSize, totalPages, totalItems);
    }

    public ProductDetail getDetail(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }
        ProductDetail detail = productDAO.findDetailById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        if (!"Available".equalsIgnoreCase(detail.getStatus())) {
            throw new IllegalArgumentException("Sản phẩm không khả dụng");
        }
        return detail;
    }

    public List<ShopOption> listAvailableShops() {
        return productDAO.findShopsWithAvailableProducts();
    }

    public List<Products> homepageHighlights() {
        return productDAO.findHighlighted(HIGHLIGHT_LIMIT);
    }

    public Products detail(int id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xoá"));
    }

    public Optional<Products> findOptionalById(int id) {
        return productDAO.findById(id);
    }

    public PaginatedResult<Products> search(String keyword, int page, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Số lượng mỗi trang phải lớn hơn 0.");
        }
        if (page < 1) {
            throw new IllegalArgumentException("Số trang phải lớn hơn hoặc bằng 1.");
        }

        String normalizedKeyword = keyword == null ? null : keyword.trim();
        int totalItems = productDAO.countByKeyword(normalizedKeyword);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(page, totalPages);
        int offset = (currentPage - 1) * pageSize;

        List<Products> items = totalItems == 0
                ? List.of()
                : productDAO.search(normalizedKeyword, pageSize, offset);

        return new PaginatedResult<>(items, currentPage, totalPages, pageSize, totalItems);
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer normalizeShopId(Integer shopId) {
        if (shopId == null || shopId <= 0) {
            return null;
        }
        return shopId;
    }
}
