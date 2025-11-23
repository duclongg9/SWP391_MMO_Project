-- ====================================================================================
-- Script để fix variant_code trong product_credentials
-- Đảm bảo variant_code khớp với variant_code trong variants_json của products
-- ====================================================================================

-- Bước 1: Kiểm tra variant_code hiện tại trong product_credentials
SELECT 
    pc.product_id,
    pc.variant_code AS current_variant_code,
    LOWER(TRIM(pc.variant_code)) AS normalized_variant_code,
    COUNT(*) AS credential_count
FROM product_credentials pc
WHERE pc.product_id = 1003
GROUP BY pc.product_id, pc.variant_code
ORDER BY pc.variant_code;

-- Bước 2: Kiểm tra variant_code trong variants_json của product
SELECT 
    id,
    name,
    variant_schema,
    variants_json
FROM products
WHERE id = 1003;

-- Bước 3: Chuẩn hóa variant_code trong product_credentials
-- Đảm bảo variant_code được trim và lowercase để khớp với query
UPDATE product_credentials
SET variant_code = LOWER(TRIM(variant_code))
WHERE product_id = 1003
  AND variant_code IS NOT NULL
  AND TRIM(variant_code) != '';

-- Bước 4: Kiểm tra lại sau khi update
SELECT 
    pc.product_id,
    pc.variant_code AS fixed_variant_code,
    COUNT(*) AS credential_count
FROM product_credentials pc
WHERE pc.product_id = 1003
GROUP BY pc.product_id, pc.variant_code
ORDER BY pc.variant_code;

-- Bước 5: Áp dụng cho TẤT CẢ products có variants
-- Chuẩn hóa variant_code cho tất cả credentials
-- LƯU Ý: Update theo từng product_id để tránh lỗi safe update mode
UPDATE product_credentials pc
INNER JOIN products p ON pc.product_id = p.id
SET pc.variant_code = LOWER(TRIM(pc.variant_code))
WHERE pc.variant_code IS NOT NULL
  AND TRIM(pc.variant_code) != ''
  AND pc.variant_code != LOWER(TRIM(pc.variant_code))
  AND p.variant_schema IS NOT NULL
  AND p.variant_schema != 'NONE'
  AND p.variant_schema != 'none';

-- Bước 6: Kiểm tra tổng quan
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    JSON_EXTRACT(p.variants_json, '$[*].variant_code') AS variant_codes_in_json,
    pc.variant_code AS variant_code_in_credentials,
    COUNT(pc.id) AS credential_count
FROM products p
LEFT JOIN product_credentials pc ON p.id = pc.product_id AND pc.is_sold = 0
WHERE p.variant_schema IS NOT NULL 
  AND p.variant_schema != 'NONE'
  AND p.variant_schema != 'none'
GROUP BY p.id, p.name, pc.variant_code
ORDER BY p.id, pc.variant_code;

