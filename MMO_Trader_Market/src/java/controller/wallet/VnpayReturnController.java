package controller.wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.wallet.WalletTopUpService;
import service.wallet.WalletTopUpServiceFactory;

/**
 * Handles the user-facing return URL after a VNPAY payment attempt. No database
 * mutation happens in this servlet.
 */
@WebServlet(name = "VnpayReturnController", urlPatterns = {"/wallet/vnpay/return"})
public class VnpayReturnController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VnpayReturnController.class.getName());
    private transient WalletTopUpService walletTopUpService;

    @Override
    public void init() throws ServletException {
        super.init();
        walletTopUpService = WalletTopUpServiceFactory.createDefault();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> params = extractParams(request);
        boolean checksumValid = walletTopUpService.verifyChecksum(params);

        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "");
        String txnRef = params.get("vnp_TxnRef");
        String amountRaw = params.get("vnp_Amount");
        BigDecimal amount = parseAmount(amountRaw);

        boolean success = checksumValid && "00".equals(responseCode) && "00".equals(transactionStatus);

        request.setAttribute("checksumValid", checksumValid);
        request.setAttribute("paymentSuccess", success);
        request.setAttribute("txnRef", txnRef);
        request.setAttribute("responseCode", responseCode);
        request.setAttribute("transactionStatus", transactionStatus);
        request.setAttribute("amount", amount);

        if (!checksumValid) {
            LOGGER.log(Level.WARNING, "Return URL checksum không hợp lệ cho giao dịch {0}", txnRef);
        }

        request.getRequestDispatcher("/WEB-INF/views/wallet/vnpay-return.jsp").forward(request, response);
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
