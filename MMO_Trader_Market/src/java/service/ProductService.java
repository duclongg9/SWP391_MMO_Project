package service;

import dao.product.ProductDAO;
import model.Products;
import service.dto.ProductSearchResult;

import java.util.List;
import java.util.Optional;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private static final int HIGHLIGHT_LIMIT = 3;

    private final ProductDAO productDAO;

    public ProductService() {
        this(new ProductDAO());
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public List<Products> homepageHighlights() {
        return productDAO.findAvailable(HIGHLIGHT_LIMIT, 0);
    }

    public Products detail(int id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xoá"));
    }

    public Optional<Products> findOptionalById(int id) {
        return productDAO.findById(id);
    }

    public Optional<Integer> findOwnerIdByProduct(int productId) {
        return productDAO.findOwnerIdByProduct(productId);
    }

    public ProductSearchResult search(int ownerId, String keyword, int page, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Số lượng mỗi trang phải lớn hơn 0.");
        }
        int safePage = Math.max(page, 1);
        String normalizedKeyword = keyword == null ? null : keyword.trim();

        long totalItems = productDAO.countSearch(ownerId, normalizedKeyword);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages > 0 && safePage > totalPages) {
            safePage = totalPages;
        }

        List<Products> items = totalItems == 0
                ? List.of()
                : productDAO.search(ownerId, normalizedKeyword, safePage, pageSize);

        return new ProductSearchResult(items, totalItems, safePage, pageSize, totalPages);
    }

    public long countAvailableProducts() {
        return productDAO.countAvailableProducts();
    }
}
