-- ====================================================================================
-- Thêm 1 Seller với 2 Shop và Sản phẩm với Hàng tồn kho
-- Shop 1: Chuyên Email & MXH
-- Shop 2: Chuyên Software & Game
-- LƯU Ý: encrypted_value phải là JSON format: {"username":"...","password":"..."}
-- ====================================================================================

-- Thêm 1 seller (user)
INSERT INTO `users` (`id`,`role_id`,`email`,`name`,`avatar_url`,`hashed_password`,`google_id`,`status`,`created_at`,`updated_at`) VALUES
 (2,2,'seller@mmo.local','Người bán MMO','https://cdn.mmo.local/avatar/seller.png','uiDM8xbTll23hMnK00khkszc0xk=',NULL,1,'2024-01-12 08:00:00','2024-01-12 08:00:00');

-- ====================================================================================
-- SHOP 1: Chuyên Email & MXH
-- ====================================================================================
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
-- LƯU Ý: encrypted_value phải là JSON format: {"username":"...","password":"..."}
-- Email và Facebook phải có định dạng email chuẩn
-- ====================================================================================

-- Credentials cho Gmail (1001) - 35 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1001,NULL,'{"username":"gmail.account.001@gmail.com","password":"Password123!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.002@gmail.com","password":"SecurePass456!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.003@gmail.com","password":"MyPass789!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.004@gmail.com","password":"StrongPass2024!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.005@gmail.com","password":"SafePass321!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.006@gmail.com","password":"NewPass654!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.007@gmail.com","password":"GoodPass987!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.008@gmail.com","password":"BestPass147!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.009@gmail.com","password":"TopPass258!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.010@gmail.com","password":"SuperPass369!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.011@gmail.com","password":"MegaPass741!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.012@gmail.com","password":"UltraPass852!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.013@gmail.com","password":"PowerPass963!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.014@gmail.com","password":"MaxPass159!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.015@gmail.com","password":"ProPass357!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.016@gmail.com","password":"ElitePass468!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.017@gmail.com","password":"PrimePass579!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.018@gmail.com","password":"GoldPass680!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.019@gmail.com","password":"SilverPass791!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.account.020@gmail.com","password":"BronzePass802!"}','gmail-basic-1m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.001@gmail.com","password":"PremiumPass123!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.002@gmail.com","password":"PremiumPass456!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.003@gmail.com","password":"PremiumPass789!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.004@gmail.com","password":"PremiumPass012!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.005@gmail.com","password":"PremiumPass345!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.006@gmail.com","password":"PremiumPass678!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.007@gmail.com","password":"PremiumPass901!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.008@gmail.com","password":"PremiumPass234!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.009@gmail.com","password":"PremiumPass567!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.010@gmail.com","password":"PremiumPass890!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.011@gmail.com","password":"PremiumPass123!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.012@gmail.com","password":"PremiumPass456!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.013@gmail.com","password":"PremiumPass789!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.014@gmail.com","password":"PremiumPass012!"}','gmail-premium-12m',0,NOW()),
(1001,NULL,'{"username":"gmail.premium.015@gmail.com","password":"PremiumPass345!"}','gmail-premium-12m',0,NOW());

-- Credentials cho Yahoo (1002) - 28 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1002,NULL,'{"username":"yahoo.account.001@yahoo.com","password":"YahooPass123!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.002@yahoo.com","password":"YahooPass456!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.003@yahoo.com","password":"YahooPass789!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.004@yahoo.com","password":"YahooPass012!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.005@yahoo.com","password":"YahooPass345!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.006@yahoo.com","password":"YahooPass678!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.007@yahoo.com","password":"YahooPass901!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.008@yahoo.com","password":"YahooPass234!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.009@yahoo.com","password":"YahooPass567!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.010@yahoo.com","password":"YahooPass890!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.011@yahoo.com","password":"YahooPass123!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.012@yahoo.com","password":"YahooPass456!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.013@yahoo.com","password":"YahooPass789!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.014@yahoo.com","password":"YahooPass012!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.015@yahoo.com","password":"YahooPass345!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.016@yahoo.com","password":"YahooPass678!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.017@yahoo.com","password":"YahooPass901!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.018@yahoo.com","password":"YahooPass234!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.019@yahoo.com","password":"YahooPass567!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.020@yahoo.com","password":"YahooPass890!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.021@yahoo.com","password":"YahooPass123!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.022@yahoo.com","password":"YahooPass456!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.023@yahoo.com","password":"YahooPass789!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.024@yahoo.com","password":"YahooPass012!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.025@yahoo.com","password":"YahooPass345!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.026@yahoo.com","password":"YahooPass678!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.027@yahoo.com","password":"YahooPass901!"}','yahoo-1m',0,NOW()),
(1002,NULL,'{"username":"yahoo.account.028@yahoo.com","password":"YahooPass234!"}','yahoo-1m',0,NOW());

-- Credentials cho Outlook (1003) - 32 credentials
-- LƯU Ý: variant_code phải khớp CHÍNH XÁC với variant_code trong variants_json (đã lowercase và trim)
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1003,NULL,'{"username":"outlook.account.001@outlook.com","password":"OutlookPass123!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.002@outlook.com","password":"OutlookPass456!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.003@outlook.com","password":"OutlookPass789!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.004@outlook.com","password":"OutlookPass012!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.005@outlook.com","password":"OutlookPass345!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.006@outlook.com","password":"OutlookPass678!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.007@outlook.com","password":"OutlookPass901!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.008@outlook.com","password":"OutlookPass234!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.009@outlook.com","password":"OutlookPass567!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.010@outlook.com","password":"OutlookPass890!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.011@outlook.com","password":"OutlookPass123!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.012@outlook.com","password":"OutlookPass456!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.013@outlook.com","password":"OutlookPass789!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.014@outlook.com","password":"OutlookPass012!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.015@outlook.com","password":"OutlookPass345!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.016@outlook.com","password":"OutlookPass678!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.017@outlook.com","password":"OutlookPass901!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.018@outlook.com","password":"OutlookPass234!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.019@outlook.com","password":"OutlookPass567!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.account.020@outlook.com","password":"OutlookPass890!"}','outlook-1m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.001@outlook.com","password":"BusinessPass123!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.002@outlook.com","password":"BusinessPass456!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.003@outlook.com","password":"BusinessPass789!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.004@outlook.com","password":"BusinessPass012!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.005@outlook.com","password":"BusinessPass345!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.006@outlook.com","password":"BusinessPass678!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.007@outlook.com","password":"BusinessPass901!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.008@outlook.com","password":"BusinessPass234!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.009@outlook.com","password":"BusinessPass567!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.010@outlook.com","password":"BusinessPass890!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.011@outlook.com","password":"BusinessPass123!"}','outlook-12m',0,NOW()),
(1003,NULL,'{"username":"outlook.business.012@outlook.com","password":"BusinessPass456!"}','outlook-12m',0,NOW());

-- Credentials cho Facebook (1004) - 42 credentials (email format)
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1004,NULL,'{"username":"fb.account.001@gmail.com","password":"FbPass123!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.002@gmail.com","password":"FbPass456!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.003@gmail.com","password":"FbPass789!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.004@gmail.com","password":"FbPass012!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.005@gmail.com","password":"FbPass345!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.006@gmail.com","password":"FbPass678!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.007@gmail.com","password":"FbPass901!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.008@gmail.com","password":"FbPass234!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.009@gmail.com","password":"FbPass567!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.010@gmail.com","password":"FbPass890!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.011@gmail.com","password":"FbPass123!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.012@gmail.com","password":"FbPass456!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.013@gmail.com","password":"FbPass789!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.014@gmail.com","password":"FbPass012!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.015@gmail.com","password":"FbPass345!"}','fb-2009',0,NOW()),
(1004,NULL,'{"username":"fb.account.016@yahoo.com","password":"FbPass678!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.017@yahoo.com","password":"FbPass901!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.018@yahoo.com","password":"FbPass234!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.019@yahoo.com","password":"FbPass567!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.020@yahoo.com","password":"FbPass890!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.021@yahoo.com","password":"FbPass123!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.022@yahoo.com","password":"FbPass456!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.023@yahoo.com","password":"FbPass789!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.024@yahoo.com","password":"FbPass012!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.025@yahoo.com","password":"FbPass345!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.026@yahoo.com","password":"FbPass678!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.027@yahoo.com","password":"FbPass901!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.028@yahoo.com","password":"FbPass234!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.029@yahoo.com","password":"FbPass567!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.030@yahoo.com","password":"FbPass890!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.031@outlook.com","password":"FbPass123!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.032@outlook.com","password":"FbPass456!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.033@outlook.com","password":"FbPass789!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.034@outlook.com","password":"FbPass012!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.035@outlook.com","password":"FbPass345!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.036@outlook.com","password":"FbPass678!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.037@outlook.com","password":"FbPass901!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.038@outlook.com","password":"FbPass234!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.039@outlook.com","password":"FbPass567!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.040@outlook.com","password":"FbPass890!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.041@outlook.com","password":"FbPass123!"}','fb-2012',0,NOW()),
(1004,NULL,'{"username":"fb.account.042@outlook.com","password":"FbPass456!"}','fb-2012',0,NOW());

-- Credentials cho TikTok (1005) - 38 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1005,NULL,'{"username":"tiktok.account.001@gmail.com","password":"TikTokPass123!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.002@gmail.com","password":"TikTokPass456!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.003@gmail.com","password":"TikTokPass789!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.004@gmail.com","password":"TikTokPass012!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.005@gmail.com","password":"TikTokPass345!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.006@gmail.com","password":"TikTokPass678!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.007@gmail.com","password":"TikTokPass901!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.008@gmail.com","password":"TikTokPass234!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.009@gmail.com","password":"TikTokPass567!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.010@gmail.com","password":"TikTokPass890!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.011@gmail.com","password":"TikTokPass123!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.012@gmail.com","password":"TikTokPass456!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.013@gmail.com","password":"TikTokPass789!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.014@gmail.com","password":"TikTokPass012!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.015@gmail.com","password":"TikTokPass345!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.016@gmail.com","password":"TikTokPass678!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.017@gmail.com","password":"TikTokPass901!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.018@gmail.com","password":"TikTokPass234!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.019@gmail.com","password":"TikTokPass567!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.020@gmail.com","password":"TikTokPass890!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.021@gmail.com","password":"TikTokPass123!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.022@gmail.com","password":"TikTokPass456!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.023@gmail.com","password":"TikTokPass789!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.024@gmail.com","password":"TikTokPass012!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.025@gmail.com","password":"TikTokPass345!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.026@gmail.com","password":"TikTokPass678!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.027@gmail.com","password":"TikTokPass901!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.028@gmail.com","password":"TikTokPass234!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.029@gmail.com","password":"TikTokPass567!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.030@gmail.com","password":"TikTokPass890!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.031@gmail.com","password":"TikTokPass123!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.032@gmail.com","password":"TikTokPass456!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.033@gmail.com","password":"TikTokPass789!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.034@gmail.com","password":"TikTokPass012!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.035@gmail.com","password":"TikTokPass345!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.036@gmail.com","password":"TikTokPass678!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.037@gmail.com","password":"TikTokPass901!"}','tt-pro',0,NOW()),
(1005,NULL,'{"username":"tiktok.account.038@gmail.com","password":"TikTokPass234!"}','tt-pro',0,NOW());

-- Credentials cho X/Twitter (1006) - 25 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(1006,NULL,'{"username":"x.account.001@gmail.com","password":"XPass123!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.002@gmail.com","password":"XPass456!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.003@gmail.com","password":"XPass789!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.004@gmail.com","password":"XPass012!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.005@gmail.com","password":"XPass345!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.006@gmail.com","password":"XPass678!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.007@gmail.com","password":"XPass901!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.008@gmail.com","password":"XPass234!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.009@gmail.com","password":"XPass567!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.010@gmail.com","password":"XPass890!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.011@gmail.com","password":"XPass123!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.012@gmail.com","password":"XPass456!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.013@gmail.com","password":"XPass789!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.014@gmail.com","password":"XPass012!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.015@gmail.com","password":"XPass345!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.016@gmail.com","password":"XPass678!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.017@gmail.com","password":"XPass901!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.018@gmail.com","password":"XPass234!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.019@gmail.com","password":"XPass567!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.020@gmail.com","password":"XPass890!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.021@gmail.com","password":"XPass123!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.022@gmail.com","password":"XPass456!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.023@gmail.com","password":"XPass789!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.024@gmail.com","password":"XPass012!"}','x-verified',0,NOW()),
(1006,NULL,'{"username":"x.account.025@gmail.com","password":"XPass345!"}','x-verified',0,NOW());

-- Credentials cho Canva (2001) - 45 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2001,NULL,'{"username":"canva.account.001@canva.com","password":"CanvaPass123!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.002@canva.com","password":"CanvaPass456!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.003@canva.com","password":"CanvaPass789!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.004@canva.com","password":"CanvaPass012!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.005@canva.com","password":"CanvaPass345!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.006@canva.com","password":"CanvaPass678!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.007@canva.com","password":"CanvaPass901!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.008@canva.com","password":"CanvaPass234!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.009@canva.com","password":"CanvaPass567!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.010@canva.com","password":"CanvaPass890!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.011@canva.com","password":"CanvaPass123!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.012@canva.com","password":"CanvaPass456!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.013@canva.com","password":"CanvaPass789!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.014@canva.com","password":"CanvaPass012!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.015@canva.com","password":"CanvaPass345!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.016@canva.com","password":"CanvaPass678!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.017@canva.com","password":"CanvaPass901!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.018@canva.com","password":"CanvaPass234!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.019@canva.com","password":"CanvaPass567!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.020@canva.com","password":"CanvaPass890!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.021@canva.com","password":"CanvaPass123!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.022@canva.com","password":"CanvaPass456!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.023@canva.com","password":"CanvaPass789!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.024@canva.com","password":"CanvaPass012!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.account.025@canva.com","password":"CanvaPass345!"}','canva-1m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.001@canva.com","password":"PremiumPass123!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.002@canva.com","password":"PremiumPass456!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.003@canva.com","password":"PremiumPass789!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.004@canva.com","password":"PremiumPass012!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.005@canva.com","password":"PremiumPass345!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.006@canva.com","password":"PremiumPass678!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.007@canva.com","password":"PremiumPass901!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.008@canva.com","password":"PremiumPass234!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.009@canva.com","password":"PremiumPass567!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.010@canva.com","password":"PremiumPass890!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.011@canva.com","password":"PremiumPass123!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.012@canva.com","password":"PremiumPass456!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.013@canva.com","password":"PremiumPass789!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.014@canva.com","password":"PremiumPass012!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.015@canva.com","password":"PremiumPass345!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.016@canva.com","password":"PremiumPass678!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.017@canva.com","password":"PremiumPass901!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.018@canva.com","password":"PremiumPass234!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.019@canva.com","password":"PremiumPass567!"}','canva-12m',0,NOW()),
(2001,NULL,'{"username":"canva.premium.020@canva.com","password":"PremiumPass890!"}','canva-12m',0,NOW());

-- Credentials cho Office (2002) - 18 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2002,NULL,'{"username":"office.account.001@outlook.com","password":"OfficePass123!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.002@outlook.com","password":"OfficePass456!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.003@outlook.com","password":"OfficePass789!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.004@outlook.com","password":"OfficePass012!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.005@outlook.com","password":"OfficePass345!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.006@outlook.com","password":"OfficePass678!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.007@outlook.com","password":"OfficePass901!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.008@outlook.com","password":"OfficePass234!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.009@outlook.com","password":"OfficePass567!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.010@outlook.com","password":"OfficePass890!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.011@outlook.com","password":"OfficePass123!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.012@outlook.com","password":"OfficePass456!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.013@outlook.com","password":"OfficePass789!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.014@outlook.com","password":"OfficePass012!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.015@outlook.com","password":"OfficePass345!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.016@outlook.com","password":"OfficePass678!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.017@outlook.com","password":"OfficePass901!"}','o365family-12m',0,NOW()),
(2002,NULL,'{"username":"office.account.018@outlook.com","password":"OfficePass234!"}','o365family-12m',0,NOW());

-- Credentials cho Windows (2003) - 30 credentials (key format - dùng username để lưu key)
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2003,NULL,'{"username":"XXXXX-XXXXX-XXXXX-XXXXX-XXXXX","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"YYYYY-YYYYY-YYYYY-YYYYY-YYYYY","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"AAAAA-AAAAA-AAAAA-AAAAA-AAAAA","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"BBBBB-BBBBB-BBBBB-BBBBB-BBBBB","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"CCCCC-CCCCC-CCCCC-CCCCC-CCCCC","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"DDDDD-DDDDD-DDDDD-DDDDD-DDDDD","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"EEEEE-EEEEE-EEEEE-EEEEE-EEEEE","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"FFFFF-FFFFF-FFFFF-FFFFF-FFFFF","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"GGGGG-GGGGG-GGGGG-GGGGG-GGGGG","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"HHHHH-HHHHH-HHHHH-HHHHH-HHHHH","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"IIIII-IIIII-IIIII-IIIII-IIIII","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"JJJJJ-JJJJJ-JJJJJ-JJJJJ-JJJJJ","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"KKKKK-KKKKK-KKKKK-KKKKK-KKKKK","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"LLLLL-LLLLL-LLLLL-LLLLL-LLLLL","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"MMMMM-MMMMM-MMMMM-MMMMM-MMMMM","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"NNNNN-NNNNN-NNNNN-NNNNN-NNNNN","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"OOOOO-OOOOO-OOOOO-OOOOO-OOOOO","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"PPPPP-PPPPP-PPPPP-PPPPP-PPPPP","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"QQQQQ-QQQQQ-QQQQQ-QQQQQ-QQQQQ","password":"Win11ProOEM"}','win11pro-oem',0,NOW()),
(2003,NULL,'{"username":"RRRRR-RRRRR-RRRRR-RRRRR-RRRRR","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"SSSSS-SSSSS-SSSSS-SSSSS-SSSSS","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"TTTTT-TTTTT-TTTTT-TTTTT-TTTTT","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"UUUUU-UUUUU-UUUUU-UUUUU-UUUUU","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"VVVVV-VVVVV-VVVVV-VVVVV-VVVVV","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"WWWWW-WWWWW-WWWWW-WWWWW-WWWWW","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"XXXXX-XXXXX-XXXXX-XXXXX-XXXXX","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"YYYYY-YYYYY-YYYYY-YYYYY-YYYYY","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ-ZZZZZ","password":"Win11ProRetail"}','win11pro-retail',0,NOW()),
(2003,NULL,'{"username":"AAAAA-AAAAA-AAAAA-AAAAA-AAAAA","password":"Win11ProRetail"}','win11pro-retail',0,NOW());

-- Credentials cho ChatGPT (2004) - 22 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2004,NULL,'{"username":"chatgpt.account.001@gmail.com","password":"ChatGPTPass123!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.002@gmail.com","password":"ChatGPTPass456!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.003@gmail.com","password":"ChatGPTPass789!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.004@gmail.com","password":"ChatGPTPass012!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.005@gmail.com","password":"ChatGPTPass345!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.006@gmail.com","password":"ChatGPTPass678!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.007@gmail.com","password":"ChatGPTPass901!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.008@gmail.com","password":"ChatGPTPass234!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.009@gmail.com","password":"ChatGPTPass567!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.010@gmail.com","password":"ChatGPTPass890!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.011@gmail.com","password":"ChatGPTPass123!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.012@gmail.com","password":"ChatGPTPass456!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.013@gmail.com","password":"ChatGPTPass789!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.014@gmail.com","password":"ChatGPTPass012!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.account.015@gmail.com","password":"ChatGPTPass345!"}','chatgpt-1m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.001@gmail.com","password":"PremiumPass123!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.002@gmail.com","password":"PremiumPass456!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.003@gmail.com","password":"PremiumPass789!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.004@gmail.com","password":"PremiumPass012!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.005@gmail.com","password":"PremiumPass345!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.006@gmail.com","password":"PremiumPass678!"}','chatgpt-12m',0,NOW()),
(2004,NULL,'{"username":"chatgpt.premium.007@gmail.com","password":"PremiumPass901!"}','chatgpt-12m',0,NOW());

-- Credentials cho Valorant (2005) - 15 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2005,NULL,'{"username":"valorant.account.001@gmail.com","password":"ValoPass123!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.002@gmail.com","password":"ValoPass456!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.003@gmail.com","password":"ValoPass789!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.004@gmail.com","password":"ValoPass012!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.005@gmail.com","password":"ValoPass345!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.006@gmail.com","password":"ValoPass678!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.007@gmail.com","password":"ValoPass901!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.008@gmail.com","password":"ValoPass234!"}','valo-silver',0,NOW()),
(2005,NULL,'{"username":"valorant.account.009@gmail.com","password":"ValoPass567!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.010@gmail.com","password":"ValoPass890!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.011@gmail.com","password":"ValoPass123!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.012@gmail.com","password":"ValoPass456!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.013@gmail.com","password":"ValoPass789!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.014@gmail.com","password":"ValoPass012!"}','valo-gold',0,NOW()),
(2005,NULL,'{"username":"valorant.account.015@gmail.com","password":"ValoPass345!"}','valo-gold',0,NOW());

-- Credentials cho League of Legends (2006) - 12 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2006,NULL,'{"username":"lol.account.001@gmail.com","password":"LoLPass123!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.002@gmail.com","password":"LoLPass456!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.003@gmail.com","password":"LoLPass789!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.004@gmail.com","password":"LoLPass012!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.005@gmail.com","password":"LoLPass345!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.006@gmail.com","password":"LoLPass678!"}','lol-gold',0,NOW()),
(2006,NULL,'{"username":"lol.account.007@gmail.com","password":"LoLPass901!"}','lol-plat',0,NOW()),
(2006,NULL,'{"username":"lol.account.008@gmail.com","password":"LoLPass234!"}','lol-plat',0,NOW()),
(2006,NULL,'{"username":"lol.account.009@gmail.com","password":"LoLPass567!"}','lol-plat',0,NOW()),
(2006,NULL,'{"username":"lol.account.010@gmail.com","password":"LoLPass890!"}','lol-plat',0,NOW()),
(2006,NULL,'{"username":"lol.account.011@gmail.com","password":"LoLPass123!"}','lol-plat',0,NOW()),
(2006,NULL,'{"username":"lol.account.012@gmail.com","password":"LoLPass456!"}','lol-plat',0,NOW());

-- Credentials cho CS2 (2007) - 20 credentials
INSERT INTO `product_credentials` (`product_id`,`order_id`,`encrypted_value`,`variant_code`,`is_sold`,`created_at`) VALUES
(2007,NULL,'{"username":"cs2.account.001@gmail.com","password":"CS2Pass123!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.002@gmail.com","password":"CS2Pass456!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.003@gmail.com","password":"CS2Pass789!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.004@gmail.com","password":"CS2Pass012!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.005@gmail.com","password":"CS2Pass345!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.006@gmail.com","password":"CS2Pass678!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.007@gmail.com","password":"CS2Pass901!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.008@gmail.com","password":"CS2Pass234!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.009@gmail.com","password":"CS2Pass567!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.010@gmail.com","password":"CS2Pass890!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.011@gmail.com","password":"CS2Pass123!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.012@gmail.com","password":"CS2Pass456!"}','cs2-prime',0,NOW()),
(2007,NULL,'{"username":"cs2.account.013@gmail.com","password":"CS2Pass789!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.014@gmail.com","password":"CS2Pass012!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.015@gmail.com","password":"CS2Pass345!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.016@gmail.com","password":"CS2Pass678!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.017@gmail.com","password":"CS2Pass901!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.018@gmail.com","password":"CS2Pass234!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.019@gmail.com","password":"CS2Pass567!"}','cs2-prime-mg',0,NOW()),
(2007,NULL,'{"username":"cs2.account.020@gmail.com","password":"CS2Pass890!"}','cs2-prime-mg',0,NOW());

-- ====================================================================================
-- Tổng kết:
-- 1 Seller (user_id = 2)
-- Shop 1 (Email & MXH): 6 products, 200 credentials
--   - Gmail: 35 credentials
--   - Yahoo: 28 credentials  
--   - Outlook: 32 credentials
--   - Facebook: 42 credentials
--   - TikTok: 38 credentials
--   - X/Twitter: 25 credentials
--
-- Shop 2 (Software & Game): 7 products, 162 credentials
--   - Canva: 45 credentials
--   - Office: 18 credentials
--   - Windows: 30 credentials
--   - ChatGPT: 22 credentials
--   - Valorant: 15 credentials
--   - LoL: 12 credentials
--   - CS2: 20 credentials
--
-- TẤT CẢ credentials đã được lưu theo format JSON: {"username":"...","password":"..."}
-- ====================================================================================

-- ====================================================================================
-- CHUẨN HÓA VARIANT_CODE: Đảm bảo variant_code khớp với variant_code trong variants_json
-- Script này sẽ normalize variant_code về lowercase và trim để khớp với query logic
-- ====================================================================================

-- Chuẩn hóa variant_code cho TẤT CẢ credentials
-- Đảm bảo variant_code được trim và lowercase để khớp với query: LOWER(TRIM(variant_code))
-- LƯU Ý: Sử dụng JOIN với products để tránh lỗi safe update mode
UPDATE product_credentials pc
INNER JOIN products p ON pc.product_id = p.id
SET pc.variant_code = LOWER(TRIM(pc.variant_code))
WHERE pc.variant_code IS NOT NULL
  AND TRIM(pc.variant_code) != ''
  AND pc.variant_code != LOWER(TRIM(pc.variant_code));

-- Fix encrypted_value cho TẤT CẢ credentials có format sai
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

-- Kiểm tra kết quả: Xem variant_code đã được normalize chưa
-- SELECT product_id, variant_code, COUNT(*) as count
-- FROM product_credentials
-- WHERE product_id IN (1001, 1002, 1003, 1004, 1005, 1006)
-- GROUP BY product_id, variant_code
-- ORDER BY product_id, variant_code;

