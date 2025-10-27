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

    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String buildAuthorizationUrl(String state) {
        String clientId = requireConfig("google.clientId");
        String redirectUri = requireConfig("google.redirectUri");
        String scope = urlEncode("openid email profile");
        return AUTH_ENDPOINT
                + "?response_type=code"
                + "&client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&scope=" + scope
                + "&state=" + urlEncode(state)
                + "&prompt=select_account";
    }

    public GoogleProfile fetchUserProfile(String code) {
        JsonObject tokenResponse = exchangeCodeForTokens(code);
        String accessToken = getRequiredField(tokenResponse, "access_token");
        JsonObject userInfo = requestUserInfo(accessToken);
        return mapProfile(userInfo);
    }

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

    private GoogleProfile mapProfile(JsonObject payload) {
        return new GoogleProfile(
                getRequiredField(payload, "sub"),
                getRequiredField(payload, "email"),
                payload.has("name") ? payload.get("name").getAsString() : ""
        );
    }

    private String requireConfig(String key) {
        String value = AppConfig.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình giá trị " + key);
        }
        return value;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(Objects.toString(value, ""), StandardCharsets.UTF_8);
    }

    public static final class GoogleProfile {

        private final String googleId;
        private final String email;
        private final String name;

        public GoogleProfile(String googleId, String email, String name) {
            this.googleId = googleId;
            this.email = email;
            this.name = name;
        }

        public String getGoogleId() {
            return googleId;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }

    private String getRequiredField(JsonObject payload, String key) {
        if (payload == null || !payload.has(key)) {
            throw new IllegalStateException("Google không trả về trường " + key);
        }
        return payload.get(key).getAsString();
    }
}
