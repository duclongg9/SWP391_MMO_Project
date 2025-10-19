package units;

/**
 * Compatibility wrapper that now delegates to the new salted hashing utility.
 */
public final class Encoding {

    private Encoding() {
    }

    public static String toSHA1(String str) {
        return HashPassword.hash(str);
    }
}
