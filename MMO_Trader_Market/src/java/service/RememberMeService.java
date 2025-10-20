package service;

import dao.user.RememberMeDAO;
import model.RememberMeToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business helper around remember-me persistent login tokens.
 */
public class RememberMeService {

    private static final Logger LOGGER = Logger.getLogger(RememberMeService.class.getName());
    private static final Duration DEFAULT_TTL = Duration.ofDays(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RememberMeDAO rememberMeDAO;

    public RememberMeService() {
        this(new RememberMeDAO());
    }

    public RememberMeService(RememberMeDAO rememberMeDAO) {
        this.rememberMeDAO = rememberMeDAO;
    }

    public RememberMeToken createToken(int userId) {
        byte[] selectorBytes = new byte[12];
        byte[] validatorBytes = new byte[32];
        RANDOM.nextBytes(selectorBytes);
        RANDOM.nextBytes(validatorBytes);
        String selector = Base64.getUrlEncoder().withoutPadding().encodeToString(selectorBytes);
        String validator = Base64.getUrlEncoder().withoutPadding().encodeToString(validatorBytes);
        Instant expiresAt = Instant.now().plus(DEFAULT_TTL);
        try {
            rememberMeDAO.deleteByUser(userId);
            RememberMeToken token = rememberMeDAO.insert(userId, selector, hashValidator(validator), expiresAt);
            token.setPlainValidator(validator);
            return token;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo token ghi nhớ đăng nhập", ex);
        }
    }

    public Optional<RememberMeToken> consume(String selector, String validator) {
        rememberMeDAO.purgeExpired(Instant.now());
        Optional<RememberMeToken> stored = rememberMeDAO.findBySelector(selector);
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        RememberMeToken token = stored.get();
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(Instant.now())) {
            try {
                rememberMeDAO.deleteBySelector(selector);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Không thể xoá token hết hạn", ex);
            }
            return Optional.empty();
        }
        String hashedValidator = hashValidator(validator);
        if (!hashedValidator.equals(token.getValidatorHash())) {
            try {
                rememberMeDAO.deleteBySelector(selector);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Không thể thu hồi token nhớ đăng nhập", ex);
            }
            return Optional.empty();
        }
        try {
            rememberMeDAO.deleteBySelector(selector);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể cập nhật token nhớ đăng nhập", ex);
        }
        return stored;
    }

    public void revokeAll(int userId) {
        try {
            rememberMeDAO.deleteByUser(userId);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Không thể thu hồi token ghi nhớ đăng nhập", ex);
        }
    }

    private String hashValidator(String validator) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(validator.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 không khả dụng", ex);
        }
    }
}
