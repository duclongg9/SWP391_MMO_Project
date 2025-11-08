-- Seller Shop Management v2 migration
-- Thêm cột updated_at và index cho bảng shops, đồng thời chuẩn hóa trạng thái sản phẩm.

ALTER TABLE shops
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD INDEX idx_shops_owner (owner_id),
    ADD INDEX idx_shops_name (name),
    ADD INDEX idx_shops_created_at (created_at),
    ADD INDEX idx_shops_updated_at (updated_at);

-- Chuẩn hóa trạng thái sản phẩm sang UNLISTED và cập nhật ENUM.
UPDATE products SET status = 'UNLISTED' WHERE status = 'Unlisted';

ALTER TABLE products
    MODIFY COLUMN status ENUM('Available','OutOfStock','UNLISTED') NOT NULL DEFAULT 'Available';
