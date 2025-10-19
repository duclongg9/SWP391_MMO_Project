-- ====================================================================================
-- ==                                                                                ==
-- ==                 COMPLETE DATABASE SCHEMA FOR FUS PROJECT                       ==
-- ==      (Includes Levels 1, 2, and 3: Base, Business Logic, & Optimization)       ==
-- ==                                                                                ==
-- ====================================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS mmo_schema;
CREATE DATABASE mmo_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mmo_schema;
-- =================================================================
-- Section 1: Core Tables (Users, Shops, Products)
-- =================================================================

-- Table for Roles (Admin, Seller, Buyer)
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for Users
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

-- Table for Shops
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

-- Table for Products
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  `price` decimal(18,4) NOT NULL,
  `inventory_count` int NOT NULL DEFAULT '0',
  `status` enum('Available','OutOfStock','Unlisted') NOT NULL DEFAULT 'Available',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for storing sensitive product data (e.g., keys, links)
DROP TABLE IF EXISTS `product_credentials`;
CREATE TABLE `product_credentials` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `order_id` int DEFAULT NULL COMMENT 'Link to order when sold',
  `encrypted_value` text NOT NULL,
  `is_sold` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;


-- =================================================================
-- Section 2: KYC (Know Your Customer) Tables
-- =================================================================

-- Table for KYC Request Statuses
DROP TABLE IF EXISTS `kyc_request_statuses`;
CREATE TABLE `kyc_request_statuses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `status_name` varchar(50) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for KYC Requests
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
-- Section 3: Financial & Transactional Tables
-- =================================================================

-- Table for Wallets
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

-- Table for Wallet Transactions (immutable log)
DROP TABLE IF EXISTS `wallet_transactions`;
CREATE TABLE `wallet_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `wallet_id` int NOT NULL,
  `related_entity_id` int DEFAULT NULL COMMENT 'Order, Deposit, or Withdrawal ID',
  `transaction_type` enum('Deposit','Purchase','Withdrawal','Refund','Fee','Payout') NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `balance_before` decimal(18,4) NOT NULL,
  `balance_after` decimal(18,4) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for Orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `buyer_id` int NOT NULL,
  `product_id` int NOT NULL,
  `payment_transaction_id` int DEFAULT NULL,
  `total_amount` decimal(18,4) NOT NULL,
  `status` enum('Pending','Processing','Completed','Failed','Refunded','Disputed') NOT NULL,
  `idempotency_key` varchar(36) DEFAULT NULL UNIQUE,
  `hold_until` timestamp NULL DEFAULT NULL COMMENT 'Escrow release time',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for Deposit Requests (VietQR flow)
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

-- Table for Withdrawal Requests
DROP TABLE IF EXISTS `withdrawal_requests`;
CREATE TABLE `withdrawal_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `amount` decimal(18,4) NOT NULL,
  `bank_account_info` text NOT NULL COMMENT 'Can be JSON for structured data',
  `status` enum('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending',
  `admin_proof_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for standardized withdrawal rejection reasons
DROP TABLE IF EXISTS `withdrawal_rejection_reasons`;
CREATE TABLE `withdrawal_rejection_reasons` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `reason_code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    PRIMARY KEY(`id`)
) ENGINE=InnoDB;

-- Junction table for many-to-many relationship between requests and reasons
DROP TABLE IF EXISTS `withdrawal_request_reasons_map`;
CREATE TABLE `withdrawal_request_reasons_map` (
    `request_id` INTEGER NOT NULL,
    `reason_id` INTEGER NOT NULL,
    PRIMARY KEY (`request_id`, `reason_id`)
) ENGINE=InnoDB;

-- =================================================================
-- Section 4: Support & Communication Tables
-- =================================================================

-- Table for managing disputes/complaints
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

-- Table for Chat Conversations
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations` (
    `id` int NOT NULL AUTO_INCREMENT,
    `related_order_id` int DEFAULT NULL,
    `related_product_id` int DEFAULT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for mapping users to conversations
DROP TABLE IF EXISTS `conversation_participants`;
CREATE TABLE `conversation_participants` (
    `conversation_id` int NOT NULL,
    `user_id` int NOT NULL,
    PRIMARY KEY (`conversation_id`, `user_id`)
) ENGINE=InnoDB;

-- Table for Chat Messages
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
-- Section 5: Auditing & System Tables
-- =================================================================

-- Immutable log for inventory changes
DROP TABLE IF EXISTS `inventory_logs`;
CREATE TABLE `inventory_logs` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `product_id` int NOT NULL,
    `related_order_id` int DEFAULT NULL,
    `change_amount` int NOT NULL COMMENT 'e.g., -1 for a sale, +1 for a restock',
    `reason` varchar(255) NOT NULL COMMENT 'e.g., ''Sale'', ''RefundRestock''',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- Table for System Configurations
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
-- Section 6: Foreign Key Constraints
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
-- Section 7: Sample Seed Data
-- =================================================================

-- Seed roles used by the authorization layer
INSERT INTO `roles` (`id`, `name`) VALUES
    (1, 'ADMIN'),
    (2, 'SELLER'),
    (3, 'BUYER');

-- Demo users for admin, seller and buyer personas
INSERT INTO `users` (`id`, `role_id`, `email`, `name`, `avatar_url`, `hashed_password`, `google_id`, `status`, `created_at`, `updated_at`) VALUES
    (1, 1, 'admin@mmo.local', 'MMO Control Center', NULL, '$2a$10$0wTNO3YKItPp4SOXGguoe.p5lWFhX.GacF6nwLxgPLZld0dfzpOw6', NULL, 1, '2024-01-10 09:00:00', '2024-01-10 09:00:00'),
    (2, 2, 'seller@mmo.local', 'Cyber Gear Seller', 'https://cdn.mmo.local/avatar/seller.png', '$2a$10$wx.3AZT4vEvSTQ8zbJk1QuVY6YwHzSgA0J8woMVgLvs9ceIRiuXXS', NULL, 1, '2024-01-12 08:00:00', '2024-01-20 09:30:00'),
    (3, 3, 'buyer@mmo.local', 'Pro Gamer Buyer', 'https://cdn.mmo.local/avatar/buyer.png', '$2a$10$5q8ZP2Y1fnQdQ/9pJ6GJ5e2o6syKQnR6kHdF63N5NfKbG65v1kW6S', NULL, 1, '2024-01-15 07:45:00', '2024-01-27 07:45:00');

-- Sample KYC statuses and requests
INSERT INTO `kyc_request_statuses` (`id`, `status_name`) VALUES
    (1, 'Pending'),
    (2, 'Approved'),
    (3, 'Rejected');

INSERT INTO `kyc_requests` (`id`, `user_id`, `status_id`, `front_image_url`, `back_image_url`, `selfie_image_url`, `id_number`, `admin_feedback`, `created_at`, `reviewed_at`) VALUES
    (1, 2, 2, 'https://cdn.mmo.local/kyc/seller_front.jpg', 'https://cdn.mmo.local/kyc/seller_back.jpg', 'https://cdn.mmo.local/kyc/seller_selfie.jpg', '079123456789', 'Hồ sơ hợp lệ', '2024-01-13 10:30:00', '2024-01-15 10:00:00'),
    (2, 3, 1, 'https://cdn.mmo.local/kyc/buyer_front.jpg', 'https://cdn.mmo.local/kyc/buyer_back.jpg', 'https://cdn.mmo.local/kyc/buyer_selfie.jpg', '092987654321', NULL, '2024-01-25 09:10:00', NULL);

-- Seller shop and inventory
INSERT INTO `shops` (`id`, `owner_id`, `name`, `description`, `status`, `created_at`) VALUES
    (1, 2, 'Cyber Gear Store', 'Chuyên cung cấp tài khoản game & phần mềm bản quyền', 'Active', '2024-01-12 08:30:00');

INSERT INTO `products` (`id`, `shop_id`, `name`, `description`, `price`, `inventory_count`, `status`, `created_at`, `updated_at`) VALUES
    (1001, 1, 'Gmail Business 50GB', 'Tài khoản Gmail doanh nghiệp dung lượng 50GB kèm hướng dẫn đổi mật khẩu.', 250000.0000, 19, 'Available', '2024-01-14 11:00:00', '2024-01-20 12:00:00'),
    (1002, 1, 'Spotify Premium 12 tháng', 'Gia hạn Spotify Premium tài khoản chính chủ, bảo hành 30 ngày.', 185000.0000, 7, 'Available', '2024-01-18 09:15:00', '2024-01-26 08:30:00'),
    (1003, 1, 'Windows 11 Pro key', 'Key bản quyền Windows 11 Pro, kích hoạt online trọn đời.', 390000.0000, 50, 'Unlisted', '2024-01-19 15:45:00', '2024-01-19 15:45:00');

INSERT INTO `product_credentials` (`id`, `product_id`, `order_id`, `encrypted_value`, `is_sold`, `created_at`) VALUES
    (1, 1001, 5001, 'ENCRYPTED-CODE-001', 1, '2024-01-20 12:05:00'),
    (2, 1001, NULL, 'ENCRYPTED-CODE-002', 0, '2024-01-21 10:20:00'),
    (3, 1002, NULL, 'ENCRYPTED-CODE-201', 0, '2024-01-26 08:30:00');

-- Wallets & financial flows
INSERT INTO `wallets` (`id`, `user_id`, `balance`, `status`, `created_at`, `updated_at`) VALUES
    (1, 1, 0.0000, 1, '2024-01-11 09:00:00', '2024-01-11 09:00:00'),
    (2, 2, 330000.0000, 1, '2024-01-12 08:40:00', '2024-01-26 09:45:00'),
    (3, 3, 50000.0000, 1, '2024-01-15 08:00:00', '2024-01-20 12:10:00');

INSERT INTO `deposit_requests` (`id`, `user_id`, `amount`, `qr_content`, `idempotency_key`, `status`, `expires_at`, `admin_note`, `created_at`) VALUES
    (1, 3, 300000.0000, 'VietQR|MMO|INV-20240120', 'DEPOSIT-UUID-1', 'Completed', '2024-01-20 12:00:00', 'Đối soát thành công', '2024-01-20 10:15:00'),
    (2, 3, 150000.0000, 'VietQR|MMO|INV-20240127', 'DEPOSIT-UUID-2', 'Pending', '2024-01-27 12:00:00', NULL, '2024-01-27 09:00:00');

INSERT INTO `wallet_transactions` (`id`, `wallet_id`, `related_entity_id`, `transaction_type`, `amount`, `balance_before`, `balance_after`, `note`, `created_at`) VALUES
    (1, 3, 1, 'Deposit', 300000.0000, 0.0000, 300000.0000, 'Nạp tiền qua VietQR', '2024-01-20 10:16:00'),
    (2, 3, 5001, 'Purchase', -250000.0000, 300000.0000, 50000.0000, 'Thanh toán đơn hàng #5001', '2024-01-20 12:00:00'),
    (3, 2, 5001, 'Payout', 250000.0000, 200000.0000, 450000.0000, 'Doanh thu đơn #5001', '2024-01-20 12:01:00'),
    (4, 2, 1, 'Withdrawal', -120000.0000, 450000.0000, 330000.0000, 'Rút tiền về Vietcombank', '2024-01-26 09:40:00');

INSERT INTO `orders` (`id`, `buyer_id`, `product_id`, `payment_transaction_id`, `total_amount`, `status`, `idempotency_key`, `hold_until`, `created_at`, `updated_at`) VALUES
    (5001, 3, 1001, 2, 250000.0000, 'Completed', 'ORDER-5001-KEY', '2024-01-23 12:00:00', '2024-01-20 10:45:00', '2024-01-20 12:05:00'),
    (5002, 3, 1002, NULL, 185000.0000, 'Disputed', 'ORDER-5002-KEY', '2024-02-01 00:00:00', '2024-01-26 09:00:00', '2024-01-27 08:10:00');

INSERT INTO `withdrawal_rejection_reasons` (`id`, `reason_code`, `description`, `is_active`) VALUES
    (1, 'ACCOUNT_ERROR', 'Thông tin tài khoản ngân hàng không khớp', 1),
    (2, 'SUSPICIOUS_ACTIVITY', 'Giao dịch bị nghi ngờ gian lận, cần xác minh thêm', 1);

INSERT INTO `withdrawal_requests` (`id`, `user_id`, `amount`, `bank_account_info`, `status`, `admin_proof_url`, `created_at`, `processed_at`) VALUES
    (1, 2, 120000.0000, '{"bank":"VCB","account":"0123456789","name":"Tran Van Seller"}', 'Completed', 'https://cdn.mmo.local/withdrawals/receipt-1.png', '2024-01-26 09:00:00', '2024-01-26 09:45:00'),
    (2, 2, 80000.0000, '{"bank":"ACB","account":"9876543210","name":"Tran Van Seller"}', 'Rejected', NULL, '2024-02-03 09:00:00', '2024-02-03 10:00:00');

INSERT INTO `withdrawal_request_reasons_map` (`request_id`, `reason_id`) VALUES
    (2, 2);

-- Support operations
INSERT INTO `disputes` (`id`, `order_id`, `reporter_id`, `resolved_by_admin_id`, `reason`, `status`, `created_at`, `updated_at`) VALUES
    (1, 5002, 3, NULL, 'Tài khoản không đăng nhập được', 'Open', '2024-01-27 08:10:00', '2024-01-27 08:10:00');

INSERT INTO `conversations` (`id`, `related_order_id`, `related_product_id`, `created_at`) VALUES
    (1, 5002, 1002, '2024-01-27 08:00:00');

INSERT INTO `conversation_participants` (`conversation_id`, `user_id`) VALUES
    (1, 2),
    (1, 3);

INSERT INTO `messages` (`id`, `conversation_id`, `sender_id`, `content`, `created_at`) VALUES
    (1, 1, 3, 'Shop ơi, tài khoản Spotify không hoạt động.', '2024-01-27 08:05:00'),
    (2, 1, 2, 'Bên mình đang kiểm tra lại thông tin giúp bạn.', '2024-01-27 08:07:00');

-- Operational configuration
INSERT INTO `inventory_logs` (`id`, `product_id`, `related_order_id`, `change_amount`, `reason`, `created_at`) VALUES
    (1, 1001, 5001, -1, 'Sale', '2024-01-20 12:00:00'),
    (2, 1002, 5002, -1, 'Sale', '2024-01-26 09:05:00'),
    (3, 1002, NULL, 5, 'ManualRestock', '2024-01-29 10:00:00');

INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `description`, `created_at`, `updated_at`) VALUES
    (1, 'escrow.release.hours', '72', 'Thời gian giữ tiền trước khi tự động giải ngân', '2024-01-10 09:00:00', '2024-01-10 09:00:00'),
    (2, 'support.email', 'support@mmo.local', 'Email bộ phận hỗ trợ khách hàng', '2024-01-10 09:00:00', '2024-01-10 09:00:00');

-- =================================================================
-- Section 8: Indexes for Performance Optimization
-- =================================================================

CREATE INDEX `idx_users_email` ON `users`(`email`);
CREATE INDEX `idx_shops_owner_id` ON `shops`(`owner_id`);
CREATE INDEX `idx_products_shop_id` ON `products`(`shop_id`);
CREATE INDEX `idx_orders_buyer_id` ON `orders`(`buyer_id`);
CREATE INDEX `idx_orders_status` ON `orders`(`status`);
CREATE INDEX `idx_wallet_transactions_wallet_id` ON `wallet_transactions`(`wallet_id`);
CREATE INDEX `idx_wallet_transactions_type` ON `wallet_transactions`(`transaction_type`);
CREATE INDEX `idx_deposit_requests_status` ON `deposit_requests`(`status`);
CREATE INDEX `idx_withdrawal_requests_status` ON `withdrawal_requests`(`status`);
CREATE INDEX `idx_messages_conversation_id` ON `messages`(`conversation_id`);

SET FOREIGN_KEY_CHECKS = 1;