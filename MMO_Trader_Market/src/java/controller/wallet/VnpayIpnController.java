package controller.wallet;

import com.google.gson.Gson;
import java.io.IOException;
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
import service.wallet.dto.IpnResult;

/**
 * Receives the server-to-server IPN callback from VNPAY and updates the wallet
 * state accordingly.
 */
@WebServlet(name = "VnpayIpnController", urlPatterns = {"/wallet/vnpay/ipn"})
public class VnpayIpnController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VnpayIpnController.class.getName());
    private final transient Gson gson = new Gson();
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
        String remoteIp = resolveClientIp(request);
        String rawQuery = request.getQueryString();

        LOGGER.log(Level.INFO, "Nhận IPN từ VNPAY: txnRef={0}, ip={1}",
                new Object[]{params.get("vnp_TxnRef"), remoteIp});

        IpnResult result = walletTopUpService.handleIpn(params, rawQuery, remoteIp);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(Map.of(
                "RspCode", result.getRspCode(),
                "Message", result.getMessage())));
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

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
