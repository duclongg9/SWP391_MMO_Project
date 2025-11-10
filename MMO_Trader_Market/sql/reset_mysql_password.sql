-- ====================================================================================
-- Script để reset password MySQL cho user root
-- Chạy script này với quyền admin MySQL
-- ====================================================================================

-- Reset password cho root user
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;

-- Kiểm tra user đã được cập nhật
SELECT User, Host FROM mysql.user WHERE User = 'root';

-- Test kết nối (chạy lệnh này trong terminal):
-- mysql -u root -p123456 -e "SELECT 'Connection successful!' AS status;"

