/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.user.UserDAO;
import java.sql.SQLException;
import model.User;

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
    public int updatePassword(int id, String hashedPassword) {
        try {
            int updated = udao.updateUserPassword(id, hashedPassword);
            if (updated < 1) {
                throw new IllegalArgumentException("Tài khoản của bạn không tồn tại hoặc đã bị khóa");
            }
            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("DB gặp sự cố khi cập nhật mật khẩu người dùng", e);
        }
    }
}
