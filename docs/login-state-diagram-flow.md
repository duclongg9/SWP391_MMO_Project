# Luồng trạng thái đăng nhập hệ thống

Tài liệu này mô tả các trạng thái và chuyển tiếp chính trong quy trình đăng nhập của hệ thống để hỗ trợ việc dựng biểu đồ trạng thái.

## Tổng quan luồng đăng nhập

1. **Bắt đầu**: Người dùng mở giao diện đăng nhập.
2. **Nhập thông tin**: Người dùng nhập email/số điện thoại và mật khẩu.
3. **Kiểm tra dữ liệu**: Hệ thống thực hiện hai bước kiểm tra:
   - Xác thực dữ liệu đầu vào (định dạng, trường bắt buộc).
   - Gửi yêu cầu xác thực tới máy chủ.
4. **Xử lý phản hồi**:
   - Nếu thông tin hợp lệ và tài khoản hoạt động, chuyển sang trạng thái đăng nhập thành công.
   - Nếu thông tin sai hoặc tài khoản bị khóa, hiển thị thông báo lỗi và yêu cầu nhập lại.
5. **Kết thúc**: Người dùng được chuyển đến trang chủ/dashboard khi đăng nhập thành công, hoặc tiếp tục ở màn đăng nhập nếu thất bại.

## Các trạng thái chính

| Trạng thái | Mô tả | Sự kiện chuyển tiếp |
|------------|-------|----------------------|
| `Idle` | Màn hình đăng nhập sẵn sàng nhận dữ liệu. | Người dùng bắt đầu nhập thông tin. |
| `Typing Credentials` | Người dùng nhập email/số điện thoại và mật khẩu. | Người dùng nhấn nút "Đăng nhập". |
| `Validating Input` | Hệ thống kiểm tra định dạng và tính đầy đủ của dữ liệu. | Dữ liệu hợp lệ → chuyển sang `Authenticating`. <br> Dữ liệu không hợp lệ → quay về `Idle` kèm thông báo lỗi. |
| `Authenticating` | Gửi yêu cầu xác thực tới máy chủ và chờ phản hồi. | Phản hồi thành công → `Logged In`. <br> Phản hồi thất bại → `Authentication Failed`. |
| `Authentication Failed` | Hiển thị lỗi (sai mật khẩu, tài khoản khóa, mạng lỗi). | Người dùng nhấn "Thử lại" → quay về `Idle`. |
| `Logged In` | Đăng nhập thành công, khởi tạo phiên và chuyển trang. | Người dùng đăng xuất → quay về `Idle`. |

## Các nhánh xử lý đặc biệt

- **Quên mật khẩu**: Từ trạng thái `Idle`, người dùng chọn "Quên mật khẩu" để chuyển đến quy trình khôi phục (biểu đồ trạng thái riêng).
- **Đăng ký tài khoản**: Người dùng chọn "Đăng ký" để đến quy trình tạo tài khoản mới.
- **Xác thực hai yếu tố (nếu có)**:
  - Sau `Authenticating`, nếu tài khoản bật 2FA, chuyển sang trạng thái `Awaiting OTP Verification`.
  - Người dùng nhập OTP hợp lệ → `Logged In`; ngược lại → quay về `Idle` hoặc hiển thị thông báo lỗi tương ứng.

## Gợi ý khi vẽ state diagram

- Bắt đầu từ trạng thái `Idle` với ký hiệu Start.
- Thể hiện rõ các nhánh thành công/thất bại bằng đường chuyển tiếp có nhãn sự kiện.
- Đặt chú thích cho các trạng thái phụ (ví dụ: hiển thị lỗi, yêu cầu OTP) nếu biểu đồ phức tạp.
- Sử dụng màu sắc khác nhau cho luồng chính và luồng ngoại lệ để dễ theo dõi.

Tài liệu này có thể dùng làm cơ sở để vẽ state diagram chi tiết trong các công cụ như draw.io, Lucidchart hoặc PlantUML.
