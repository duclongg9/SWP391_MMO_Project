package service;

import model.Users;

/**
 * Ngoai le bieu thi tai khoan ton tai nhung dang o trang thai chua kich hoat.
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
