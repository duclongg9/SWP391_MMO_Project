package com.mmo.trader.util;

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
}
