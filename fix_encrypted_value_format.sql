-- ====================================================================================
-- Script để kiểm tra và fix encrypted_value không đúng format JSON
-- ====================================================================================

-- Bước 1: Kiểm tra credentials có encrypted_value không đúng format
SELECT 
    id,
    product_id,
    variant_code,
    encrypted_value,
    LENGTH(encrypted_value) as value_length,
    CASE 
        WHEN encrypted_value IS NULL THEN 'NULL'
        WHEN encrypted_value = '' THEN 'EMPTY'
        WHEN encrypted_value NOT LIKE '{%' THEN 'NOT_JSON_START'
        WHEN encrypted_value NOT LIKE '%}' THEN 'NOT_JSON_END'
        WHEN encrypted_value NOT LIKE '%"username"%' THEN 'NO_USERNAME'
        WHEN encrypted_value NOT LIKE '%"password"%' THEN 'NO_PASSWORD'
        ELSE 'OK'
    END as format_status
FROM product_credentials
WHERE product_id = 1003
ORDER BY id DESC
LIMIT 20;

-- Bước 2: Đếm số credentials có vấn đề
SELECT 
    COUNT(*) as total_credentials,
    SUM(CASE WHEN encrypted_value IS NULL THEN 1 ELSE 0 END) as null_count,
    SUM(CASE WHEN encrypted_value = '' THEN 1 ELSE 0 END) as empty_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '{%' OR encrypted_value NOT LIKE '%}' THEN 1 ELSE 0 END) as not_json_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '%"username"%' THEN 1 ELSE 0 END) as no_username_count,
    SUM(CASE WHEN encrypted_value NOT LIKE '%"password"%' THEN 1 ELSE 0 END) as no_password_count
FROM product_credentials
WHERE product_id = 1003;

-- Bước 3: Kiểm tra variant_code có khớp với variants_json không
SELECT 
    p.id as product_id,
    p.name as product_name,
    JSON_EXTRACT(p.variants_json, '$[*].variant_code') as variant_codes_in_json,
    pc.variant_code as variant_code_in_db,
    LOWER(TRIM(pc.variant_code)) as normalized_variant_code,
    COUNT(pc.id) as credential_count
FROM products p
LEFT JOIN product_credentials pc ON p.id = pc.product_id AND pc.is_sold = 0
WHERE p.id = 1003
GROUP BY p.id, p.name, pc.variant_code
ORDER BY pc.variant_code;

-- Bước 4: Fix encrypted_value cho credentials có format sai
-- Tạo lại encrypted_value với format JSON đúng cho các credentials có vấn đề
UPDATE product_credentials pc
INNER JOIN products p ON pc.product_id = p.id
SET pc.encrypted_value = JSON_OBJECT(
    'username', CONCAT('temp_user_', pc.id),
    'password', CONCAT('TempPass', pc.id, '!')
)
WHERE pc.product_id = 1003
  AND (
    pc.encrypted_value IS NULL 
    OR pc.encrypted_value = ''
    OR pc.encrypted_value NOT LIKE '{%'
    OR pc.encrypted_value NOT LIKE '%}'
    OR pc.encrypted_value NOT LIKE '%"username"%'
    OR pc.encrypted_value NOT LIKE '%"password"%'
  );

-- Bước 5: Kiểm tra lại sau khi fix
SELECT 
    id,
    product_id,
    variant_code,
    encrypted_value,
    CASE 
        WHEN encrypted_value LIKE '{"username":"%","password":"%"}' THEN 'OK'
        ELSE 'STILL_WRONG'
    END as format_status
FROM product_credentials
WHERE product_id = 1003
ORDER BY id DESC
LIMIT 10;

