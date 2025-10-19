package service;

import dao.user.UserDAO;
import model.Users;
import units.HashPassword;
import units.SendMail;

import java.sql.SQLException;
import java.util.regex.Pattern;
public class UserService {

    private final UserDAO udao;

    public UserService(UserDAO udao) {
        this.udao = udao;
    }
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[A-Za-z])(?=.*\\d).{8,}");
    private static final int DEFAULT_ROLE_ID = 2;

    /** Đăng ký tài khoản mới */
    public Users registerNewUser(String email, String name, String password, String confirmPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }
        email = email.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên hiển thị");
        }
        name = name.trim();

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Mật khẩu phải ≥ 8 ký tự và bao gồm cả chữ và số");
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp");
        }

        try {
            if (udao.emailExists(email)) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }

            String hashedPassword = HashPassword.toSHA1(password);
            Users created = udao.createUser(email, name, hashedPassword, DEFAULT_ROLE_ID);
            if (created == null) {
                throw new IllegalStateException("Không thể tạo tài khoản mới.");
            }
            return created;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi tạo tài khoản mới", e);
        }
    }
    
    /** Xem thông tin cá nhân */
    public Users viewMyProfile(int id) {
        try {
            Users user = udao.getUserByUserId(id);   // <-- sửa: không phải (int id)
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
            int updated = udao.updateUserProfileBasic(id, name);
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
        UserDAO udao = new UserDAO();
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
            Users user = udao.getUserByUserId(id);
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
            int updated = udao.updateUserPassword(id, newHash);
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
                    String userEmail = udao.getUserByUserId(id).getEmail();
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
}
