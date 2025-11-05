package conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads configuration values from the {@code conf/database.properties} file.
 */
public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream stream = AppConfig.class.getClassLoader()
                .getResourceAsStream("conf/database.properties")) {
            if (stream == null) {
                throw new ExceptionInInitializerError("Missing configuration file conf/database.properties on the classpath");
            }
            PROPERTIES.load(stream);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        String override = firstNonEmpty(
                System.getProperty(key),
                System.getenv(key),
                System.getenv(toEnvKey(key)));
        if (override != null) {
            return override;
        }
        return Objects.toString(PROPERTIES.getProperty(key), "");
    }

    private static String firstNonEmpty(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

    private static String toEnvKey(String key) {
        return key.replace('.', '_').toUpperCase();
    }
}
