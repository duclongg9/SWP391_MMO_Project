-- ============================================================================
-- Dispute reporting schema (simplified)
--
-- Tài liệu này cung cấp phiên bản rút gọn của các bảng cần thiết để vận hành
-- chức năng báo cáo đơn hàng và đóng băng escrow. Phần mềm thực tế sử dụng
-- schema đầy đủ trong DB_v1.sql, tuy nhiên đoạn script dưới đây tập trung vào
-- những bảng và cột tối thiểu để đội phát triển dễ nắm bắt mối quan hệ dữ
-- liệu cốt lõi.
--
-- Lưu ý: bản đầy đủ còn có bảng order_escrow_events và order_escrow_adjustments
-- để audit chi tiết từng lần tạm dừng/gia hạn escrow.
-- ============================================================================

DROP TABLE IF EXISTS dispute_attachments;
DROP TABLE IF EXISTS disputes;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS users;

-- --------------------------------------------------------------------------
-- Người dùng hệ thống. Chỉ giữ các cột quan trọng phục vụ demo khiếu nại.
-- --------------------------------------------------------------------------
CREATE TABLE users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    role            ENUM('Admin','Seller','Buyer') NOT NULL DEFAULT 'Buyer',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------------
-- Đơn hàng có bổ sung các trường escrow tối thiểu để biểu diễn luồng freeze.
-- --------------------------------------------------------------------------
CREATE TABLE orders (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id                    INT NOT NULL,
    product_title               VARCHAR(255) NOT NULL,
    total_amount                DECIMAL(18,2) NOT NULL,
    status                      ENUM('Pending','Completed','Disputed','Refunded') NOT NULL DEFAULT 'Pending',
    escrow_status               ENUM('Scheduled','Paused','Released') DEFAULT 'Scheduled',
    escrow_release_at           TIMESTAMP NULL,
    escrow_hold_seconds         INT NULL,
    escrow_remaining_seconds    INT NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- --------------------------------------------------------------------------
-- Báo cáo đơn hàng của người mua. "order_reference_code" giúp admin tra cứu.
-- --------------------------------------------------------------------------
CREATE TABLE disputes (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    order_id                    INT NOT NULL,
    order_reference_code        VARCHAR(50) NOT NULL UNIQUE,
    reporter_id                 INT NOT NULL,
    issue_type                  ENUM('ACCOUNT_NOT_WORKING','ACCOUNT_DUPLICATED','ACCOUNT_EXPIRED','ACCOUNT_MISSING','OTHER')
                                NOT NULL,
    custom_issue_title          VARCHAR(255) NULL,
    reason                      TEXT NOT NULL,
    status                      ENUM('Open','InReview','ResolvedWithRefund','ResolvedWithoutRefund','Closed','Cancelled')
                                NOT NULL DEFAULT 'Open',
    escrow_paused_at            TIMESTAMP NULL,
    escrow_remaining_seconds    INT NULL,
    resolved_at                 TIMESTAMP NULL,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_disputes_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_disputes_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

-- --------------------------------------------------------------------------
-- Ảnh bằng chứng mà người mua gửi kèm khi tạo dispute.
-- --------------------------------------------------------------------------
CREATE TABLE dispute_attachments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_id      INT NOT NULL,
    file_path       VARCHAR(512) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dispute_attachments_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id)
);

-- --------------------------------------------------------------------------
-- Dữ liệu mẫu minh họa một đơn hàng hoàn tất, một đơn bị report và ảnh chứng.
-- --------------------------------------------------------------------------
INSERT INTO users (email, display_name, role) VALUES
    ('buyer@example.com', 'Người mua', 'Buyer'),
    ('seller@example.com', 'Người bán', 'Seller'),
    ('admin@example.com', 'Quản trị viên', 'Admin');

INSERT INTO orders (buyer_id, product_title, total_amount, status, escrow_status, escrow_release_at, escrow_hold_seconds)
VALUES
    (1, 'Gói tài khoản Canva Pro 1 tháng', 150000.00, 'Completed', 'Scheduled', NOW() + INTERVAL 24 HOUR, 86400);

INSERT INTO disputes (order_id, order_reference_code, reporter_id, issue_type, reason, status, escrow_paused_at, escrow_remaining_seconds)
VALUES
    (1, 'ORD-000001', 1, 'ACCOUNT_NOT_WORKING',
     'Tài khoản nhận được không đăng nhập được, đã thử nhiều lần trước khi mở khóa.',
     'Open', NOW(), 43200);

INSERT INTO dispute_attachments (dispute_id, file_path)
VALUES
    (1, 'assets/uploads/disputes/evidence-login-error.png');

