# BÀI THUYẾT TRÌNH: QUẢN LÝ KHO - THÊM SẢN PHẨM VÀ HÀNG TỒN KHO

## 📋 MỤC LỤC
1. [Tổng quan hệ thống quản lý kho](#1-tổng-quan-hệ-thống-quản-lý-kho)
2. [Chức năng thêm sản phẩm mới](#2-chức-năng-thêm-sản-phẩm-mới)
3. [Chức năng thêm hàng tồn kho](#3-chức-năng-thêm-hàng-tồn-kho)
4. [Luồng xử lý dữ liệu](#4-luồng-xử-lý-dữ-liệu)
5. [Kiến trúc và công nghệ](#5-kiến-trúc-và-công-nghệ)
6. [Demo và ví dụ](#6-demo-và-ví-dụ)

---

## 1. TỔNG QUAN HỆ THỐNG QUẢN LÝ KHO

### 1.1. Mục đích
- **Quản lý sản phẩm**: Cho phép seller tạo và quản lý sản phẩm của shop
- **Quản lý tồn kho**: Theo dõi số lượng hàng tồn kho theo từng biến thể sản phẩm
- **Quản lý credentials**: Thêm và quản lý tài khoản/key cho từng sản phẩm

### 1.2. Các chức năng chính
```
┌─────────────────────────────────────┐
│   QUẢN LÝ KHO                        │
├─────────────────────────────────────┤
│ 1. Xem danh sách sản phẩm           │
│ 2. Tạo sản phẩm mới                  │
│ 3. Xem chi tiết tồn kho              │
│ 4. Thêm hàng tồn kho (credentials)   │
│ 5. Sửa/Xóa sản phẩm                  │
└─────────────────────────────────────┘
```

### 1.3. Đối tượng sử dụng
- **Seller**: Người bán hàng, quản lý shop của mình
- **Yêu cầu**: Phải có shop đã được kích hoạt (status = "Active")

---

## 2. CHỨC NĂNG THÊM SẢN PHẨM MỚI

### 2.1. Quy trình tạo sản phẩm

```
┌──────────────┐
│ Seller truy  │
│ cập form     │
│ tạo sản phẩm │
└──────┬───────┘
       │
       ▼
┌─────────────────────┐
│ Điền thông tin      │
│ - Tên sản phẩm      │
│ - Loại/Phân loại    │
│ - Mô tả             │
│ - Biến thể + Ảnh    │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Submit form         │
│ (Multipart/Form)    │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Server xử lý        │
│ - Validate dữ liệu   │
│ - Upload ảnh        │
│ - Lưu vào DB        │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Redirect về         │
│ trang quản lý kho   │
└─────────────────────┘
```

### 2.2. Dữ liệu cần nhập

#### A. Thông tin cơ bản
- **Tên sản phẩm** (bắt buộc)
- **Loại sản phẩm** (bắt buộc)
  - EMAIL, SOCIAL, GAME, SOFTWARE, OTHER
- **Phân loại chi tiết** (bắt buộc)
  - Ví dụ: GMAIL, FACEBOOK, VALORANT, CANVA...
- **Mô tả ngắn** (tùy chọn)
- **Mô tả chi tiết** (tùy chọn)

#### B. Biến thể sản phẩm (Variants)
**Mỗi biến thể bao gồm:**
- **Tên biến thể**: Ví dụ "Canva 1 tháng", "Canva 12 tháng"
- **Giá**: Giá bán của biến thể
- **Ảnh**: 1-3 ảnh cho mỗi biến thể
- **Mã biến thể**: Tự động tạo từ tên (ví dụ: "canva_1_thang_0")

**Ví dụ biến thể:**
```
Biến thể 1:
- Tên: "Canva 1 tháng"
- Giá: 90,000 ₫
- Ảnh: [image1.jpg, image2.jpg]
- Mã: "canva_1_thang_0"

Biến thể 2:
- Tên: "Canva 12 tháng"
- Giá: 750,000 ₫
- Ảnh: [image3.jpg, image4.jpg, image5.jpg]
- Mã: "canva_12_thang_1"
```

### 2.3. Dữ liệu gửi lên server

**Form Parameters:**
```
- shopId: 1
- productName: "Tài khoản Canva Premium"
- productType: "SOFTWARE"
- productSubtype: "CANVA"
- shortDescription: "Tài khoản Canva Premium..."
- description: "Chi tiết sản phẩm..."
- price: "0" (giá thực tế lấy từ variants)
```

**Variants JSON:**
```json
[
  {
    "variant_code": "canva_1_thang_0",
    "name": "Canva 1 tháng",
    "price": 90000,
    "inventory_count": 0,
    "status": "Available"
  },
  {
    "variant_code": "canva_12_thang_1",
    "name": "Canva 12 tháng",
    "price": 750000,
    "inventory_count": 0,
    "status": "Available"
  }
]
```

**File ảnh:**
```
- variantImages_0_0: [File: image1.jpg]
- variantImages_0_1: [File: image2.jpg]
- variantImages_1_0: [File: image3.jpg]
- variantImages_1_1: [File: image4.jpg]
- variantImages_1_2: [File: image5.jpg]
```

**Variant Indices:**
```
variantIndices: "0:2,1:3"
(Nghĩa là: variant 0 có 2 ảnh, variant 1 có 3 ảnh)
```

### 2.4. Xử lý trên server

**Bước 1: Validate dữ liệu**
- Kiểm tra shop hợp lệ
- Kiểm tra tên sản phẩm không rỗng
- Kiểm tra mỗi variant có ít nhất 1 ảnh
- Kiểm tra giá hợp lệ

**Bước 2: Upload ảnh**
- Lưu từng ảnh vào thư mục `assets/images/products/`
- Tạo tên file unique bằng UUID
- Trả về đường dẫn relative

**Bước 3: Xử lý variants**
- Parse JSON variants
- Gắn đường dẫn ảnh vào từng variant
- Tính giá thấp nhất từ các variants

**Bước 4: Lưu vào database**
```sql
INSERT INTO products (
    shop_id, product_type, product_subtype, name,
    short_description, description, price,
    primary_image_url, gallery_json,
    inventory_count, sold_count, status,
    variant_schema, variants_json,
    created_at, updated_at
) VALUES (...)
```

**Kết quả:**
- Sản phẩm được tạo với `inventory_count = 0`
- Status = "Available" (hiển thị ngay trên shop)
- Redirect về trang quản lý kho

---

## 3. CHỨC NĂNG THÊM HÀNG TỒN KHO

### 3.1. Khái niệm

**Hàng tồn kho = Credentials (Tài khoản/Key)**
- Mỗi sản phẩm cần có credentials để bán
- Credentials được lưu dưới dạng mã hóa trong database
- Mỗi credential có thể gắn với một biến thể cụ thể

### 3.2. Quy trình thêm hàng tồn kho

```
┌──────────────┐
│ Seller chọn  │
│ sản phẩm     │
│ từ danh sách│
└──────┬───────┘
       │
       ▼
┌─────────────────────┐
│ Click "Thêm hàng"   │
│ hoặc "Thêm sản phẩm"│
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Hiển thị form       │
│ - Chọn biến thể     │
│   (nếu có)          │
│ - Nhập username     │
│ - Nhập password     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Submit form         │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Server xử lý        │
│ - Mã hóa password   │
│ - Lưu credential    │
│ - Tăng inventory    │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Cập nhật tồn kho    │
│ - inventory_count++ │
│ - variant inventory│
└─────────────────────┘
```

### 3.3. Form thêm hàng tồn kho

**Các trường nhập liệu:**

1. **Sản phẩm** (tự động chọn từ danh sách)
2. **Mã biến thể** (nếu sản phẩm có variants)
   - Dropdown select hiển thị các biến thể
   - Format: "Tên biến thể - Giá"
   - Ví dụ: "Canva 1 tháng - 90,000 ₫"
3. **Username** (bắt buộc)
   - Tên đăng nhập của tài khoản
4. **Password** (bắt buộc)
   - Mật khẩu của tài khoản
   - Sẽ được mã hóa trước khi lưu

### 3.4. Xử lý trên server

**Bước 1: Parse variants từ JSON**
```java
List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
    product.getVariantSchema(), 
    product.getVariantsJson()
);
```

**Bước 2: Validate variant code**
- Kiểm tra variant code có hợp lệ không
- Nếu không có variant, để null

**Bước 3: Mã hóa password**
- Sử dụng thuật toán mã hóa (BCrypt)
- Lưu password đã mã hóa vào database

**Bước 4: Lưu credential**
```sql
INSERT INTO product_credentials (
    product_id, 
    encrypted_value,  -- Username + Password đã mã hóa
    variant_code,     -- Mã biến thể (có thể NULL)
    is_sold           -- Mặc định = 0 (chưa bán)
) VALUES (...)
```

**Bước 5: Cập nhật tồn kho**
- Tăng `inventory_count` của sản phẩm
- Tăng `inventory_count` của variant (nếu có)
- Cập nhật `variants_json` với số lượng mới

---

## 4. LUỒNG XỬ LÝ DỮ LIỆU

### 4.1. Kiến trúc tổng quan

```
┌─────────────┐
│   FRONTEND  │
│   (JSP)     │
└──────┬──────┘
       │ HTTP Request
       ▼
┌─────────────┐
│ CONTROLLER  │
│ (Servlet)   │
└──────┬──────┘
       │
       ├──► Validate
       ├──► Parse JSON
       ├──► Upload Files
       │
       ▼
┌─────────────┐
│    DAO      │
│ (Database)  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  DATABASE   │
│  (MySQL)    │
└─────────────┘
```

### 4.2. Luồng dữ liệu khi tạo sản phẩm

```
1. User điền form → Submit
   │
   ├─► Form Data (text fields)
   ├─► variantsJson (JSON string)
   ├─► variantIndices (String)
   └─► Files (multipart)
   
2. Controller nhận request
   │
   ├─► Parse variantsJson → List<Map>
   ├─► Parse variantIndices → Map<index, imageCount>
   ├─► Upload files → Save to server
   └─► Combine metadata + image URLs
   
3. Tạo Products object
   │
   ├─► Set basic info
   ├─► Set variants_json (with image URLs)
   ├─► Set gallery_json (all images)
   └─► Set variant_schema = "custom"
   
4. DAO.insert()
   │
   └─► INSERT INTO products
   
5. Redirect → Inventory page
```

### 4.3. Luồng dữ liệu khi thêm hàng tồn kho

```
1. User chọn sản phẩm → Click "Thêm hàng"
   │
   └─► GET /seller/inventory/add?productId=X
   
2. Controller load sản phẩm
   │
   ├─► Get product from DB
   ├─► Parse variants_json → List<ProductVariantOption>
   └─► Set variants to request
   
3. JSP render form
   │
   ├─► Dropdown variants (nếu có)
   └─► Input username/password
   
4. User submit form
   │
   ├─► productId
   ├─► variantCode (có thể null)
   ├─► username
   └─► password
   
5. Controller xử lý
   │
   ├─► Encrypt password
   ├─► INSERT credential
   └─► UPDATE inventory_count
   
6. Redirect → View inventory
```

### 4.4. Cấu trúc dữ liệu trong Database

**Bảng `products`:**
```sql
- id (PK)
- shop_id
- product_type
- product_subtype
- name
- short_description
- description
- price
- primary_image_url
- gallery_json (TEXT)        ← JSON array of image URLs
- inventory_count
- sold_count
- status
- variant_schema (VARCHAR)  ← "custom" hoặc "none"
- variants_json (TEXT)      ← JSON array of variants
- created_at
- updated_at
```

**Bảng `product_credentials`:**
```sql
- id (PK)
- product_id (FK)
- encrypted_value (TEXT)     ← Username + Password đã mã hóa
- variant_code (VARCHAR)      ← Mã biến thể (có thể NULL)
- is_sold (BOOLEAN)          ← 0 = chưa bán, 1 = đã bán
- created_at
```

---

## 5. KIẾN TRÚC VÀ CÔNG NGHỆ

### 5.1. Công nghệ sử dụng

**Backend:**
- Java Servlet (Jakarta EE)
- JDBC cho database connection
- Gson cho JSON parsing
- BCrypt cho mã hóa password

**Frontend:**
- JSP (JavaServer Pages)
- JSTL (JSP Standard Tag Library)
- JavaScript (FormData API cho file upload)
- HTML/CSS

**Database:**
- MySQL
- JSON storage trong TEXT fields

### 5.2. Các class chính

**Controller:**
- `SellerCreateProductController` - Tạo sản phẩm
- `SellerViewInventoryController` - Xem tồn kho
- `SellerAddCredentialController` - Thêm hàng tồn kho

**DAO:**
- `ProductDAO` - Thao tác với bảng products
- `CredentialDAO` - Thao tác với bảng credentials

**Model:**
- `Products` - Entity sản phẩm
- `ProductVariantOption` - Model cho variant
- `ProductVariantUtils` - Utility class parse JSON

**Service:**
- `ProductVariantUtils` - Parse và xử lý variants

### 5.3. Điểm nổi bật

**1. Lưu trữ variants dưới dạng JSON**
- Linh hoạt, không cần thay đổi schema khi thêm variant mới
- Dễ mở rộng thêm thuộc tính cho variant

**2. Upload ảnh multipart**
- Hỗ trợ upload nhiều ảnh cùng lúc
- Tự động tạo tên file unique
- Validate kích thước và định dạng

**3. Mã hóa credentials**
- Bảo mật thông tin tài khoản
- Không lưu plain text password

**4. Quản lý tồn kho theo variant**
- Mỗi variant có inventory riêng
- Tự động cập nhật khi thêm/xóa credentials

---

## 6. DEMO VÀ VÍ DỤ

### 6.1. Ví dụ tạo sản phẩm

**Input:**
```
Tên: "Tài khoản Canva Premium"
Loại: SOFTWARE
Phân loại: CANVA

Biến thể 1:
- Tên: "Canva 1 tháng"
- Giá: 90,000 ₫
- Ảnh: 2 file

Biến thể 2:
- Tên: "Canva 12 tháng"
- Giá: 750,000 ₫
- Ảnh: 3 file
```

**Output trong Database:**
```json
{
  "id": 123,
  "name": "Tài khoản Canva Premium",
  "product_type": "SOFTWARE",
  "product_subtype": "CANVA",
  "price": 90000,  // Giá thấp nhất
  "variant_schema": "custom",
  "variants_json": "[{\"variant_code\":\"canva_1_thang_0\",\"name\":\"Canva 1 tháng\",\"price\":90000,\"images\":[\"/assets/images/products/uuid1.jpg\",\"/assets/images/products/uuid2.jpg\"]},{\"variant_code\":\"canva_12_thang_1\",\"name\":\"Canva 12 tháng\",\"price\":750000,\"images\":[\"/assets/images/products/uuid3.jpg\",\"/assets/images/products/uuid4.jpg\",\"/assets/images/products/uuid5.jpg\"]}]",
  "inventory_count": 0,
  "status": "Available"
}
```

### 6.2. Ví dụ thêm hàng tồn kho

**Input:**
```
Sản phẩm: "Tài khoản Canva Premium" (ID: 123)
Biến thể: "Canva 1 tháng" (variant_code: "canva_1_thang_0")
Username: "user@example.com"
Password: "password123"
```

**Output:**
```
1. Credential được lưu:
   - product_id: 123
   - variant_code: "canva_1_thang_0"
   - encrypted_value: "$2a$10$..." (BCrypt hash)
   - is_sold: 0

2. Inventory được cập nhật:
   - products.inventory_count: 0 → 1
   - variants_json[0].inventory_count: 0 → 1
```

### 6.3. Màn hình demo

**Trang tạo sản phẩm:**
```
┌─────────────────────────────────────┐
│  ĐĂNG SẢN PHẨM MỚI                  │
├─────────────────────────────────────┤
│  Tên sản phẩm: [____________]       │
│  Loại: [SOFTWARE ▼]                 │
│  Phân loại: [CANVA ▼]               │
│                                      │
│  Biến thể sản phẩm:                 │
│  ┌─────────────────────────────┐   │
│  │ Biến thể 1                  │   │
│  │ Tên: [Canva 1 tháng]        │   │
│  │ Giá: [90000]                │   │
│  │ Ảnh: [Chọn ảnh] [2/3]       │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ Biến thể 2                  │   │
│  │ Tên: [Canva 12 tháng]       │   │
│  │ Giá: [750000]               │   │
│  │ Ảnh: [Chọn ảnh] [3/3]       │   │
│  └─────────────────────────────┘   │
│                                      │
│  [Thêm biến thể]                    │
│                                      │
│  [Đăng sản phẩm]                    │
└─────────────────────────────────────┘
```

**Trang thêm hàng tồn kho:**
```
┌─────────────────────────────────────┐
│  THÊM HÀNG TỒN KHO                  │
├─────────────────────────────────────┤
│  Sản phẩm: Tài khoản Canva Premium  │
│                                      │
│  Mã biến thể:                       │
│  [Canva 1 tháng - 90,000 ₫ ▼]       │
│    - Canva 1 tháng - 90,000 ₫       │
│    - Canva 12 tháng - 750,000 ₫     │
│                                      │
│  Username: [____________]           │
│  Password: [____________]            │
│                                      │
│  [Hủy]  [Lưu thay đổi]              │
└─────────────────────────────────────┘
```

---

## 7. TÓM TẮT VÀ KẾT LUẬN

### 7.1. Điểm mạnh

✅ **Linh hoạt**: Hỗ trợ nhiều biến thể cho mỗi sản phẩm
✅ **Bảo mật**: Mã hóa credentials trước khi lưu
✅ **Dễ mở rộng**: JSON storage cho phép thêm thuộc tính mới
✅ **User-friendly**: Giao diện trực quan, dễ sử dụng

### 7.2. Cải tiến có thể thực hiện

🔧 **Validation nâng cao**: Kiểm tra format username/password
🔧 **Batch upload**: Cho phép upload nhiều credentials cùng lúc
🔧 **Import/Export**: Xuất nhập dữ liệu từ file Excel
🔧 **Thống kê**: Dashboard hiển thị tồn kho theo thời gian

### 7.3. Kết luận

Hệ thống quản lý kho cung cấp đầy đủ chức năng để seller:
- Tạo và quản lý sản phẩm với nhiều biến thể
- Thêm và theo dõi hàng tồn kho
- Quản lý credentials một cách an toàn

---

## 📝 GHI CHÚ CHO NGƯỜI THUYẾT TRÌNH

### Thời gian đề xuất: 10-15 phút

**Phân bổ thời gian:**
- Tổng quan: 2 phút
- Thêm sản phẩm: 5 phút
- Thêm hàng tồn kho: 4 phút
- Demo: 3 phút
- Q&A: 1 phút

### Điểm nhấn khi trình bày:

1. **Nhấn mạnh tính linh hoạt** của việc dùng JSON cho variants
2. **Giải thích quy trình upload ảnh** và xử lý multipart
3. **Nhấn mạnh bảo mật** trong việc mã hóa credentials
4. **Demo trực tiếp** nếu có thể để tăng tính thuyết phục

### Câu hỏi thường gặp:

**Q: Tại sao dùng JSON thay vì bảng riêng cho variants?**
A: Linh hoạt hơn, không cần migration khi thêm thuộc tính mới, phù hợp với yêu cầu dynamic của hệ thống.

**Q: Làm sao đảm bảo không trùng variant code?**
A: Variant code được tạo tự động từ tên + index, và được normalize khi so sánh.

**Q: Credentials được mã hóa như thế nào?**
A: Sử dụng BCrypt, một thuật toán one-way hashing, không thể decrypt ngược lại.

---

**Chúc bạn thuyết trình thành công! 🎉**

