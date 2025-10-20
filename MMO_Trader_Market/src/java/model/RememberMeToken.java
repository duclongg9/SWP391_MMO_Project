package model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a persistent login token stored for the remember-me feature.
 */
public class RememberMeToken {

    private Integer id;
    private Integer userId;
    private String selector;
    private String validatorHash;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private transient String plainValidator;

    public RememberMeToken() {
    }

    public RememberMeToken(Integer id, Integer userId, String selector, String validatorHash,
            Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.selector = selector;
        this.validatorHash = validatorHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getValidatorHash() {
        return validatorHash;
    }

    public void setValidatorHash(String validatorHash) {
        this.validatorHash = validatorHash;
    }

    public String getPlainValidator() {
        return plainValidator;
    }

    public void setPlainValidator(String plainValidator) {
        this.plainValidator = plainValidator;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void touchUpdatedAt(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now");
    }
}
