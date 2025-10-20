-- Script tạo bảng seller_requests cho hệ thống yêu cầu trở thành seller
-- Chạy script này trước khi sử dụng tính năng seller request

CREATE TABLE seller_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    business_name VARCHAR(100) NOT NULL,
    business_description TEXT NOT NULL,
    experience TEXT NOT NULL,
    contact_info VARCHAR(200) NOT NULL,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    rejection_reason TEXT NULL,
    reviewed_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    
    -- Foreign key constraints
    CONSTRAINT fk_seller_request_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_seller_request_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes for better performance
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_reviewed_by (reviewed_by)
);

-- Thêm comment cho các cột
ALTER TABLE seller_requests 
COMMENT = 'Bảng lưu trữ các yêu cầu trở thành seller',
MODIFY COLUMN id INT AUTO_INCREMENT COMMENT 'ID yêu cầu',
MODIFY COLUMN user_id INT NOT NULL COMMENT 'ID của user gửi yêu cầu',
MODIFY COLUMN business_name VARCHAR(100) NOT NULL COMMENT 'Tên doanh nghiệp/gian hàng dự kiến',
MODIFY COLUMN business_description TEXT NOT NULL COMMENT 'Mô tả chi tiết về doanh nghiệp',
MODIFY COLUMN experience TEXT NOT NULL COMMENT 'Kinh nghiệm bán hàng của user',
MODIFY COLUMN contact_info VARCHAR(200) NOT NULL COMMENT 'Thông tin liên hệ để xác minh',
MODIFY COLUMN status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending' COMMENT 'Trạng thái yêu cầu',
MODIFY COLUMN rejection_reason TEXT NULL COMMENT 'Lý do từ chối (nếu có)',
MODIFY COLUMN reviewed_by INT NULL COMMENT 'ID admin xử lý yêu cầu',
MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian gửi yêu cầu',
MODIFY COLUMN reviewed_at TIMESTAMP NULL COMMENT 'Thời gian admin xử lý yêu cầu';

-- Sample data cho testing (optional)
-- INSERT INTO seller_requests (user_id, business_name, business_description, experience, contact_info) VALUES
-- (3, 'Game Store Pro', 'Chuyên bán các tài khoản game MMO chất lượng cao với giá cả hợp lý. Cam kết tài khoản sạch, không hack, có đầy đủ thông tin đăng nhập.', 'Đã có 2 năm kinh nghiệm bán tài khoản game trên các forum và Facebook. Đã giao dịch thành công với hơn 100 khách hàng.', 'SĐT: 0987654321, Facebook: facebook.com/gamestore, Email: contact@gamestore.com');

-- Kiểm tra dữ liệu
-- SELECT * FROM seller_requests;
