-- Tạo CSDL pikachu (utf8mb4) và tài khoản riêng cho server, chỉ có quyền trên CSDL này.
-- Chạy một lần bằng tài khoản quản trị MySQL:
--   mysql -u root -p < server/db/create-db.sql
-- Mật khẩu của tài khoản pikachu dưới đây dùng cho máy chạy thử; đổi thì sửa cả db.password trong server.properties.
-- Xem docs/pha4-cai-dat.md, mục 1d. Phụ trách: Dev 2.

CREATE DATABASE IF NOT EXISTS pikachu CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'pikachu'@'localhost' IDENTIFIED BY 'pikachu';
GRANT ALL PRIVILEGES ON pikachu.* TO 'pikachu'@'localhost';
