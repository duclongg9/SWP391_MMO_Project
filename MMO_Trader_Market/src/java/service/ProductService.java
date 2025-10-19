package service;

import dao.product.ProductDAO;
import model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> findAll() {
        return productDAO.findAll();
    }

    public List<Product> getHighlightedProducts() {
        return productDAO.findHighlighted();
    }

    public Optional<Product> findById(int id) {
        return productDAO.findById(id);
    }
}
