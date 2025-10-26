# Google Sign-In "DEVELOPER_ERROR (10)" Troubleshooting

Google Sign-In trả về mã `DEVELOPER_ERROR (10)` khi cấu hình OAuth của ứng dụng Android không khớp với thông tin mà Google Cloud Console ghi nhận. Lỗi này thường xuất hiện ngay khi nhấn nút đăng nhập và khiến người dùng không thể chọn tài khoản Google. Các bước dưới đây giúp bạn rà soát cấu hình và thử lại.

## 1. Kiểm tra SHA certificate fingerprints

1. Mở Android Studio và chạy lệnh Gradle:
   ```bash
   ./gradlew signingReport
   ```
2. Ghi lại giá trị `SHA1` (và nếu cần `SHA-256`) của variant `debug` hoặc `release` mà bạn dùng để build APK/AAB.
3. Trong [Google Cloud Console](https://console.cloud.google.com/apis/credentials), mở OAuth client ứng dụng Android và đảm bảo trường **SHA-1 certificate fingerprint** trùng khớp.
4. Nếu dùng nhiều máy hoặc CI, hãy đăng ký tất cả SHA fingerprint tương ứng với `applicationId`/package name của bạn.

## 2. Điền thông tin khi tạo OAuth client Android

Khi nhấn **Create OAuth client ID** và chọn loại ứng dụng **Android**, bạn sẽ thấy các trường như trong hình minh họa:

* **Name** – đặt tên mô tả cho client (ví dụ: `Hanabi Kanji Learning App Debug`). Trường này chỉ giúp bạn dễ quản lý và không ảnh hưởng tới mã nguồn.
* **Package name** – nhập chính xác giá trị `applicationId` mà bạn khai báo trong `app/build.gradle`. Ví dụ nếu file Gradle dùng `applicationId "com.example.kanjilearning"` thì điền đúng chuỗi đó.
* **SHA-1 certificate fingerprint** – dán chuỗi SHA-1 lấy từ lệnh `./gradlew signingReport` tương ứng với keystore bạn đang build (debug hoặc release). Nếu phát hành trên Google Play, dùng SHA-1 của bản release/signing key thực tế.

Sau khi tạo, Google sẽ ghép cặp (package name, SHA-1) với OAuth client. Nếu bạn dùng nhiều build type/flavor với `applicationId` hoặc keystore khác nhau, hãy tạo thêm client tương ứng để tránh lỗi.

### Ví dụ: cách map JSON trả về sang các trường Android

Nếu Google Cloud trả về đoạn JSON như:

```json
{
  "installed": {
    "client_id": "748643708301-90fr170bgq8s0ienon6hg4cg9dnnui2b.apps.googleusercontent.com",
    "project_id": "certain-gearbox-476003-c7",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs"
  }
}
```

| Bạn cần điền ở đâu | Giá trị lấy từ JSON | Ghi chú |
| --- | --- | --- |
| Trường **Package name** trên Google Cloud | _(không có trong JSON)_ | Bạn vẫn phải tự nhập `applicationId` của app (ví dụ `com.example.kanjilearning`). |
| Trường **SHA-1 certificate fingerprint** | _(không có trong JSON)_ | Lấy từ `./gradlew signingReport` như bước 1 ở trên. |
| `default_web_client_id` trong `res/values/strings.xml` | `client_id` | Ví dụ:<br>`<string name="default_web_client_id">748643708301-90fr170bgq8s0ienon6hg4cg9dnnui2b.apps.googleusercontent.com</string>` |
| Logcat/debug khi kiểm tra project | `project_id` | Hữu ích nếu bạn cần xác nhận đang gọi đúng project Google Cloud. |
| Cấu hình thủ công (ít gặp) của OAuth flow | `auth_uri`, `token_uri`, `auth_provider_x509_cert_url` | Các giá trị mặc định; chỉ cần khi bạn viết flow OAuth tùy chỉnh. |

> 🔁 **Tự động điền trong Android Studio**: sau khi đặt `google-services.json` vào thư mục `app/` và sync Gradle, plugin Google Services sẽ tự phát sinh resource `@string/default_web_client_id`. Tuy nhiên nếu bạn muốn kiểm tra hoặc nhập thủ công, hãy dùng giá trị `client_id` từ JSON như bảng trên.

### Script tự động cập nhật cấu hình web

Đối với dự án web trong repo này, bạn có thể dùng script `scripts/apply_google_oauth.py` để đồng bộ thông tin từ file JSON tải về:

```bash
./scripts/apply_google_oauth.py path/to/client_secret.json \
  --redirect http://localhost:9999/MMO_Trader_Market/auth/google
```

Script sẽ cập nhật các khóa `google.clientId`, `google.clientSecret` và `google.redirectUri` trong `MMO_Trader_Market/src/conf/database.properties` (đồng thời bản copy trong `MMO_Trader_Market/conf/database.properties` nếu tồn tại). Trường nào không có trong JSON sẽ được giữ nguyên.

## 3. Đảm bảo package name và OAuth client khớp dữ liệu build

* Trên Google Cloud Console, trường **Package name** phải đúng với `applicationId` trong file `app/build.gradle`.
* Nếu bạn đổi `applicationId` giữa các flavor/buildType, tạo riêng mỗi OAuth client cho từng biến thể.

## 4. Cập nhật `google-services.json`

* Sau khi chỉnh sửa OAuth client, tải lại file `google-services.json` từ Firebase/Google Cloud và thay thế bản cũ trong thư mục `app/`.
* Kiểm tra xem Gradle đã đồng bộ lại (Sync) để plugin `com.google.gms.google-services` lấy cấu hình mới.

## 5. Bật Google Sign-In API

* Tại mục **APIs & Services → Enabled APIs & services**, xác nhận "Google Sign-In API" hoặc "Google Identity Services" đã được bật cho project.
* Nếu mới bật, đợi vài phút để thay đổi có hiệu lực rồi thử đăng nhập lại.

## 6. Xóa cache ứng dụng trên thiết bị/emulator

* Vào **Settings → Apps → [Tên app] → Storage** và chọn **Clear data**.
* Hoặc gỡ cài đặt ứng dụng và cài lại bản debug mới build.

## 7. Kiểm tra đồng bộ hóa thời gian hệ thống

Thiết bị có thời gian lệch quá nhiều so với máy chủ Google cũng có thể gây lỗi xác thực. Đảm bảo thời gian và múi giờ trên thiết bị/emulator được đặt chính xác.

---

Sau khi hoàn tất các bước trên, build lại ứng dụng (`./gradlew assembleDebug`), triển khai và thử đăng nhập. Nếu lỗi vẫn xảy ra, bật logging chi tiết bằng cách bắt `ApiException` trong `onActivityResult`/`ActivityResultCallback` để in ra `statusCode` và thông tin bổ sung từ `status.statusMessage` nhằm tiếp tục điều tra.
