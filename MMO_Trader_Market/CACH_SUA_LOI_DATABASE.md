# 🔧 HƯỚNG DẪN SỬA LỖI DATABASE - THÊM 'OTHER' VÀO ENUM

## Vấn đề
Lỗi: `Data truncated for column 'product_type' at row 1`

Nguyên nhân: Database chưa có giá trị 'OTHER' trong ENUM của cột `product_type`

## ✅ GIẢI PHÁP - 3 CÁCH

---

### **CÁCH 1: Dùng MySQL Workbench (KHUYẾN NGHỊ)**

1. **Mở MySQL Workbench**
2. **Kết nối đến database `mmo_schema`**
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: 123456

3. **Chạy câu lệnh SQL sau:**

```sql
-- Cập nhật product_type
ALTER TABLE products 
MODIFY COLUMN product_type ENUM('EMAIL', 'SOCIAL', 'GAME', 'SOFTWARE', 'OTHER') NOT NULL;

-- Kiểm tra kết quả
SHOW COLUMNS FROM products WHERE Field='product_type';
```

4. **Kiểm tra output** - Bạn sẽ thấy:
```
Type: enum('EMAIL','SOCIAL','GAME','SOFTWARE','OTHER')
```

---

### **CÁCH 2: Dùng Command Line**

1. **Mở Command Prompt (CMD)**

2. **Tìm đường dẫn MySQL:**
   - Thường ở: `C:\Program Files\MySQL\MySQL Server 8.0\bin\`
   - Hoặc: `C:\xampp\mysql\bin\`

3. **Chạy lệnh:**

```cmd
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql -uroot -p123456 mmo_schema
```

4. **Trong MySQL prompt, chạy:**

```sql
ALTER TABLE products 
MODIFY COLUMN product_type ENUM('EMAIL', 'SOCIAL', 'GAME', 'SOFTWARE', 'OTHER') NOT NULL;

SHOW COLUMNS FROM products WHERE Field='product_type';
exit;
```

---

### **CÁCH 3: Dùng Servlet Migration (từ trình duyệt)**

1. **Build lại project** (F11 trong NetBeans)

2. **Khởi động server** (F6)

3. **Truy cập URL:**
```
http://localhost:8080/MMO_Trader_Market/admin/migrate
```

4. **Chờ servlet chạy và hiển thị kết quả**

5. **SAU ĐÓ XÓA file** `src/java/admin/DatabaseMigrationServlet.java`

---

## 🧪 KIỂM TRA SAU KHI SỬA

1. **Khởi động lại server**

2. **Vào trang tạo sản phẩm:**
   - http://localhost:8080/MMO_Trader_Market/seller/products/create

3. **Thử tạo sản phẩm với:**
   - Loại sản phẩm: **Khác**
   - Phân loại chi tiết: **Khác**

4. **Nếu thành công** → Thấy message "Đã đăng sản phẩm thành công!"

---

## ❓ NẾU VẪN LỖI

Kiểm tra console log và gửi lại error message đầy đủ.

---

## 📝 LƯU Ý

- Sau khi chạy migration, **KHÔNG CẦN** build lại project
- Chỉ cần **restart server** (hoặc hot reload)
- Nếu dùng CÁCH 3, nhớ **XÓA servlet** sau khi xong

---

**Chúc bạn thành công!** 🚀

