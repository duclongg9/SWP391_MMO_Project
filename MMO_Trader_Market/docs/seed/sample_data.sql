-- Seed data for MMO Trader Market demo environment
-- Roles
INSERT INTO roles (id, name) VALUES
    (1, 'ADMIN'),
    (2, 'SELLER'),
    (3, 'BUYER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Users
INSERT INTO users (id, role_id, email, name, hashed_password, status, created_at, updated_at)
VALUES
    (101, 2, 'seller@example.com', 'Demo Seller', '$2a$10$e0NRuT7xvFeoDyAJhDig0eVE3DybMT0SqnX5nGooy2cuglRez2ciW', 1, NOW(), NOW()),
    (102, 3, 'buyer@example.com', 'Demo Buyer', '$2a$10$e0NRuT7xvFeoDyAJhDig0eVE3DybMT0SqnX5nGooy2cuglRez2ciW', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), hashed_password = VALUES(hashed_password), status = VALUES(status);

-- Shops
INSERT INTO shops (id, owner_id, name, description, status, created_at, updated_at)
VALUES
    (201, 101, 'Demo MMO Shop', 'Gian hàng chuyên bán tài khoản MMO uy tín.', 'Active', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), status = VALUES(status);

-- Products
INSERT INTO products (id, shop_id, name, description, price, inventory_count, status, created_at, updated_at)
VALUES
    (301, 201, 'Account Genshin AR55', 'Tài khoản AR55 sẵn sàng chơi, nhiều nhân vật 5★.', 1500000, 5, 'Available', NOW(), NOW()),
    (302, 201, 'Account Honkai: Star Rail', 'Tài khoản chuẩn meta, đã xác minh email.', 1800000, 0, 'OutOfStock', NOW(), NOW()),
    (303, 201, 'Dịch vụ cày thuê rank Valorant', 'Hoàn thành trong 48 giờ, hỗ trợ mọi mức rank.', 950000, 12, 'Available', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), price = VALUES(price),
    inventory_count = VALUES(inventory_count), status = VALUES(status);

-- Wallets
INSERT INTO wallets (id, user_id, balance, hold_balance, status, created_at, updated_at)
VALUES
    (401, 102, 2500000, 0, 1, NOW(), NOW()),
    (402, 101, 5000000, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE balance = VALUES(balance), hold_balance = VALUES(hold_balance), status = VALUES(status);

-- Orders
INSERT INTO orders (id, buyer_id, product_id, total_amount, status, idempotency_key, payment_transaction_id, created_at, updated_at)
VALUES
    (501, 102, 301, 1500000, 'COMPLETED', 'seed-order-token', NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE status = VALUES(status), total_amount = VALUES(total_amount);

-- Order items
INSERT INTO order_items (id, order_id, product_id, quantity, created_at)
VALUES
    (601, 501, 301, 1, NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- Product credentials mapped to order
INSERT INTO product_credentials (id, product_id, encrypted_value, is_sold, order_id, created_at, updated_at)
VALUES
    (701, 301, 'GENSHIN-ACCESS-KEY-123', 1, 501, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE encrypted_value = VALUES(encrypted_value), is_sold = VALUES(is_sold), order_id = VALUES(order_id);

-- Wallet transactions
INSERT INTO wallet_transactions (id, wallet_id, related_entity_id, transaction_type, amount, balance_before, balance_after, note, created_at)
VALUES
    (801, 401, 501, 'CAPTURE', 1500000, 4000000, 2500000, 'Thanh toán đơn #501', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE amount = VALUES(amount), balance_after = VALUES(balance_after);

-- Remember-me tokens table remains empty to avoid leaking secrets.
