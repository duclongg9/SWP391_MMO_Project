/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.user.UserDAO;
import java.sql.SQLException;
import model.User;
import units.HashPassword;
import units.SendMail;

/**
 *
 * @author D E L L
 */
public class UserService {

    private final UserDAO udao;

    public UserService(UserDAO udao) {
        this.udao = udao;
    }

    /*Xem thông tin cá nhân của mình*/
    public User viewMyProfile(int id) throws SQLException {
        try {
            User user = udao.getUserByUserId(id);
            if (user == null) {
                throw new IllegalArgumentException("Tài khoản của bạn không tồn tại hoặc đã bị khóa");
            }
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi xem profile", e);
        }
    }

    /*Cập nhật thông tin cá nhân*/
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

    /*Cập nhật mật khẩu mới*/
    public int updatePassword(int id, String oldPassword, String newPassword) {
        //Gọi thông tin liên quan
        UserDAO udao = new UserDAO();

        //Validate input
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
            //Lấy user + hash hiện tại
            User user = udao.getUserByUserId(id);
            if (user == null) {
                throw new IllegalArgumentException("Tài khoản không tồn tại hoặc đã bị khóa");
            }

            String currentHash = user.getHashPassword();
            if (currentHash == null || currentHash.isBlank()) {
                throw new IllegalStateException("Tài khoản chưa thiết lập mật khẩu");
            }

            //So khớp mật khẩu cũ
            if (currentHash.equals(HashPassword.toSHA1(oldPassword))) {
                throw new IllegalArgumentException("Mật khẩu cũ không đúng");
            }

            //Băm mật khẩu mới và cập nhật
            String newHash = HashPassword.toSHA1(newPassword);
            int updated = udao.updateUserPassword(id, newHash);
            if (updated < 1) {
                throw new IllegalStateException("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            }

            //Gửi email
            if (updated > 0) {
                String subject = "[Thông báo quan trọng]-Thông tin tài khoản của bạn";
                String messageText = "Chào " + 1 + ",\n\n"
                        + "Bạn đã thay đổi mật khẩu thành công \n\n"
                        + "Trân trọng,\nAdmin Material Management";

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
