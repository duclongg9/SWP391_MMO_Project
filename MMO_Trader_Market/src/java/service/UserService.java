package service;

import dao.user.PasswordResetTokenDAO;
import dao.user.UserDAO;
import model.PasswordResetToken;
import model.Users;
import units.HashPassword;
import units.SendMail;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[A-Za-z])(?=.*\\d).{8,}");
    private static final int DEFAULT_ROLE_ID = 3;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserDAO userDAO;
    private final PasswordResetTokenDAO passwordResetTokenDAO;

    public UserService(UserDAO userDAO) {
        this(userDAO, new PasswordResetTokenDAO());
    }

    public UserService(UserDAO userDAO, PasswordResetTokenDAO passwordResetTokenDAO) {
        this.userDAO = userDAO;
        this.passwordResetTokenDAO = passwordResetTokenDAO;
    }

    /** Đăng ký tài khoản mới */
    public Users registerNewUser(String email, String name, String password, String confirmPassword) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);

        String normalizedName = requireText(name, "Vui lòng nhập tên hiển thị");
        String rawPassword = requireText(password, "Vui lòng nhập mật khẩu");
        validatePassword(rawPassword);
        ensurePasswordMatch(rawPassword, confirmPassword);

        try {
            ensureEmailAvailable(normalizedEmail);
            String hashedPassword = HashPassword.toSHA1(rawPassword);
            Users created = userDAO.createUser(normalizedEmail, normalizedName, hashedPassword, DEFAULT_ROLE_ID);
            if (created == null) {
                throw new IllegalStateException("Không thể tạo tài khoản mới.");
            }
            return created;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi tạo tài khoản mới", e);
        }
    }

    public Users authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        String rawPassword = requireText(password, "Vui lòng nhập mật khẩu");

        Users user = userDAO.getUserByEmail(normalizedEmail);
        if (user == null) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản của bạn đang bị khóa");
        }
        String hashed = user.getHashedPassword();
        if (hashed == null || hashed.isBlank()) {
            throw new IllegalStateException("Tài khoản được tạo bằng Google. Vui lòng đăng nhập bằng Google");
        }
        if (!hashed.equals(HashPassword.toSHA1(rawPassword))) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        return user;
    }

    public Users loginWithGoogle(String googleId, String email, String displayName) {
        String normalizedGoogleId = requireText(googleId, "Google ID không hợp lệ");
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        String normalizedName = displayName == null || displayName.isBlank()
                ? normalizedEmail
                : displayName.trim();

        try {
            Users existingGoogleUser = userDAO.getUserByGoogleId(normalizedGoogleId);
            if (existingGoogleUser != null) {
                return existingGoogleUser;
            }
            Users linkedUser = linkGoogleAccount(normalizedEmail, normalizedGoogleId);
            if (linkedUser != null) {
                return linkedUser;
            }
            return createGoogleAccount(normalizedEmail, normalizedName, normalizedGoogleId);
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi xử lý Google SSO", e);
        }
    }

    public void requestPasswordReset(String email, String resetBaseUrl) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        if (resetBaseUrl == null || resetBaseUrl.isBlank()) {
            throw new IllegalArgumentException("Thiếu đường dẫn reset mật khẩu");
        }

        try {
            Users user = userDAO.getUserByEmail(normalizedEmail);
            if (user == null) {
                throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
            }

            String token = UUID.randomUUID().toString().replace("-", "");
            Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(RESET_TOKEN_EXPIRY_MINUTES * 60L));
            passwordResetTokenDAO.createToken(user.getId(), token, expiresAt);
            String resetLink = resetBaseUrl + "?token=" + token;
            sendResetMail(user.getEmail(), user.getName(), resetLink);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        }
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        String normalizedToken = requireText(token, "Token đặt lại mật khẩu không hợp lệ");
        String normalizedPassword = requireText(newPassword, "Vui lòng nhập mật khẩu mới");
        validatePassword(normalizedPassword);
        ensurePasswordMatch(normalizedPassword, confirmPassword);

        try {
            PasswordResetToken resetToken = passwordResetTokenDAO.findActiveToken(normalizedToken);
            if (resetToken == null || resetToken.getExpiresAt() == null
                    || resetToken.getExpiresAt().toInstant().isBefore(Instant.now())) {
                throw new IllegalArgumentException("Link đặt lại mật khẩu đã hết hạn hoặc không hợp lệ");
            }

            String hashed = HashPassword.toSHA1(normalizedPassword);
            int updated = userDAO.updateUserPassword(resetToken.getUserId(), hashed);
            if (updated < 1) {
                throw new IllegalStateException("Không thể cập nhật mật khẩu. Vui lòng thử lại");
            }
            passwordResetTokenDAO.markUsed(resetToken.getId());
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đặt lại mật khẩu lúc này. Vui lòng thử lại sau.", e);
        }
    }
    
    /** Xem thông tin cá nhân */
    public Users viewMyProfile(int id) {
        try {
            Users user = userDAO.getUserByUserId(id);   // <-- sửa: không phải (int id)
            if (user == null) {
                throw new IllegalArgumentException("Tài khoản của bạn không tồn tại hoặc đã bị khóa");
            }
            return user;
        } catch (Exception e) {
            // DAO của bạn đã bắt SQLException trong getUserByUserId; để an toàn vẫn wrap mọi lỗi
            throw new RuntimeException("DB gặp sự cố khi xem profile", e);
        }
    }

    /** Cập nhật tên hiển thị */
    public int updateMyProfile(int id, String name) {
        try {
            int updated = userDAO.updateUserProfileBasic(id, name);
            if (updated < 1) {
                throw new IllegalArgumentException("Tài khoản của bạn không tồn tại hoặc đã bị khóa");
            }
            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi cập nhật dữ liệu người dùng", e);
        }
    }

    public int updatePassword(int id, String oldPassword, String newPassword) {
        // 1) Validate input
        //Gọi thông tin liên quan
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cũ");
        }
        if (newPassword == null || !newPassword.matches("(?=.*[A-Za-z])(?=.*\\d).{8,}")) {
            throw new IllegalArgumentException("Mật khẩu mới phải ≥ 8 ký tự và có cả chữ lẫn số");
        }
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        try {
            Users user = userDAO.getUserByUserId(id);
            if (user == null) {
                throw new IllegalArgumentException("Tài khoản không tồn tại hoặc đã bị khóa");
            }

            String currentHash = user.getHashedPassword(); // <-- sửa: đúng tên getter theo model
            if (currentHash == null || currentHash.isBlank()) {
                throw new IllegalStateException("Tài khoản chưa thiết lập mật khẩu");
            }

            // 3) So khớp mật khẩu cũ (sửa điều kiện: phải KHÔNG trùng mới báo sai)
            if (!currentHash.equals(HashPassword.toSHA1(oldPassword))) {
                throw new IllegalArgumentException("Mật khẩu cũ không đúng");
            }

            // 4) Hash mật khẩu mới và cập nhật
            String newHash = HashPassword.toSHA1(newPassword);
            int updated = userDAO.updateUserPassword(id, newHash);
            if (updated < 1) {
                throw new IllegalStateException("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            }

            //Gửi email
            if (updated > 0) {
                String subject = "[Thông báo quan trọng]-Thông tin tài khoản của bạn";
                String messageText = "Chào Kiệt,\n\n"
                        + "Bạn đã thay đổi mật khẩu thành công trên hệ thống sàn thương mại điện tử MMO Trader System \n\n"
                        + "Vui lòng kiểm tra lại tài khoản nếu bạn không thực hiện hành động này\n\n"
                        + "Trân trọng,\nAdmin MMO Trader System"; // đang test fig cứng tên

                try {
                    String userEmail = userDAO.getUserByUserId(id).getEmail();
                    SendMail.sendMail(userEmail, subject, messageText);
                } catch (Exception e) {
                    e.printStackTrace(); // log lỗi gửi mail (không làm hỏng luồng chính)
                }

            }

            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi cập nhật mật khẩu người dùng", e);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void validateEmail(String email) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Mật khẩu phải ≥ 8 ký tự và bao gồm cả chữ và số");
        }
    }

    private void ensurePasswordMatch(String password, String confirmPassword) {
        String confirm = confirmPassword == null ? "" : confirmPassword.trim();
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp");
        }
    }

    private void ensureEmailAvailable(String email) throws SQLException {
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
    }

    private String generateRandomSecret() {
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return UUID.nameUUIDFromBytes(random).toString();
    }

    private void sendResetMail(String email, String name, String resetLink) {
        String subject = "Đặt lại mật khẩu MMO Trader Market";
        String displayName = name == null || name.isBlank() ? email : name;
        String body = "Xin chào " + displayName + ",\n\n" +
                "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản MMO Trader Market. " +
                "Vui lòng nhấn vào liên kết bên dưới trong vòng " + RESET_TOKEN_EXPIRY_MINUTES +
                " phút:\n" + resetLink + "\n\n" +
                "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.\n\n" +
                "Trân trọng,\nĐội ngũ MMO Trader Market";
        try {
            SendMail.sendMail(email, subject, body);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        }
    }

    private Users linkGoogleAccount(String email, String googleId) throws SQLException {
        Users existingEmailUser = userDAO.getUserByEmail(email);
        if (existingEmailUser == null) {
            return null;
        }
        userDAO.updateGoogleId(existingEmailUser.getId(), googleId);
        existingEmailUser.setGoogleId(googleId);
        return existingEmailUser;
    }

    private Users createGoogleAccount(String email, String name, String googleId) throws SQLException {
        ensureEmailAvailable(email);
        String fallbackHash = HashPassword.toSHA1(generateRandomSecret());
        Users created = userDAO.createUserWithGoogle(email, name, googleId, fallbackHash, DEFAULT_ROLE_ID);
        if (created == null) {
            throw new IllegalStateException("Không thể tạo tài khoản Google mới");
        }
        return created;
    }
}
