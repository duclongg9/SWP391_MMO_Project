package service.wallet;

import conf.VnpayConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.crypto.HmacUtils;

/**
 * Helper responsible for constructing signed VNPAY requests and validating the
 * checksum returned by the gateway.
 */
public class VnpayGateway {

    private static final Logger LOGGER = Logger.getLogger(VnpayGateway.class.getName());
    private static final DateTimeFormatter VNP_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayConfig config;

    public VnpayGateway(VnpayConfig config) {
        this.config = config;
    }

    /**
     * Prepares the core parameter set expected by VNPAY.
     */
    public Map<String, String> createPaymentParams(String txnRef, long amountVnd, String orderInfo,
            String orderType, String clientIp, Locale locale, LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.tmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_Locale", locale != null && "en".equalsIgnoreCase(locale.getLanguage()) ? "en" : "vn");
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", orderType);
        params.put("vnp_ReturnUrl", config.returnUrl());
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_CreateDate", VNP_DATETIME.format(createdAt));
        params.put("vnp_ExpireDate", VNP_DATETIME.format(expiresAt));
        return params;
    }

    /**
     * Builds a fully signed payment URL.
     */
    public String buildPaymentUrl(String txnRef, long amountVnd, String orderInfo,
            String orderType, String clientIp, Locale locale, LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        Map<String, String> params = createPaymentParams(txnRef, amountVnd, orderInfo, orderType,
                clientIp, locale, createdAt, expiresAt);
        Map<String, String> sortedParams = new TreeMap<>(params);
        String queryString = buildQuery(sortedParams);
        String secureHash = HmacUtils.hmacSha512(config.hashSecret(), queryString);
        try {
            return config.payUrl() + "?" + queryString
                    + "&vnp_SecureHashType=HmacSHA512"
                    + "&vnp_SecureHash=" + URLEncoder.encode(secureHash, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, "Không thể mã hóa URL VNPAY", ex);
            throw new IllegalStateException("Failed to encode VNPAY URL", ex);
        }
    }

    /**
     * Validates the checksum attached to a VNPAY callback.
     */
    public boolean isChecksumValid(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null) {
            return false;
        }
        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key)) {
                continue;
            }
            filtered.put(key, entry.getValue());
        }
        String recalculated = HmacUtils.hmacSha512(config.hashSecret(), buildQuery(filtered));
        return secureHash.equalsIgnoreCase(recalculated);
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append('&');
            }
            first = false;
            try {
                builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("UTF-8 not supported", ex);
            }
        }
        return builder.toString();
    }
}
