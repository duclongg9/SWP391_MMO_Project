package controller.wallet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.wallet.WalletTopUpService;
import service.wallet.WalletTopUpServiceFactory;
import service.wallet.dto.CreatePaymentResult;

/**
 * Handles API requests that create a VNPAY payment URL for wallet top-ups.
 */
@WebServlet(name = "WalletTopUpController", urlPatterns = {"/wallet/deposit/create"})
public class WalletTopUpController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(WalletTopUpController.class.getName());
    private final transient Gson gson = new Gson();
    private transient WalletTopUpService walletTopUpService;
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        walletTopUpService = WalletTopUpServiceFactory.createDefault();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, Map.of("error", "Bạn cần đăng nhập để nạp tiền"));
            return;
        }

        JsonObject payload;
        try {
            payload = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", "Payload không hợp lệ"));
            return;
        }

        if (!payload.has("amount")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", "Thiếu trường amount"));
            return;
        }

        long amount;
        try {
            amount = payload.get("amount").getAsLong();
        } catch (NumberFormatException | UnsupportedOperationException | ClassCastException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", "Giá trị amount không hợp lệ"));
            return;
        }

        if (amount < WalletTopUpService.MIN_TOPUP_AMOUNT) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", "Số tiền nạp tối thiểu là " + WalletTopUpService.MIN_TOPUP_AMOUNT + " VNĐ"));
            return;
        }
        if (amount > WalletTopUpService.MAX_TOPUP_AMOUNT) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", "Số tiền nạp tối đa là " + WalletTopUpService.MAX_TOPUP_AMOUNT + " VNĐ"));
            return;
        }

        String note = null;
        if (payload.has("note") && !payload.get("note").isJsonNull()) {
            try {
                note = normalizeNote(payload.get("note").getAsString());
            } catch (ClassCastException | IllegalStateException | UnsupportedOperationException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(response, Map.of("error", "Ghi chú không hợp lệ"));
                return;
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(response, Map.of("error", ex.getMessage()));
                return;
            }
        }

        Locale locale = request.getLocale();
        String clientIp = resolveClientIp(request);

        try {
            CreatePaymentResult result = walletTopUpService.createPaymentUrl(userId, amount, note, locale, clientIp);
            writeJson(response, Map.of("paymentUrl", result.getPaymentUrl(),
                    "txnRef", result.getTxnRef(),
                    "depositRequestId", result.getDepositRequestId()));
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Tạo URL nạp tiền thất bại: {0}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi tạo URL nạp tiền", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, Map.of("error", "Không thể tạo URL nạp tiền, vui lòng thử lại"));
        }
    }

    private void writeJson(HttpServletResponse response, Map<String, ?> payload) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(payload));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Chuẩn hoá ghi chú của người dùng để đồng bộ với ràng buộc ở tầng service.
     *
     * @param rawNote ghi chú trước khi xử lý
     * @return ghi chú đã chuẩn hoá hoặc {@code null}
     */
    private String normalizeNote(String rawNote) {
        if (rawNote == null) {
            return null;
        }
        String trimmed = rawNote.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > WalletTopUpService.NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Ghi chú tối đa " + WalletTopUpService.NOTE_MAX_LENGTH + " ký tự");
        }
        return trimmed;
    }
}
