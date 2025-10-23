package units;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility methods that normalize image URLs used throughout the application.
 * <p>
 * The seed data that powers the demo references a mock CDN domain
 * (cdn.mmo.local) which is not reachable in the local development
 * environment. When such URLs are encountered we transparently replace them
 * with local placeholder assets that live inside the {@code assets/img}
 * directory of the web module.
 */
public final class ImageUrlResolver {

    private static final String PLACEHOLDER_PRODUCT = "/assets/img/placeholders/product-placeholder.svg";
    private static final String PLACEHOLDER_AVATAR = "/assets/img/placeholders/avatar-placeholder.svg";
    private static final String PLACEHOLDER_DOCUMENT = "/assets/img/placeholders/document-placeholder.svg";
    private static final String PLACEHOLDER_LOGO = "/assets/img/logo.svg";

    private ImageUrlResolver() {
    }

    public static String resolveProductImage(String contextPath, String imageUrl) {
        return resolve(contextPath, imageUrl, PLACEHOLDER_PRODUCT);
    }

    public static String resolveAvatarImage(String contextPath, String imageUrl) {
        return resolve(contextPath, imageUrl, PLACEHOLDER_AVATAR);
    }

    public static String resolveDocumentImage(String contextPath, String imageUrl) {
        return resolve(contextPath, imageUrl, PLACEHOLDER_DOCUMENT);
    }

    public static String resolveLogoImage(String contextPath, String imageUrl) {
        return resolve(contextPath, imageUrl, PLACEHOLDER_LOGO);
    }

    private static String resolve(String contextPath, String candidate, String placeholder) {
        String normalizedContext = normalizeContextPath(contextPath);
        if (!hasText(candidate)) {
            return normalizedContext + placeholder;
        }

        String trimmed = candidate.trim();
        if (isDataUri(trimmed)) {
            return trimmed;
        }
        if (isKnownOfflineHost(trimmed)) {
            return normalizedContext + placeholder;
        }
        if (isAbsoluteUrl(trimmed)) {
            return trimmed;
        }
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        return normalizedContext + trimmed;
    }

    private static boolean isKnownOfflineHost(String value) {
        try {
            URI uri = new URI(value);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            return Objects.equals(normalized, "cdn.mmo.local")
                    || Objects.equals(normalized, "i.pravatar.cc");
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private static boolean isDataUri(String value) {
        return value.regionMatches(true, 0, "data:", 0, 5);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return "";
        }
        return contextPath;
    }
}
