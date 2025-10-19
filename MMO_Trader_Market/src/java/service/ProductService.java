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

    private final ProductDAO productDAO = new ProductDAO();

    public List<Products> homepageHighlights() {
        return productDAO.findHighlighted();
    }

    public List<Products> findAll() {
        return productDAO.findAll();
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
        List<Products> filtered = productDAO.search(keyword);
        int totalItems = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.min(page, totalPages);
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Products> pageItems = filtered.subList(fromIndex, toIndex);
        return new PaginatedResult<>(pageItems, currentPage, totalPages, pageSize, totalItems);
    }
}
