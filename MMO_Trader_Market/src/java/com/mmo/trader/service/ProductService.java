package com.mmo.trader.service;

import com.mmo.trader.dao.ProductDAO;
import com.mmo.trader.model.Product;
import java.util.List;

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
}
