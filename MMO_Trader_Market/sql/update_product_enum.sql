-- Cập nhật ENUM cho bảng products để thêm giá trị 'OTHER'
-- Chạy file SQL này để fix lỗi "Data truncated for column 'product_type'"

-- Thêm 'OTHER' vào product_type ENUM
ALTER TABLE products 
MODIFY COLUMN product_type ENUM('EMAIL', 'SOCIAL', 'GAME', 'SOFTWARE', 'OTHER') NOT NULL;

-- Kiểm tra kết quả
DESCRIBE products;

