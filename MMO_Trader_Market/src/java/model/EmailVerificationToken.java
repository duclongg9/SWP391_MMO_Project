package model;

import java.util.Date;

/**
 * Đại diện cho mã xác thực email đang chờ xử lý cho người dùng.
 */
public class EmailVerificationToken {

    private Integer userId;
    private String code;
    private Date createdAt;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
