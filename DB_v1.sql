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
-- Section 7: Indexes for Performance Optimization
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