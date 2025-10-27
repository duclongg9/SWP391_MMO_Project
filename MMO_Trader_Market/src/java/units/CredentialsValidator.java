package units;

/**
 * Provides simple checks for login credentials before hitting the data layer.
 */
public final class CredentialsValidator {

    private CredentialsValidator() {
        // utility class
    }

    /**
     * Basic validation to ensure the username and password are filled in.
     *
     * @param username the supplied username
     * @param password the supplied password
     * @return {@code true} when both fields contain text, {@code false}
     * otherwise
     */
    public static boolean isValid(String username, String password) {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
