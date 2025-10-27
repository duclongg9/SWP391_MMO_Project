package units;

/**
 * Compatibility wrapper that provides the same hashing helper used in legacy
 * controllers while delegating to the current hashing utility.
 */
public final class Encoding {

    private Encoding() {
        // utility class
    }

    /**
     * Hashes the provided text using the legacy SHA-1 helper to remain backward
     * compatible with existing code paths.
     *
     * @param str original text that needs hashing
     * @return SHA-1 hash encoded in Base64 format
     */
    public static String toSHA1(String str) {
        return HashPassword.toSHA1(str);
    }
}
