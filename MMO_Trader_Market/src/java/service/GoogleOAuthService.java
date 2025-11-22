package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import conf.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class GoogleOAuthService {

    // URL ủy quyền của Google.
    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    // API trao đổi mã lấy access token.
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    // API lấy thông tin người dùng.
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";
    // HTTP client chuẩn để gọi API Google.
    private final HttpClient httpClient = HttpClient.newHttpClient();
    // Gson phục vụ parse JSON phản hồi.
    private final Gson gson = new Gson();
    // tạo ra URL cho bước login Google
    public String buildAuthorizationUrl(String state) {
        String clientId = requireConfig("google.clientId"); // Lấy clientId từ file cấu hình (Google cấp  trên Google Cloud Console)
        String redirectUri = requireConfig("google.redirectUri"); //Lấy redirectUri từ config. (địa chỉ callback) 
//        Tạo chuỗi scope: "openid email profile".
//
//openid: kích hoạt OpenID Connect → cho phép nhận ID Token (chứa thông tin user).
//
//email: xin quyền truy cập email của user.
//
//profile: xin quyền truy cập thông tin cơ bản (tên, avatar,…).
        String scope = urlEncode("openid email profile");
        return AUTH_ENDPOINT //ghép đủ query string:
                + "?response_type=code"
                + "&client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&scope=" + scope
                + "&state=" + urlEncode(state)
                + "&prompt=select_account";
    } //Ghép URL đầy đủ tới Authorization Endpoint của Google

    // code → lấy token → gọi Google lấy thông tin user → trả về profile.
    public GoogleProfile fetchUserProfile(String code) {
        JsonObject tokenResponse = exchangeCodeForTokens(code); //Đổi mã code → lấy access token
        String accessToken = getRequiredField(tokenResponse, "access_token"); //Lấy access_token từ response
        JsonObject userInfo = requestUserInfo(accessToken); //Gọi Google UserInfo Endpoint
        return mapProfile(userInfo); //Map dữ liệu Google → đối tượng GoogleProfile
    }

    // Gửi yêu cầu POST với body x-www-form-urlencoded.
    private JsonObject sendPost(String endpoint, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Không thể trao đổi mã xác thực với Google");
            }
            return gson.fromJson(response.body(), JsonObject.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Không thể kết nối tới Google", e);
        } catch (IOException e) {
            throw new RuntimeException("Không thể kết nối tới Google", e);
        }
    }

    // Trao đổi code sang token truy cập và refresh token.
    private JsonObject exchangeCodeForTokens(String code) {
        String clientId = requireConfig("google.clientId");
        String clientSecret = requireConfig("google.clientSecret");
        String redirectUri = requireConfig("google.redirectUri");
        String body = "code=" + urlEncode(code)
                + "&client_id=" + urlEncode(clientId)
                + "&client_secret=" + urlEncode(clientSecret)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&grant_type=authorization_code";
        return sendPost(TOKEN_ENDPOINT, body);
    }

    // Lấy thông tin hồ sơ người dùng bằng access token.
    private JsonObject requestUserInfo(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERINFO_ENDPOINT))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Không thể lấy thông tin người dùng Google");
            }
            return gson.fromJson(response.body(), JsonObject.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Không thể kết nối tới Google để lấy thông tin người dùng", e);
        } catch (IOException e) {
            throw new RuntimeException("Không thể kết nối tới Google để lấy thông tin người dùng", e);
        }
    }

    // Chuyển đổi JSON userinfo thành đối tượng GoogleProfile.
    private GoogleProfile mapProfile(JsonObject payload) {
        return new GoogleProfile(
                getRequiredField(payload, "sub"),
                getRequiredField(payload, "email"),
                payload.has("name") ? payload.get("name").getAsString() : ""
        );
    }

    // Đọc cấu hình bắt buộc và báo lỗi nếu thiếu.
    private String requireConfig(String key) {
        String value = AppConfig.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình giá trị " + key);
        }
        return value;
    }

    // Mã hóa tham số URL với UTF-8.
    private String urlEncode(String value) {
        return URLEncoder.encode(Objects.toString(value, ""), StandardCharsets.UTF_8);
    }

    public static final class GoogleProfile {

        // ID Google duy nhất của người dùng.
        private final String googleId;
        // Email Google.
        private final String email;
        // Tên đầy đủ từ hồ sơ Google.
        private final String name;

        public GoogleProfile(String googleId, String email, String name) {
            this.googleId = googleId;
            this.email = email;
            this.name = name;
        }

        // Lấy Google ID.
        public String getGoogleId() {
            return googleId;
        }

        // Lấy email.
        public String getEmail() {
            return email;
        }

        // Lấy tên hiển thị.
        public String getName() {
            return name;
        }
    }

    // Lấy trường bắt buộc từ JSON, ném lỗi nếu vắng mặt.
    private String getRequiredField(JsonObject payload, String key) {
        if (payload == null || !payload.has(key)) {
            throw new IllegalStateException("Google không trả về trường " + key);
        }
        return payload.get(key).getAsString();
    }
}
