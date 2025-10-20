# Hệ thống yêu cầu trở thành Seller

## 📋 Tổng quan

Hệ thống này cho phép user (buyer) gửi yêu cầu đến admin để được chuyển thành seller và có thể tạo gian hàng, đăng bán sản phẩm.

## 🚀 Cách setup

### 1. Chạy SQL Script
```sql
-- Chạy file database_scripts/seller_requests_table.sql
-- để tạo bảng seller_requests trong database
```

### 2. Kiểm tra các file đã được tạo
```
Backend:
├── model/SellerRequest.java
├── model/view/SellerRequestView.java  
├── dao/user/SellerRequestDAO.java
├── service/SellerRequestService.java
├── controller/user/SellerRequestController.java
└── controller/admin/SellerRequestManagementController.java

Frontend:
├── user/seller-request-form.jsp
├── user/seller-request-status.jsp
├── admin/seller-request-list.jsp
├── admin/seller-request-detail.jsp
└── admin/seller-request-reject.jsp

Navigation: NavigationBuilder.java (đã cập nhật)
Database: UserDAO.java (thêm updateUserRole method)
```

## 🎯 Workflow hoàn chỉnh

### Cho User (Buyer - Role 3):
1. **Gửi yêu cầu**: User click "Trở thành seller" trong menu
2. **Điền form**: Nhập thông tin doanh nghiệp, kinh nghiệm, liên hệ
3. **Chờ duyệt**: Theo dõi trạng thái qua trang seller request
4. **Nhận kết quả**: Được duyệt → Trở thành seller | Bị từ chối → Gửi lại

### Cho Admin (Role 1):
1. **Xem danh sách**: Truy cập "Duyệt yêu cầu seller" trong menu admin
2. **Lọc theo trạng thái**: Pending, Approved, Rejected
3. **Xem chi tiết**: Click "Chi tiết" để xem đầy đủ thông tin
4. **Xử lý yêu cầu**: 
   - **Duyệt**: User được chuyển thành seller (role 2) tự động
   - **Từ chối**: Nhập lý do từ chối chi tiết

## 📱 URL Routes

### User Routes:
- `GET /user/seller-request` - Xem form gửi yêu cầu hoặc trạng thái
- `POST /user/seller-request` - Gửi yêu cầu mới

### Admin Routes:
- `GET /admin/seller-requests` - Danh sách yêu cầu (có phân trang + lọc)
- `GET /admin/seller-requests?action=view&id=X` - Chi tiết yêu cầu
- `GET /admin/seller-requests?action=approve&id=X` - Duyệt yêu cầu
- `GET /admin/seller-requests?action=reject&id=X` - Form từ chối
- `POST /admin/seller-requests?action=submit-reject` - Submit từ chối

## 🛡️ Security & Validation

### Input Validation:
- **Business Name**: 3-100 ký tự, chỉ chữ, số và ký tự đặc biệt
- **Business Description**: 20-1000 ký tự
- **Experience**: 10-500 ký tự  
- **Contact Info**: 10-200 ký tự
- **Rejection Reason**: 10-500 ký tự (admin)

### Business Rules:
- Chỉ buyer (role 3) mới được gửi yêu cầu
- Mỗi user chỉ có thể có 1 yêu cầu Pending
- Sau khi bị từ chối có thể gửi yêu cầu mới
- Admin có thể duyệt/từ chối các yêu cầu Pending
- Khi duyệt: User role tự động chuyển từ 3 → 2

### Authorization:
- User routes: Cần đăng nhập + role 3
- Admin routes: Cần đăng nhập + role 1

## 🎨 UI Components

### User Interface:
- **Form gửi yêu cầu**: Form đẹp với validation, hướng dẫn chi tiết
- **Trạng thái yêu cầu**: Hiển thị trạng thái Pending/Approved/Rejected
- **Process flow**: Hiển thị các bước trở thành seller

### Admin Interface:
- **Danh sách yêu cầu**: Table với phân trang, lọc theo trạng thái
- **Chi tiết yêu cầu**: Hiển thị đầy đủ thông tin, action buttons
- **Form từ chối**: Textarea nhập lý do + gợi ý lý do phổ biến

## 📊 Database Schema

```sql
seller_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    business_name VARCHAR(100) NOT NULL,
    business_description TEXT NOT NULL,
    experience TEXT NOT NULL,
    contact_info VARCHAR(200) NOT NULL,
    status ENUM('Pending', 'Approved', 'Rejected'),
    rejection_reason TEXT NULL,
    reviewed_by INT NULL,
    created_at TIMESTAMP,
    reviewed_at TIMESTAMP NULL
)
```

## 🔄 Luồng chuyển Role

```
User Registration → Role 3 (Buyer)
    ↓ Gửi seller request
Seller Request → Status: Pending  
    ↓ Admin duyệt
User Role: 3 → 2 (Seller)
    ↓ Có thể sử dụng
Seller Features: Tạo shop, đăng sản phẩm
```

## ✅ Features hoàn chỉnh

### ✅ User Features:
- Gửi yêu cầu trở thành seller
- Xem trạng thái yêu cầu hiện tại  
- Gửi lại yêu cầu sau khi bị từ chối
- Menu navigation "Trở thành seller"

### ✅ Admin Features:
- Xem danh sách tất cả yêu cầu
- Lọc theo trạng thái (All/Pending/Approved/Rejected)
- Phân trang danh sách
- Xem chi tiết từng yêu cầu
- Duyệt yêu cầu (tự động chuyển role)
- Từ chối yêu cầu với lý do cụ thể
- Menu navigation "Duyệt yêu cầu seller"

### ✅ System Features:
- Validation input đầy đủ
- Error handling graceful
- Responsive UI
- Real-time status updates
- Email có thể tích hợp sau
- Audit trail (created_at, reviewed_at, reviewed_by)

## 🎉 Kết quả

Sau khi implement thành công:
1. **User** có thể gửi yêu cầu trở thành seller một cách dễ dàng
2. **Admin** có giao diện quản lý yêu cầu chuyên nghiệp  
3. **Hệ thống** tự động chuyển role khi được duyệt
4. **Integration** hoàn hảo với hệ thống seller/shop hiện tại

User được duyệt sẽ có thể ngay lập tức:
- Tạo gian hàng (/seller/shop)
- Đăng sản phẩm (/seller/products) 
- Quản lý bán hàng
- Sử dụng đầy đủ tính năng seller

**Hệ thống seller request đã hoàn thiện 100%!** 🚀
