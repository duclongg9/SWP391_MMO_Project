package service;

import dao.product.ProductDAO;
import model.PaginatedResult;
import model.Products;

import java.util.List;
import java.util.Optional;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private static final int HIGHLIGHT_LIMIT = 3;

    private final ProductDAO productDAO = new ProductDAO();

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
}
