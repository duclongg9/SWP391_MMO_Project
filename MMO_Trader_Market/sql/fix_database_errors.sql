-- ============================================
-- FIX DATABASE ERRORS - MMO Trader Market
-- Mã lỗi: 18bb956c-390b-4bfe-9a45-090db7749505
-- ============================================

USE mmo_schema;

-- ============================================
-- 1. TẠO VIEW product_sales_view
-- ============================================
-- View này tính tổng số lượng sản phẩm đã bán từ bảng orders
DROP VIEW IF EXISTS product_sales_view;

CREATE VIEW product_sales_view AS
SELECT 
    product_id,
    SUM(quantity) AS sold_count
FROM orders
WHERE status = 'Completed'
GROUP BY product_id;

-- Kiểm tra view đã tạo
SELECT 'View product_sales_view created successfully' AS status;

-- ============================================
-- 2. THÊM CÁC CỘT THIẾU VÀO BẢNG products
-- ============================================

-- Kiểm tra và thêm cột product_type nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'product_type'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN product_type ENUM(''EMAIL'', ''SOCIAL'', ''GAME'', ''SOFTWARE'', ''OTHER'') NOT NULL DEFAULT ''OTHER'' AFTER shop_id',
    'ALTER TABLE products MODIFY COLUMN product_type ENUM(''EMAIL'', ''SOCIAL'', ''GAME'', ''SOFTWARE'', ''OTHER'') NOT NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột product_subtype nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'product_subtype'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN product_subtype VARCHAR(100) NULL AFTER product_type',
    'SELECT ''Column product_subtype already exists'' AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột short_description nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'short_description'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN short_description VARCHAR(500) NULL AFTER name',
    'SELECT ''Column short_description already exists'' AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột primary_image_url nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'primary_image_url'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN primary_image_url VARCHAR(500) NULL AFTER price',
    'SELECT ''Column primary_image_url already exists'' AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột gallery_json nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'gallery_json'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN gallery_json TEXT NULL AFTER primary_image_url',
    'SELECT ''Column gallery_json already exists'' AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột variant_schema nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'variant_schema'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN variant_schema VARCHAR(50) NULL DEFAULT ''none'' AFTER status',
    'ALTER TABLE products MODIFY COLUMN variant_schema VARCHAR(50) NULL DEFAULT ''none'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột variants_json nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'mmo_schema' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'variants_json'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE products ADD COLUMN variants_json TEXT NULL AFTER variant_schema',
    'SELECT ''Column variants_json already exists'' AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 3. KIỂM TRA KẾT QUẢ
-- ============================================
SELECT 'Database migration completed successfully!' AS status;

-- Hiển thị cấu trúc bảng products sau khi sửa
DESCRIBE products;

-- Kiểm tra view đã tạo
SHOW CREATE VIEW product_sales_view;

