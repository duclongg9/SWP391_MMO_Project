package service;

import dao.user.UserDAO;
import model.Users;
import units.HashPassword;

import java.sql.SQLException;

public class UserService {

    private final UserDAO udao;

    public UserService(UserDAO udao) {
        this.udao = udao;
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

            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi cập nhật mật khẩu người dùng", e);
        }
    }
}
