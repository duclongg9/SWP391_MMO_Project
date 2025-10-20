-- Script cập nhật bảng seller_requests thành hệ thống KYC
-- Chạy script này để thêm các cột KYC

-- Thêm các cột mới cho KYC
ALTER TABLE seller_requests 
ADD COLUMN full_name VARCHAR(100) NOT NULL AFTER user_id,
ADD COLUMN date_of_birth DATE NOT NULL AFTER full_name,
ADD COLUMN id_number VARCHAR(20) NOT NULL AFTER date_of_birth,
ADD COLUMN front_id_image_path VARCHAR(255) NULL AFTER id_number,
ADD COLUMN back_id_image_path VARCHAR(255) NULL AFTER front_id_image_path,
ADD COLUMN selfie_image_path VARCHAR(255) NULL AFTER back_id_image_path;

-- Thêm indexes cho performance
ALTER TABLE seller_requests
ADD INDEX idx_id_number (id_number),
ADD INDEX idx_full_name (full_name),
ADD INDEX idx_date_of_birth (date_of_birth);

-- Thêm constraints
ALTER TABLE seller_requests
ADD CONSTRAINT chk_id_number_length CHECK (CHAR_LENGTH(id_number) >= 9),
ADD CONSTRAINT chk_full_name_length CHECK (CHAR_LENGTH(full_name) >= 2);

-- Cập nhật comments
ALTER TABLE seller_requests 
COMMENT = 'Bảng lưu trữ các yêu cầu KYC để trở thành seller',
MODIFY COLUMN full_name VARCHAR(100) NOT NULL COMMENT 'Họ tên đầy đủ từ CCCD',
MODIFY COLUMN date_of_birth DATE NOT NULL COMMENT 'Ngày sinh từ CCCD',
MODIFY COLUMN id_number VARCHAR(20) NOT NULL COMMENT 'Số CCCD/CMND',
MODIFY COLUMN front_id_image_path VARCHAR(255) NULL COMMENT 'Đường dẫn ảnh CCCD mặt trước',
MODIFY COLUMN back_id_image_path VARCHAR(255) NULL COMMENT 'Đường dẫn ảnh CCCD mặt sau',
MODIFY COLUMN selfie_image_path VARCHAR(255) NULL COMMENT 'Đường dẫn ảnh selfie';

-- Sample data cho testing (optional - comment out khi không cần)
/*
INSERT INTO seller_requests (
    user_id, full_name, date_of_birth, id_number,
    front_id_image_path, back_id_image_path, selfie_image_path,
    business_name, business_description, experience, contact_info
) VALUES (
    3, 'Nguyễn Văn A', '1990-05-15', '123456789012',
    'kyc/3/front_12345.jpg', 'kyc/3/back_12345.jpg', 'kyc/3/selfie_12345.jpg',
    'Game Store Pro', 
    'Chuyên bán các tài khoản game MMO chất lượng cao với giá cả hợp lý.',
    'Đã có 2 năm kinh nghiệm bán tài khoản game trên các forum.',
    'SĐT: 0987654321, Facebook: facebook.com/gamestore'
);
*/

-- Kiểm tra cấu trúc bảng sau khi update
DESCRIBE seller_requests;
