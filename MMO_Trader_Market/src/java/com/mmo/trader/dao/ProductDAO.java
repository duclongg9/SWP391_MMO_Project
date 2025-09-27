package com.mmo.trader.dao;

import com.mmo.trader.model.Product;
import com.mmo.trader.model.ProductStatus;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO responsible for retrieving products. The current implementation uses
 * an in-memory list to keep the sample lightweight, but the BaseDAO provides
 * JDBC utilities for a future MySQL integration.
 */
public class ProductDAO extends BaseDAO {

    private static final List<Product> SAMPLE_PRODUCTS = Arrays.asList(
            new Product(1, "Acc game A", "Tài khoản full skin", new BigDecimal("120000"), ProductStatus.PENDING_REVIEW),
            new Product(2, "Acc game B", "Rank cao mùa mới", new BigDecimal("340000"), ProductStatus.APPROVED),
            new Product(3, "Thẻ nạp C", "Giao ngay sau khi thanh toán", new BigDecimal("95000"), ProductStatus.APPROVED)
    );

    public List<Product> findAll() {
        return SAMPLE_PRODUCTS;
    }

    public List<Product> findHighlighted() {
        return SAMPLE_PRODUCTS.stream()
                .filter(product -> product.getStatus() == ProductStatus.APPROVED)
                .collect(Collectors.toList());
    }
}
