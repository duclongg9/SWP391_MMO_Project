# Luồng xử lý mua hàng trong hệ thống

Tài liệu này mô tả chi tiết các bước từ khi người dùng bấm **Mua ngay** trên giao diện cho tới khi đơn hàng hoàn tất và thông tin bàn giao được hiển thị. Các mốc chính được liệt kê kèm tên lớp/hàm để thuận tiện tra cứu mã nguồn.

## 1. Người dùng gửi yêu cầu mua
- **Giao diện**: `web/WEB-INF/views/product/detail.jsp` hiển thị nút "Mua ngay".
- **Controller**: yêu cầu POST tới `/order/buy-now` được xử lý bởi `controller.order.OrderController#handleBuyNow`.
  - Hàm kiểm tra phiên đăng nhập, đọc tham số sản phẩm (`productId`, `qty`, `variantCode`) và khóa idempotent `idemKey`.
  - Sau khi chuẩn hóa dữ liệu, controller gọi `service.OrderService#placeOrderPending`.

## 2. OrderService chuẩn bị dữ liệu và tạo đơn Pending
- `OrderService#placeOrderPending` thực hiện lần lượt các bước:
  1. Xác nhận khóa idempotent chưa được sử dụng cho người mua hiện tại.
  2. Tải thông tin sản phẩm thông qua `dao.product.ProductDAO#findAvailableById` và kiểm tra tồn kho/biến thể.
  3. Kiểm tra số lượng credential khả dụng bằng `dao.order.CredentialDAO#fetchAvailability` (hoặc bản theo biến thể).
  4. Đảm bảo ví người mua hợp lệ và đủ số dư bằng `dao.user.WalletsDAO#ensureUserWallet`.
  5. Tạo bản ghi đơn hàng Pending qua `dao.order.OrderDAO#createPending` và publish thông điệp tới hàng đợi bằng `queue.memory.InMemoryOrderQueue#publish`.

## 3. Hàng đợi nội bộ nhận thông điệp
- `InMemoryOrderQueue` đưa thông điệp `OrderMessage` vào queue nội bộ và thread dispatcher chuyển đến `AsyncOrderWorker`.
- Worker được khởi tạo thông qua `InMemoryOrderQueue#ensureWorkerInitialized` (được gọi từ constructor `OrderService`).

## 4. Worker xử lý đơn hàng bất đồng bộ
`queue.memory.AsyncOrderWorker#handle` và `#processMessage` đảm nhiệm các bước:
1. Lock đơn hàng, đặt trạng thái `Processing` để tránh xử lý trùng.
2. `lockWalletAndValidateBalance`: khóa ví người mua, kiểm tra số dư.
3. `lockProductAndVerifyInventory`: khóa tồn kho sản phẩm và biến thể.
4. `reserveCredentials`: giữ chỗ các `product_credentials` phù hợp.
5. `chargeWallet`: trừ tiền ví, ghi log bằng `dao.user.WalletTransactionDAO#insertTransaction` và gắn `payment_transaction_id` cho đơn.
6. `finalizeFulfillment`: trừ tồn kho, đánh dấu credential đã bán, ghi log tồn kho.
7. Cập nhật trạng thái đơn sang `Completed` và commit transaction.
8. Worker đọc cấu hình `escrow.hold.default.seconds` từ bảng `system_configs` để đặt `orders.escrow_release_at`, ghi log sự kiện
   vào `order_escrow_events` và chuẩn bị cho việc giải ngân tự động khi không có khiếu nại.

Nếu có lỗi nghiệp vụ/SQL, worker rollback và đánh dấu đơn `Failed` thông qua `OrderDAO#setStatus`.

## 5. Người dùng xem chi tiết đơn hàng
- GET `/orders/detail/{token}` được điều phối bởi `OrderController#showOrderDetail`.
  - Hàm kiểm tra quyền sở hữu, nạp `OrderDetailView` qua `OrderService#getDetail`.
  - Nếu người dùng đã mở khóa credential, service trả về danh sách plaintext để JSP hiển thị.
- Giao diện `order/detail.jsp` gọi AJAX `/orders/detail/{token}/wallet-events` (do `OrderController#handleWalletEventsApi` cung cấp).
  - API trả về danh sách sự kiện ví được dựng bởi `OrderService#buildWalletTimeline`, mô tả các bước: kiểm tra ví, trừ tiền, chờ giao dịch và trạng thái đơn.

## 6. Mở khóa thông tin bàn giao
- Khi đơn ở trạng thái `Completed`, người dùng bấm nút mở khóa.
- POST `/orders/unlock` (trong `OrderController#handleUnlockCredentials`) ghi nhận log mở khóa thông qua `CredentialDAO#logView` và cho phép tải plaintext credential trong lần truy cập tiếp theo.

---
Tài liệu này giúp lập trình viên nhanh chóng lần theo luồng xử lý khi cần debug, tối ưu hoặc mở rộng tính năng liên quan đến mua hàng.
