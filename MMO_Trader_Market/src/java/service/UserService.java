package service;

import dao.user.EmailVerificationTokenDAO;
import conf.AppConfig;
import dao.user.PasswordResetTokenDAO;
import dao.user.UserDAO;
import dao.user.WalletsDAO;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import model.EmailVerificationToken;
import model.PasswordResetToken;
import model.Users;
import units.HashPassword;
import units.AsyncEmailSender;
import units.SendMail;

import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import utils.ImageUtils;

public class UserService {

    private static final String RELATIVE_UPLOAD_DIR = "\\assets\\images\\avata";
    private static final String ABSOLUTE_UPLOAD_DIR = "D:\\DH_FPT\\Ky_7\\SWP391_MMO_Project\\MMO_Trader_Market\\web\\assets\\images\\avata";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"); //khai báo hằng số kiểm tra mail
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[A-Za-z])(?=.*\\d).{8,}");
    private static final int DEFAULT_ROLE_ID = 3;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 1440;
    private static final int VERIFICATION_TOKEN_EXPIRY_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserDAO userDAO;
    private final PasswordResetTokenDAO passwordResetTokenDAO;
    private final EmailVerificationTokenDAO emailVerificationTokenDAO;
    private final WalletsDAO wdao = new WalletsDAO();

    public UserService(UserDAO userDAO) {
        this(userDAO, new PasswordResetTokenDAO(), new EmailVerificationTokenDAO());
    }

    public UserService(UserDAO userDAO, PasswordResetTokenDAO passwordResetTokenDAO) {
        this(userDAO, passwordResetTokenDAO, new EmailVerificationTokenDAO());
    }

    public UserService(UserDAO userDAO, PasswordResetTokenDAO passwordResetTokenDAO,
            EmailVerificationTokenDAO emailVerificationTokenDAO) {
        this.userDAO = userDAO;
        this.passwordResetTokenDAO = passwordResetTokenDAO;
        this.emailVerificationTokenDAO = emailVerificationTokenDAO;
    }

    /**
     * Đăng ký tài khoản mới
     */
    public Users registerNewUser(String email, String name, String password, String confirmPassword) {
        String normalizedEmail = normalizeEmail(email);

        validateEmail(normalizedEmail);

        String normalizedName = requireText(name, "Vui lòng nhập tên hiển thị");
        String rawPassword = requireText(password, "Vui lòng nhập mật khẩu");
        validatePassword(rawPassword);
        ensurePasswordMatch(rawPassword, confirmPassword); // bảo đảm ===
        

        try {
            ensureEmailAvailable(normalizedEmail); // check mail trùng
            String hashedPassword = HashPassword.toSHA1(rawPassword);
            Users created = userDAO.createUser(normalizedEmail, normalizedName, hashedPassword, DEFAULT_ROLE_ID,
                    2);
//            int result = wdao.createWallet(created.getId());
            if (created == null) {
                throw new IllegalStateException("Không thể tạo tài khoản mới.");
            }
//            if(result<1){
//                throw new IllegalStateException("Tạo tài khoản thành công nhưng tạo ví thất bại");
//            }
            String verificationCode = createAndStoreVerificationCode(created.getId()); //Tạo mã xác thực email duy nhất
            sendVerificationEmail(created.getEmail(), created.getName(), verificationCode); // gửi mail kèm code 
            return created;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi tạo tài khoản mới", e);
        } 
    }

    public Users authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        String rawPassword = requireText(password, "Vui lòng nhập mật khẩu");

        Users user = userDAO.getUserByEmailAnyStatus(normalizedEmail);
        if (user == null) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        String hashed = user.getHashedPassword();
        if (hashed == null || hashed.isBlank()) {
            throw new IllegalStateException("Tài khoản được tạo bằng Google. Vui lòng đăng nhập bằng Google");
        }
        if (!hashed.equals(HashPassword.toSHA1(rawPassword))) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        if ((user.getStatus()) == 2) { //Kiểm tra trạng thái tài khoản – chưa xác thực email
            if (isEmailVerificationPending(user)) {
                throw new InactiveAccountException("Tài khoản của bạn chưa được xác thực email", user);
            }
        }
        
        if((user.getStatus()) == 0){
            throw new IllegalStateException("Tài khoản của bạn đang bị khóa");
        }
        return user;
    }   
// đăng nhập = gg
    public Users loginWithGoogle(String googleId, String email, String displayName) {
        String normalizedGoogleId = requireText(googleId, "Google ID không hợp lệ"); // ép ggid k null, rỗng-> ném lỗi
        String normalizedEmail = normalizeEmail(email); // chuẩn hóa
        validateEmail(normalizedEmail);
        String normalizedName = displayName == null || displayName.isBlank()
                ? normalizedEmail
                : displayName.trim();

        try {
            Users existingGoogleUser = userDAO.getUserByGoogleId(normalizedGoogleId); //Tra cứu theo googleId
            if (existingGoogleUser != null) {
                return existingGoogleUser;
            }
            Users linkedUser = linkGoogleAccount(normalizedEmail, normalizedGoogleId); // ch có-> liên kết vào user sẵn có theo email
            if (linkedUser != null) {
                return linkedUser;
            }
            return createGoogleAccount(normalizedEmail, normalizedName, normalizedGoogleId);
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi xử lý Google SSO", e);
        }
    }

// yêu cầu đặt lại mk
    public boolean requestPasswordReset(String email, String resetBaseUrl) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        if (resetBaseUrl == null || resetBaseUrl.isBlank()) {
            throw new IllegalArgumentException("Thiếu đường dẫn reset mật khẩu");
        }

        try {
            Users user = userDAO.getUserByEmail(normalizedEmail); // lấy ng dùng theo email
            if (user == null) {
                throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
            }
            //tìm token đặt lại mật khẩu gần nhất của 1 user, mà chưa được sử dụng
            PasswordResetToken existing = passwordResetTokenDAO.findLatestActiveByUser(user.getId()); 
            if (existing != null && existing.getExpiresAt() != null
                    && existing.getExpiresAt().toInstant().isAfter(Instant.now())) { //token vẫn còn hạn và token chưa dùng bao giờ 
                return false; // tránh spam
            }
            String token = UUID.randomUUID().toString().replace("-", "");
            Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(RESET_TOKEN_EXPIRY_MINUTES * 60L)); //hời điểm hết hạn
            passwordResetTokenDAO.createToken(user.getId(), token, expiresAt);
            String resetLink = resetBaseUrl + "?token=" + token; //Tạo URL đầy đủ, bấm trong email.
            sendResetMail(user.getEmail(), user.getName(), resetLink);
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        }
    }
// gửi mã
    public void resendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        try {
            Users user = userDAO.getUserByEmailAnyStatus(normalizedEmail);
            if (user == null) {
                throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
            }
            String code = createAndStoreVerificationCode(user.getId()); //Tạo mã xác thực mới + lưu DB
            sendVerificationEmail(user.getEmail(), user.getName(), code); // gửi về mail user
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi gửi lại mã xác thực", e);
        }
    }
// xác thực token
    public boolean verifyEmailCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        String normalizedCode = requireText(code, "Vui lòng nhập mã xác thực"); //đảm bảo người dùng có nhập mã; nếu code rỗng/null thì ném lỗi với message này. Sau đó gán vào normalizedCode
        try {
            Users user = userDAO.getUserByEmailAnyStatus(normalizedEmail);
            if (user == null) {
                throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
            }

            EmailVerificationToken token = emailVerificationTokenDAO.findTokenByUserId(user.getId()); //Lấy bản ghi token theo userid
            if (token == null || token.getCode() == null || token.getCode().isBlank()) {
                throw new IllegalStateException("Tài khoản này không có mã xác thực hợp lệ");
            }
            if (token.getCreatedAt() == null || token.getCreatedAt().toInstant()
                    .plusSeconds(VERIFICATION_TOKEN_EXPIRY_MINUTES * 60L) //Thời điểm mã này sẽ hết hạn = lúc tạo + số phút cho phép
                    .isBefore(Instant.now())) { // Thời điểm hết hạn có trước thời điểm hiện tại hay không
                throw new IllegalArgumentException("Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.");
            }
            if (!token.getCode().equals(normalizedCode)) {
                throw new IllegalArgumentException("Mã xác thực không chính xác hoặc đã được thay thế");
            }
            int updated = userDAO.activateUser(user.getId()); //kích hoạt tài khoản
//            if (updated == 1) {
//                throw new IllegalStateException("Không thể kích hoạt tài khoản lúc này. Vui lòng thử lại sau");
//            }
            emailVerificationTokenDAO.deleteByUserId(user.getId()); // xóa userid
            int result = wdao.createWallet(user.getId());
             if(result<1){
                throw new IllegalStateException("Tạo tài khoản thành công nhưng tạo ví thất bại");
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi xác thực email", e);
        }
    }

    /**
     * Đặt lại mật khẩu dựa trên token hợp lệ và trả về thông tin người dùng
     * phục vụ bước đăng nhập tự động.
   
     */
    public Users resetPassword(String token, String newPassword, String confirmPassword) {
        String normalizedToken = requireText(token, "Token đặt lại mật khẩu không hợp lệ");
        String normalizedPassword = requireText(newPassword, "Vui lòng nhập mật khẩu mới");
        validatePassword(normalizedPassword);
        ensurePasswordMatch(normalizedPassword, confirmPassword);

        try {
            PasswordResetToken resetToken = passwordResetTokenDAO.findActiveToken(normalizedToken);
            if (resetToken == null || resetToken.getExpiresAt() == null // check token còn hạn không, đã dùng chưa
                    || resetToken.getExpiresAt().toInstant().isBefore(Instant.now())) {
                throw new IllegalArgumentException("Link đặt lại mật khẩu đã hết hạn hoặc không hợp lệ");
            }

            Users user = userDAO.getUserByUserId(resetToken.getUserId());
            if (user == null) {
                throw new IllegalStateException("Tài khoản không tồn tại hoặc đã bị khóa");
            }
            String hashed = HashPassword.toSHA1(normalizedPassword); // băm mk
            int updated = userDAO.updateUserPassword(resetToken.getUserId(), hashed);
            if (updated < 1) {
                throw new IllegalStateException("Không thể cập nhật mật khẩu. Vui lòng thử lại");
            }
            passwordResetTokenDAO.markUsed(resetToken.getId()); //Đánh dấu token đã dùng để không thể dùng lại
            user.setHashedPassword(hashed); // set mk băm vào object 
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đặt lại mật khẩu lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Xem thông tin cá nhân
     */
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

    /**
     * Cập nhật tên hiển thị
     */
    public int updateMyProfile(int id, String name, Part avatar) throws IOException, SQLException {
        Users current = userDAO.getUserByUserId(id);
        boolean changed = false;

        //người dùng có update tên
        String finalName = current.getName();
        if (name != null && !name.trim().isEmpty()) {

            name = name.trim().replaceAll("\\s{2,}", " "); // xoá khoảng trắng thừa

            if (!name.matches("^[\\p{L}]+(?: [\\p{L}]+)*$")) {
                throw new IOException("Tên chỉ được chứa chữ ,khoảng trắng, không chứa ký tự đặc biệt.");
            }
            finalName = name;
            changed = true;
        }
        
        //Người dùng nhập ảnh
        String avatarRel = current.getAvatarUrl(); // giữ ảnh cũ mặc định
        Path avatarPath = null;
        if (avatar != null && avatar.getSize() > 0) {
            // Validate MIME
            if (!ImageUtils.isAllowedImage(avatar.getContentType())) {
                throw new IOException("Chỉ chấp nhận ảnh JPG, PNG, hoặc WebP.");
            }

            Path uploadDir = Paths.get(ABSOLUTE_UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            // Tạo tên file duy nhất
            String avatarFileName = "avata_" + UUID.randomUUID() + ImageUtils.extFromMime(avatar.getContentType());

            avatarPath = uploadDir.resolve(avatarFileName);

            // Ghi file
            try {
                avatar.write(avatarPath.toString());
            } catch (Exception e) {
                throw new IOException("Không thể lưu file ảnh lên máy chủ.", e);
            }

            // Lưu DB
             avatarRel = RELATIVE_UPLOAD_DIR + "/" + avatarFileName;
             changed = true;
        }
        //Gắn cờ, nếu không có gì thay đổi thì không update DB
        if(!changed){
            return 0;
        }
        
        int rows = userDAO.updateUserProfileBasic(id, finalName, avatarRel);
        if (rows <= 0) {
            Files.deleteIfExists(avatarPath);
            throw new IOException("Cập nhật thông tin người dùng thất ");
        }

        return rows;
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
                String messageText = "Chào [" + user.getName() + "] ,\n\n"
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
// trim khoảng trắng, chuyển về lowercase, nếu null thì trả null
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
// password không được null/rỗng/toàn khoảng trắng.
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


    private void sendResetMail(String email, String name, String resetLink) {
        String subject = "Đặt lại mật khẩu MMO Trader Market";
        String displayName = resolveDisplayName(email, name);
        String body = "Xin chào " + displayName + ",\n\n"
                + "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản MMO Trader Market. "
                + "Vui lòng nhấn vào liên kết bên dưới trong vòng " 
                + " 24 giờ:\n" + resetLink + "\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.\n\n"
                + "Trân trọng,\nĐội ngũ MMO Trader Market";
        AsyncEmailSender.send(email, subject, body);
    }

    private void sendVerificationEmail(String email, String name, String code) {
        String subject = "Xác thực tài khoản MMO Trader Market";
        String displayName = resolveDisplayName(email, name);
        String body = "Xin chào " + displayName + ",\n\n"
                + "Cảm ơn bạn đã đăng ký tài khoản tại MMO Trader Market. "
                + "Mã xác thực email của bạn là: " + code + "\n\n"
                + "Vui lòng nhập mã này trên trang đăng nhập để kích hoạt tài khoản trong vòng "
                + VERIFICATION_TOKEN_EXPIRY_MINUTES + " phút. Mỗi lần yêu cầu lại, hệ thống sẽ tạo mã mới.\n\n"
                + "Nếu bạn không yêu cầu tạo tài khoản, hãy bỏ qua email này.\n\n"
                + "Trân trọng,\nĐội ngũ MMO Trader Market";
        AsyncEmailSender.send(email, subject, body);
    }

    private String resolveDisplayName(String email, String name) {
        return name == null || name.isBlank() ? email : name;
    }

    private boolean isEmailVerificationPending(Users user) {
        if (user == null || user.getId() == null ||user.getStatus()==1){
            return false;
        }
        try {
            return emailVerificationTokenDAO.hasToken(user.getId()); //Nếu có token → trả về true → đang chờ xác thực email.
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi kiểm tra mã xác thực email", e);
        }
    }

    private String createAndStoreVerificationCode(int userId) throws SQLException {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = generateVerificationCode(); //// 1) Sinh mã ngẫu nhiên (OTP/token)
            try {
                emailVerificationTokenDAO.upsertToken(userId, code);
                return code; // // 3) Thành công → trả mã
            } catch (SQLException e) {
                if (isDuplicateCode(e) && attempt < 4) {
                    continue; //// 4) Nếu đụng UNIQUE (mã bị trùng) → thử lại
                }
                throw e;
            }
        }
        throw new SQLException("Không thể tạo mã xác thực email duy nhất");
    }

    private boolean isDuplicateCode(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return true;
        }
        String sqlState = e.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
    }

    private String generateVerificationCode() {
        int code = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
    private Users linkGoogleAccount(String email, String googleId) throws SQLException {
        Users existingEmailUser = userDAO.getUserByEmail(email); //// user theo email đã có trong hệ thống
        if (existingEmailUser == null) {
            return null;
        }
        userDAO.updateGoogleId(existingEmailUser.getId(), googleId); //cập nhật googleId vào DB
        existingEmailUser.setGoogleId(googleId); //cập nhật lại giá trị trên object đang dùng,
        return existingEmailUser;
    }

    /**
     * Khởi tạo người dùng mới thông qua Google SSO và bảo đảm có ví khả dụng.
     */
    private Users createGoogleAccount(String email, String name, String googleId) throws SQLException {
        ensureEmailAvailable(email);
        Users created = userDAO.createUserWithGoogle(email, name, googleId, DEFAULT_ROLE_ID);
        if (created == null) {
            throw new IllegalStateException("Không thể tạo tài khoản Google mới");
        }
        int createdWallet = wdao.createWallet(created.getId());
        if (createdWallet < 1) {
            /*
             * Đảm bảo tài khoản Google mới có ví hoạt động ngay sau khi khởi tạo.
             * Nếu thao tác này thất bại, trả lỗi để caller có thể hiển thị thông báo phù hợp.
             */
            throw new IllegalStateException("Tạo tài khoản thành công nhưng tạo ví thất bại");
        }
        return created;
    }
}
