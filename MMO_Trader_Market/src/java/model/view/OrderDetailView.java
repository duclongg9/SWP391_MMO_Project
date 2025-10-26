package model.view;

import model.Orders;
import model.Products;
import model.product.ProductVariantOption;

import java.util.List;

/**
 * Aggregated detail of an order with its product and credential list.
 */
public record OrderDetailView(Orders order, Products product, List<String> credentials, ProductVariantOption selectedVariant) {
}
