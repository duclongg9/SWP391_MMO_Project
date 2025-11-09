package service;

import model.Users;

/**
 * Ngoại lệ biểu thị tài khoản tồn tại nhưng đang ở trạng thái chưa kích hoạt.
 */
public class InactiveAccountException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final Users user;

    public InactiveAccountException(String message, Users user) {
        super(message);
        this.user = user;
    }

    public Users getUser() {
        return user;
    }
}
