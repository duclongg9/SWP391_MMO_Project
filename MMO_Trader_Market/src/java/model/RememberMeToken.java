package model;

import java.sql.Timestamp;

/**
 * Model đại diện cho token "ghi nhớ đăng nhập" của người dùng.
 * Token được lưu trong database và sử dụng để tự động đăng nhập người dùng.
 */
public class RememberMeToken {

    private Integer id;
    private Integer userId;
    private String selector;
    private String hashedValidator;
    private Timestamp expiresAt;

    public RememberMeToken() {
    }

    public RememberMeToken(Integer id, Integer userId, String selector, String hashedValidator, Timestamp expiresAt) {
        this.id = id;
        this.userId = userId;
        this.selector = selector;
        this.hashedValidator = hashedValidator;
        this.expiresAt = expiresAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getHashedValidator() {
        return hashedValidator;
    }

    public void setHashedValidator(String hashedValidator) {
        this.hashedValidator = hashedValidator;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }
}

