package units;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility responsible for hashing and verifying passwords using SHA-256 with a
 * per-user random salt. The resulting hash is encoded using the format
 * {@code salt:hash} so it can be stored in a single database column.
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    private PasswordHasher() {
    }

    /**
     * Generates a salted SHA-256 hash for the provided password.
     *
     * @param rawPassword plaintext password
     * @return encoded hash in the format {@code salt:hash}
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] digest = sha256(salt, rawPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(salt) + ':'
                + Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Verifies that the provided password matches the stored salted hash.
     *
     * @param rawPassword plaintext password entered by the user
     * @param storedHash  encoded hash retrieved from the database
     * @return {@code true} when the password matches, {@code false} otherwise
     */
    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = sha256(salt, rawPassword.getBytes(StandardCharsets.UTF_8));
        if (expected.length != actual.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length; i++) {
            result |= expected[i] ^ actual[i];
        }
        return result == 0;
    }

    private static byte[] sha256(byte[] salt, byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
