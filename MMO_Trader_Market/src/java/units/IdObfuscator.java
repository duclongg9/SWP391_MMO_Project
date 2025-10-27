package units;

import java.util.Locale;

/**
 * Utility class that obfuscates numeric identifiers before exposing them to
 * clients.
 * <p>
 * The implementation performs a reversible transformation consisting of bit
 * shifting and XOR with a private mask, then converts the result to a base36
 * string. The transformation is deterministic so the same identifier always
 * yields the same token while keeping the raw value hidden from the URL.</p>
 */
public final class IdObfuscator {

    private static final int SHIFT_BITS = 17;
    private static final long LOWER_MASK = (1L << SHIFT_BITS) - 1;
    private static final long SECRET_MASK = 0x5A6C3E9BD4A7L;

    private IdObfuscator() {
        // Utility class
    }

    /**
     * Encode a positive identifier to a non-guessable token safe for URLs.
     *
     * @param id numeric identifier, must be positive
     * @return obfuscated token using base36 alphabet in upper case
     * @throws IllegalArgumentException if {@code id} is not positive
     */
    public static String encode(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Identifier must be a positive integer");
        }
        long obfuscated = (((long) id) << SHIFT_BITS) ^ SECRET_MASK;
        return Long.toUnsignedString(obfuscated, 36).toUpperCase(Locale.ROOT);
    }

    /**
     * Decode an obfuscated token back to its numeric identifier.
     *
     * @param token encoded representation returned by {@link #encode(int)}
     * @return the original positive identifier
     * @throws IllegalArgumentException if the token is null, malformed or
     * cannot be decoded
     */
    public static int decode(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Identifier token must not be null");
        }
        String normalized = token.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Identifier token must not be empty");
        }
        long obfuscated;
        try {
            obfuscated = Long.parseUnsignedLong(normalized.toLowerCase(Locale.ROOT), 36);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Identifier token is invalid", ex);
        }
        long combined = obfuscated ^ SECRET_MASK;
        if ((combined & LOWER_MASK) != 0) {
            throw new IllegalArgumentException("Identifier token is invalid");
        }
        long id = combined >>> SHIFT_BITS;
        if (id <= 0 || id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Identifier token is invalid");
        }
        return (int) id;
    }
}
