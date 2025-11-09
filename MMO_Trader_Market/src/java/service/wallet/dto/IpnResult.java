package service.wallet.dto;

/**
 * Represents the JSON payload required by VNPAY when answering an IPN request.
 */
public class IpnResult {

    private final String rspCode;
    private final String message;

    public IpnResult(String rspCode, String message) {
        this.rspCode = rspCode;
        this.message = message;
    }

    public String getRspCode() {
        return rspCode;
    }

    public String getMessage() {
        return message;
    }
}
