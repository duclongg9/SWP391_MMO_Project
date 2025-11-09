package service.wallet;

import conf.VnpayConfig;
import dao.wallet.DepositRequestRepository;
import dao.wallet.VnpayTransactionRepository;
import dao.wallet.WalletRepository;
import dao.wallet.WalletTransactionRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import model.DepositRequests;
import model.TransactionType;
import model.VnpayTransaction;
import model.Wallets;
import service.wallet.dto.CreatePaymentResult;
import service.wallet.dto.IpnResult;
import utils.crypto.HmacUtils;

/**
 * Unit tests covering IPN flows using in-memory repository implementations.
 */
public class WalletTopUpServiceIpnTest {

    public static void main(String[] args) {
        testSuccessfulIpn();
        testInvalidAmount();
        testChecksumFailure();
        testIdempotentIpn();
        System.out.println("WalletTopUpServiceIpnTest passed");
    }

    private static void testSuccessfulIpn() {
        TestContext context = new TestContext();
        CreatePaymentResult result = context.service.createPaymentUrl(1, 100_000, "test", Locale.forLanguageTag("vi-VN"), "127.0.0.1");
        Map<String, String> params = baseParams(context, result.getTxnRef(), "10000000", "00", "00");
        signParams(context, params);

        IpnResult ipnResult = context.service.handleIpn(params, toQueryString(params), "127.0.0.1");
        assertEquals("00", ipnResult.getRspCode(), "Expected IPN success code");
        DepositRequests deposit = context.depositRepository.getByTxnRef(result.getTxnRef());
        assertEquals("Completed", deposit.getStatus(), "Deposit status should update to Completed");
        Wallets wallet = context.walletRepository.getWallet(1);
        assertEquals(new BigDecimal("100000"), wallet.getBalance(), "Wallet should be credited");
        assertEquals(1, context.walletTransactionRepository.transactionCount(), "Wallet transaction must be recorded");
    }

    private static void testInvalidAmount() {
        TestContext context = new TestContext();
        CreatePaymentResult result = context.service.createPaymentUrl(1, 50_000, null, Locale.US, "127.0.0.1");
        Map<String, String> params = baseParams(context, result.getTxnRef(), "6000000", "00", "00");
        signParams(context, params);

        IpnResult ipnResult = context.service.handleIpn(params, toQueryString(params), "127.0.0.1");
        assertEquals("04", ipnResult.getRspCode(), "Invalid amount must be detected");
        DepositRequests deposit = context.depositRepository.getByTxnRef(result.getTxnRef());
        assertEquals("Pending", deposit.getStatus(), "Deposit should remain pending on invalid amount");
    }

    private static void testChecksumFailure() {
        TestContext context = new TestContext();
        CreatePaymentResult result = context.service.createPaymentUrl(1, 10_000, null, Locale.US, "127.0.0.1");
        Map<String, String> params = baseParams(context, result.getTxnRef(), "1000000", "00", "00");
        params.put("vnp_SecureHash", "INVALID");
        IpnResult ipnResult = context.service.handleIpn(params, toQueryString(params), "127.0.0.1");
        assertEquals("97", ipnResult.getRspCode(), "Checksum failure must return 97");
    }

    private static void testIdempotentIpn() {
        TestContext context = new TestContext();
        CreatePaymentResult result = context.service.createPaymentUrl(1, 80_000, null, Locale.US, "127.0.0.1");
        Map<String, String> params = baseParams(context, result.getTxnRef(), "8000000", "00", "00");
        signParams(context, params);
        context.service.handleIpn(params, toQueryString(params), "127.0.0.1");
        IpnResult second = context.service.handleIpn(params, toQueryString(params), "127.0.0.1");
        assertEquals("02", second.getRspCode(), "Second IPN should be idempotent");
        Wallets wallet = context.walletRepository.getWallet(1);
        assertEquals(new BigDecimal("80000"), wallet.getBalance(), "Wallet must not double credit");
        assertEquals(1, context.walletTransactionRepository.transactionCount(), "Only one wallet transaction expected");
    }

    private static Map<String, String> baseParams(TestContext context, String txnRef, String amount,
            String responseCode, String transactionStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", context.config.tmnCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Amount", amount);
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TransactionStatus", transactionStatus);
        params.put("vnp_OrderInfo", "Test");
        params.put("vnp_PayDate", "20240101120000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_TransactionNo", "12345678");
        return params;
    }

    private static void signParams(TestContext context, Map<String, String> params) {
        Map<String, String> sorted = new java.util.TreeMap<>(params);
        StringBuilder data = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!first) {
                data.append('&');
            }
            first = false;
            data.append(entry.getKey()).append('=').append(entry.getValue());
        }
        String hash = HmacUtils.hmacSha512(context.config.hashSecret(), data.toString());
        params.put("vnp_SecureHash", hash);
    }

    private static String toQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append('&');
            }
            first = false;
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static Connection createFakeConnection() {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setAutoCommit", "commit", "rollback", "close" -> null;
                    case "isClosed" -> Boolean.FALSE;
                    case "unwrap" -> null;
                    case "isWrapperFor" -> Boolean.FALSE;
                    default -> throw new UnsupportedOperationException("Method not implemented: " + method.getName());
                });
    }

    private static final class TestContext {

        final FakeDepositRequestRepository depositRepository = new FakeDepositRequestRepository();
        final FakeVnpayTransactionRepository vnpayRepository = new FakeVnpayTransactionRepository();
        final FakeWalletRepository walletRepository = new FakeWalletRepository();
        final FakeWalletTransactionRepository walletTransactionRepository = new FakeWalletTransactionRepository();
        final VnpayConfig config = VnpayConfig.of("TEST", "SECRETKEY",
                "https://pay", "https://query", "https://return", "https://ipn");
        final WalletTopUpService service = new WalletTopUpService(config,
                depositRepository, vnpayRepository, walletRepository, walletTransactionRepository,
                WalletTopUpServiceIpnTest::createFakeConnection);

        TestContext() {
            walletRepository.ensureWallet(1, BigDecimal.ZERO);
        }
    }

    private static final class FakeDepositRequestRepository implements DepositRequestRepository {

        private final Map<String, DepositRequests> byTxnRef = new HashMap<>();
        private final Map<Integer, DepositRequests> byId = new HashMap<>();
        private final AtomicInteger idSeq = new AtomicInteger(1);

        @Override
        public DepositRequests insert(Connection connection, int userId, BigDecimal amount,
                String qrContent, String idempotencyKey, Instant expiresAt) {
            DepositRequests deposit = new DepositRequests();
            deposit.setId(idSeq.getAndIncrement());
            deposit.setUserId(userId);
            deposit.setAmount(amount);
            deposit.setQrContent(qrContent);
            deposit.setIdempotencyKey(idempotencyKey);
            deposit.setStatus("Pending");
            deposit.setExpiresAt(java.util.Date.from(expiresAt));
            deposit.setCreatedAt(java.util.Date.from(Instant.now()));
            byTxnRef.put(idempotencyKey, deposit);
            byId.put(deposit.getId(), deposit);
            return deposit;
        }

        @Override
        public Optional<DepositRequests> findByTxnRef(Connection connection, String txnRef,
                boolean forUpdate) {
            return Optional.ofNullable(byTxnRef.get(txnRef));
        }

        @Override
        public void updateStatus(Connection connection, int id, String status) {
            DepositRequests deposit = byId.get(id);
            if (deposit != null) {
                deposit.setStatus(status);
            }
        }

        DepositRequests getByTxnRef(String txnRef) {
            return byTxnRef.get(txnRef);
        }
    }

    private static final class FakeVnpayTransactionRepository implements VnpayTransactionRepository {

        private final Map<Integer, VnpayTransaction> byDepositId = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public VnpayTransaction insert(Connection connection, int depositRequestId, String linkData) {
            VnpayTransaction tx = new VnpayTransaction();
            tx.setId(seq.getAndIncrement());
            tx.setDepositRequestId(depositRequestId);
            tx.setLinkData(linkData);
            tx.setVnpayStatus("pending");
            tx.setCreatedAt(java.util.Date.from(Instant.now()));
            byDepositId.put(depositRequestId, tx);
            return tx;
        }

        @Override
        public void updateStatusAndPayload(Connection connection, int depositRequestId,
                String status, String linkData) {
            VnpayTransaction tx = byDepositId.get(depositRequestId);
            if (tx != null) {
                tx.setVnpayStatus(status);
                tx.setLinkData(linkData);
            }
        }

        @Override
        public Optional<VnpayTransaction> findByDepositRequestId(Connection connection,
                int depositRequestId, boolean forUpdate) {
            return Optional.ofNullable(byDepositId.get(depositRequestId));
        }
    }

    private static final class FakeWalletRepository implements WalletRepository {

        private final Map<Integer, Wallets> wallets = new HashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        void ensureWallet(int userId, BigDecimal initialBalance) {
            Wallets wallet = new Wallets();
            wallet.setId(seq.getAndIncrement());
            wallet.setUserId(userId);
            wallet.setBalance(initialBalance);
            wallets.put(userId, wallet);
        }

        Wallets getWallet(int userId) {
            return wallets.get(userId);
        }

        @Override
        public Wallets lockWalletForUser(Connection connection, int userId) {
            return wallets.get(userId);
        }

        @Override
        public boolean updateBalance(Connection connection, int walletId, BigDecimal newBalance) {
            for (Wallets wallet : wallets.values()) {
                if (wallet.getId() == walletId) {
                    wallet.setBalance(newBalance);
                    return true;
                }
            }
            return false;
        }
    }

    private static final class FakeWalletTransactionRepository implements WalletTransactionRepository {

        private final AtomicInteger txCount = new AtomicInteger();

        @Override
        public int insertTransaction(Connection connection, int walletId, Integer relatedEntityId,
                TransactionType type, BigDecimal amount, BigDecimal balanceBefore,
                BigDecimal balanceAfter, String note) {
            return txCount.incrementAndGet();
        }

        int transactionCount() {
            return txCount.get();
        }
    }
}
