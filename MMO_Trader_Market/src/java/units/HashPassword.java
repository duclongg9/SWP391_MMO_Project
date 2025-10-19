package units;

/**
 * Legacy facade kept for backward compatibility. Internally delegates to the
 * {@link PasswordHasher} which provides salted SHA-256 hashing.
 */
public final class HashPassword {

    private HashPassword() {
    }

    /**
     * @deprecated Prefer {@link PasswordHasher#hash(String)}. This method is
     * provided to avoid breaking older code paths.
     */
    @Deprecated
    public static String toSHA1(String str) {
        return PasswordHasher.hash(str);
    }

    /**
     * Generates a salted hash for the provided password.
     */
    public static String hash(String str) {
        return PasswordHasher.hash(str);
    }

    /**
     * Verifies a raw password against the stored hash.
     */
    public static boolean matches(String rawPassword, String storedHash) {
        return PasswordHasher.matches(rawPassword, storedHash);
    }
}
