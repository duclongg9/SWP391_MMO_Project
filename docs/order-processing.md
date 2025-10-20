# Order Processing Flow

Sơ đồ trạng thái đơn hàng:

```
Pending → Processing → (Completed | Failed) → (Refunded | Disputed)
```

Các trạng thái chính được áp dụng cho tất cả đơn hàng của người mua. Luồng xử lý bất đồng bộ đảm bảo đơn mới tạo sẽ chuyển từ `Pending` sang `Processing`, sau đó:

- Nếu bàn giao thành công: cập nhật `Completed` và gắn thông tin credential.
- Nếu gặp lỗi (hết hàng, lỗi hệ thống sau 3 lần retry): chuyển sang `Failed`.
- Sau khi hoàn thành, có thể tiếp tục tiến tới `Refunded` hoặc `Disputed` tùy theo quy trình chăm sóc khách hàng.
