# TÓM TẮT THUYẾT TRÌNH: QUẢN LÝ KHO

## 🎯 SLIDE 1: GIỚI THIỆU
- **Chủ đề**: Quản lý kho - Thêm sản phẩm và hàng tồn kho
- **Mục đích**: Cho phép seller quản lý sản phẩm và tồn kho của shop

---

## 📦 SLIDE 2: TỔNG QUAN CHỨC NĂNG
1. **Xem danh sách sản phẩm**
2. **Tạo sản phẩm mới** ⭐
3. **Xem chi tiết tồn kho**
4. **Thêm hàng tồn kho** ⭐
5. **Sửa/Xóa sản phẩm**

---

## ➕ SLIDE 3: THÊM SẢN PHẨM - QUY TRÌNH

```
User điền form → Submit → Server xử lý → Lưu DB → Redirect
```

**Dữ liệu cần nhập:**
- Thông tin cơ bản (tên, loại, mô tả)
- **Biến thể sản phẩm** (tên, giá, ảnh 1-3 file)

---

## 📤 SLIDE 4: THÊM SẢN PHẨM - DỮ LIỆU GỬI LÊN

**Form Parameters:**
- `productName`, `productType`, `productSubtype`
- `variantsJson` (JSON string)
- `variantIndices` ("0:2,1:3" - variant: số ảnh)
- **Files**: `variantImages_0_0`, `variantImages_0_1`, ...

**Ví dụ variantsJson:**
```json
[
  {
    "variant_code": "canva_1_thang_0",
    "name": "Canva 1 tháng",
    "price": 90000
  }
]
```

---

## ⚙️ SLIDE 5: THÊM SẢN PHẨM - XỬ LÝ SERVER

1. **Validate** dữ liệu
2. **Upload ảnh** → Lưu vào server
3. **Parse JSON** variants
4. **Gắn đường dẫn ảnh** vào variants
5. **Tính giá thấp nhất** từ variants
6. **INSERT vào database**

**Kết quả:**
- Sản phẩm có `inventory_count = 0`
- Status = "Available"
- Variants lưu trong `variants_json` (TEXT field)

---

## 📥 SLIDE 6: THÊM HÀNG TỒN KHO - KHÁI NIỆM

**Hàng tồn kho = Credentials (Tài khoản/Key)**

- Mỗi sản phẩm cần credentials để bán
- Credentials được **mã hóa** trước khi lưu
- Mỗi credential có thể gắn với **variant code**

---

## 📝 SLIDE 7: THÊM HÀNG TỒN KHO - FORM

**Các trường:**
1. **Sản phẩm** (tự động chọn)
2. **Mã biến thể** (dropdown - nếu có variants)
   - Format: "Tên - Giá"
   - Ví dụ: "Canva 1 tháng - 90,000 ₫"
3. **Username** (bắt buộc)
4. **Password** (bắt buộc - sẽ mã hóa)

---

## 🔄 SLIDE 8: THÊM HÀNG TỒN KHO - XỬ LÝ

1. **Parse variants** từ `variants_json`
2. **Validate variant code**
3. **Mã hóa password** (BCrypt)
4. **INSERT credential** vào DB
5. **UPDATE inventory_count**
   - Tăng `products.inventory_count`
   - Tăng `variants_json[].inventory_count`

---

## 🗄️ SLIDE 9: CẤU TRÚC DATABASE

**Bảng `products`:**
- `variants_json` (TEXT) - JSON array
- `variant_schema` ("custom" hoặc "none")
- `gallery_json` (TEXT) - Tất cả ảnh

**Bảng `product_credentials`:**
- `product_id`
- `variant_code` (có thể NULL)
- `encrypted_value` (Username + Password đã mã hóa)
- `is_sold` (0 = chưa bán)

---

## 💡 SLIDE 10: ĐIỂM NỔI BẬT

✅ **JSON Storage**: Linh hoạt, dễ mở rộng
✅ **Multipart Upload**: Hỗ trợ nhiều ảnh
✅ **Bảo mật**: Mã hóa credentials
✅ **Variant Support**: Quản lý tồn kho theo variant

---

## 🎬 SLIDE 11: DEMO

**Ví dụ tạo sản phẩm:**
- Tên: "Tài khoản Canva Premium"
- 2 biến thể: "1 tháng" (90k), "12 tháng" (750k)
- Mỗi variant có 2-3 ảnh

**Ví dụ thêm hàng:**
- Chọn variant "Canva 1 tháng"
- Nhập username/password
- Inventory tăng từ 0 → 1

---

## ✅ SLIDE 12: KẾT LUẬN

- Hệ thống cung cấp đầy đủ chức năng quản lý kho
- Hỗ trợ nhiều biến thể linh hoạt
- Bảo mật thông tin credentials
- Dễ sử dụng cho seller

**Cảm ơn! Q&A**

---

## 📌 GHI CHÚ NHANH

**Khi trình bày:**
- Nhấn mạnh: JSON linh hoạt, bảo mật credentials
- Giải thích: Quy trình upload ảnh multipart
- Demo: Nếu có thể, show trực tiếp

**Thời gian: 10-15 phút**
- Tổng quan: 2p
- Thêm sản phẩm: 5p
- Thêm hàng: 4p
- Demo: 3p
- Q&A: 1p

