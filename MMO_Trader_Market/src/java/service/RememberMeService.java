package service;

import dao.user.RememberMeTokenDAO;
import dao.user.UserDAO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.RememberMeToken;
import model.Users;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dich vu bao boc toan bo quy trinh tao, xac thuc va don dep token "ghi nho
 * dang nhap".
 */
public class RememberMeService {

    public static final String COOKIE_NAME = "rememberMeToken";
    private static final int SELECTOR_BYTES = 12;
    private static final int VALIDATOR_BYTES = 32;
    private static final int EXPIRY_DAYS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Logger LOGGER = Logger.getLogger(RememberMeService.class.getName());
    private final RememberMeTokenDAO tokenDAO;
    private final UserDAO userDAO;

    public RememberMeService(RememberMeTokenDAO tokenDAO, UserDAO userDAO) {
        this.tokenDAO = tokenDAO;
        this.userDAO = userDAO;
    }

    // Tao token va cookie moi cho nguoi dung.
    public void createRememberMeCookie(HttpServletRequest request, HttpServletResponse response, int userId) {
        clearRememberMe(request, response);
        String selector = generateRandomToken(SELECTOR_BYTES);
        String validator = generateRandomToken(VALIDATOR_BYTES);
        String hashedValidator = hashValidator(validator);
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS));
        try {
            RememberMeToken created = tokenDAO.createToken(userId, selector, hashedValidator, expiresAt);
            if (created == null) {
                LOGGER.log(Level.WARNING, "Failed to persist remember-me token for user {0}", userId);
                return;
            }
            Cookie cookie = buildCookie(request, selector + ':' + validator, expiresAt, request.isSecure());
            response.addCookie(cookie);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist remember-me token", e);
        }
    }

    // Tu dong dang nhap dua tren cookie neu hop le.
    public Users autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request);
        if (cookie == null) {
            return null;
        }
        String value = cookie.getValue();
        if (value == null || value.isBlank()) {
            clearCookie(request, response);
            return null;
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            clearCookie(request, response);
            return null;
        }
        String selector = parts[0];
        String validator = parts[1];

        RememberMeToken token = tokenDAO.findBySelector(selector);
        if (token == null || token.getExpiresAt() == null) {
            clearCookie(request, response);
            return null;
        }
        if (token.getExpiresAt().toInstant().isBefore(Instant.now())) {
            removeToken(token);
            clearCookie(request, response);
            return null;
        }

        String hashedValidator = hashValidator(validator);
        String storedHashed = token.getHashedValidator();
        if (storedHashed == null || storedHashed.isBlank()
                || !MessageDigest.isEqual(hashedValidator.getBytes(StandardCharsets.UTF_8),
                        storedHashed.getBytes(StandardCharsets.UTF_8))) {
            handleCompromisedToken(token, request, response);
            return null;
        }

        Users user = userDAO.getUserByUserId(token.getUserId());
        if (user == null) {
            removeToken(token);
            clearCookie(request, response);
            return null;
        }

        rotateToken(request, response, token);
        return user;
    }

    // Xoa cookie va token luu tren DB.
    public void clearRememberMe(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = findCookie(request);
        if (cookie == null) {
            return;
        }
        String value = cookie.getValue();
        if (value != null && !value.isBlank()) {
            String selector = value.split(":")[0];
            try {
                tokenDAO.deleteBySelector(selector);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to delete remember-me token during logout", e);
            }
        }
        clearCookie(request, response);
    }

    // Tao token moi sau moi lan auto login de tranh reuse token cu.
    private void rotateToken(HttpServletRequest request, HttpServletResponse response, RememberMeToken token) {
        String newValidator = generateRandomToken(VALIDATOR_BYTES);
        String hashedValidator = hashValidator(newValidator);
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS));
        try {
            tokenDAO.updateValidator(token.getId(), hashedValidator, expiresAt);
            Cookie cookie = buildCookie(request, token.getSelector() + ':' + newValidator, expiresAt, request.isSecure());
            response.addCookie(cookie);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to rotate remember-me token", e);
            clearCookie(request, response);
        }
    }

    // Xu ly tinh huong token bi gia mao: xoa toan bo token va cookie.
    private void handleCompromisedToken(RememberMeToken token, HttpServletRequest request, HttpServletResponse response) {
        try {
            tokenDAO.deleteAllForUser(token.getUserId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to clean remember-me tokens after mismatch", e);
        }
        clearCookie(request, response);
    }

    // Xoa token don le khi het han hoac user khong ton tai.
    private void removeToken(RememberMeToken token) {
        try {
            tokenDAO.deleteById(token.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to remove remember-me token", e);
        }
    }

    // Tim cookie ghi nho trong danh sach cookie cua request.
    private Cookie findCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    // Xoa cookie bang cach set gia tri rong va maxAge=0.
    private void clearCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath(resolveCookiePath(request));
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }

    // Tao cookie remember-me voi thoi han va thuoc tinh bao mat phu hop.
    private Cookie buildCookie(HttpServletRequest request, String value, Timestamp expiresAt, boolean secure) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath(resolveCookiePath(request));
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        long seconds = ChronoUnit.SECONDS.between(Instant.now(), expiresAt.toInstant());
        cookie.setMaxAge((int) Math.min(Integer.MAX_VALUE, Math.max(seconds, 0)));
        return cookie;
    }

    // Xac dinh path cookie dua tren context path hien tai.
    private String resolveCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            return "/";
        }
        return contextPath;
    }

    private String generateRandomToken(int bytes) {
        byte[] randomBytes = new byte[bytes];
        RANDOM.nextBytes(randomBytes);
        return ENCODER.encodeToString(randomBytes);
    }

    private String hashValidator(String validator) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(validator.getBytes(StandardCharsets.UTF_8));
            return ENCODER.encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
