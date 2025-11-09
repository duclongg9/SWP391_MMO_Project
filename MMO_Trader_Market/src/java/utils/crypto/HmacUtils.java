package utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Small helper around {@link Mac} to create HMAC signatures using common
 * algorithms. This utility intentionally avoids caching {@link Mac} instances
 * because they are not thread-safe.
 */
public final class HmacUtils {

    private static final Logger LOGGER = Logger.getLogger(HmacUtils.class.getName());

    private HmacUtils() {
    }

    /**
     * Calculates an HMAC SHA-512 digest for the given data.
     *
     * @param secret raw secret string
     * @param data   the input to sign
     * @return hexadecimal upper case representation of the digest
     */
    public static String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            LOGGER.log(Level.SEVERE, "Không thể ký HMAC-SHA512", ex);
            throw new IllegalStateException("Failed to sign payload using HMAC-SHA512", ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
