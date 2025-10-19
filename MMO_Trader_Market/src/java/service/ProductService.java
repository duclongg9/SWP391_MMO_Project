package service;

import dao.product.ProductDAO;
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
}
