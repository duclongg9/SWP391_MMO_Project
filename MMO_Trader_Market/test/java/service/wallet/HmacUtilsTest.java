package service.wallet;

import utils.crypto.HmacUtils;

/**
 * Basic verification for the HMAC SHA-512 implementation against a known
 * sample from VNPAY documentation.
 */
public class HmacUtilsTest {

    public static void main(String[] args) {
        String secret = "SECRETKEY";
        String data = "vnp_Amount=10000000&vnp_Command=pay&vnp_TmnCode=ABC123&vnp_TxnRef=123456";
        String expected = "3CE5254D5BA52975CB8F21C1F6052E04C4AA86708480D4AF1CA841E7595C5F79B7DBC2B82978EACD4756818FF1F49CA33B2D1973B9B9C743B975DB7564734C1C";

        String actual = HmacUtils.hmacSha512(secret, data);
        if (!expected.equals(actual)) {
            throw new AssertionError("HMAC mismatch. Expected " + expected + " but got " + actual);
        }
        System.out.println("HmacUtilsTest passed");
    }
}
