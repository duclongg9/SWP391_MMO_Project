package service.wallet.dto;

/**
 * Immutable response returned when a payment URL is successfully generated.
 */
public class CreatePaymentResult {

    private final String paymentUrl;
    private final String txnRef;
    private final int depositRequestId;

    public CreatePaymentResult(String paymentUrl, String txnRef, int depositRequestId) {
        this.paymentUrl = paymentUrl;
        this.txnRef = txnRef;
        this.depositRequestId = depositRequestId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public int getDepositRequestId() {
        return depositRequestId;
    }
}
