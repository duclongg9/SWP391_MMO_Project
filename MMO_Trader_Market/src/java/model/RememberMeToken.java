package model;

import java.sql.Timestamp;

/**
 * Represents a persistent login token used for the "remember me" feature.
 */
public class RememberMeToken {

    private int id;
    private int userId;
    private String selector;
    private String hashedValidator;
    private Timestamp expiresAt;
    private Timestamp createdAt;
    private Timestamp lastUsedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Timestamp lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
