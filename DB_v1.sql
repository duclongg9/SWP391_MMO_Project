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
  `hashed_password` varchar(255) NOT NULL,
  `google_id` varchar(255) DEFAULT NULL UNIQUE,
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1: Active, 0: Inactive',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
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
  `status` enum('Pending','Active','Suspended') NOT NULL DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- ========================= PRODUCTS (optimized) =========================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int NOT NULL,

  -- Loại & Subtype cố định (ENUM)
  `product_type` ENUM('EMAIL','SOCIAL','SOFTWARE','GAME'),
  `product_subtype` ENUM('GMAIL','FACEBOOK','TIKTOK','CANVA','VALORANT','OTHER') NOT NULL DEFAULT 'OTHER',

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
  `is_sold` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
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
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `buyer_id` int NOT NULL,
  `product_id` int NOT NULL,
  `payment_transaction_id` int DEFAULT NULL,
  `total_amount` decimal(18,4) NOT NULL,
  `status` enum('Pending','Processing','Completed','Failed','Refunded','Disputed') NOT NULL,
  `idempotency_key` varchar(36) DEFAULT NULL UNIQUE,
  `hold_until` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `deposit_requests`;
CREATE TABLE `deposit_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `qr_content` varchar(255) NOT NULL UNIQUE,
  `idempotency_key` varchar(36) DEFAULT NULL UNIQUE,
  `status` enum('Pending','Completed','Failed','Expired','RequiresManualCheck') NOT NULL DEFAULT 'Pending',
  `expires_at` timestamp NOT NULL,
  `admin_note` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `withdrawal_requests`;
CREATE TABLE `withdrawal_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `bank_account_info` text NOT NULL,
  `status` enum('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending',
  `admin_proof_url` varchar(255) DEFAULT NULL,
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
DROP TABLE IF EXISTS `disputes`;
CREATE TABLE `disputes` (
    `id` int NOT NULL AUTO_INCREMENT,
    `order_id` int NOT NULL UNIQUE,
    `reporter_id` int NOT NULL,
    `resolved_by_admin_id` int DEFAULT NULL,
    `reason` text NOT NULL,
    `status` enum('Open','ResolvedWithRefund','ResolvedWithoutRefund','Closed') NOT NULL DEFAULT 'Open',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations` (
    `id` int NOT NULL AUTO_INCREMENT,
    `related_order_id` int DEFAULT NULL,
    `related_product_id` int DEFAULT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `conversation_participants`;
CREATE TABLE `conversation_participants` (
    `conversation_id` int NOT NULL,
    `user_id` int NOT NULL,
    PRIMARY KEY (`conversation_id`, `user_id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `conversation_id` int NOT NULL,
    `sender_id` int NOT NULL,
    `content` text NOT NULL,
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

ALTER TABLE `kyc_requests` ADD CONSTRAINT `fk_kyc_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `kyc_requests` ADD CONSTRAINT `fk_kyc_status_id` FOREIGN KEY (`status_id`) REFERENCES `kyc_request_statuses` (`id`);

ALTER TABLE `wallets` ADD CONSTRAINT `fk_wallets_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `wallet_transactions` ADD CONSTRAINT `fk_transactions_wallet_id` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`);

ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_buyer_id` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`);
ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);
ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_payment_transaction_id` FOREIGN KEY (`payment_transaction_id`) REFERENCES `wallet_transactions` (`id`);

ALTER TABLE `deposit_requests` ADD CONSTRAINT `fk_deposits_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `withdrawal_requests` ADD CONSTRAINT `fk_withdrawals_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `withdrawal_request_reasons_map` ADD CONSTRAINT `fk_map_request_id` FOREIGN KEY (`request_id`) REFERENCES `withdrawal_requests` (`id`);
ALTER TABLE `withdrawal_request_reasons_map` ADD CONSTRAINT `fk_map_reason_id` FOREIGN KEY (`reason_id`) REFERENCES `withdrawal_rejection_reasons` (`id`);

ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);
ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`);
ALTER TABLE `disputes` ADD CONSTRAINT `fk_disputes_admin_id` FOREIGN KEY (`resolved_by_admin_id`) REFERENCES `users` (`id`);

ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_conversation_id` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`);
ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`);
ALTER TABLE `conversation_participants` ADD CONSTRAINT `fk_participants_conversation_id` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`);
ALTER TABLE `conversation_participants` ADD CONSTRAINT `fk_participants_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `conversations` ADD CONSTRAINT `fk_conversations_order_id` FOREIGN KEY (`related_order_id`) REFERENCES `orders` (`id`);
ALTER TABLE `conversations` ADD CONSTRAINT `fk_conversations_product_id` FOREIGN KEY (`related_product_id`) REFERENCES `products` (`id`);

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
CREATE INDEX `idx_wallet_transactions_wallet_id` ON `wallet_transactions`(`wallet_id`);
CREATE INDEX `idx_wallet_transactions_type` ON `wallet_transactions`(`transaction_type`);
CREATE INDEX `idx_deposit_requests_status` ON `deposit_requests`(`status`);
CREATE INDEX `idx_withdrawal_requests_status` ON `withdrawal_requests`(`status`);
CREATE INDEX `idx_messages_conversation_id` ON `messages`(`conversation_id`);

-- =================================================================
-- Section 8: Seed Data (Vietnamese)
-- =================================================================

-- Roles
INSERT INTO `roles` (`id`,`name`) VALUES
 (1,'ADMIN'),(2,'SELLER'),(3,'BUYER');

-- Users (hash minh hoạ)
INSERT INTO `users` (`id`,`role_id`,`email`,`name`,`avatar_url`,`hashed_password`,`google_id`,`status`,`created_at`,`updated_at`) VALUES
 (1,1,'admin@mmo.local','Trung tâm điều hành MMO',NULL,'$2a$10$0wTNO3YKItPp4SOXGguoe.p5lWFhX.GacF6nwLxgPLZld0dfzpOw6',NULL,1,'2024-01-10 09:00:00','2024-01-10 09:00:00'),
 (2,2,'seller@mmo.local','Người bán Cyber Gear','https://cdn.mmo.local/avatar/seller.png','$2a$10$wx.3AZT4vEvSTQ8zbJk1QuVY6YwHzSgA0J8woMVgLvs9ceIRiuXXS',NULL,1,'2024-01-12 08:00:00','2024-01-20 09:30:00'),
 (3,3,'buyer@mmo.local','Người mua Pro Gamer','https://cdn.mmo.local/avatar/buyer.png','$2a$10$5q8ZP2Y1fnQdQ/9pJ6GJ5e2o6syKQnR6kHdF63N5NfKbG65v1kW6S',NULL,1,'2024-01-15 07:45:00','2024-01-27 07:45:00');

-- KYC statuses
INSERT INTO `kyc_request_statuses` (`id`,`status_name`) VALUES
 (1,'Đang chờ'),(2,'Đã duyệt'),(3,'Từ chối');

INSERT INTO `kyc_requests` (`id`,`user_id`,`status_id`,`front_image_url`,`back_image_url`,`selfie_image_url`,`id_number`,`admin_feedback`,`created_at`,`reviewed_at`) VALUES
 (1,2,2,'https://cdn.mmo.local/kyc/seller_front.jpg','https://cdn.mmo.local/kyc/seller_back.jpg','https://cdn.mmo.local/kyc/seller_selfie.jpg','079123456789','Hồ sơ hợp lệ','2024-01-13 10:30:00','2024-01-15 10:00:00'),
 (2,3,1,'https://cdn.mmo.local/kyc/buyer_front.jpg','https://cdn.mmo.local/kyc/buyer_back.jpg','https://cdn.mmo.local/kyc/buyer_selfie.jpg','092987654321',NULL,'2024-01-25 09:10:00',NULL);

-- Shop
INSERT INTO `shops` (`id`,`owner_id`,`name`,`description`,`status`,`created_at`) VALUES
 (1,2,'Cửa hàng Cyber Gear','Chuyên cung cấp tài khoản game và phần mềm bản quyền','Active','2024-01-12 08:30:00');

-- Products (6 sản phẩm mẫu, đủ loại/subtype; tiếng Việt, có biến thể JSON)
INSERT INTO `products`
(`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
VALUES
-- 1001: EMAIL/GMAIL
(1001,1,'EMAIL','GMAIL',
 'Gmail Doanh nghiệp 50GB',
 'Email doanh nghiệp 50GB, bảo hành, hỗ trợ đổi mật khẩu.',
 'Tài khoản Gmail Doanh nghiệp 50GB. Bạn nhận được email và mật khẩu kèm hướng dẫn đổi bảo mật 2 lớp.',
 250000.0000,
  'gmail.png',
  JSON_ARRAY('gmail.png'),
 19,128,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','gmail-basic-1m','attributes', JSON_OBJECT('service','gmail','plan','basic','duration','1m'),'price',250000.0000,'inventory_count',10,'status','Available'),
   JSON_OBJECT('variant_code','gmail-premium-12m','attributes', JSON_OBJECT('service','gmail','plan','premium','duration','12m'),'price',1200000.0000,'inventory_count',9,'status','Available')
 ),
 '2024-01-14 11:00:00','2024-01-20 12:00:00'),

-- 1002: SOFTWARE/OTHER (Spotify)
(1002,1,'SOFTWARE','OTHER',
 'Spotify Premium 12 tháng',
 'Gia hạn Spotify Premium 12 tháng, tài khoản chính chủ.',
 'Gia hạn Spotify Premium tài khoản chính chủ, bảo hành 30 ngày. Hỗ trợ kích hoạt nhanh.',
 80000.0000,
  'spotify.png',
  JSON_ARRAY('spotify.png'),
 27,256,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','sp-1m','attributes', JSON_OBJECT('service','spotify','duration','1m'),'price',80000.0000,'inventory_count',20,'status','Available'),
   JSON_OBJECT('variant_code','sp-12m','attributes', JSON_OBJECT('service','spotify','duration','12m'),'price',185000.0000,'inventory_count',7,'status','Available')
 ),
 '2024-01-18 09:15:00','2024-01-26 08:30:00'),

-- 1003: SOFTWARE/OTHER (Windows key) - Unlisted
(1003,1,'SOFTWARE','OTHER',
 'Key Windows 11 Pro',
 'Key bản quyền Windows 11 Pro, kích hoạt online.',
 'Khóa bản quyền Windows 11 Pro, kích hoạt online trọn đời. Lưu ý: sản phẩm hiện đang ẩn khỏi gian hàng.',
 390000.0000,
 'https://cdn.mmo.local/products/win11pro_main.jpg',
 JSON_ARRAY('https://cdn.mmo.local/products/win11pro_main.jpg'),
 50,42,'Unlisted','EDITION_LICENSE',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','win11pro-oem','attributes', JSON_OBJECT('edition','pro','license','oem'),'price',390000.0000,'inventory_count',50,'status','Unlisted')
 ),
 '2024-01-19 15:45:00','2024-01-19 15:45:00'),

-- 1004: SOCIAL/FACEBOOK
(1004,1,'SOCIAL','FACEBOOK',
 'Tài khoản Facebook cổ 2009+',
 'Facebook cổ năm 2009+, bảo hành đổi pass.',
 'Tài khoản Facebook cổ (năm tạo 2009–2012), có bảo hành đổi mật khẩu. Dùng cho chạy quảng cáo & seeding.',
 150000.0000,
  'facebook.png',
  JSON_ARRAY('facebook.png'),
 30,73,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','fb-2009','attributes', JSON_OBJECT('age','2009'),'price',180000.0000,'inventory_count',10,'status','Available'),
   JSON_OBJECT('variant_code','fb-2012','attributes', JSON_OBJECT('age','2012'),'price',150000.0000,'inventory_count',20,'status','Available')
 ),
 NOW(),NOW()),

-- 1005: SOCIAL/TIKTOK
(1005,1,'SOCIAL','TIKTOK',
 'Tài khoản TikTok Pro',
 'Tài khoản TikTok Pro, bảo hành đăng nhập.',
 'Tài khoản TikTok Pro, dùng quay và đăng video với analytics nâng cao.',
 99000.0000,
  'tiktok.png',
  JSON_ARRAY('tiktok.png'),
 40,58,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','tt-pro','attributes', JSON_OBJECT('tier','pro'),'price',99000.0000,'inventory_count',40,'status','Available')
 ),
 NOW(),NOW()),

-- 1006: GAME/VALORANT
(1006,1,'GAME','VALORANT',
 'Valorant VP (top-up)',
 'Nạp Valorant Points nhanh, giá tốt.',
 'Dịch vụ nạp Valorant Points (VP) nhiều mệnh giá, xử lý trong 5–10 phút.',
 95000.0000,
  'valorant.png',
  JSON_ARRAY('valorant.png'),
 100,121,'Available','CUSTOM',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','vp-470','attributes', JSON_OBJECT('amount','470VP'),'price',95000.0000,'inventory_count',50,'status','Available'),
   JSON_OBJECT('variant_code','vp-1375','attributes', JSON_OBJECT('amount','1375VP'),'price',270000.0000,'inventory_count',50,'status','Available')
 ),
 NOW(),NOW()),

-- 1007: SOFTWARE/CANVA
(1007,1,'SOFTWARE','CANVA',
 'Canva Pro chính chủ',
 'Canva Pro bản quyền 12 tháng, kích hoạt trực tiếp.',
 'Cung cấp tài khoản Canva Pro chính chủ, kích hoạt ngay sau khi thanh toán, bảo hành 30 ngày.',
 90000.0000,
  'canva.jpg',
  JSON_ARRAY('canva.jpg'),
 60,187,'Available','DURATION_PLAN',
 JSON_ARRAY(
   JSON_OBJECT('variant_code','canva-1m','attributes',JSON_OBJECT('duration','1m'),'price',90000.0000,'inventory_count',40,'status','Available'),
   JSON_OBJECT('variant_code','canva-12m','attributes',JSON_OBJECT('duration','12m'),'price',750000.0000,'inventory_count',20,'status','Available')
 ),
 '2024-01-18 10:00:00','2024-01-28 08:00:00');

INSERT INTO `products`
(`id`,`shop_id`,`product_type`,`product_subtype`,`name`,`short_description`,`description`,`price`,`primary_image_url`,`gallery_json`,`inventory_count`,`sold_count`,`status`,`variant_schema`,`variants_json`,`created_at`,`updated_at`)
VALUES
(1008,1,'EMAIL','GMAIL','Gmail Doanh nghiệp 100GB','Gmail 100GB, bảo hành, hướng dẫn 2FA.','Tài khoản Gmail dung lượng 100GB, hỗ trợ bảo mật 2 lớp và khôi phục.',350000.0000,'gmail.png',JSON_ARRAY('gmail.png'),20,95,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','gmail-100g-1m','attributes', JSON_OBJECT('service','gmail','plan','100GB','duration','1m'),'price',350000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','gmail-100g-12m','attributes', JSON_OBJECT('service','gmail','plan','100GB','duration','12m'),'price',1800000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1009,1,'SOCIAL','FACEBOOK','Facebook cổ 2013+ (Mail+Pass)','Tài khoản Facebook cổ, có mail & pass.','Tài khoản Facebook năm 2013–2015, dành cho marketing hợp lệ theo chính sách.',180000.0000,'facebook.png',JSON_ARRAY('facebook.png'),25,210,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','fb-2013','attributes', JSON_OBJECT('year','2013','verify','mail+pass'),'price',180000.0000,'inventory_count',15,'status','Available'),
  JSON_OBJECT('variant_code','fb-2015','attributes', JSON_OBJECT('year','2015','verify','mail+pass'),'price',160000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1010,1,'SOCIAL','TIKTOK','TikTok Shop – Seller Pro','Thiết lập & tối ưu TikTok Shop.','Dịch vụ tạo TikTok Shop cho người bán mới, kèm tài liệu vận hành.',300000.0000,'tiktok.png',JSON_ARRAY('tiktok.png'),20,74,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','ttshop-basic-1m','attributes', JSON_OBJECT('service','tiktok_shop','plan','basic','duration','1m'),'price',300000.0000,'inventory_count',12,'status','Available'),
  JSON_OBJECT('variant_code','ttshop-pro-3m','attributes', JSON_OBJECT('service','tiktok_shop','plan','pro','duration','3m'),'price',700000.0000,'inventory_count',8,'status','Available')
),NOW(),NOW()),
(1011,1,'SOFTWARE','CANVA','Canva Pro 1 tháng','Cấp quyền Canva Pro 1 tháng.','Tài khoản Canva Pro 1 tháng, dùng thiết kế không giới hạn mẫu.',90000.0000,'canva.jpg',JSON_ARRAY('canva.jpg'),40,320,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','canva-1m','attributes', JSON_OBJECT('service','canva','plan','pro','duration','1m'),'price',90000.0000,'inventory_count',25,'status','Available'),
  JSON_OBJECT('variant_code','canva-12m','attributes', JSON_OBJECT('service','canva','plan','pro','duration','12m'),'price',850000.0000,'inventory_count',15,'status','Available')
),NOW(),NOW()),
(1012,1,'SOFTWARE','CANVA','Canva Team 5 người','Nhóm Canva Pro 5 seats.','Canva Pro cho nhóm 5 người, chia sẻ brand kit & thư viện.',250000.0000,'canva-team.png',JSON_ARRAY('canva-team.png'),18,96,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','canvateam5-1m','attributes', JSON_OBJECT('service','canva','seats',5,'duration','1m'),'price',250000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','canvateam5-12m','attributes', JSON_OBJECT('service','canva','seats',5,'duration','12m'),'price',2400000.0000,'inventory_count',8,'status','Available')
),NOW(),NOW()),
(1013,1,'SOFTWARE','OTHER','Windows 11 Pro (Key Retail)','Key kích hoạt Windows 11 Pro bản quyền.','Cung cấp key Windows 11 Pro loại Retail, hướng dẫn active hợp lệ.',450000.0000,'win11pro.png',JSON_ARRAY('win11pro.png'),30,190,'Available','EDITION_LICENSE',JSON_ARRAY(
  JSON_OBJECT('variant_code','win11pro-retail','attributes', JSON_OBJECT('edition','Pro','activation','Retail'),'price',450000.0000,'inventory_count',30,'status','Available')
),NOW(),NOW()),
(1014,1,'EMAIL','OTHER','Outlook 50GB','Hộp thư Outlook 50GB, bảo mật.','Tài khoản Outlook dung lượng 50GB, kèm hướng dẫn bảo mật & khôi phục.',200000.0000,'outlook.png',JSON_ARRAY('outlook.png'),14,57,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','outlook-1m','attributes', JSON_OBJECT('service','outlook','plan','standard','duration','1m'),'price',200000.0000,'inventory_count',8,'status','Available'),
  JSON_OBJECT('variant_code','outlook-12m','attributes', JSON_OBJECT('service','outlook','plan','standard','duration','12m'),'price',1200000.0000,'inventory_count',6,'status','Available')
),NOW(),NOW()),
(1015,1,'GAME','VALORANT','Valorant Account – Rank Silver/Gold','Tài khoản Valorant rank Silver/Gold.','Tài khoản Valorant, vùng AP/SEA, bảo hành đăng nhập.',400000.0000,'valorant.png',JSON_ARRAY('valorant.png'),12,45,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','valo-silver','attributes', JSON_OBJECT('rank','Silver','region','AP'),'price',400000.0000,'inventory_count',7,'status','Available'),
  JSON_OBJECT('variant_code','valo-gold','attributes', JSON_OBJECT('rank','Gold','region','AP'),'price',550000.0000,'inventory_count',5,'status','Available')
),NOW(),NOW()),
(1016,1,'GAME','VALORANT','Nạp Valorant VP','Gói nạp VP chính chủ.','Dịch vụ nạp Valorant Points (VP) theo mệnh giá, giao nhanh.',160000.0000,'valorant-topup.png',JSON_ARRAY('valorant-topup.png'),50,260,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','vp-475','attributes', JSON_OBJECT('amount','475'),'price',160000.0000,'inventory_count',20,'status','Available'),
  JSON_OBJECT('variant_code','vp-1375','attributes', JSON_OBJECT('amount','1375'),'price',430000.0000,'inventory_count',20,'status','Available'),
  JSON_OBJECT('variant_code','vp-2400','attributes', JSON_OBJECT('amount','2400'),'price',720000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1017,1,'SOCIAL','FACEBOOK','Business Manager (BM) gói hỗ trợ','Tư vấn & cấu hình BM.','Thiết lập BM, hướng dẫn bảo mật & phân quyền đúng chính sách.',220000.0000,'facebook-bm.png',JSON_ARRAY('facebook-bm.png'),22,81,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','bm-basic','attributes', JSON_OBJECT('package','basic','support','7d'),'price',220000.0000,'inventory_count',12,'status','Available'),
  JSON_OBJECT('variant_code','bm-pro','attributes', JSON_OBJECT('package','pro','support','30d'),'price',480000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1018,1,'SOCIAL','TIKTOK','TikTok Live – Setup & Coaching','Thiết lập Live + coaching.','Cài đặt TikTok Live Studio, cấu hình cảnh & âm thanh, huấn luyện vận hành.',350000.0000,'tiktok-live.png',JSON_ARRAY('tiktok-live.png'),18,69,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','ttlive-basic','attributes', JSON_OBJECT('package','basic','sessions',1),'price',350000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','ttlive-pro','attributes', JSON_OBJECT('package','pro','sessions',3),'price',900000.0000,'inventory_count',8,'status','Available')
),NOW(),NOW()),
(1019,1,'EMAIL','GMAIL','Gmail gói bảo hành 3 tháng','Gmail bảo hành 3 tháng, hỗ trợ bảo mật.','Tài khoản Gmail mới, hỗ trợ kỹ thuật trong 3 tháng.',270000.0000,'gmail.png',JSON_ARRAY('gmail.png'),20,102,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','gmail-3m','attributes', JSON_OBJECT('service','gmail','plan','standard','duration','3m'),'price',270000.0000,'inventory_count',12,'status','Available'),
  JSON_OBJECT('variant_code','gmail-12m','attributes', JSON_OBJECT('service','gmail','plan','standard','duration','12m'),'price',1000000.0000,'inventory_count',8,'status','Available')
),NOW(),NOW()),
(1020,1,'SOFTWARE','OTHER','Office 365 Family 12 tháng (6 người)','Chia sẻ Office 365 Family hợp lệ.','Gói Office 365 Family 12 tháng cho 6 tài khoản, hướng dẫn kích hoạt.',890000.0000,'office365.png',JSON_ARRAY('office365.png'),16,130,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','o365family-12m','attributes', JSON_OBJECT('seats',6,'duration','12m'),'price',890000.0000,'inventory_count',16,'status','Available')
),NOW(),NOW()),
(1021,1,'SOFTWARE','OTHER','Adobe Photoshop (Key 12 tháng)','Key kích hoạt Photoshop 12 tháng.','Cung cấp key Adobe Photoshop dùng 12 tháng cho cá nhân.',1500000.0000,'adobe-ps.png',JSON_ARRAY('adobe-ps.png'),10,41,'Available','EDITION_LICENSE',JSON_ARRAY(
  JSON_OBJECT('variant_code','ps-12m','attributes', JSON_OBJECT('product','Photoshop','term','12m'),'price',1500000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1022,1,'GAME','OTHER','Steam Wallet Code (VN)','Mã nạp Steam mệnh giá VN.','Cung cấp mã Steam Wallet Code theo mệnh giá phổ biến.',500000.0000,'steam.png',JSON_ARRAY('steam.png'),35,220,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','steam-100k','attributes', JSON_OBJECT('amount','100000'),'price',105000.0000,'inventory_count',15,'status','Available'),
  JSON_OBJECT('variant_code','steam-200k','attributes', JSON_OBJECT('amount','200000'),'price',205000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','steam-500k','attributes', JSON_OBJECT('amount','500000'),'price',500000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1023,1,'GAME','VALORANT','Valorant – Skin Bundle Random','Account có skin bundle ngẫu nhiên.','Tài khoản Valorant unranked, sở hữu skin random; bảo hành đăng nhập.',350000.0000,'valorant-skin.png',JSON_ARRAY('valorant-skin.png'),9,33,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','valo-skin-rand','attributes', JSON_OBJECT('rank','Unranked','skin_bundle','random'),'price',350000.0000,'inventory_count',9,'status','Available')
),NOW(),NOW()),
(1024,1,'SOCIAL','OTHER','Discord Nitro','Gói Discord Nitro theo thời hạn.','Discord Nitro hỗ trợ upload lớn, emoji & perks.',95000.0000,'discord.png',JSON_ARRAY('discord.png'),28,175,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','nitro-1m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','1m'),'price',95000.0000,'inventory_count',18,'status','Available'),
  JSON_OBJECT('variant_code','nitro-3m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','3m'),'price',270000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','nitro-12m','attributes', JSON_OBJECT('service','discord','plan','nitro','duration','12m'),'price',980000.0000,'inventory_count',5,'status','Available')
),NOW(),NOW()),
(1025,1,'SOCIAL','FACEBOOK','Fanpage Template & Hướng dẫn','Bộ template + hướng dẫn vận hành fanpage.','Tài nguyên số cho fanpage: template bài viết, checklist, lịch đăng.',120000.0000,'facebook-page.png',JSON_ARRAY('facebook-page.png'),40,95,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','fp-template-basic','attributes', JSON_OBJECT('package','basic'),'price',120000.0000,'inventory_count',25,'status','Available'),
  JSON_OBJECT('variant_code','fp-template-pro','attributes', JSON_OBJECT('package','pro'),'price',240000.0000,'inventory_count',15,'status','Available')
),NOW(),NOW()),
(1026,1,'SOCIAL','TIKTOK','TikTok Shop – Gói đào tạo','Đào tạo vận hành TikTok Shop.','Khoá đào tạo 1:1/nhóm về vận hành TikTok Shop.',650000.0000,'tiktok-course.png',JSON_ARRAY('tiktok-course.png'),12,58,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','tttrain-1s','attributes', JSON_OBJECT('sessions',1,'duration','2h'),'price',650000.0000,'inventory_count',7,'status','Available'),
  JSON_OBJECT('variant_code','tttrain-3s','attributes', JSON_OBJECT('sessions',3,'duration','6h'),'price',1800000.0000,'inventory_count',5,'status','Available')
),NOW(),NOW()),
(1027,1,'EMAIL','GMAIL','Gmail – Cài đặt Forward & Filter','Thiết lập chuyển tiếp, bộ lọc, nhãn chuyên nghiệp.','Dịch vụ thiết kế hệ thống hộp thư: forward, filter, nhãn, auto-reply.',170000.0000,'gmail-setup.png',JSON_ARRAY('gmail-setup.png'),25,60,'Available','CUSTOM',JSON_ARRAY(
  JSON_OBJECT('variant_code','gmail-setup-basic','attributes', JSON_OBJECT('package','basic'),'price',170000.0000,'inventory_count',15,'status','Available'),
  JSON_OBJECT('variant_code','gmail-setup-pro','attributes', JSON_OBJECT('package','pro'),'price',320000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW()),
(1028,1,'SOFTWARE','CANVA','Canva Pro – Gia hạn 36 tháng','Gia hạn Canva Pro 24–36 tháng.','Dịch vụ gia hạn Canva Pro dài hạn tiết kiệm.',2300000.0000,'canva-extend.png',JSON_ARRAY('canva-extend.png'),6,22,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','canva-24m','attributes', JSON_OBJECT('service','canva','duration','24m'),'price',1600000.0000,'inventory_count',3,'status','Available'),
  JSON_OBJECT('variant_code','canva-36m','attributes', JSON_OBJECT('service','canva','duration','36m'),'price',2300000.0000,'inventory_count',3,'status','Available')
),NOW(),NOW()),
(1029,1,'EMAIL','GMAIL','Gmail Edu (tuỳ trường)','Gmail Edu dung lượng lớn, dùng cho học tập.','Tài khoản Gmail Edu (tuỳ trường), kèm hướng dẫn bảo mật & sử dụng.',300000.0000,'gmail-edu.png',JSON_ARRAY('gmail-edu.png'),18,70,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','gmail-edu-6m','attributes', JSON_OBJECT('service','gmail','segment','edu','duration','6m'),'price',300000.0000,'inventory_count',10,'status','Available'),
  JSON_OBJECT('variant_code','gmail-edu-12m','attributes', JSON_OBJECT('service','gmail','segment','edu','duration','12m'),'price',520000.0000,'inventory_count',8,'status','Available')
),NOW(),NOW()),
(1030,1,'SOCIAL','OTHER','Telegram Premium','Nâng cấp Telegram Premium theo kỳ hạn.','Tính năng Premium: upload lớn, chuyển giọng nói sang text, sticker/emoji nâng cao.',115000.0000,'telegram-premium.png',JSON_ARRAY('telegram-premium.png'),26,110,'Available','DURATION_PLAN',JSON_ARRAY(
  JSON_OBJECT('variant_code','tg-prem-1m','attributes', JSON_OBJECT('service','telegram','duration','1m'),'price',115000.0000,'inventory_count',16,'status','Available'),
  JSON_OBJECT('variant_code','tg-prem-12m','attributes', JSON_OBJECT('service','telegram','duration','12m'),'price',1150000.0000,'inventory_count',10,'status','Available')
),NOW(),NOW());



-- Credentials (ví dụ ràng buộc với đơn)
INSERT INTO `product_credentials` (`id`,`product_id`,`order_id`,`encrypted_value`,`is_sold`,`created_at`) VALUES
 (1,1001,5001,'ENCRYPTED-CODE-001',1,'2024-01-20 12:05:00'),
 (2,1001,NULL,'ENCRYPTED-CODE-002',0,'2024-01-21 10:20:00'),
 (3,1002,NULL,'ENCRYPTED-CODE-201',0,'2024-01-26 08:30:00');

-- Wallets
INSERT INTO `wallets` (`id`,`user_id`,`balance`,`status`,`created_at`,`updated_at`) VALUES
 (1,1,0.0000,1,'2024-01-11 09:00:00','2024-01-11 09:00:00'),
 (2,2,330000.0000,1,'2024-01-12 08:40:00','2024-01-26 09:45:00'),
 (3,3,50000.0000,1,'2024-01-15 08:00:00','2024-01-20 12:10:00');

INSERT INTO `deposit_requests` (`id`,`user_id`,`amount`,`qr_content`,`idempotency_key`,`status`,`expires_at`,`admin_note`,`created_at`) VALUES
 (1,3,300000.0000,'VietQR|MMO|INV-20240120','DEPOSIT-UUID-1','Completed','2024-01-20 12:00:00','Đối soát thành công','2024-01-20 10:15:00'),
 (2,3,150000.0000,'VietQR|MMO|INV-20240127','DEPOSIT-UUID-2','Pending','2024-01-27 12:00:00',NULL,'2024-01-27 09:00:00');

INSERT INTO `wallet_transactions` (`id`,`wallet_id`,`related_entity_id`,`transaction_type`,`amount`,`balance_before`,`balance_after`,`note`,`created_at`) VALUES
 (1,3,1,'Deposit',300000.0000,0.0000,300000.0000,'Nạp tiền qua VietQR','2024-01-20 10:16:00'),
 (2,3,5001,'Purchase',-250000.0000,300000.0000,50000.0000,'Thanh toán đơn hàng #5001','2024-01-20 12:00:00'),
 (3,2,5001,'Payout',250000.0000,200000.0000,450000.0000,'Doanh thu đơn #5001','2024-01-20 12:01:00'),
 (4,2,1,'Withdrawal',-120000.0000,450000.0000,330000.0000,'Rút tiền về Vietcombank','2024-01-26 09:40:00');

-- Orders (ví dụ Completed + Disputed)
INSERT INTO `orders` (`id`,`buyer_id`,`product_id`,`payment_transaction_id`,`total_amount`,`status`,`idempotency_key`,`hold_until`,`created_at`,`updated_at`) VALUES
 (5001,3,1001,2,250000.0000,'Completed','ORDER-5001-KEY','2024-01-23 12:00:00','2024-01-20 10:45:00','2024-01-20 12:05:00'),
 (5002,3,1002,NULL,185000.0000,'Disputed','ORDER-5002-KEY','2024-02-01 00:00:00','2024-01-26 09:00:00','2024-01-27 08:10:00');

-- Withdrawals
INSERT INTO `withdrawal_rejection_reasons` (`id`,`reason_code`,`description`,`is_active`) VALUES
 (1,'ACCOUNT_ERROR','Thông tin tài khoản ngân hàng không khớp',1),
 (2,'SUSPICIOUS_ACTIVITY','Giao dịch có dấu hiệu bất thường, cần xác minh thêm',1);

INSERT INTO `withdrawal_requests` (`id`,`user_id`,`amount`,`bank_account_info`,`status`,`admin_proof_url`,`created_at`,`processed_at`) VALUES
 (1,2,120000.0000,'{\"bank\":\"VCB\",\"account\":\"0123456789\",\"name\":\"Trần Văn Seller\"}','Completed','https://cdn.mmo.local/withdrawals/receipt-1.png','2024-01-26 09:00:00','2024-01-26 09:45:00'),
 (2,2,80000.0000,'{\"bank\":\"ACB\",\"account\":\"9876543210\",\"name\":\"Trần Văn Seller\"}','Rejected',NULL,'2024-02-03 09:00:00','2024-02-03 10:00:00');

INSERT INTO `withdrawal_request_reasons_map` (`request_id`,`reason_id`) VALUES (2,2);

-- Support / Chat
INSERT INTO `disputes` (`id`,`order_id`,`reporter_id`,`resolved_by_admin_id`,`reason`,`status`,`created_at`,`updated_at`) VALUES
 (1,5002,3,NULL,'Tài khoản Spotify không hoạt động','Open','2024-01-27 08:10:00','2024-01-27 08:10:00');

INSERT INTO `conversations` (`id`,`related_order_id`,`related_product_id`,`created_at`) VALUES
 (1,5002,1002,'2024-01-27 08:00:00');

INSERT INTO `conversation_participants` (`conversation_id`,`user_id`) VALUES (1,2),(1,3);

INSERT INTO `messages` (`id`,`conversation_id`,`sender_id`,`content`,`created_at`) VALUES
 (1,1,3,'Shop ơi, tài khoản Spotify không hoạt động.','2024-01-27 08:05:00'),
 (2,1,2,'Bên mình đang kiểm tra lại thông tin giúp bạn.','2024-01-27 08:07:00');

-- Inventory logs
INSERT INTO `inventory_logs` (`id`,`product_id`,`related_order_id`,`change_amount`,`reason`,`created_at`) VALUES
 (1,1001,5001,-1,'Sale','2024-01-20 12:00:00'),
 (2,1002,5002,-1,'Sale','2024-01-26 09:05:00'),
 (3,1002,NULL,5,'ManualRestock','2024-01-29 10:00:00');

-- System configs
INSERT INTO `system_configs` (`id`,`config_key`,`config_value`,`description`,`created_at`,`updated_at`) VALUES
 (1,'escrow.release.hours','72','Thời gian giữ tiền trước khi tự động giải ngân','2024-01-10 09:00:00','2024-01-10 09:00:00'),
 (2,'support.email','support@mmo.local','Email bộ phận hỗ trợ khách hàng','2024-01-10 09:00:00','2024-01-10 09:00:00');

-- =================================================================
SET FOREIGN_KEY_CHECKS = 1;
-- ====================================================================================
-- END OF FULL DB
-- ====================================================================================
