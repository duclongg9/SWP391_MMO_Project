package service;

import dao.product.ProductDAO;
import model.Products;

import java.util.List;

/**
 * Contains business logic related to products.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Products> homepageHighlights() {
        return productDAO.findHighlighted();
    }

    public Products detail(int id) {
        Products p = productDAO.findById(id);
        if (p == null) throw new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã bị xoá");
        return p;
    }
    public List<Products> findAll() {
        return productDAO.findAll();
    }
}
