package service.wallet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import conf.VnpayConfig;
import dao.wallet.DepositRequestRepository;
import dao.wallet.VnpayTransactionRepository;
import dao.wallet.WalletRepository;
import dao.wallet.WalletTransactionRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DepositRequests;
import model.TransactionType;
import model.VnpayTransaction;
import model.Wallets;
import service.wallet.dto.CreatePaymentResult;
import service.wallet.dto.IpnResult;

/**
 * Core service orchestrating wallet top-up flows with VNPAY. The service keeps
 * business logic separated from servlets and DAO layers so it can be unit
 * tested in isolation.
 */
public class WalletTopUpService {

    private static final Logger LOGGER = Logger.getLogger(WalletTopUpService.class.getName());
    private static final DateTimeFormatter TXN_REF_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String ORDER_TYPE = "other";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100L);
    public static final long MIN_TOPUP_AMOUNT = 1_000L;
    public static final long MAX_TOPUP_AMOUNT = 50_000_000L;
    public static final int NOTE_MAX_LENGTH = 120;

    private final VnpayConfig config;
    private final VnpayGateway gateway;
    private final DepositRequestRepository depositRequestRepository;
    private final VnpayTransactionRepository vnpayTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ConnectionProvider connectionProvider;
    private final Random random = new Random();
    private final Gson gson = new Gson();

    public WalletTopUpService(VnpayConfig config,
            DepositRequestRepository depositRequestRepository,
            VnpayTransactionRepository vnpayTransactionRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            ConnectionProvider connectionProvider) {
        this.config = config;
        this.gateway = new VnpayGateway(config);
        this.depositRequestRepository = depositRequestRepository;
        this.vnpayTransactionRepository = vnpayTransactionRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Generates a signed VNPAY payment URL and persists the associated deposit
     * request for tracking.
     */
    public CreatePaymentResult createPaymentUrl(int userId, long amountVnd, String note,
            Locale locale, String clientIp) {
        if (amountVnd < MIN_TOPUP_AMOUNT) {
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 1.000 VNĐ");
        }
        if (amountVnd > MAX_TOPUP_AMOUNT) {
            throw new IllegalArgumentException("Số tiền nạp tối đa là 50.000.000 VNĐ cho mỗi giao dịch");
        }
        String resolvedIp = (clientIp == null || clientIp.isBlank()) ? "0.0.0.0" : clientIp;
        Locale resolvedLocale = (locale == null) ? config.defaultLocale() : locale;

        LocalDateTime createdAt = LocalDateTime.now(config.zoneId());
        LocalDateTime expiresAt = createdAt.plus(config.paymentTimeout());
        String txnRef = generateTxnRef(createdAt);
        String normalizedNote = normalizeNote(note);
        Map<String, String> params = gateway.createPaymentParams(txnRef, amountVnd,
                buildOrderInfo(txnRef, normalizedNote), ORDER_TYPE, resolvedIp, resolvedLocale,
                createdAt, expiresAt);
        String paymentUrl = gateway.buildPaymentUrl(txnRef, amountVnd,
                buildOrderInfo(txnRef, normalizedNote), ORDER_TYPE, resolvedIp, resolvedLocale,
                createdAt, expiresAt);
        String linkData = buildCreationLinkData(params, paymentUrl, resolvedIp, normalizedNote);

        Instant expireInstant = expiresAt.atZone(config.zoneId()).toInstant();
        BigDecimal amount = BigDecimal.valueOf(amountVnd);
        int depositId = insertDepositRequest(userId, amount, txnRef, expireInstant, linkData);

        return new CreatePaymentResult(paymentUrl, txnRef, depositId);
    }

    /**
     * Handles the VNPAY server-to-server callback.
     */
    public IpnResult handleIpn(Map<String, String> params, String rawQuery, String remoteIp) {
        if (!gateway.isChecksumValid(params)) {
            LOGGER.log(Level.WARNING, "IPN checksum không hợp lệ từ IP {0}", remoteIp);
            return new IpnResult("97", "Invalid Checksum");
        }

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            return new IpnResult("01", "Order not Found");
        }

        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            Optional<DepositRequests> depositOpt = depositRequestRepository.findByTxnRef(connection, txnRef, true);
            if (depositOpt.isEmpty()) {
                connection.rollback();
                return new IpnResult("01", "Order not Found");
            }
            DepositRequests deposit = depositOpt.get();
            BigDecimal expectedAmount = deposit.getAmount();
            BigDecimal paidAmount = parseAmount(params.get("vnp_Amount"));
            if (paidAmount == null || expectedAmount.compareTo(paidAmount) != 0) {
                connection.rollback();
                return new IpnResult("04", "Invalid Amount");
            }

            if (!"Pending".equalsIgnoreCase(deposit.getStatus())) {
                connection.rollback();
                return new IpnResult("02", "Order already confirmed");
            }

            Optional<VnpayTransaction> txOpt = vnpayTransactionRepository
                    .findByDepositRequestId(connection, deposit.getId(), true);
            String linkData = updateLinkData(txOpt.map(VnpayTransaction::getLinkData).orElse(null),
                    params, rawQuery, remoteIp);

            String responseCode = params.getOrDefault("vnp_ResponseCode", "");
            String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "");
            String mappedStatus = mapVnpayStatus(responseCode, transactionStatus);

            if ("success".equals(mappedStatus)) {
                processSuccessfulIpn(connection, deposit, expectedAmount, txnRef, linkData);
            } else {
                String depositStatus = mapDepositStatus(mappedStatus);
                depositRequestRepository.updateStatus(connection, deposit.getId(), depositStatus);
                vnpayTransactionRepository.updateStatusAndPayload(connection, deposit.getId(), mappedStatus, linkData);
            }

            connection.commit();
            return new IpnResult("00", "Confirm Success");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Không xử lý được IPN từ VNPAY", ex);
            return new IpnResult("99", "Unknown error");
        }
    }

    /**
     * Verifies the checksum for UI return flows.
     */
    public boolean verifyChecksum(Map<String, String> params) {
        return gateway.isChecksumValid(params);
    }

    private int insertDepositRequest(int userId, BigDecimal amount, String txnRef,
            Instant expiresAt, String linkData) {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            DepositRequests deposit = depositRequestRepository.insert(connection, userId,
                    amount, "VNPAY-" + txnRef, txnRef, expiresAt);
            vnpayTransactionRepository.insert(connection, deposit.getId(), linkData);
            connection.commit();
            return deposit.getId();
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollEx) {
                    LOGGER.log(Level.WARNING, "Rollback create deposit failed", rollEx);
                }
            }
            LOGGER.log(Level.SEVERE, "Không thể tạo yêu cầu nạp tiền", ex);
            throw new IllegalStateException("Không thể tạo yêu cầu nạp tiền", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.log(Level.WARNING, "Không thể đóng kết nối sau khi tạo deposit", closeEx);
                }
            }
        }
    }

    private void processSuccessfulIpn(Connection connection, DepositRequests deposit,
            BigDecimal amount, String txnRef, String linkData) throws SQLException {
        Wallets wallet = walletRepository.lockWalletForUser(connection, deposit.getUserId());
        if (wallet == null) {
            throw new SQLException("Wallet not found for user " + deposit.getUserId());
        }
        BigDecimal currentBalance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);

        if (!walletRepository.updateBalance(connection, wallet.getId(), newBalance)) {
            throw new SQLException("Không cập nhật được số dư ví");
        }

        walletTransactionRepository.insertTransaction(connection, wallet.getId(), deposit.getId(),
                TransactionType.DEPOSIT, amount, currentBalance, newBalance,
                "Nạp ví qua VNPAY - " + txnRef);

        depositRequestRepository.updateStatus(connection, deposit.getId(), "Completed");
        vnpayTransactionRepository.updateStatusAndPayload(connection, deposit.getId(), "success", linkData);
    }

    private String buildOrderInfo(String txnRef, String note) {
        if (note != null && !note.isBlank()) {
            return "Nap vi VNPAY " + txnRef + " - " + note;
        }
        return "Nap vi VNPAY " + txnRef;
    }

    /**
     * Chuẩn hoá ghi chú người dùng nhập vào nhằm tránh lỗi payload với VNPAY.
     *
     * @param note ghi chú thô nhận từ client
     * @return ghi chú đã được cắt gọn hoặc {@code null} nếu trống
     */
    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String singleLine = trimmed.replaceAll("\\s+", " ");
        if (singleLine.length() > NOTE_MAX_LENGTH) {
            return singleLine.substring(0, NOTE_MAX_LENGTH);
        }
        return singleLine;
    }

    private String buildCreationLinkData(Map<String, String> params, String paymentUrl,
            String clientIp, String note) {
        JsonObject event = new JsonObject();
        event.addProperty("phase", "create");
        event.addProperty("client_ip", clientIp);
        event.addProperty("payment_url", paymentUrl);
        event.addProperty("created_at", Instant.now().toString());
        if (note != null && !note.isBlank()) {
            event.addProperty("note", note);
        }
        event.add("params", gson.toJsonTree(params));
        if (paymentUrl != null && paymentUrl.contains("?")) {
            event.addProperty("query_string", paymentUrl.substring(paymentUrl.indexOf('?') + 1));
        }
        return appendHistory(null, event);
    }

    private String updateLinkData(String existing, Map<String, String> params,
            String rawQuery, String remoteIp) {
        JsonObject event = new JsonObject();
        event.addProperty("phase", "ipn");
        event.addProperty("received_at", Instant.now().toString());
        event.addProperty("remote_ip", remoteIp);
        if (rawQuery != null) {
            event.addProperty("query_string", rawQuery);
        }
        event.add("params", gson.toJsonTree(params));
        return appendHistory(existing, event);
    }

    private String appendHistory(String existing, JsonObject event) {
        JsonObject root;
        if (existing == null || existing.isBlank()) {
            root = new JsonObject();
        } else {
            root = gson.fromJson(existing, JsonObject.class);
        }
        JsonArray history;
        if (root.has("history") && root.get("history").isJsonArray()) {
            history = root.getAsJsonArray("history");
        } else {
            history = new JsonArray();
            root.add("history", history);
        }
        history.add(event);
        root.addProperty("last_updated_at", Instant.now().toString());
        return gson.toJson(root);
    }

    private String mapVnpayStatus(String responseCode, String transactionStatus) {
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return "success";
        }
        if ("07".equals(responseCode) || "09".equals(responseCode)) {
            return "suspected";
        }
        return "failed";
    }

    private String mapDepositStatus(String vnpayStatus) {
        return switch (vnpayStatus) {
            case "success" -> "Completed";
            case "suspected" -> "RequiresManualCheck";
            default -> "Failed";
        };
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            BigDecimal amount = new BigDecimal(raw);
            return amount.divide(HUNDRED);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String generateTxnRef(LocalDateTime createdAt) {
        String datePart = createdAt.format(TXN_REF_DATE);
        int randomNumber = 100000 + random.nextInt(900000);
        return datePart + randomNumber;
    }
}
