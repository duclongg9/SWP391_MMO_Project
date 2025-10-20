# Hướng dẫn thiết lập hệ thống KYC

## Tổng quan
Hệ thống KYC (Know Your Customer) cho phép người dùng gửi yêu cầu xác minh danh tính để trở thành seller thông qua upload CCCD và ảnh selfie.

## Các thành phần đã triển khai

### 1. Database
- **Bảng seller_requests** đã được cập nhật với các cột KYC:
  - `full_name`: Họ tên đầy đủ từ CCCD
  - `date_of_birth`: Ngày sinh
  - `id_number`: Số CCCD/CMND
  - `front_id_image_path`: Đường dẫn ảnh CCCD mặt trước
  - `back_id_image_path`: Đường dẫn ảnh CCCD mặt sau
  - `selfie_image_path`: Đường dẫn ảnh selfie

### 2. Backend Components
- **Models**: `SellerRequest` đã được cập nhật với các field KYC
- **Services**: 
  - `KycRequestService`: Xử lý logic nghiệp vụ KYC
  - `FileUploadService`: Xử lý upload và quản lý file ảnh
- **Controllers**:
  - `KycRequestController`: User gửi yêu cầu KYC
  - `KycRequestManagementController`: Admin quản lý yêu cầu
  - `FileServlet`: Serve uploaded files
- **DAO**: `SellerRequestDAO` đã được cập nhật để xử lý dữ liệu KYC

### 3. Frontend Components
- **User Interface**:
  - `kyc-request-form.jsp`: Form upload CCCD và thông tin
  - `kyc-request-status.jsp`: Xem trạng thái yêu cầu KYC
- **Admin Interface**:
  - `kyc-request-list.jsp`: Danh sách yêu cầu chờ duyệt
  - `kyc-request-detail.jsp`: Chi tiết và duyệt yêu cầu
- **Navigation**: Updated để có link "Xác minh KYC" cho buyer và "Duyệt yêu cầu KYC" cho admin

## Cách thiết lập

### 1. Cập nhật Database
Chạy script SQL sau để cập nhật bảng seller_requests:

```sql
-- Thêm các cột mới cho KYC
ALTER TABLE seller_requests 
ADD COLUMN full_name VARCHAR(100) NOT NULL AFTER user_id,
ADD COLUMN date_of_birth DATE NOT NULL AFTER full_name,
ADD COLUMN id_number VARCHAR(20) NOT NULL AFTER date_of_birth,
ADD COLUMN front_id_image_path VARCHAR(255) NULL AFTER id_number,
ADD COLUMN back_id_image_path VARCHAR(255) NULL AFTER front_id_image_path,
ADD COLUMN selfie_image_path VARCHAR(255) NULL AFTER back_id_image_path;

-- Thêm indexes
ALTER TABLE seller_requests
ADD INDEX idx_id_number (id_number),
ADD INDEX idx_full_name (full_name);
```

### 2. Tạo thư mục uploads
```bash
mkdir -p uploads/kyc
chmod 755 uploads
chmod 755 uploads/kyc
```

### 3. Cấu hình Web Server
Đảm bảo Tomcat có quyền ghi vào thư mục `uploads/`.

### 4. URL Mappings
- **User**: `/user/kyc-request` - Gửi và xem trạng thái KYC
- **Admin**: `/admin/kyc-requests` - Quản lý yêu cầu KYC
- **Files**: `/uploads/*` - Serve uploaded files

## Workflow

### Quy trình KYC
1. **User (Buyer) gửi yêu cầu**:
   - Điền thông tin cá nhân (họ tên, ngày sinh, số CCCD)
   - Upload ảnh CCCD mặt trước, mặt sau
   - Upload ảnh selfie
   - Điền thông tin kinh doanh
   - Submit form

2. **Hệ thống xử lý**:
   - Validate dữ liệu input
   - Upload và lưu trữ ảnh
   - Lưu thông tin vào database với status "Pending"
   - Gửi notification cho admin

3. **Admin xem xét**:
   - Xem danh sách yêu cầu pending
   - Click vào chi tiết để xem thông tin và ảnh
   - Verify thông tin trên CCCD với ảnh selfie
   - Approve hoặc Reject với lý do

4. **Kết quả**:
   - **Nếu Approve**: User role chuyển thành seller (role_id = 2)
   - **Nếu Reject**: User có thể gửi lại yêu cầu mới

### Security Features
- **File Upload Security**:
  - Giới hạn file size (5MB)
  - Chỉ chấp nhận ảnh (.jpg, .png)
  - Validate MIME type
  - Random filename để tránh conflict
  - Path traversal protection

- **Access Control**:
  - Chỉ buyer mới gửi được KYC request
  - Chỉ admin mới xem/duyệt được request
  - File chỉ accessible qua servlet với security check

## Testing

### Test Cases để kiểm tra
1. **User Flow**:
   - [ ] Buyer có thể truy cập form KYC
   - [ ] Upload ảnh CCCD và selfie thành công
   - [ ] Validation hoạt động đúng (file size, format)
   - [ ] Xem được trạng thái sau khi gửi
   - [ ] Seller không thể gửi KYC request

2. **Admin Flow**:
   - [ ] Admin xem được danh sách request
   - [ ] Click vào detail hiển thị đầy đủ thông tin
   - [ ] Xem được ảnh KYC rõ nét (click để zoom)
   - [ ] Approve request thành công → user thành seller
   - [ ] Reject request với lý do

3. **Security**:
   - [ ] Non-admin không truy cập được admin pages
   - [ ] File uploads được validate đúng
   - [ ] Không thể access file của user khác
   - [ ] Directory traversal bị block

## Monitoring & Maintenance

### Log Files cần theo dõi
- Upload errors trong `FileUploadService`
- KYC processing errors trong `KycRequestService`  
- File serving errors trong `FileServlet`

### Cleanup Tasks
- Định kỳ xóa file của KYC request bị reject
- Archive các KYC request cũ đã processed
- Monitor disk usage của thư mục uploads

## Mở rộng trong tương lai
1. **Email Notifications**: Gửi email khi KYC được approve/reject
2. **Advanced OCR**: Tự động extract thông tin từ ảnh CCCD
3. **Face Recognition**: So sánh ảnh selfie với ảnh trên CCCD
4. **Audit Trail**: Log chi tiết các hành động của admin
5. **Batch Processing**: Approve/reject nhiều request cùng lúc

## Troubleshooting

### Common Issues
1. **File upload fails**: Kiểm tra quyền write của thư mục uploads
2. **Images không hiển thị**: Kiểm tra FileServlet mapping
3. **Database errors**: Chạy lại SQL script update
4. **Memory issues**: Kiểm tra file size limits trong web.xml

### Debug Steps
1. Check tomcat logs cho errors
2. Verify database schema đã update đúng
3. Test file permissions: `ls -la uploads/`
4. Verify URL mappings trong web.xml hoặc annotations
