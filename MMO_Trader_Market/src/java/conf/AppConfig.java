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
    private static final String CONFIG_RESOURCE = "conf/database.properties";

    static {
        try (InputStream stream = openConfigStream()) {
            if (stream != null) {
                PROPERTIES.load(stream);
            }
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return Objects.toString(PROPERTIES.getProperty(key), "");
    }

    private static InputStream openConfigStream() throws IOException {
        InputStream stream = null;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            stream = contextLoader.getResourceAsStream(CONFIG_RESOURCE);
        }
        if (stream == null) {
            stream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE);
        }
        if (stream != null) {
            return stream;
        }

        java.nio.file.Path[] candidates = {
                java.nio.file.Path.of("src", "conf", "database.properties"),
                java.nio.file.Path.of("conf", "database.properties"),
                java.nio.file.Path.of("WEB-INF", "classes", "conf", "database.properties")
        };
        for (java.nio.file.Path candidate : candidates) {
            if (java.nio.file.Files.exists(candidate)) {
                return java.nio.file.Files.newInputStream(candidate);
            }
        }
        System.err.println("[AppConfig] Không tìm thấy file cấu hình cơ sở dữ liệu: " + CONFIG_RESOURCE);
        return null;
    }
}
