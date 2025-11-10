# Hướng dẫn tạo mã QR VNPAY cho tài khoản merchant

Tài liệu này tổng hợp lại các bước cần thiết để bạn (merchant) tự tạo hình ảnh mã QR VNPAY phục vụ cho việc in ấn, gửi cho khách hàng hoặc cấu hình tại quầy giao dịch. Nội dung được biên soạn dựa trên tài liệu chính thức của VNPAY và kinh nghiệm triển khai thực tế.

## 1. Thông tin bắt buộc phải chuẩn bị

Trước khi bắt đầu, hãy đảm bảo bạn đã nhận được các thông tin do VNPAY cấp:

- **TMN Code** (hay còn gọi là mã website/terminal) và **Terminal ID**: dùng để xác định đúng điểm bán.
- **Secret Key** và **API URL**: phục vụ cho việc ký checksum khi tích hợp hệ thống (các giá trị này nằm trong file cấu hình ứng dụng).
- **Tài liệu hướng dẫn tích hợp** từ VNPAY: đặc biệt là phần *Hướng dẫn tạo QR* và *Bảng mã lỗi*.
- **Thông tin tài khoản ngân hàng nhận tiền**: để đối soát sau khi có giao dịch.

Nếu thiếu bất kỳ thông tin nào, hãy liên hệ quản trị viên VNPAY phụ trách dự án hoặc bộ phận hỗ trợ merchant của VNPAY.

## 2. Các bước tạo QR động trực tiếp trên cổng Merchant

1. Đăng nhập vào cổng quản trị <https://merchant.vnpay.vn> bằng tài khoản được VNPAY cấp.
2. Trên thanh menu, chọn **Quản lý QR** → **Tạo QR**.
3. Chọn loại QR cần tạo:
   - **QR động**: gắn với một giao dịch cụ thể (có số tiền cố định). Phù hợp khi muốn tạo hoá đơn có sẵn số tiền.
   - **QR tĩnh**: chỉ chứa thông tin điểm bán, khách tự nhập số tiền. Dùng để in ấn, trưng bày tại quầy.
4. Điền các trường bắt buộc:
   - **Terminal ID/TMN Code**: chọn đúng điểm bán cần nhận tiền.
   - **Amount**: số tiền cần thu (đối với QR động). Nên khớp với số tiền mà hệ thống MMO Trader Market sinh ra để tiện đối soát.
   - **Order Info/Mô tả**: nên sử dụng chuỗi `Nap vi VNPAY <TxnRef>` để trùng với giao dịch trong hệ thống.
   - **Expire Date** (nếu có): nên thiết lập cùng giá trị hết hạn 15 phút giống cấu hình `paymentTimeout` trong hệ thống.
5. Nhấn **Tạo QR**. Hệ thống sẽ hiển thị ảnh QR và cho phép tải về dưới định dạng PNG hoặc SVG.
6. Lưu trữ hoặc in mã QR để sử dụng. Khi khách quét QR, giao dịch sẽ được xử lý qua cùng TMN Code nên vẫn nhận IPN như bình thường.

## 3. Lấy thông tin từ file cấu hình của dự án

Trong dự án MMO Trader Market, các thông tin tích hợp VNPAY được lưu tại file `src/conf/database.properties` (hoặc đọc qua biến môi trường tương ứng):

```
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_SECRET
vnpay.payUrl=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=https://yourdomain.com/wallet/vnpay/return
vnpay.ipnUrl=https://yourdomain.com/api/vnpay/ipn
```

Ngoài ra, lớp `conf.VnpayConfig` ánh xạ các giá trị này vào hệ thống Java. Bạn có thể kiểm tra nhanh bằng cách tìm các phương thức `tmnCode()`, `hashSecret()` trong lớp này.

## 4. Gợi ý điền thông tin khi tạo QR

| Trường trên cổng VNPAY | Giá trị khuyến nghị | Giải thích |
| ---------------------- | ------------------- | ---------- |
| Terminal ID            | Chọn đúng ID VNPAY cấp | Đảm bảo tiền về đúng điểm bán |
| Amount                 | Trùng số tiền giao dịch | Hỗ trợ đối soát và tránh sai lệch |
| Order Info             | `Nap vi VNPAY <TxnRef>` | Giúp khớp với mã trong bảng `DepositRequests` |
| Order ID               | Có thể sử dụng lại `<TxnRef>` | Dễ tra cứu khi làm việc với VNPAY |
| Expire Date            | `now + 15 phút`          | Đồng bộ với thời hạn thanh toán trong app |

> **Lưu ý:** Với QR tĩnh, trường `Amount` sẽ để trống và khách sẽ nhập trực tiếp khi quét. Tuy nhiên, vẫn nên in hướng dẫn số tiền chính xác cần thanh toán trên hoá đơn.

## 5. Sau khi tạo QR

- Kiểm tra QR bằng ứng dụng ngân hàng/ ví điện tử để đảm bảo hiển thị đúng thông tin điểm bán và mô tả giao dịch.
- Lưu trữ file QR ở định dạng chất lượng cao (SVG/PNG 1024px) để phục vụ in ấn.
- Định kỳ rà soát lại hạn mức và thông tin Terminal ID với VNPAY nếu có thay đổi địa điểm/chi nhánh.
- Nếu cần thay đổi mô tả giao dịch, hãy cập nhật song song trên hệ thống MMO Trader Market để tránh lệch thông tin đối soát.

## 6. Tài liệu tham khảo

- Sổ tay merchant VNPAY (file PDF do VNPAY cung cấp).
- Tài liệu tích hợp API VNPAY – mục *Payment QR Code*.
- Bộ phận hỗ trợ merchant VNPAY: hotline 1900 55 55 77 hoặc email `merchant@vnpay.vn`.

Chúc bạn triển khai thành công!
