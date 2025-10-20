-- Recommended indexes for buyer order queries
CREATE INDEX idx_orders_buyer_id_status ON orders(buyer_id, status);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_credentials_product_sold ON product_credentials(product_id, is_sold);
