package model;

import java.util.Date;

public class Users {

    private Integer id;
    private Integer roleId;
    private String email;
    private String name;
    private String avatarUrl;
    private String hashedPassword;
    private String googleId;
    private int status;
    private Date createdAt;
    private Date updatedAt;
    private String roleName;

    public Users() {
    }

    @Override
    public String toString() {
        return "Users{"
                + "id=" + id
                + ", roleId=" + roleId
                + ", email='" + email + '\''
                + ", name='" + name + '\''
                + ", avatarUrl='" + avatarUrl + '\''
                + ", hashedPassword='" + hashedPassword + '\''
                + ", googleId='" + googleId + '\''
                + ", status=" + status
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + '}';
    }
    public boolean isActive() { return status == 1; }
    public Users(Integer id, Integer roleId, String email, String name, String avatarUrl, String hashedPassword, String googleId, int status, Date createdAt, Date updatedAt, String roleName) {
        this.id = id;
        this.roleId = roleId;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.hashedPassword = hashedPassword;
        this.googleId = googleId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleName = roleName;
    }

    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public boolean isActive() { return status == 1; }
}
