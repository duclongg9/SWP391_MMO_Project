package model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * Represents a user in the system.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;
    private Role role;
    private String hashPassword;
    private String googleId;
    private String avataUrl;
    private int status;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(int id, String username, String email, Role role, String hashPassword, String googleId, String avataUrl, int status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.hashPassword = hashPassword;
        this.googleId = googleId;
        this.avataUrl = avataUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getAvataUrl() {
        return avataUrl;
    }

    public int getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public void setAvataUrl(String avataUrl) {
        this.avataUrl = avataUrl;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAtDate() {
        return updatedAt == null ? null : Date.from(updatedAt);
    }

    public Date getCreatedAtDate() {
        return createdAt == null ? null : Date.from(createdAt);
    }

}
