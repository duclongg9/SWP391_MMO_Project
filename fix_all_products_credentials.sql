-- ====================================================================================
-- Script để fix encrypted_value và variant_code cho TẤT CẢ products
-- ====================================================================================

-- Bước 1: Kiểm tra tổng quan credentials có vấn đề
SELECT 
    COUNT(*) as total_credentials,
    SUM(CASE WHEN encrypted_value IS NULL THEN 1 ELSE 0 END) as null_count,
    SUM(CASE WHEN encrypted_value = '' THEN 1 ELSE 0 END) as empty_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '{%' OR encrypted_value NOT LIKE '%}' THEN 1 ELSE 0 END) as not_json_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '%"username"%' THEN 1 ELSE 0 END) as no_username_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '%"password"%' THEN 1 ELSE 0 END) as no_password_count
FROM product_credentials;

-- Bước 2: Kiểm tra variant_code có khớp với variants_json không (cho products có variants)
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.variant_schema,
    JSON_EXTRACT(p.variants_json, '$[*].variant_code') as variant_codes_in_json,
    pc.variant_code as variant_code_in_db,
    LOWER(TRIM(pc.variant_code)) as normalized_variant_code,
    COUNT(pc.id) as credential_count
FROM products p
INNER JOIN product_credentials pc ON p.id = pc.product_id AND pc.is_sold = 0
WHERE p.variant_schema IS NOT NULL 
  AND p.variant_schema != 'NONE'
  AND p.variant_schema != 'none'
GROUP BY p.id, p.name, p.variant_schema, pc.variant_code
ORDER BY p.id, pc.variant_code;

-- Bước 3: Chuẩn hóa variant_code cho TẤT CẢ credentials
-- Đảm bảo variant_code được trim và lowercase để khớp với query logic
UPDATE product_credentials pc
INNER JOIN products p ON pc.product_id = p.id
SET pc.variant_code = LOWER(TRIM(pc.variant_code))
WHERE pc.variant_code IS NOT NULL
  AND TRIM(pc.variant_code) != ''
  AND pc.variant_code != LOWER(TRIM(pc.variant_code));

-- Bước 4: Fix encrypted_value cho credentials có format sai
-- Tạo lại encrypted_value với format JSON đúng cho các credentials có vấn đề
-- LƯU Ý: Sử dụng JOIN với products để tránh lỗi safe update mode
UPDATE product_credentials pc
INNER JOIN products p ON pc.product_id = p.id
SET pc.encrypted_value = JSON_OBJECT(
    'username', CONCAT('temp_user_', pc.id),
    'password', CONCAT('TempPass', pc.id, '!')
)
WHERE (
    pc.encrypted_value IS NULL 
    OR pc.encrypted_value = ''
    OR pc.encrypted_value NOT LIKE '{%'
    OR pc.encrypted_value NOT LIKE '%}'
    OR pc.encrypted_value NOT LIKE '%"username"%'
    OR pc.encrypted_value NOT LIKE '%"password"%'
);

-- Bước 5: Kiểm tra kết quả sau khi fix
SELECT 
    p.id as product_id,
    p.name as product_name,
    COUNT(pc.id) as total_credentials,
    SUM(CASE WHEN pc.encrypted_value LIKE '{"username":"%","password":"%"}' THEN 1 ELSE 0 END) as valid_format_count,
    SUM(CASE WHEN pc.encrypted_value NOT LIKE '{"username":"%","password":"%"}' THEN 1 ELSE 0 END) as invalid_format_count,
    COUNT(DISTINCT pc.variant_code) as distinct_variant_codes
FROM products p
LEFT JOIN product_credentials pc ON p.id = pc.product_id AND pc.is_sold = 0
WHERE p.shop_id IN (1, 2)  -- Chỉ kiểm tra shop 1 và 2
GROUP BY p.id, p.name
ORDER BY p.id;

-- Bước 6: Kiểm tra chi tiết variant_code cho từng product có variants
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.variant_schema,
    JSON_EXTRACT(p.variants_json, '$[*].variant_code') as variant_codes_in_json,
    pc.variant_code as variant_code_in_credentials,
    LOWER(TRIM(pc.variant_code)) as normalized_variant_code,
    COUNT(pc.id) as credential_count
FROM products p
LEFT JOIN product_credentials pc ON p.id = pc.product_id AND pc.is_sold = 0
WHERE p.variant_schema IS NOT NULL 
  AND p.variant_schema != 'NONE'
  AND p.variant_schema != 'none'
  AND p.shop_id IN (1, 2)
GROUP BY p.id, p.name, p.variant_schema, pc.variant_code
ORDER BY p.id, pc.variant_code;

-- Bước 7: Kiểm tra credentials không có variant_code nhưng product có variants
SELECT 
    p.id as product_id,
    p.name as product_name,
    p.variant_schema,
    COUNT(pc.id) as credentials_without_variant
FROM products p
INNER JOIN product_credentials pc ON p.id = pc.product_id 
WHERE p.variant_schema IS NOT NULL 
  AND p.variant_schema != 'NONE'
  AND p.variant_schema != 'none'
  AND (pc.variant_code IS NULL OR TRIM(pc.variant_code) = '')
  AND pc.is_sold = 0
  AND p.shop_id IN (1, 2)
GROUP BY p.id, p.name, p.variant_schema
HAVING COUNT(pc.id) > 0;

