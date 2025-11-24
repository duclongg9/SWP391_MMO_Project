-- ====================================================================================
-- MMO SCHEMA - FULL DB (Optimized Products + Vietnamese Seed)
-- Requires: MySQL 8.x
-- ====================================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS mmo_schema;
CREATE DATABASE mmo_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mmo_schema;

-- =================================================================
-- Section 1: Core Tables (unchanged business semantics)
-- =================================================================

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  `name` varchar(100) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `hashed_password` varchar(255) DEFAULT NULL COMMENT 'NULL đối với tài khoản đăng nhập qua SSO',
  `google_id` varchar(255) DEFAULT NULL UNIQUE,
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1: Active, 0: Inactive , 2: UnAuth',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `email_verification_tokens`;
CREATE TABLE `email_verification_tokens` (
  `user_id` int NOT NULL,
  `code` varchar(16) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email_verification_tokens_code` (`code`),
  CONSTRAINT `fk_email_verification_tokens_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `password_reset_tokens`;
CREATE TABLE `password_reset_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(64) NOT NULL UNIQUE,
  `expires_at` timestamp NOT NULL,
  `used_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_password_reset_user` (`user_id`)
) ENGINE=InnoDB;


DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops` (
  `id` int NOT NULL AUTO_INCREMENT,
  `owner_id` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  `admin_note` text DEFAULT NULL,
  `status` enum('Pending','Active','Suspended') NOT NULL DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- ========================= PRODUCTS (optimized) =========================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int NOT NULL,

  -- Loại & Subtype cố định (ENUM)
  `product_type` ENUM('EMAIL','SOCIAL','SOFTWARE','GAME','OTHER') NOT NULL,
  `product_subtype` ENUM('GMAIL','YAHOO','OUTLOOK','FACEBOOK','TIKTOK','X','CANVA','OFFICE','WINDOWS','CHATGPT','VALORANT','LEAGUE_OF_LEGENDS','CS2','OTHER') NOT NULL DEFAULT 'OTHER',


  `name` varchar(255) NOT NULL,
  `short_description` varchar(300) DEFAULT NULL,
  `description` text,

  `price` decimal(18,4) NOT NULL,                -- giá cơ sở (min price nếu có biến thể)
  `primary_image_url` varchar(512) DEFAULT NULL, -- ảnh đại diện
  `gallery_json` json DEFAULT NULL,              -- mảng URL ảnh

  `inventory_count` int NOT NULL DEFAULT 0,
  `sold_count` int NOT NULL DEFAULT 0,

  `status` enum('Available','OutOfStock','Unlisted') NOT NULL DEFAULT 'Available',

  -- Biến thể gọn trong 1 cột JSON + schema để UI render
  `variant_schema` ENUM('NONE','COLOR_SIZE','DURATION_PLAN','EDITION_LICENSE','CUSTOM') NOT NULL DEFAULT 'NONE',
  `variants_json` json DEFAULT NULL,             -- [{variant_code, attributes:{...}, price, inventory_count, status}]

  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `product_credentials`;
CREATE TABLE `product_credentials` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `order_id` int DEFAULT NULL,
  `encrypted_value` text NOT NULL,
  `variant_code` varchar(100) DEFAULT NULL,
  `is_sold` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `credential_view_logs`;
CREATE TABLE `credential_view_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `buyer_id` int NOT NULL,
  `variant_code` varchar(100) DEFAULT NULL,
  `viewer_ip` varchar(64) DEFAULT NULL,
  `viewed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credential_view_logs_order_buyer` (`order_id`,`buyer_id`)
) ENGINE=InnoDB;

-- =================================================================
-- Section 2: KYC
-- =================================================================
DROP TABLE IF EXISTS `kyc_request_statuses`;
CREATE TABLE `kyc_request_statuses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status_name` varchar(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `kyc_requests`;
CREATE TABLE `kyc_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `status_id` int NOT NULL,
  `front_image_url` varchar(255) NOT NULL,
  `back_image_url` varchar(255) NOT NULL,
  `selfie_image_url` varchar(255) NOT NULL,
  `id_number` varchar(50) NOT NULL,
  `admin_feedback` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- =================================================================
-- Section 3: Financial & Transactional
-- =================================================================
DROP TABLE IF EXISTS `wallets`;
CREATE TABLE `wallets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL UNIQUE,
  `balance` decimal(18,4) NOT NULL DEFAULT '0.0000',
  `status` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `wallet_transactions`;
CREATE TABLE `wallet_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `wallet_id` int NOT NULL,
  `related_entity_id` int DEFAULT NULL,
  `transaction_type` enum('Deposit','Purchase','Withdrawal','Refund','Fee','Payout') NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `balance_before` decimal(18,4) NOT NULL,
  `balance_after` decimal(18,4) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `orders`;
-- Bảng lưu đơn hàng của người mua, phục vụ toàn bộ luồng mua - thanh toán - bàn giao.
-- Cột `payment_transaction_id` liên kết sang bảng wallet_transactions giúp truy vết dòng tiền.
-- `idempotency_key` lưu khóa duy nhất mà OrderService cấp phát để chống việc double-submit khi mua.
-- `variant_code` cho phép worker xác định SKU cụ thể để trừ tồn kho và credential tương ứng.
-- `hold_until` dùng cho cơ chế giữ hàng tạm thời (nếu có), worker sẽ tham chiếu để quyết định giải phóng.
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `buyer_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(18,4) NOT NULL,
  `payment_transaction_id` int DEFAULT NULL,
  `total_amount` decimal(18,4) NOT NULL,
  `status` enum('Pending','Processing','Completed','Failed','Refunded','Disputed') NOT NULL,
  `idempotency_key` varchar(36) DEFAULT NULL UNIQUE,
  `variant_code` varchar(100) DEFAULT NULL,
  `hold_until` timestamp NULL DEFAULT NULL,
  `escrow_hold_seconds` int DEFAULT NULL COMMENT 'Ảnh chụp thời lượng giữ tiền (tính bằng giây) theo cấu hình tại thời điểm tạo đơn',
  `escrow_original_release_at` timestamp NULL DEFAULT NULL COMMENT 'Mốc giải ngân escrow ban đầu trước khi phát sinh khiếu nại',
  `escrow_release_at` timestamp NULL DEFAULT NULL COMMENT 'Thời điểm dự kiến giải ngân tự động cho seller khi không có khiếu nại',
  `escrow_status` enum('Scheduled','Paused','Released','Cancelled') NOT NULL DEFAULT 'Scheduled' COMMENT 'Trạng thái giữ tiền escrow của đơn',
  `escrow_paused_at` timestamp NULL DEFAULT NULL COMMENT 'Mốc thời gian hệ thống dừng đếm escrow do khiếu nại/report',
  `escrow_remaining_seconds` int DEFAULT NULL COMMENT 'Số giây escrow còn lại tại thời điểm tạm dừng để hoàn trả khi tiếp tục',
  `escrow_resumed_at` timestamp NULL DEFAULT NULL COMMENT 'Thời điểm hệ thống tiếp tục đếm escrow sau khi mở khóa report',
  `escrow_released_at_actual` timestamp NULL DEFAULT NULL COMMENT 'Mốc escrow thực sự giải ngân cho seller sau khi kết thúc xử lý',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Nhật ký chi tiết từng lần thay đổi trạng thái escrow của đơn hàng.
DROP TABLE IF EXISTS `order_escrow_events`;
CREATE TABLE `order_escrow_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `event_type` enum('CREATED','PAUSED','RESUMED','ADJUSTED','RELEASED','CANCELLED') NOT NULL,
  `actor_type` enum('SYSTEM','ADMIN','BUYER') NOT NULL,
  `actor_admin_id` int DEFAULT NULL,
  `related_dispute_id` int DEFAULT NULL,
  `release_at_snapshot` timestamp NULL DEFAULT NULL,
  `remaining_seconds_snapshot` int DEFAULT NULL,
  `metadata` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Bảng lưu lịch sử admin điều chỉnh thời gian giữ tiền escrow.
DROP TABLE IF EXISTS `order_escrow_adjustments`;
CREATE TABLE `order_escrow_adjustments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `event_id` bigint NOT NULL,
  `admin_id` int NOT NULL,
  `related_dispute_id` int DEFAULT NULL,
  `previous_release_at` timestamp NULL DEFAULT NULL,
  `previous_remaining_seconds` int DEFAULT NULL,
  `new_release_at` timestamp NULL DEFAULT NULL,
  `new_remaining_seconds` int DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- View tổng hợp số lượng sản phẩm đã bán dựa trên bảng orders
DROP VIEW IF EXISTS `product_sales_view`;
CREATE VIEW `product_sales_view` AS
SELECT
  o.product_id,
  SUM(o.quantity) AS sold_count
FROM orders o
WHERE o.status = 'Completed'
GROUP BY o.product_id;

DROP TABLE IF EXISTS `deposit_requests`;
CREATE TABLE `deposit_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `qr_content` varchar(255),
  `idempotency_key` varchar(36) DEFAULT NULL UNIQUE,
  `status` enum('Pending','Completed','Failed','Expired','RequiresManualCheck') NOT NULL DEFAULT 'Pending',
  `expires_at` timestamp NOT NULL,
  `admin_note` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- =================================================================
-- VNPAY audit trail for wallet deposits (linked via deposit_request)
-- =================================================================
DROP TABLE IF EXISTS `vnpay_transaction`;
CREATE TABLE `vnpay_transaction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `deposit_request_id` int NOT NULL,
  `link_data` json NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vnpay_status` varchar(16) NOT NULL DEFAULT 'pending',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vnpay_tx_deposit` (`deposit_request_id`),
  CONSTRAINT `fk_vnpay_tx_deposit` FOREIGN KEY (`deposit_request_id`)
    REFERENCES `deposit_requests`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `withdrawal_requests`;
CREATE TABLE `withdrawal_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `bank_account_info` text NOT NULL,
  `status` enum('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending',
  `admin_proof_url` varchar(255) DEFAULT NULL,
  `admin_note` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `withdrawal_rejection_reasons`;
CREATE TABLE `withdrawal_rejection_reasons` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `reason_code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `withdrawal_request_reasons_map`;
CREATE TABLE `withdrawal_request_reasons_map` (
    `request_id` INTEGER NOT NULL,
    `reason_id` INTEGER NOT NULL,
    PRIMARY KEY (`request_id`, `reason_id`)
) ENGINE=InnoDB;

-- =================================================================
-- Section 4: Support & Communication
-- =================================================================
-- Bảng dispute mở rộng để lưu report đơn hàng (kèm phân loại, ảnh bằng chứng, thông tin escrow).
DROP TABLE IF EXISTS `disputes`;
CREATE TABLE `disputes` (
    `id` int NOT NULL AUTO_INCREMENT,
    `order_id` int NOT NULL,
    `order_reference_code` varchar(50) NOT NULL,
    `reporter_id` int NOT NULL,
    `resolved_by_admin_id` int DEFAULT NULL,
    `issue_type` enum('ACCOUNT_NOT_WORKING','ACCOUNT_DUPLICATED','ACCOUNT_EXPIRED','ACCOUNT_MISSING','OTHER') NOT NULL,
    `custom_issue_title` varchar(255) DEFAULT NULL,
    `reason` text NOT NULL,
    `status` enum('Open','InReview','ResolvedWithRefund','ResolvedWithoutRefund','Closed','Cancelled') NOT NULL DEFAULT 'Open',
    `escrow_paused_at` timestamp NULL DEFAULT NULL,
    `escrow_remaining_seconds` int DEFAULT NULL,
    `resolved_at` timestamp NULL DEFAULT NULL,
    `resolution_note` text DEFAULT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_disputes_order_reference_code` (`order_reference_code`),
    UNIQUE KEY `uk_disputes_order_id` (`order_id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `dispute_attachments`;
CREATE TABLE `dispute_attachments` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `dispute_id` int NOT NULL,
    `file_path` varchar(512) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;


-- =================================================================
-- Section 5: Auditing & System
-- =================================================================
DROP TABLE IF EXISTS `inventory_logs`;
CREATE TABLE `inventory_logs` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `product_id` int NOT NULL,
    `related_order_id` int DEFAULT NULL,
    `change_amount` int NOT NULL,
    `reason` varchar(255) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL UNIQUE,
  `config_value` varchar(255) NOT NULL,
  `description` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- =================================================================
-- Section 6: Foreign Keys
-- =================================================================

ALTER TABLE `users` ADD CONSTRAINT `fk_users_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

ALTER TABLE `shops` ADD CONSTRAINT `fk_shops_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`);

ALTER TABLE `products` ADD CONSTRAINT `fk_products_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`);
ALTER TABLE `product_credentials` ADD CONSTRAINT `fk_credentials_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
ALTER TABLE `product_credentials` ADD CONSTRAINT `fk_credentials_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);
ALTER TABLE `credential_view_logs` ADD CONSTRAINT `fk_credential_view_logs_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);
ALTER TABLE `credential_view_logs` ADD CONSTRAINT `fk_credential_view_logs_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
ALTER TABLE `credential_view_logs` ADD CONSTRAINT `fk_credential_view_logs_buyer_id` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`);

ALTER TABLE `kyc_requests` ADD CONSTRAINT `fk_kyc_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `kyc_requests` ADD CONSTRAINT `fk_kyc_status_id` FOREIGN KEY (`status_id`) REFERENCES `kyc_request_statuses` (`id`);

ALTER TABLE `wallets` ADD CONSTRAINT `fk_wallets_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `wallet_transactions` ADD CONSTRAINT `fk_transactions_wallet_id` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`);

ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_buyer_id` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`);
ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_payment_transaction_id` FOREIGN KEY (`payment_transaction_id`) REFERENCES `wallet_transactions` (`id`);

ALTER TABLE `order_escrow_events` ADD CONSTRAINT `fk_escrow_events_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE;
ALTER TABLE `order_escrow_events` ADD CONSTRAINT `fk_escrow_events_admin_id` FOREIGN KEY (`actor_admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `order_escrow_events` ADD CONSTRAINT `fk_escrow_events_dispute_id` FOREIGN KEY (`related_dispute_id`) REFERENCES `disputes` (`id`);

ALTER TABLE `order_escrow_adjustments` ADD CONSTRAINT `fk_escrow_adjustments_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE;
ALTER TABLE `order_escrow_adjustments` ADD CONSTRAINT `fk_escrow_adjustments_event_id` FOREIGN KEY (`event_id`) REFERENCES `order_escrow_events` (`id`) ON DELETE CASCADE;
ALTER TABLE `order_escrow_adjustments` ADD CONSTRAINT `fk_escrow_adjustments_admin_id` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `order_escrow_adjustments` ADD CONSTRAINT `fk_escrow_adjustments_dispute_id` FOREIGN KEY (`related_dispute_id`) REFERENCES `disputes` (`id`);


ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);
ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`);
ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_admin_id` FOREIGN KEY (`resolved_by_admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `dispute_attachments` ADD CONSTRAINT `fk_dispute_attachments_dispute_id` FOREIGN KEY (`dispute_id`) REFERENCES `disputes` (`id`) ON DELETE CASCADE;

ALTER TABLE `deposit_requests` ADD CONSTRAINT `fk_deposits_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `withdrawal_requests` ADD CONSTRAINT `fk_withdrawals_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `withdrawal_request_reasons_map` ADD CONSTRAINT `fk_map_request_id` FOREIGN KEY (`request_id`) REFERENCES `withdrawal_requests` (`id`);
ALTER TABLE `withdrawal_request_reasons_map` ADD CONSTRAINT `fk_map_reason_id` FOREIGN KEY (`reason_id`) REFERENCES `withdrawal_rejection_reasons` (`id`);

ALTER TABLE `inventory_logs` ADD CONSTRAINT `fk_inventory_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
ALTER TABLE `inventory_logs` ADD CONSTRAINT `fk_inventory_order_id` FOREIGN KEY (`related_order_id`) REFERENCES `orders` (`id`);

-- =================================================================
-- Section 7: Indexes
-- =================================================================
CREATE INDEX `idx_users_email` ON `users`(`email`);
CREATE INDEX `idx_shops_owner_id` ON `shops`(`owner_id`);
CREATE INDEX `idx_products_shop_id` ON `products`(`shop_id`);
CREATE INDEX `idx_products_type_subtype` ON `products`(`product_type`,`product_subtype`);
CREATE INDEX `idx_products_status_created` ON `products`(`status`,`created_at`);
CREATE INDEX `idx_orders_buyer_id` ON `orders`(`buyer_id`);
CREATE INDEX `idx_orders_status` ON `orders`(`status`);
CREATE INDEX `idx_orders_escrow_status` ON `orders`(`escrow_status`);
CREATE INDEX `idx_escrow_events_order_id` ON `order_escrow_events`(`order_id`,`created_at`);
CREATE INDEX `idx_escrow_events_dispute` ON `order_escrow_events`(`related_dispute_id`);
CREATE INDEX `idx_escrow_adjustments_order` ON `order_escrow_adjustments`(`order_id`);
CREATE INDEX `idx_escrow_adjustments_admin` ON `order_escrow_adjustments`(`admin_id`);
CREATE INDEX `idx_disputes_status` ON `disputes`(`status`);
CREATE INDEX `idx_disputes_issue_type` ON `disputes`(`issue_type`);
CREATE INDEX `idx_dispute_attachments_dispute_id` ON `dispute_attachments`(`dispute_id`);
CREATE INDEX `idx_wallet_transactions_wallet_id` ON `wallet_transactions`(`wallet_id`);
CREATE INDEX `idx_wallet_transactions_type` ON `wallet_transactions`(`transaction_type`);
CREATE INDEX `idx_deposit_requests_status` ON `deposit_requests`(`status`);
CREATE INDEX `idx_withdrawal_requests_status` ON `withdrawal_requests`(`status`);


-- =================================================================
-- Section 8: Seed Data (Vietnamese)
-- =================================================================

-- Roles
INSERT INTO `roles` (`id`,`name`) VALUES
 (1,'ADMIN'),(2,'SELLER'),(3,'BUYER');

-- Users (hash minh hoạ)
INSERT INTO `users` (`id`,`role_id`,`email`,`name`,`avatar_url`,`hashed_password`,`google_id`,`status`,`created_at`,`updated_at`) VALUES
 (1,1,'admin@mmo.local','Trung tâm điều hành MMO',NULL,'Omq9O4yodqYbjkdPv6rfzV8TYEs=',NULL,1,'2024-01-10 09:00:00','2024-01-10 09:00:00'),
 (2,2,'seller@mmo.local','Người bán Cyber Gear','https://cdn.mmo.local/avatar/seller.png','Omq9O4yodqYbjkdPv6rfzV8TYEs=',NULL,1,'2024-01-12 08:00:00','2024-01-20 09:30:00'),
 (3,3,'buyer@mmo.local','Người mua Pro Gamer','https://cdn.mmo.local/avatar/buyer.png','Omq9O4yodqYbjkdPv6rfzV8TYEs=',NULL,1,'2024-01-15 07:45:00','2024-01-27 07:45:00');

-- KYC statuses
INSERT INTO `kyc_request_statuses` (`id`,`status_name`) VALUES
 (1,'Đang chờ'),(2,'Đã duyệt'),(3,'Từ chối');

-- INSERT INTO `kyc_requests` (`id`,`user_id`,`status_id`,`front_image_url`,`back_image_url`,`selfie_image_url`,`id_number`,`admin_feedback`,`created_at`,`reviewed_at`) VALUES
--  (1,2,2,'https://cdn.mmo.local/kyc/seller_front.jpg','https://cdn.mmo.local/kyc/seller_back.jpg','https://cdn.mmo.local/kyc/seller_selfie.jpg','079123456789','Hồ sơ hợp lệ','2024-01-13 10:30:00','2024-01-15 10:00:00'),
--  (2,3,1,'https://cdn.mmo.local/kyc/buyer_front.jpg','https://cdn.mmo.local/kyc/buyer_back.jpg','https://cdn.mmo.local/kyc/buyer_selfie.jpg','092987654321',NULL,'2024-01-25 09:10:00',NULL);

-- -- Shop
-- INSERT INTO `shops` (`id`,`owner_id`,`name`,`description`,`admin_note`,`status`,`created_at`,`updated_at`) VALUES
--  (1,2,'Cửa hàng Cyber Gear','Chuyên cung cấp tài khoản game và phần mềm bản quyền',NULL,'Active','2024-01-12 08:30:00','2024-01-20 09:30:00');

-- -- Products (6 sản phẩm mẫu, đủ loại/subtype; tiếng Việt, có biến thể JSON)
-- INSERT INTO `products`
-- (`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
-- VALUES
-- -- 1001: EMAIL/GMAIL
-- (1001,1,'EMAIL','GMAIL',
--  'Gmail Doanh nghiệp 50GB',
--  'Email doanh nghiệp 50GB, bảo hành, hỗ trợ đổi mật khẩu.',
--  'Tài khoản Gmail Doanh nghiệp 50GB. Bạn nhận được email và mật khẩu kèm hướng dẫn đổi bảo mật 2 lớp.',
--  250000.0000,
--   'gmail.png',
--   JSON_ARRAY('gmail2.jpg','mailedu.png'),
--  19,128,'Available','DURATION_PLAN',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','gmail-basic-1m','attributes', JSON_OBJECT('service','gmail','plan','basic','duration','1m'),'price',250000.0000,'inventory_count',10,'image_url','gmail.png','status','Available'),
--    JSON_OBJECT('variant_code','gmail-premium-12m','attributes', JSON_OBJECT('service','gmail','plan','premium','duration','12m'),'price',1200000.0000,'inventory_count',9,'image_url','gmail2.jpg','status','Available')
--  ),
--  '2024-01-14 11:00:00','2024-01-20 12:00:00'),

-- -- 1002: SOFTWARE/OTHER (Spotify)
-- (1002,1,'SOFTWARE','OTHER',
--  'Spotify Premium 12 tháng',
--  'Gia hạn Spotify Premium 12 tháng, tài khoản chính chủ.',
--  'Gia hạn Spotify Premium tài khoản chính chủ, bảo hành 30 ngày. Hỗ trợ kích hoạt nhanh.',
--  80000.0000,
--   'spotify.png',
--   JSON_ARRAY('telegram.png'),
--  27,256,'Available','DURATION_PLAN',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','sp-1m','attributes', JSON_OBJECT('service','spotify','duration','1m'),'price',80000.0000,'inventory_count',20,'image_url','spotify.png','status','Available'),
--    JSON_OBJECT('variant_code','sp-12m','attributes', JSON_OBJECT('service','spotify','duration','12m'),'price',185000.0000,'inventory_count',7,'image_url','telegram.png','status','Available')
--  ),
--  '2024-01-18 09:15:00','2024-01-26 08:30:00'),

-- -- 1003: SOFTWARE/OTHER (Windows key) - Unlisted
-- (1003,1,'SOFTWARE','OTHER',
--  'Key Windows 11 Pro',
--  'Key bản quyền Windows 11 Pro, kích hoạt online.',
--  'Khóa bản quyền Windows 11 Pro, kích hoạt online trọn đời. Lưu ý: sản phẩm hiện đang ẩn khỏi gian hàng.',
--  390000.0000,
--  'win11.jpg',
--  JSON_ARRAY('office.jpg'),
--  50,42,'Unlisted','EDITION_LICENSE',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','win11pro-oem','attributes', JSON_OBJECT('edition','pro','license','oem'),'price',390000.0000,'inventory_count',50,'image_url','win11.jpg','status','Unlisted')
--  ),
--  '2024-01-19 15:45:00','2024-01-19 15:45:00'),

-- -- 1004: SOCIAL/FACEBOOK
-- (1004,1,'SOCIAL','FACEBOOK',
--  'Tài khoản Facebook cổ 2009+',
--  'Facebook cổ năm 2009+, bảo hành đổi pass.',
--  'Tài khoản Facebook cổ (năm tạo 2009–2012), có bảo hành đổi mật khẩu. Dùng cho chạy quảng cáo & seeding.',
--  150000.0000,
--   'facebook.png',
--   JSON_ARRAY('fb2.png'),
--  30,73,'Available','CUSTOM',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','fb-2009','attributes', JSON_OBJECT('age','2009'),'price',180000.0000,'inventory_count',10,'image_url','facebook.png','status','Available'),
--    JSON_OBJECT('variant_code','fb-2012','attributes', JSON_OBJECT('age','2012'),'price',150000.0000,'inventory_count',20,'image_url','fb2.png','status','Available')
--  ),
--  NOW(),NOW()),

-- -- 1005: SOCIAL/TIKTOK
-- (1005,1,'SOCIAL','TIKTOK',
--  'Tài khoản TikTok Pro',
--  'Tài khoản TikTok Pro, bảo hành đăng nhập.',
--  'Tài khoản TikTok Pro, dùng quay và đăng video với analytics nâng cao.',
--  99000.0000,
--   'tiktok.png',
--   JSON_ARRAY('tiktok2.png','tiktoklive.png'),
--  40,58,'Available','CUSTOM',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','tt-pro','attributes', JSON_OBJECT('tier','pro'),'price',99000.0000,'inventory_count',40,'image_url','tiktok.png','status','Available')
--  ),
--  NOW(),NOW()),

-- -- 1006: GAME/VALORANT
-- (1006,1,'GAME','VALORANT',
--  'Valorant VP (top-up)',
--  'Nạp Valorant Points nhanh, giá tốt.',
--  'Dịch vụ nạp Valorant Points (VP) nhiều mệnh giá, xử lý trong 5–10 phút.',
--  95000.0000,
--   'valorant.png',
--   JSON_ARRAY('varo.png'),
--  100,121,'Available','CUSTOM',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','vp-470','attributes', JSON_OBJECT('amount','470VP'),'price',95000.0000,'inventory_count',50,'image_url','valorant.png','status','Available'),
--    JSON_OBJECT('variant_code','vp-1375','attributes', JSON_OBJECT('amount','1375VP'),'price',270000.0000,'inventory_count',50,'image_url','varo.png','status','Available')
--  ),
--  NOW(),NOW()),

-- -- 1007: SOFTWARE/CANVA
-- (1007,1,'SOFTWARE','CANVA',
--  'Canva Pro chính chủ',
--  'Canva Pro bản quyền 12 tháng, kích hoạt trực tiếp.',
--  'Cung cấp tài khoản Canva Pro chính chủ, kích hoạt ngay sau khi thanh toán, bảo hành 30 ngày.',
--  90000.0000,
--   'canva.jpg',
--   JSON_ARRAY('canva2.png'),
--  60,187,'Available','DURATION_PLAN',
--  JSON_ARRAY(
--    JSON_OBJECT('variant_code','canva-1m','attributes',JSON_OBJECT('duration','1m'),'price',90000.0000,'inventory_count',40,'image_url','canva.jpg','status','Available'),
--    JSON_OBJECT('variant_code','canva-12m','attributes',JSON_OBJECT('duration','12m'),'price',750000.0000,'inventory_count',20,'image_url','canva2.png','status','Available')
--  ),
--  '2024-01-18 10:00:00','2024-01-28 08:00:00');

-- INSERT INTO `products`
-- (`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
-- VALUES
-- (1008,1,'EMAIL','GMAIL','Gmail Doanh nghiệp 100GB','Gmail 100GB, bảo hành, hướng dẫn 2FA.','Tài khoản Gmail dung lượng 100GB, hỗ trợ bảo mật 2 lớp và khôi phục.',350000.0000,'gmail.png',JSON_ARRAY('gmail2.jpg','mailedu.png'),20,95,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','gmail-100g-1m','attributes', JSON_OBJECT('service','gmail','plan','100GB','duration','1m'),'price',350000.0000,'inventory_count',10,'image_url','gmail.png','status','Available'),
--   JSON_OBJECT('variant_code','gmail-100g-12m','attributes', JSON_OBJECT('service','gmail','plan','100GB','duration','12m'),'price',1800000.0000,'inventory_count',10,'image_url','mailedu.png','status','Available')
-- ),NOW(),NOW()),
-- (1009,1,'SOCIAL','FACEBOOK','Facebook cổ 2013+ (Mail+Pass)','Tài khoản Facebook cổ, có mail & pass.','Tài khoản Facebook năm 2013–2015, dành cho marketing hợp lệ theo chính sách.',180000.0000,'facebook.png',JSON_ARRAY('fb2.png'),25,210,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','fb-2013','attributes', JSON_OBJECT('year','2013','verify','mail+pass'),'price',180000.0000,'inventory_count',15,'image_url','facebook.png','status','Available'),
--   JSON_OBJECT('variant_code','fb-2015','attributes', JSON_OBJECT('year','2015','verify','mail+pass'),'price',160000.0000,'inventory_count',10,'image_url','fb2.png','status','Available')
-- ),NOW(),NOW()),
-- (1010,1,'SOCIAL','TIKTOK','TikTok Shop – Seller Pro','Thiết lập & tối ưu TikTok Shop.','Dịch vụ tạo TikTok Shop cho người bán mới, kèm tài liệu vận hành.',300000.0000,'tiktok.png',JSON_ARRAY('tiktok2.png','tiktoklive.png'),20,74,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','ttshop-basic-1m','attributes', JSON_OBJECT('service','tiktok_shop','plan','basic','duration','1m'),'price',300000.0000,'inventory_count',12,'image_url','tiktok.png','status','Available'),
--   JSON_OBJECT('variant_code','ttshop-pro-3m','attributes', JSON_OBJECT('service','tiktok_shop','plan','pro','duration','3m'),'price',700000.0000,'inventory_count',8,'image_url','tiktok2.png','status','Available')
-- ),NOW(),NOW()),
-- (1011,1,'SOFTWARE','CANVA','Canva Pro 1 tháng','Cấp quyền Canva Pro 1 tháng.','Tài khoản Canva Pro 1 tháng, dùng thiết kế không giới hạn mẫu.',90000.0000,'canva.jpg',JSON_ARRAY('canva2.png'),40,320,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','canva-1m','attributes', JSON_OBJECT('service','canva','plan','pro','duration','1m'),'price',90000.0000,'inventory_count',25,'image_url','canva.jpg','status','Available'),
--   JSON_OBJECT('variant_code','canva-12m','attributes', JSON_OBJECT('service','canva','plan','pro','duration','12m'),'price',850000.0000,'inventory_count',15,'image_url','canva2.png','status','Available')
-- ),NOW(),NOW()),
-- (1012,1,'SOFTWARE','CANVA','Canva Team 5 người','Nhóm Canva Pro 5 seats.','Canva Pro cho nhóm 5 người, chia sẻ brand kit & thư viện.',250000.0000,'canva2.png',JSON_ARRAY('canva.jpg'),18,96,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','canvateam5-1m','attributes', JSON_OBJECT('service','canva','seats',5,'duration','1m'),'price',250000.0000,'inventory_count',10,'image_url','canva2.png','status','Available'),
--   JSON_OBJECT('variant_code','canvateam5-12m','attributes', JSON_OBJECT('service','canva','seats',5,'duration','12m'),'price',2400000.0000,'inventory_count',8,'image_url','canva.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1013,1,'SOFTWARE','OTHER','Windows 11 Pro (Key Retail)','Key kích hoạt Windows 11 Pro bản quyền.','Cung cấp key Windows 11 Pro loại Retail, hướng dẫn active hợp lệ.',450000.0000,'win11.jpg',JSON_ARRAY('office.jpg'),30,190,'Available','EDITION_LICENSE',JSON_ARRAY(
--   JSON_OBJECT('variant_code','win11pro-retail','attributes', JSON_OBJECT('edition','Pro','activation','Retail'),'price',450000.0000,'inventory_count',30,'image_url','win11.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1014,1,'EMAIL','OTHER','Outlook 50GB','Hộp thư Outlook 50GB, bảo mật.','Tài khoản Outlook dung lượng 50GB, kèm hướng dẫn bảo mật & khôi phục.',200000.0000,'mailedu.png',JSON_ARRAY('gmail2.jpg','gmail.png'),14,57,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','outlook-1m','attributes', JSON_OBJECT('service','outlook','plan','standard','duration','1m'),'price',200000.0000,'inventory_count',8,'image_url','mailedu.png','status','Available'),
--   JSON_OBJECT('variant_code','outlook-12m','attributes', JSON_OBJECT('service','outlook','plan','standard','duration','12m'),'price',1200000.0000,'inventory_count',6,'image_url','gmail2.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1015,1,'GAME','VALORANT','Valorant Account – Rank Silver/Gold','Tài khoản Valorant rank Silver/Gold.','Tài khoản Valorant, vùng AP/SEA, bảo hành đăng nhập.',400000.0000,'valorant.png',JSON_ARRAY('varo.png'),12,45,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','valo-silver','attributes', JSON_OBJECT('rank','Silver','region','AP'),'price',400000.0000,'inventory_count',7,'image_url','valorant.png','status','Available'),
--   JSON_OBJECT('variant_code','valo-gold','attributes', JSON_OBJECT('rank','Gold','region','AP'),'price',550000.0000,'inventory_count',5,'image_url','varo.png','status','Available')
-- ),NOW(),NOW()),
-- (1016,1,'GAME','VALORANT','Nạp Valorant VP','Gói nạp VP chính chủ.','Dịch vụ nạp Valorant Points (VP) theo mệnh giá, giao nhanh.',160000.0000,'valorant.png',JSON_ARRAY('varo.png'),50,260,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','vp-475','attributes', JSON_OBJECT('amount','475'),'price',160000.0000,'inventory_count',20,'image_url','valorant.png','status','Available'),
--   JSON_OBJECT('variant_code','vp-1375','attributes', JSON_OBJECT('amount','1375'),'price',430000.0000,'inventory_count',20,'image_url','varo.png','status','Available'),
--   JSON_OBJECT('variant_code','vp-2400','attributes', JSON_OBJECT('amount','2400'),'price',720000.0000,'inventory_count',10,'image_url','valorant.png','status','Available')
-- ),NOW(),NOW()),
-- (1017,1,'SOCIAL','FACEBOOK','Business Manager (BM) gói hỗ trợ','Tư vấn & cấu hình BM.','Thiết lập BM, hướng dẫn bảo mật & phân quyền đúng chính sách.',220000.0000,'facebook.png',JSON_ARRAY('fb2.png'),22,81,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','bm-basic','attributes', JSON_OBJECT('package','basic','support','7d'),'price',220000.0000,'inventory_count',12,'image_url','facebook.png','status','Available'),
--   JSON_OBJECT('variant_code','bm-pro','attributes', JSON_OBJECT('package','pro','support','30d'),'price',480000.0000,'inventory_count',10,'image_url','fb2.png','status','Available')
-- ),NOW(),NOW()),
-- (1018,1,'SOCIAL','TIKTOK','TikTok Live – Setup & Coaching','Thiết lập Live + coaching.','Cài đặt TikTok Live Studio, cấu hình cảnh & âm thanh, huấn luyện vận hành.',350000.0000,'tiktoklive.png',JSON_ARRAY('tiktok.png','tiktok2.png'),18,69,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','ttlive-basic','attributes', JSON_OBJECT('package','basic','sessions',1),'price',350000.0000,'inventory_count',10,'image_url','tiktoklive.png','status','Available'),
--   JSON_OBJECT('variant_code','ttlive-pro','attributes', JSON_OBJECT('package','pro','sessions',3),'price',900000.0000,'inventory_count',8,'image_url','tiktok2.png','status','Available')
-- ),NOW(),NOW()),
-- (1019,1,'EMAIL','GMAIL','Gmail gói bảo hành 3 tháng','Gmail bảo hành 3 tháng, hỗ trợ bảo mật.','Tài khoản Gmail mới, hỗ trợ kỹ thuật trong 3 tháng.',270000.0000,'gmail.png',JSON_ARRAY('gmail2.jpg','mailedu.png'),20,102,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','gmail-3m','attributes', JSON_OBJECT('service','gmail','plan','standard','duration','3m'),'price',270000.0000,'inventory_count',12,'image_url','gmail.png','status','Available'),
--   JSON_OBJECT('variant_code','gmail-12m','attributes', JSON_OBJECT('service','gmail','plan','standard','duration','12m'),'price',1000000.0000,'inventory_count',8,'image_url','mailedu.png','status','Available')
-- ),NOW(),NOW()),
-- (1020,1,'SOFTWARE','OTHER','Office 365 Family 12 tháng (6 người)','Chia sẻ Office 365 Family hợp lệ.','Gói Office 365 Family 12 tháng cho 6 tài khoản, hướng dẫn kích hoạt.',890000.0000,'office.jpg',JSON_ARRAY('win11.jpg'),16,130,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','o365family-12m','attributes', JSON_OBJECT('seats',6,'duration','12m'),'price',890000.0000,'inventory_count',16,'image_url','office.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1021,1,'SOFTWARE','OTHER','Adobe Photoshop (Key 12 tháng)','Key kích hoạt Photoshop 12 tháng.','Cung cấp key Adobe Photoshop dùng 12 tháng cho cá nhân.',1500000.0000,'adope.jpg',JSON_ARRAY('canva2.png'),10,41,'Available','EDITION_LICENSE',JSON_ARRAY(
--   JSON_OBJECT('variant_code','ps-12m','attributes', JSON_OBJECT('product','Photoshop','term','12m'),'price',1500000.0000,'inventory_count',10,'image_url','adope.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1022,1,'GAME','OTHER','Steam Wallet Code (VN)','Mã nạp Steam mệnh giá VN.','Cung cấp mã Steam Wallet Code theo mệnh giá phổ biến.',500000.0000,'steam.png',JSON_ARRAY('valorant.png','varo.png'),35,220,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','steam-100k','attributes', JSON_OBJECT('amount','100000'),'price',105000.0000,'inventory_count',15,'image_url','steam.png','status','Available'),
--   JSON_OBJECT('variant_code','steam-200k','attributes', JSON_OBJECT('amount','200000'),'price',205000.0000,'inventory_count',10,'image_url','valorant.png','status','Available'),
--   JSON_OBJECT('variant_code','steam-500k','attributes', JSON_OBJECT('amount','500000'),'price',500000.0000,'inventory_count',10,'image_url','varo.png','status','Available')
-- ),NOW(),NOW()),
-- (1023,1,'GAME','VALORANT','Valorant – Skin Bundle Random','Account có skin bundle ngẫu nhiên.','Tài khoản Valorant unranked, sở hữu skin random; bảo hành đăng nhập.',350000.0000,'varo.png',JSON_ARRAY('valorant.png'),9,33,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','valo-skin-rand','attributes', JSON_OBJECT('rank','Unranked','skin_bundle','random'),'price',350000.0000,'inventory_count',9,'image_url','varo.png','status','Available')
-- ),NOW(),NOW()),
-- (1024,1,'SOCIAL','OTHER','Discord Nitro','Gói Discord Nitro theo thời hạn.','Discord Nitro hỗ trợ upload lớn, emoji & perks.',95000.0000,'telegram.png',JSON_ARRAY('tiktok2.png'),28,175,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','nitro-1m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','1m'),'price',95000.0000,'inventory_count',18,'image_url','telegram.png','status','Available'),
--   JSON_OBJECT('variant_code','nitro-3m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','3m'),'price',270000.0000,'inventory_count',10,'image_url','tiktok.png','status','Available'),
--   JSON_OBJECT('variant_code','nitro-12m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','12m'),'price',980000.0000,'inventory_count',5,'image_url','tiktok2.png','status','Available')
-- ),NOW(),NOW()),
-- (1025,1,'SOCIAL','FACEBOOK','Fanpage Template & Hướng dẫn','Bộ template + hướng dẫn vận hành fanpage.','Tài nguyên số cho fanpage: template bài viết, checklist, lịch đăng.',120000.0000,'facebook.png',JSON_ARRAY('fb2.png'),40,95,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','fp-template-basic','attributes', JSON_OBJECT('package','basic'),'price',120000.0000,'inventory_count',25,'image_url','facebook.png','status','Available'),
--   JSON_OBJECT('variant_code','fp-template-pro','attributes', JSON_OBJECT('package','pro'),'price',240000.0000,'inventory_count',15,'image_url','fb2.png','status','Available')
-- ),NOW(),NOW()),
-- (1026,1,'SOCIAL','TIKTOK','TikTok Shop – Gói đào tạo','Đào tạo vận hành TikTok Shop.','Khoá đào tạo 1:1/nhóm về vận hành TikTok Shop.',650000.0000,'tiktok2.png',JSON_ARRAY('tiktok.png'),12,58,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','tttrain-1s','attributes', JSON_OBJECT('sessions',1,'duration','2h'),'price',650000.0000,'inventory_count',7,'image_url','tiktok2.png','status','Available'),
--   JSON_OBJECT('variant_code','tttrain-3s','attributes', JSON_OBJECT('sessions',3,'duration','6h'),'price',1800000.0000,'inventory_count',5,'image_url','tiktok.png','status','Available')
-- ),NOW(),NOW()),
-- (1027,1,'EMAIL','GMAIL','Gmail – Cài đặt Forward & Filter','Thiết lập chuyển tiếp, bộ lọc, nhãn chuyên nghiệp.','Dịch vụ thiết kế hệ thống hộp thư: forward, filter, nhãn, auto-reply.',170000.0000,'gmail2.jpg',JSON_ARRAY('gmail.png','mailedu.png'),25,60,'Available','CUSTOM',JSON_ARRAY(
--   JSON_OBJECT('variant_code','gmail-setup-basic','attributes', JSON_OBJECT('package','basic'),'price',170000.0000,'inventory_count',15,'image_url','gmail2.jpg','status','Available'),
--   JSON_OBJECT('variant_code','gmail-setup-pro','attributes', JSON_OBJECT('package','pro'),'price',320000.0000,'inventory_count',10,'image_url','mailedu.png','status','Available')
-- ),NOW(),NOW()),
-- (1028,1,'SOFTWARE','CANVA','Canva Pro – Gia hạn 36 tháng','Gia hạn Canva Pro 24–36 tháng.','Dịch vụ gia hạn Canva Pro dài hạn tiết kiệm.',2300000.0000,'canva2.png',JSON_ARRAY('canva.jpg'),6,22,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','canva-24m','attributes', JSON_OBJECT('service','canva','duration','24m'),'price',1600000.0000,'inventory_count',3,'image_url','canva2.png','status','Available'),
--   JSON_OBJECT('variant_code','canva-36m','attributes', JSON_OBJECT('service','canva','duration','36m'),'price',2300000.0000,'inventory_count',3,'image_url','canva.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1029,1,'EMAIL','GMAIL','Gmail Edu (tuỳ trường)','Gmail Edu dung lượng lớn, dùng cho học tập.','Tài khoản Gmail Edu (tuỳ trường), kèm hướng dẫn bảo mật & sử dụng.',300000.0000,'mailedu.png',JSON_ARRAY('gmail.png','gmail2.jpg'),18,70,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','gmail-edu-6m','attributes', JSON_OBJECT('service','gmail','segment','edu','duration','6m'),'price',300000.0000,'inventory_count',10,'image_url','mailedu.png','status','Available'),
--   JSON_OBJECT('variant_code','gmail-edu-12m','attributes', JSON_OBJECT('service','gmail','segment','edu','duration','12m'),'price',520000.0000,'inventory_count',8,'image_url','gmail2.jpg','status','Available')
-- ),NOW(),NOW()),
-- (1030,1,'SOCIAL','OTHER','Telegram Premium','Nâng cấp Telegram Premium theo kỳ hạn.','Tính năng Premium: upload lớn, chuyển giọng nói sang text, sticker/emoji nâng cao.',115000.0000,'telegram.png',JSON_ARRAY(),26,110,'Available','DURATION_PLAN',JSON_ARRAY(
--   JSON_OBJECT('variant_code','tg-prem-1m','attributes', JSON_OBJECT('service','telegram','duration','1m'),'price',115000.0000,'inventory_count',16,'image_url','telegram.png','status','Available'),
--   JSON_OBJECT('variant_code','tg-prem-12m','attributes', JSON_OBJECT('service','telegram','duration','12m'),'price',1150000.0000,'inventory_count',10,'image_url','tiktok.png','status','Available')
-- ),NOW(),NOW());



-- -- Credentials (ví dụ ràng buộc với đơn)
-- INSERT INTO `product_credentials` (`id`,`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
--  (1,1001,5001,'ENCRYPTED-CODE-001','gmail-basic-1m',1,'2024-01-20 12:05:00'),
--  (2,1001,NULL,'ENCRYPTED-CODE-002','gmail-premium-12m',0,'2024-01-21 10:20:00'),
--  (3,1002,NULL,'ENCRYPTED-CODE-201','sp-12m',0,'2024-01-26 08:30:00'),
--  (4,1002,5003,'ENCRYPTED-CODE-202','sp-12m',1,'2024-01-31 09:10:00');

-- Wallets
INSERT INTO `wallets` (`id`,`user_id`,`balance`,`status`,`created_at`,`updated_at`) VALUES
 (1,1,0.0000,1,'2024-01-11 09:00:00','2024-01-11 09:00:00'),
 (2,3,100000000.0000,1,'2024-01-15 07:45:00','2024-01-15 07:45:00');

-- INSERT INTO `deposit_requests` (`id`,`user_id`,`amount`,`qr_content`,`idempotency_key`,`status`,`expires_at`,`admin_note`,`created_at`) VALUES
--  (1,3,300000.0000,'VietQR|MMO|INV-20240120','DEPOSIT-UUID-1','Completed','2024-01-20 12:00:00','Đối soát thành công','2024-01-20 10:15:00'),
--  (2,3,150000.0000,'VietQR|MMO|INV-20240127','DEPOSIT-UUID-2','Pending','2024-01-27 12:00:00',NULL,'2024-01-27 09:00:00'),
--  (3,3,200000.0000,'VietQR|MMO|INV-20240131','DEPOSIT-UUID-3','Completed','2024-01-31 12:00:00','Bổ sung số dư chuẩn bị mua gói mới','2024-01-31 08:55:00');

-- INSERT INTO `wallet_transactions` (`id`,`wallet_id`,`related_entity_id`,`transaction_type`,`amount`,`balance_before`,`balance_after`,`note`,`created_at`) VALUES
--  (1,3,1,'Deposit',300000.0000,0.0000,300000.0000,'Nạp tiền qua VietQR','2024-01-20 10:16:00'),
--  (2,3,5001,'Purchase',-250000.0000,300000.0000,50000.0000,'Thanh toán đơn hàng #5001','2024-01-20 12:00:00'),
--  (3,2,5001,'Payout',250000.0000,200000.0000,450000.0000,'Doanh thu đơn #5001','2024-01-20 12:01:00'),
--  (4,2,1,'Withdrawal',-120000.0000,450000.0000,330000.0000,'Rút tiền về Vietcombank','2024-01-26 09:40:00'),
--  (5,3,3,'Deposit',200000.0000,50000.0000,250000.0000,'Nạp tiền chuẩn bị mua gói Spotify khác','2024-01-31 09:00:00'),
--  (6,3,5003,'Purchase',-185000.0000,250000.0000,65000.0000,'Thanh toán đơn hàng #5003','2024-01-31 09:05:00'),
--  (7,2,5003,'Payout',185000.0000,330000.0000,515000.0000,'Giải ngân đơn #5003 sau khi đóng report','2024-02-02 10:00:00');

-- -- Đơn hàng mẫu minh họa trạng thái Completed và Disputed
-- INSERT INTO `orders` (`id`,`buyer_id`,`product_id`,`quantity`,`unit_price`,`payment_transaction_id`,`total_amount`,`status`,`variant_code`,`idempotency_key`,`hold_until`,`escrow_hold_seconds`,`escrow_original_release_at`,`escrow_release_at`,`escrow_status`,`escrow_paused_at`,`escrow_remaining_seconds`,`escrow_resumed_at`,`escrow_released_at_actual`,`created_at`,`updated_at`) VALUES
-- (5001,3,1001,1,250000.0000,2,250000.0000,'Completed','gmail-basic-1m','ORDER-5001-KEY','2024-01-23 12:00:00',259200,'2024-01-23 12:00:00','2024-01-23 12:00:00','Released',NULL,NULL,NULL,'2024-01-23 12:00:00','2024-01-20 10:45:00','2024-01-20 12:05:00'),
-- (5002,3,1002,1,185000.0000,NULL,185000.0000,'Disputed','sp-12m','ORDER-5002-KEY','2024-02-01 00:00:00',432000,'2024-02-01 00:00:00','2024-02-01 00:00:00','Paused','2024-01-27 08:10:00',402600,NULL,NULL,'2024-01-26 09:00:00','2024-01-27 08:10:00'),
-- (5003,3,1002,1,185000.0000,6,185000.0000,'Completed','sp-12m','ORDER-5003-KEY','2024-02-02 10:00:00',176400,'2024-02-02 10:00:00','2024-02-02 10:00:00','Released','2024-02-02 09:30:00',1800,'2024-02-02 09:45:00','2024-02-02 10:00:00','2024-01-31 09:00:00','2024-02-02 10:00:00');

-- -- Withdrawals
-- INSERT INTO `withdrawal_rejection_reasons` (`id`,`reason_code`,`description`,`is_active`) VALUES
--  (1,'ACCOUNT_ERROR','Thông tin tài khoản ngân hàng không khớp',1),
--  (2,'SUSPICIOUS_ACTIVITY','Giao dịch có dấu hiệu bất thường, cần xác minh thêm',1);

-- INSERT INTO `withdrawal_requests` (`id`,`user_id`,`amount`,`bank_account_info`,`status`,`admin_proof_url`,`created_at`,`processed_at`) VALUES
--  (1,2,120000.0000,'{\"bank\":\"VCB\",\"account\":\"0123456789\",\"name\":\"Trần Văn Seller\"}','Completed','https://cdn.mmo.local/withdrawals/receipt-1.png','2024-01-26 09:00:00','2024-01-26 09:45:00'),
--  (2,2,80000.0000,'{\"bank\":\"ACB\",\"account\":\"9876543210\",\"name\":\"Trần Văn Seller\"}','Rejected',NULL,'2024-02-03 09:00:00','2024-02-03 10:00:00');

-- INSERT INTO `withdrawal_request_reasons_map` (`request_id`,`reason_id`) VALUES (2,2);

-- -- Support / Chat
-- INSERT INTO `disputes` (`id`,`order_id`,`order_reference_code`,`reporter_id`,`resolved_by_admin_id`,`issue_type`,`custom_issue_title`,`reason`,`status`,`escrow_paused_at`,`escrow_remaining_seconds`,`resolved_at`,`resolution_note`,`created_at`,`updated_at`) VALUES
--  (1,5002,'ORD-5002',3,NULL,'ACCOUNT_NOT_WORKING',NULL,'Tài khoản Spotify không hoạt động','Open','2024-01-27 08:10:00',402600,NULL,NULL,'2024-01-27 08:10:00','2024-01-27 08:10:00'),
--  (2,5003,'ORD-5003',3,1,'ACCOUNT_NOT_WORKING',NULL,'Tài khoản Spotify báo lỗi nhưng đã hoạt động lại sau khi xác minh','Closed','2024-02-02 09:30:00',1200,'2024-02-02 09:45:00','Admin xác nhận tài khoản hoạt động bình thường, mở lại escrow','2024-02-02 09:30:00','2024-02-02 09:45:00');

-- INSERT INTO `dispute_attachments` (`id`,`dispute_id`,`file_path`,`created_at`) VALUES
--  (1,1,'https://cdn.mmo.local/disputes/5002/proof-1.png','2024-01-27 08:11:00'),
--  (2,1,'https://cdn.mmo.local/disputes/5002/proof-2.png','2024-01-27 08:11:30'),
--  (3,2,'https://cdn.mmo.local/disputes/5003/proof-1.png','2024-02-02 09:32:00');

-- INSERT INTO `order_escrow_events` (`id`,`order_id`,`event_type`,`actor_type`,`actor_admin_id`,`related_dispute_id`,`release_at_snapshot`,`remaining_seconds_snapshot`,`metadata`,`created_at`) VALUES
--  (1,5002,'CREATED','SYSTEM',NULL,NULL,'2024-02-01 00:00:00',432000,JSON_OBJECT('source','checkout'),'2024-01-26 09:00:00'),
--  (2,5002,'PAUSED','SYSTEM',NULL,1,'2024-02-01 00:00:00',402600,JSON_OBJECT('dispute_id',1,'reason','Buyer report mở khi escrow còn 4d16h'),'2024-01-27 08:10:00'),
--  (3,5002,'ADJUSTED','ADMIN',1,1,'2024-02-03 12:00:00',432000,JSON_OBJECT('change','extend','delta_seconds',172800),'2024-01-27 08:12:00'),
--  (4,5003,'CREATED','SYSTEM',NULL,NULL,'2024-02-02 10:00:00',176400,JSON_OBJECT('source','checkout'),'2024-01-31 09:00:00'),
--  (5,5003,'PAUSED','SYSTEM',NULL,2,'2024-02-02 10:00:00',1800,JSON_OBJECT('dispute_id',2,'reason','Buyer báo lỗi trước khi hết escrow'),'2024-02-02 09:30:00'),
--  (6,5003,'RESUMED','ADMIN',1,2,'2024-02-02 10:00:00',900,JSON_OBJECT('action','resume_after_validation'),'2024-02-02 09:45:00'),
--  (7,5003,'RELEASED','SYSTEM',NULL,2,'2024-02-02 10:00:00',0,JSON_OBJECT('action','auto_release_after_resume'),'2024-02-02 10:00:00');

-- INSERT INTO `order_escrow_adjustments` (`id`,`order_id`,`event_id`,`admin_id`,`related_dispute_id`,`previous_release_at`,`previous_remaining_seconds`,`new_release_at`,`new_remaining_seconds`,`note`,`created_at`) VALUES
--  (1,5002,3,1,1,'2024-02-01 00:00:00',259200,'2024-02-03 12:00:00',432000,'Gia hạn thêm 2 ngày để xử lý khiếu nại','2024-01-27 08:12:00');


-- -- Inventory logs
-- INSERT INTO `inventory_logs` (`id`,`product_id`,`related_order_id`,`change_amount`,`reason`,`created_at`) VALUES
--  (1,1001,5001,-1,'Sale','2024-01-20 12:00:00'),
--  (2,1002,5002,-1,'Sale','2024-01-26 09:05:00'),
--  (3,1002,NULL,5,'ManualRestock','2024-01-29 10:00:00'),
--  (4,1002,5003,-1,'Sale','2024-01-31 09:05:00');
INSERT INTO `shops` (`id`,`owner_id`,`name`,`description`,`admin_note`,`status`,`created_at`,`updated_at`) VALUES
 (1,2,'Email & MXH Store','Chuyên cung cấp tài khoản Email (Gmail, Yahoo, Outlook) và tài khoản mạng xã hội (Facebook, TikTok, X/Twitter)',NULL,'Active','2024-01-12 08:30:00','2024-01-12 08:30:00');

-- Products cho Shop 1 (Email & MXH)
INSERT INTO `products`
(`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
VALUES
-- Gmail
(1001,1,'EMAIL','GMAIL',
 'Gmail Doanh nghiệp 50GB',
 'Email doanh nghiệp 50GB, bảo hành, hỗ trợ đổi mật khẩu.',
 'Tài khoản Gmail Doanh nghiệp 50GB. Bạn nhận được email và mật khẩu kèm hướng dẫn đổi bảo mật 2 lớp.',
 250000.0000,
 'gmail.png',
 JSON_ARRAY('gmail2.jpg','mailedu.png'),
 35,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','gmail-basic-1m','attributes', JSON_OBJECT('service','gmail','plan','basic','duration','1m'),'price',250000.0000,'inventory_count',20,'image_url','gmail.png','status','Available'),
   JSON_OBJECT('variant_code','gmail-premium-12m','attributes', JSON_OBJECT('service','gmail','plan','premium','duration','12m'),'price',1200000.0000,'inventory_count',15,'image_url','gmail2.jpg','status','Available')
 ),
 NOW(),NOW()),

-- Yahoo Mail
(1002,1,'EMAIL','YAHOO',
 'Yahoo Mail Premium 1TB',
 'Yahoo Mail Premium dung lượng 1TB, bảo hành đổi mật khẩu.',
 'Tài khoản Yahoo Mail Premium với dung lượng 1TB, hỗ trợ bảo mật và khôi phục tài khoản.',
 180000.0000,
 'yahoo.png',
 JSON_ARRAY('yahoo2.jpg'),
 28,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','yahoo-1m','attributes', JSON_OBJECT('service','yahoo','plan','premium','duration','1m'),'price',180000.0000,'inventory_count',28,'image_url','yahoo.png','status','Available')
 ),
 NOW(),NOW()),

-- Outlook
(1003,1,'EMAIL','OUTLOOK',
 'Outlook Business 100GB',
 'Outlook Business 100GB, hỗ trợ bảo mật và khôi phục.',
 'Tài khoản Outlook Business với dung lượng 100GB, kèm hướng dẫn bảo mật & khôi phục.',
 300000.0000,
 'outlook.png',
 JSON_ARRAY('outlook2.jpg'),
 32,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','outlook-1m','attributes', JSON_OBJECT('service','outlook','plan','business','duration','1m'),'price',300000.0000,'inventory_count',20,'image_url','outlook.png','status','Available'),
   JSON_OBJECT('variant_code','outlook-12m','attributes', JSON_OBJECT('service','outlook','plan','business','duration','12m'),'price',3200000.0000,'inventory_count',12,'image_url','outlook2.jpg','status','Available')
 ),
 NOW(),NOW()),

-- Facebook
(1004,1,'SOCIAL','FACEBOOK',
 'Tài khoản Facebook cổ 2009+',
 'Facebook cổ năm 2009+, bảo hành đổi pass.',
 'Tài khoản Facebook cổ (năm tạo 2009–2012), có bảo hành đổi mật khẩu. Dùng cho chạy quảng cáo & seeding.',
 150000.0000,
 'facebook.png',
 JSON_ARRAY('fb2.png'),
 42,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','fb-2009','attributes', JSON_OBJECT('age','2009'),'price',180000.0000,'inventory_count',15,'image_url','facebook.png','status','Available'),
   JSON_OBJECT('variant_code','fb-2012','attributes', JSON_OBJECT('age','2012'),'price',150000.0000,'inventory_count',27,'image_url','fb2.png','status','Available')
 ),
 NOW(),NOW()),

-- TikTok
(1005,1,'SOCIAL','TIKTOK',
 'Tài khoản TikTok Pro',
 'Tài khoản TikTok Pro, bảo hành đăng nhập.',
 'Tài khoản TikTok Pro, dùng quay và đăng video với analytics nâng cao.',
 99000.0000,
 'tiktok.png',
 JSON_ARRAY('tiktok2.png','tiktoklive.png'),
 38,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','tt-pro','attributes', JSON_OBJECT('tier','pro'),'price',99000.0000,'inventory_count',38,'image_url','tiktok.png','status','Available')
 ),
 NOW(),NOW()),

-- X (Twitter)
(1006,1,'SOCIAL','X',
 'Tài khoản X (Twitter) Verified',
 'Tài khoản X Verified, bảo hành đăng nhập.',
 'Tài khoản X (Twitter) đã được xác minh, hỗ trợ đầy đủ tính năng premium.',
 220000.0000,
 'x.png',
 JSON_ARRAY('x2.png'),
 25,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','x-verified','attributes', JSON_OBJECT('tier','verified'),'price',220000.0000,'inventory_count',25,'image_url','x.png','status','Available')
 ),
 NOW(),NOW());

-- ====================================================================================
-- SHOP 2: Chuyên Software & Game
-- ====================================================================================
INSERT INTO `shops` (`id`,`owner_id`,`name`,`description`,`admin_note`,`status`,`created_at`,`updated_at`) VALUES
 (2,2,'Software & Game Store','Chuyên cung cấp tài khoản phần mềm bản quyền (Canva, Office, Windows, ChatGPT) và tài khoản game (Valorant, League of Legends, CS2)',NULL,'Active','2024-01-12 08:30:00','2024-01-12 08:30:00');

-- Products cho Shop 2 (Software & Game)
INSERT INTO `products`
(`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
VALUES
-- Canva
(2001,2,'SOFTWARE','CANVA',
 'Canva Pro chính chủ',
 'Canva Pro bản quyền 12 tháng, kích hoạt trực tiếp.',
 'Cung cấp tài khoản Canva Pro chính chủ, kích hoạt ngay sau khi thanh toán, bảo hành 30 ngày.',
 90000.0000,
 'canva.jpg',
 JSON_ARRAY('canva2.png'),
 45,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','canva-1m','attributes',JSON_OBJECT('duration','1m'),'price',90000.0000,'inventory_count',25,'image_url','canva.jpg','status','Available'),
   JSON_OBJECT('variant_code','canva-12m','attributes',JSON_OBJECT('duration','12m'),'price',750000.0000,'inventory_count',20,'image_url','canva2.png','status','Available')
 ),
 NOW(),NOW()),

-- Office
(2002,2,'SOFTWARE','OFFICE',
 'Office 365 Family 12 tháng',
 'Office 365 Family 12 tháng cho 6 người, kích hoạt ngay.',
 'Gói Office 365 Family 12 tháng cho 6 tài khoản, hướng dẫn kích hoạt và sử dụng đầy đủ tính năng.',
 890000.0000,
 'office.jpg',
 JSON_ARRAY('office2.jpg'),
 18,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','o365family-12m','attributes', JSON_OBJECT('seats',6,'duration','12m'),'price',890000.0000,'inventory_count',18,'image_url','office.jpg','status','Available')
 ),
 NOW(),NOW()),

-- Windows
(2003,2,'SOFTWARE','WINDOWS',
 'Key Windows 11 Pro',
 'Key bản quyền Windows 11 Pro, kích hoạt online.',
 'Khóa bản quyền Windows 11 Pro, kích hoạt online trọn đời. Hỗ trợ kích hoạt ngay sau khi mua.',
 390000.0000,
 'win11.jpg',
 JSON_ARRAY('office.jpg'),
 30,0,'Available','EDITION_LICENSE',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','win11pro-oem','attributes', JSON_OBJECT('edition','pro','license','oem'),'price',390000.0000,'inventory_count',20,'image_url','win11.jpg','status','Available'),
   JSON_OBJECT('variant_code','win11pro-retail','attributes', JSON_OBJECT('edition','pro','license','retail'),'price',450000.0000,'inventory_count',10,'image_url','win11.jpg','status','Available')
 ),
 NOW(),NOW()),

-- ChatGPT
(2004,2,'SOFTWARE','CHATGPT',
 'ChatGPT Plus 1 tháng',
 'ChatGPT Plus 1 tháng, truy cập GPT-4 và tính năng nâng cao.',
 'Tài khoản ChatGPT Plus với quyền truy cập GPT-4, tạo hình ảnh DALL-E và các tính năng premium khác.',
 250000.0000,
 'chatgpt.png',
 JSON_ARRAY('chatgpt2.png'),
 22,0,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','chatgpt-1m','attributes', JSON_OBJECT('service','chatgpt','plan','plus','duration','1m'),'price',250000.0000,'inventory_count',15,'image_url','chatgpt.png','status','Available'),
   JSON_OBJECT('variant_code','chatgpt-12m','attributes', JSON_OBJECT('service','chatgpt','plan','plus','duration','12m'),'price',2500000.0000,'inventory_count',7,'image_url','chatgpt2.png','status','Available')
 ),
 NOW(),NOW()),

-- Valorant
(2005,2,'GAME','VALORANT',
 'Tài khoản Valorant Rank Gold',
 'Tài khoản Valorant rank Gold, vùng AP/SEA, bảo hành đăng nhập.',
 'Tài khoản Valorant rank Gold, vùng AP/SEA, đã có skin và agent mở khóa. Bảo hành đăng nhập 7 ngày.',
 400000.0000,
 'valorant.png',
 JSON_ARRAY('varo.png'),
 15,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','valo-silver','attributes', JSON_OBJECT('rank','Silver','region','AP'),'price',350000.0000,'inventory_count',8,'image_url','valorant.png','status','Available'),
   JSON_OBJECT('variant_code','valo-gold','attributes', JSON_OBJECT('rank','Gold','region','AP'),'price',400000.0000,'inventory_count',7,'image_url','varo.png','status','Available')
 ),
 NOW(),NOW()),

-- League of Legends
(2006,2,'GAME','LEAGUE_OF_LEGENDS',
 'Tài khoản LoL Rank Platinum',
 'Tài khoản League of Legends rank Platinum, nhiều tướng và skin.',
 'Tài khoản LoL rank Platinum, đã có nhiều tướng và skin hiếm. Bảo hành đăng nhập và chuyển vùng.',
 350000.0000,
 'lol.png',
 JSON_ARRAY('lol2.png'),
 12,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','lol-gold','attributes', JSON_OBJECT('rank','Gold','region','VN'),'price',280000.0000,'inventory_count',6,'image_url','lol.png','status','Available'),
   JSON_OBJECT('variant_code','lol-plat','attributes', JSON_OBJECT('rank','Platinum','region','VN'),'price',350000.0000,'inventory_count',6,'image_url','lol2.png','status','Available')
 ),
 NOW(),NOW()),

-- CS2
(2007,2,'GAME','CS2',
 'Tài khoản CS2 Prime',
 'Tài khoản CS2 Prime, đã có skin và rank.',
 'Tài khoản CS2 Prime với nhiều skin và rank ổn định. Bảo hành đăng nhập và chuyển tài khoản.',
 320000.0000,
 'cs2.png',
 JSON_ARRAY('cs2_2.png'),
 20,0,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','cs2-prime','attributes', JSON_OBJECT('type','prime','rank','Gold Nova'),'price',320000.0000,'inventory_count',12,'image_url','cs2.png','status','Available'),
   JSON_OBJECT('variant_code','cs2-prime-mg','attributes', JSON_OBJECT('type','prime','rank','Master Guardian'),'price',450000.0000,'inventory_count',8,'image_url','cs2_2.png','status','Available')
 ),
 NOW(),NOW());

-- ====================================================================================
-- HÀNG TỒN KHO: Product Credentials
-- Email và Facebook phải có định dạng email chuẩn
-- ====================================================================================

-- Credentials cho Gmail (1001) - 35 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1001,NULL,'gmail.account.001@gmail.com|Password123!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.002@gmail.com|SecurePass456!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.003@gmail.com|MyPass789!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.004@gmail.com|StrongPass2024!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.005@gmail.com|SafePass321!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.006@gmail.com|NewPass654!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.007@gmail.com|GoodPass987!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.008@gmail.com|BestPass147!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.009@gmail.com|TopPass258!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.010@gmail.com|SuperPass369!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.011@gmail.com|MegaPass741!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.012@gmail.com|UltraPass852!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.013@gmail.com|PowerPass963!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.014@gmail.com|MaxPass159!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.015@gmail.com|ProPass357!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.016@gmail.com|ElitePass468!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.017@gmail.com|PrimePass579!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.018@gmail.com|GoldPass680!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.019@gmail.com|SilverPass791!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.account.020@gmail.com|BronzePass802!','gmail-basic-1m',0,NOW()),
(1001,NULL,'gmail.premium.001@gmail.com|PremiumPass123!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.002@gmail.com|PremiumPass456!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.003@gmail.com|PremiumPass789!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.004@gmail.com|PremiumPass012!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.005@gmail.com|PremiumPass345!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.006@gmail.com|PremiumPass678!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.007@gmail.com|PremiumPass901!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.008@gmail.com|PremiumPass234!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.009@gmail.com|PremiumPass567!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.010@gmail.com|PremiumPass890!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.011@gmail.com|PremiumPass123!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.012@gmail.com|PremiumPass456!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.013@gmail.com|PremiumPass789!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.014@gmail.com|PremiumPass012!','gmail-premium-12m',0,NOW()),
(1001,NULL,'gmail.premium.015@gmail.com|PremiumPass345!','gmail-premium-12m',0,NOW());

-- Credentials cho Yahoo (1002) - 28 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1002,NULL,'yahoo.account.001@yahoo.com|YahooPass123!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.002@yahoo.com|YahooPass456!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.003@yahoo.com|YahooPass789!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.004@yahoo.com|YahooPass012!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.005@yahoo.com|YahooPass345!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.006@yahoo.com|YahooPass678!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.007@yahoo.com|YahooPass901!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.008@yahoo.com|YahooPass234!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.009@yahoo.com|YahooPass567!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.010@yahoo.com|YahooPass890!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.011@yahoo.com|YahooPass123!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.012@yahoo.com|YahooPass456!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.013@yahoo.com|YahooPass789!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.014@yahoo.com|YahooPass012!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.015@yahoo.com|YahooPass345!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.016@yahoo.com|YahooPass678!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.017@yahoo.com|YahooPass901!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.018@yahoo.com|YahooPass234!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.019@yahoo.com|YahooPass567!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.020@yahoo.com|YahooPass890!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.021@yahoo.com|YahooPass123!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.022@yahoo.com|YahooPass456!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.023@yahoo.com|YahooPass789!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.024@yahoo.com|YahooPass012!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.025@yahoo.com|YahooPass345!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.026@yahoo.com|YahooPass678!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.027@yahoo.com|YahooPass901!','yahoo-1m',0,NOW()),
(1002,NULL,'yahoo.account.028@yahoo.com|YahooPass234!','yahoo-1m',0,NOW());

-- Credentials cho Outlook (1003) - 32 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1003,NULL,'outlook.account.001@outlook.com|OutlookPass123!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.002@outlook.com|OutlookPass456!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.003@outlook.com|OutlookPass789!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.004@outlook.com|OutlookPass012!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.005@outlook.com|OutlookPass345!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.006@outlook.com|OutlookPass678!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.007@outlook.com|OutlookPass901!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.008@outlook.com|OutlookPass234!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.009@outlook.com|OutlookPass567!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.010@outlook.com|OutlookPass890!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.011@outlook.com|OutlookPass123!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.012@outlook.com|OutlookPass456!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.013@outlook.com|OutlookPass789!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.014@outlook.com|OutlookPass012!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.015@outlook.com|OutlookPass345!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.016@outlook.com|OutlookPass678!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.017@outlook.com|OutlookPass901!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.018@outlook.com|OutlookPass234!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.019@outlook.com|OutlookPass567!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.account.020@outlook.com|OutlookPass890!','outlook-1m',0,NOW()),
(1003,NULL,'outlook.business.001@outlook.com|BusinessPass123!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.002@outlook.com|BusinessPass456!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.003@outlook.com|BusinessPass789!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.004@outlook.com|BusinessPass012!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.005@outlook.com|BusinessPass345!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.006@outlook.com|BusinessPass678!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.007@outlook.com|BusinessPass901!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.008@outlook.com|BusinessPass234!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.009@outlook.com|BusinessPass567!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.010@outlook.com|BusinessPass890!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.011@outlook.com|BusinessPass123!','outlook-12m',0,NOW()),
(1003,NULL,'outlook.business.012@outlook.com|BusinessPass456!','outlook-12m',0,NOW());

-- Credentials cho Facebook (1004) - 42 credentials (email format)
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1004,NULL,'fb.account.001@gmail.com|FbPass123!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.002@gmail.com|FbPass456!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.003@gmail.com|FbPass789!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.004@gmail.com|FbPass012!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.005@gmail.com|FbPass345!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.006@gmail.com|FbPass678!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.007@gmail.com|FbPass901!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.008@gmail.com|FbPass234!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.009@gmail.com|FbPass567!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.010@gmail.com|FbPass890!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.011@gmail.com|FbPass123!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.012@gmail.com|FbPass456!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.013@gmail.com|FbPass789!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.014@gmail.com|FbPass012!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.015@gmail.com|FbPass345!','fb-2009',0,NOW()),
(1004,NULL,'fb.account.016@yahoo.com|FbPass678!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.017@yahoo.com|FbPass901!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.018@yahoo.com|FbPass234!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.019@yahoo.com|FbPass567!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.020@yahoo.com|FbPass890!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.021@yahoo.com|FbPass123!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.022@yahoo.com|FbPass456!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.023@yahoo.com|FbPass789!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.024@yahoo.com|FbPass012!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.025@yahoo.com|FbPass345!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.026@yahoo.com|FbPass678!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.027@yahoo.com|FbPass901!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.028@yahoo.com|FbPass234!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.029@yahoo.com|FbPass567!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.030@yahoo.com|FbPass890!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.031@outlook.com|FbPass123!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.032@outlook.com|FbPass456!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.033@outlook.com|FbPass789!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.034@outlook.com|FbPass012!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.035@outlook.com|FbPass345!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.036@outlook.com|FbPass678!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.037@outlook.com|FbPass901!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.038@outlook.com|FbPass234!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.039@outlook.com|FbPass567!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.040@outlook.com|FbPass890!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.041@outlook.com|FbPass123!','fb-2012',0,NOW()),
(1004,NULL,'fb.account.042@outlook.com|FbPass456!','fb-2012',0,NOW());

-- Credentials cho TikTok (1005) - 38 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1005,NULL,'tiktok.account.001@gmail.com|TikTokPass123!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.002@gmail.com|TikTokPass456!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.003@gmail.com|TikTokPass789!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.004@gmail.com|TikTokPass012!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.005@gmail.com|TikTokPass345!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.006@gmail.com|TikTokPass678!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.007@gmail.com|TikTokPass901!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.008@gmail.com|TikTokPass234!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.009@gmail.com|TikTokPass567!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.010@gmail.com|TikTokPass890!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.011@gmail.com|TikTokPass123!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.012@gmail.com|TikTokPass456!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.013@gmail.com|TikTokPass789!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.014@gmail.com|TikTokPass012!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.015@gmail.com|TikTokPass345!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.016@gmail.com|TikTokPass678!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.017@gmail.com|TikTokPass901!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.018@gmail.com|TikTokPass234!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.019@gmail.com|TikTokPass567!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.020@gmail.com|TikTokPass890!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.021@gmail.com|TikTokPass123!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.022@gmail.com|TikTokPass456!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.023@gmail.com|TikTokPass789!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.024@gmail.com|TikTokPass012!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.025@gmail.com|TikTokPass345!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.026@gmail.com|TikTokPass678!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.027@gmail.com|TikTokPass901!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.028@gmail.com|TikTokPass234!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.029@gmail.com|TikTokPass567!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.030@gmail.com|TikTokPass890!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.031@gmail.com|TikTokPass123!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.032@gmail.com|TikTokPass456!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.033@gmail.com|TikTokPass789!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.034@gmail.com|TikTokPass012!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.035@gmail.com|TikTokPass345!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.036@gmail.com|TikTokPass678!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.037@gmail.com|TikTokPass901!','tt-pro',0,NOW()),
(1005,NULL,'tiktok.account.038@gmail.com|TikTokPass234!','tt-pro',0,NOW());

-- Credentials cho X/Twitter (1006) - 25 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1006,NULL,'x.account.001@gmail.com|XPass123!','x-verified',0,NOW()),
(1006,NULL,'x.account.002@gmail.com|XPass456!','x-verified',0,NOW()),
(1006,NULL,'x.account.003@gmail.com|XPass789!','x-verified',0,NOW()),
(1006,NULL,'x.account.004@gmail.com|XPass012!','x-verified',0,NOW()),
(1006,NULL,'x.account.005@gmail.com|XPass345!','x-verified',0,NOW()),
(1006,NULL,'x.account.006@gmail.com|XPass678!','x-verified',0,NOW()),
(1006,NULL,'x.account.007@gmail.com|XPass901!','x-verified',0,NOW()),
(1006,NULL,'x.account.008@gmail.com|XPass234!','x-verified',0,NOW()),
(1006,NULL,'x.account.009@gmail.com|XPass567!','x-verified',0,NOW()),
(1006,NULL,'x.account.010@gmail.com|XPass890!','x-verified',0,NOW()),
(1006,NULL,'x.account.011@gmail.com|XPass123!','x-verified',0,NOW()),
(1006,NULL,'x.account.012@gmail.com|XPass456!','x-verified',0,NOW()),
(1006,NULL,'x.account.013@gmail.com|XPass789!','x-verified',0,NOW()),
(1006,NULL,'x.account.014@gmail.com|XPass012!','x-verified',0,NOW()),
(1006,NULL,'x.account.015@gmail.com|XPass345!','x-verified',0,NOW()),
(1006,NULL,'x.account.016@gmail.com|XPass678!','x-verified',0,NOW()),
(1006,NULL,'x.account.017@gmail.com|XPass901!','x-verified',0,NOW()),
(1006,NULL,'x.account.018@gmail.com|XPass234!','x-verified',0,NOW()),
(1006,NULL,'x.account.019@gmail.com|XPass567!','x-verified',0,NOW()),
(1006,NULL,'x.account.020@gmail.com|XPass890!','x-verified',0,NOW()),
(1006,NULL,'x.account.021@gmail.com|XPass123!','x-verified',0,NOW()),
(1006,NULL,'x.account.022@gmail.com|XPass456!','x-verified',0,NOW()),
(1006,NULL,'x.account.023@gmail.com|XPass789!','x-verified',0,NOW()),
(1006,NULL,'x.account.024@gmail.com|XPass012!','x-verified',0,NOW()),
(1006,NULL,'x.account.025@gmail.com|XPass345!','x-verified',0,NOW());

-- Credentials cho Canva (2001) - 45 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2001,NULL,'canva.account.001@canva.com|CanvaPass123!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.002@canva.com|CanvaPass456!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.003@canva.com|CanvaPass789!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.004@canva.com|CanvaPass012!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.005@canva.com|CanvaPass345!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.006@canva.com|CanvaPass678!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.007@canva.com|CanvaPass901!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.008@canva.com|CanvaPass234!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.009@canva.com|CanvaPass567!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.010@canva.com|CanvaPass890!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.011@canva.com|CanvaPass123!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.012@canva.com|CanvaPass456!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.013@canva.com|CanvaPass789!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.014@canva.com|CanvaPass012!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.015@canva.com|CanvaPass345!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.016@canva.com|CanvaPass678!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.017@canva.com|CanvaPass901!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.018@canva.com|CanvaPass234!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.019@canva.com|CanvaPass567!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.020@canva.com|CanvaPass890!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.021@canva.com|CanvaPass123!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.022@canva.com|CanvaPass456!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.023@canva.com|CanvaPass789!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.024@canva.com|CanvaPass012!','canva-1m',0,NOW()),
(2001,NULL,'canva.account.025@canva.com|CanvaPass345!','canva-1m',0,NOW()),
(2001,NULL,'canva.premium.001@canva.com|PremiumPass123!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.002@canva.com|PremiumPass456!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.003@canva.com|PremiumPass789!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.004@canva.com|PremiumPass012!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.005@canva.com|PremiumPass345!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.006@canva.com|PremiumPass678!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.007@canva.com|PremiumPass901!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.008@canva.com|PremiumPass234!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.009@canva.com|PremiumPass567!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.010@canva.com|PremiumPass890!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.011@canva.com|PremiumPass123!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.012@canva.com|PremiumPass456!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.013@canva.com|PremiumPass789!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.014@canva.com|PremiumPass012!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.015@canva.com|PremiumPass345!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.016@canva.com|PremiumPass678!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.017@canva.com|PremiumPass901!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.018@canva.com|PremiumPass234!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.019@canva.com|PremiumPass567!','canva-12m',0,NOW()),
(2001,NULL,'canva.premium.020@canva.com|PremiumPass890!','canva-12m',0,NOW());

-- Credentials cho Office (2002) - 18 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2002,NULL,'office.account.001@outlook.com|OfficePass123!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.002@outlook.com|OfficePass456!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.003@outlook.com|OfficePass789!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.004@outlook.com|OfficePass012!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.005@outlook.com|OfficePass345!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.006@outlook.com|OfficePass678!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.007@outlook.com|OfficePass901!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.008@outlook.com|OfficePass234!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.009@outlook.com|OfficePass567!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.010@outlook.com|OfficePass890!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.011@outlook.com|OfficePass123!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.012@outlook.com|OfficePass456!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.013@outlook.com|OfficePass789!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.014@outlook.com|OfficePass012!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.015@outlook.com|OfficePass345!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.016@outlook.com|OfficePass678!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.017@outlook.com|OfficePass901!','o365family-12m',0,NOW()),
(2002,NULL,'office.account.018@outlook.com|OfficePass234!','o365family-12m',0,NOW());

-- Credentials cho Windows (2003) - 30 credentials (key format)
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2003,NULL,'XXXXX-XXXXX-XXXXX-XXXXX-XXXXX|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'YYYYY-YYYYY-YYYYY-YYYYY-YYYYY|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'AAAAA-AAAAA-AAAAA-AAAAA-AAAAA|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'BBBBB-BBBBB-BBBBB-BBBBB-BBBBB|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'CCCCC-CCCCC-CCCCC-CCCCC-CCCCC|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'DDDDD-DDDDD-DDDDD-DDDDD-DDDDD|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'EEEEE-EEEEE-EEEEE-EEEEE-EEEEE|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'FFFFF-FFFFF-FFFFF-FFFFF-FFFFF|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'GGGGG-GGGGG-GGGGG-GGGGG-GGGGG|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'HHHHH-HHHHH-HHHHH-HHHHH-HHHHH|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'IIIII-IIIII-IIIII-IIIII-IIIII|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'JJJJJ-JJJJJ-JJJJJ-JJJJJ-JJJJJ|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'KKKKK-KKKKK-KKKKK-KKKKK-KKKKK|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'LLLLL-LLLLL-LLLLL-LLLLL-LLLLL|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'MMMMM-MMMMM-MMMMM-MMMMM-MMMMM|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'NNNNN-NNNNN-NNNNN-NNNNN-NNNNN|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'OOOOO-OOOOO-OOOOO-OOOOO-OOOOO|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'PPPPP-PPPPP-PPPPP-PPPPP-PPPPP|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'QQQQQ-QQQQQ-QQQQQ-QQQQQ-QQQQQ|Win11ProOEM','win11pro-oem',0,NOW()),
(2003,NULL,'RRRRR-RRRRR-RRRRR-RRRRR-RRRRR|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'SSSSS-SSSSS-SSSSS-SSSSS-SSSSS|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'TTTTT-TTTTT-TTTTT-TTTTT-TTTTT|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'UUUUU-UUUUU-UUUUU-UUUUU-UUUUU|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'VVVVV-VVVVV-VVVVV-VVVVV-VVVVV|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'WWWWW-WWWWW-WWWWW-WWWWW-WWWWW|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'XXXXX-XXXXX-XXXXX-XXXXX-XXXXX|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'YYYYY-YYYYY-YYYYY-YYYYY-YYYYY|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ|Win11ProRetail','win11pro-retail',0,NOW()),
(2003,NULL,'AAAAA-AAAAA-AAAAA-AAAAA-AAAAA|Win11ProRetail','win11pro-retail',0,NOW());

-- Credentials cho ChatGPT (2004) - 22 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2004,NULL,'chatgpt.account.001@gmail.com|ChatGPTPass123!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.002@gmail.com|ChatGPTPass456!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.003@gmail.com|ChatGPTPass789!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.004@gmail.com|ChatGPTPass012!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.005@gmail.com|ChatGPTPass345!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.006@gmail.com|ChatGPTPass678!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.007@gmail.com|ChatGPTPass901!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.008@gmail.com|ChatGPTPass234!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.009@gmail.com|ChatGPTPass567!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.010@gmail.com|ChatGPTPass890!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.011@gmail.com|ChatGPTPass123!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.012@gmail.com|ChatGPTPass456!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.013@gmail.com|ChatGPTPass789!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.014@gmail.com|ChatGPTPass012!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.account.015@gmail.com|ChatGPTPass345!','chatgpt-1m',0,NOW()),
(2004,NULL,'chatgpt.premium.001@gmail.com|PremiumPass123!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.002@gmail.com|PremiumPass456!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.003@gmail.com|PremiumPass789!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.004@gmail.com|PremiumPass012!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.005@gmail.com|PremiumPass345!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.006@gmail.com|PremiumPass678!','chatgpt-12m',0,NOW()),
(2004,NULL,'chatgpt.premium.007@gmail.com|PremiumPass901!','chatgpt-12m',0,NOW());

-- Credentials cho Valorant (2005) - 15 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2005,NULL,'valorant.account.001@gmail.com|ValoPass123!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.002@gmail.com|ValoPass456!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.003@gmail.com|ValoPass789!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.004@gmail.com|ValoPass012!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.005@gmail.com|ValoPass345!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.006@gmail.com|ValoPass678!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.007@gmail.com|ValoPass901!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.008@gmail.com|ValoPass234!','valo-silver',0,NOW()),
(2005,NULL,'valorant.account.009@gmail.com|ValoPass567!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.010@gmail.com|ValoPass890!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.011@gmail.com|ValoPass123!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.012@gmail.com|ValoPass456!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.013@gmail.com|ValoPass789!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.014@gmail.com|ValoPass012!','valo-gold',0,NOW()),
(2005,NULL,'valorant.account.015@gmail.com|ValoPass345!','valo-gold',0,NOW());

-- Credentials cho League of Legends (2006) - 12 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2006,NULL,'lol.account.001@gmail.com|LoLPass123!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.002@gmail.com|LoLPass456!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.003@gmail.com|LoLPass789!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.004@gmail.com|LoLPass012!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.005@gmail.com|LoLPass345!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.006@gmail.com|LoLPass678!','lol-gold',0,NOW()),
(2006,NULL,'lol.account.007@gmail.com|LoLPass901!','lol-plat',0,NOW()),
(2006,NULL,'lol.account.008@gmail.com|LoLPass234!','lol-plat',0,NOW()),
(2006,NULL,'lol.account.009@gmail.com|LoLPass567!','lol-plat',0,NOW()),
(2006,NULL,'lol.account.010@gmail.com|LoLPass890!','lol-plat',0,NOW()),
(2006,NULL,'lol.account.011@gmail.com|LoLPass123!','lol-plat',0,NOW()),
(2006,NULL,'lol.account.012@gmail.com|LoLPass456!','lol-plat',0,NOW());

-- Credentials cho CS2 (2007) - 20 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2007,NULL,'cs2.account.001@gmail.com|CS2Pass123!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.002@gmail.com|CS2Pass456!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.003@gmail.com|CS2Pass789!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.004@gmail.com|CS2Pass012!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.005@gmail.com|CS2Pass345!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.006@gmail.com|CS2Pass678!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.007@gmail.com|CS2Pass901!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.008@gmail.com|CS2Pass234!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.009@gmail.com|CS2Pass567!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.010@gmail.com|CS2Pass890!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.011@gmail.com|CS2Pass123!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.012@gmail.com|CS2Pass456!','cs2-prime',0,NOW()),
(2007,NULL,'cs2.account.013@gmail.com|CS2Pass789!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.014@gmail.com|CS2Pass012!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.015@gmail.com|CS2Pass345!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.016@gmail.com|CS2Pass678!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.017@gmail.com|CS2Pass901!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.018@gmail.com|CS2Pass234!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.019@gmail.com|CS2Pass567!','cs2-prime-mg',0,NOW()),
(2007,NULL,'cs2.account.020@gmail.com|CS2Pass890!','cs2-prime-mg',0,NOW());
-- =================================================================
SET FOREIGN_KEY_CHECKS = 1;
-- ====================================================================================
-- END OF FULL DB
-- ====================================================================================

