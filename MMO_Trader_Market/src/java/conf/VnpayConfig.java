package conf;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable configuration holder for VNPAY integration. Values are resolved
 * from {@link AppConfig} so they can be overridden via environment variables or
 * JVM system properties without rebuilding the application.
 */
public final class VnpayConfig {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(15);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    private final String tmnCode;
    private final String hashSecret;
    private final String payUrl;
    private final String queryUrl;
    private final String returnUrl;
    private final String ipnUrl;

    private VnpayConfig(String tmnCode, String hashSecret, String payUrl,
            String queryUrl, String returnUrl, String ipnUrl) {
        this.tmnCode = Objects.requireNonNull(tmnCode, "tmnCode must not be null");
        this.hashSecret = Objects.requireNonNull(hashSecret, "hashSecret must not be null");
        this.payUrl = Objects.requireNonNull(payUrl, "payUrl must not be null");
        this.queryUrl = Objects.requireNonNull(queryUrl, "queryUrl must not be null");
        this.returnUrl = Objects.requireNonNull(returnUrl, "returnUrl must not be null");
        this.ipnUrl = Objects.requireNonNull(ipnUrl, "ipnUrl must not be null");
    }

    /**
     * Builds a configuration instance using {@link AppConfig} as the backing
     * source.
     *
     * @return configuration resolved from properties and environment variables
     */
    public static VnpayConfig fromAppConfig() {
        return new VnpayConfig(
                AppConfig.get("vnpay.tmnCode"),
                AppConfig.get("vnpay.hashSecret"),
                AppConfig.get("vnpay.payUrl"),
                AppConfig.get("vnpay.queryUrl"),
                AppConfig.get("vnpay.returnUrl"),
                AppConfig.get("vnpay.ipnUrl"));
    }

    /**
     * Factory primarily used by tests to create a configuration with
     * deterministic values.
     */
    public static VnpayConfig of(String tmnCode, String hashSecret, String payUrl,
            String queryUrl, String returnUrl, String ipnUrl) {
        return new VnpayConfig(tmnCode, hashSecret, payUrl, queryUrl, returnUrl, ipnUrl);
    }

    public String tmnCode() {
        return tmnCode;
    }

    public String hashSecret() {
        return hashSecret;
    }

    public String payUrl() {
        return payUrl;
    }

    public String queryUrl() {
        return queryUrl;
    }

    public String returnUrl() {
        return returnUrl;
    }

    public String ipnUrl() {
        return ipnUrl;
    }

    /**
     * VNPAY expects payment requests and timestamps to be aligned with the
     * Vietnam time-zone.
     *
     * @return zone identifier fixed to Asia/Ho_Chi_Minh
     */
    public ZoneId zoneId() {
        return ZONE_ID;
    }

    /**
     * Payment URLs expire after a fixed 15 minute window.
     *
     * @return timeout duration used for {@code vnp_ExpireDate}
     */
    public Duration paymentTimeout() {
        return PAYMENT_TIMEOUT;
    }

    /**
     * Default locale used for VNPAY requests when the caller does not specify
     * one explicitly.
     *
     * @return Vietnamese locale by default
     */
    public Locale defaultLocale() {
        return Locale.forLanguageTag("vi-VN");
    }
}
