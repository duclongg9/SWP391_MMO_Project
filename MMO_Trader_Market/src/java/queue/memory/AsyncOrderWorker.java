package queue.memory;

import dao.order.CredentialDAO;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import model.OrderStatus;
import model.Orders;
import model.TransactionType;
import model.Wallets;
import model.product.ProductVariantOption;
import queue.OrderMessage;
import queue.OrderWorker;
import service.util.ProductVariantUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker xử lý thông điệp đơn hàng theo cơ chế hàng đợi nội bộ.
 * <p>
 * Thuật toán chính nằm trong {@link #processMessage(queue.OrderMessage)} và bao
 * gồm 6 bước: trích đơn -> khóa trạng thái -> khóa ví -> khóa tồn
 * kho/credential -> trừ tiền + ghi giao dịch -> đánh dấu hoàn thành.</p>
 */
public class AsyncOrderWorker implements OrderWorker {

    private static final Logger LOGGER = Logger.getLogger(AsyncOrderWorker.class.getName());
    private static final int[] RETRY_DELAYS = {5, 15, 30};

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CredentialDAO credentialDAO;
    private final WalletsDAO walletsDAO;
    private final WalletTransactionDAO walletTransactionDAO;

    public AsyncOrderWorker(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO,
            WalletsDAO walletsDAO, WalletTransactionDAO walletTransactionDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.credentialDAO = credentialDAO;
        this.walletsDAO = walletsDAO;
        this.walletTransactionDAO = walletTransactionDAO;
    }

    /**
     * Nhận thông điệp từ hàng đợi và xử lý với cơ chế retry tuyến tính (5s,
     * 15s, 30s). Nếu hết số lần retry mà vẫn lỗi SQL sẽ đánh dấu đơn Failed để
     * controller/JSP thông báo cho người mua.
     */
    @Override
    public void handle(OrderMessage msg) {
        for (int attempt = 0; attempt < RETRY_DELAYS.length; attempt++) {
            try {
                processMessage(msg);
                return;
            } catch (OrderProcessingException ex) {
                LOGGER.log(ex.level(), "Đơn hàng {0} thất bại: {1}", new Object[]{msg.orderId(), ex.getMessage()});
                LOGGER.log(Level.FINE, "Chi tiết ngoại lệ khi xử lý đơn hàng " + msg.orderId(), ex);
                return;
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Lỗi xử lý đơn hàng {0} ở lần thử {1}", new Object[]{msg.orderId(), attempt + 1});
                if (attempt >= RETRY_DELAYS.length - 1) {
                    markFailed(msg.orderId());
                    return;
                }
                sleepSeconds(RETRY_DELAYS[attempt]);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi không mong muốn khi xử lý đơn hàng " + msg.orderId(), ex);
                markFailed(msg.orderId());
                return;
            }
        }
    }

    private void processMessage(OrderMessage msg) throws SQLException {
        Optional<Orders> optionalOrder = orderDAO.findById(msg.orderId());
        if (optionalOrder.isEmpty()) {
            LOGGER.log(Level.WARNING, "Bỏ qua đơn hàng {0} vì không tồn tại", msg.orderId());
            return;
        }
        Orders order = optionalOrder.get();
        if (isTerminal(order.getStatus())) {
            // Đơn đã ở trạng thái kết thúc -> không xử lý lại để tránh trừ tiền lần hai.
            return;
        }
        try (Connection connection = orderDAO.openConnection()) {
            connection.setAutoCommit(false);
            try {
                OrderProcessingContext context = new OrderProcessingContext(msg, order);
                // B1: Đánh dấu đơn hàng đang xử lý để đảm bảo các worker khác không xử lý trùng.
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.PROCESSING);
                // B2: Chuẩn hóa dữ liệu đầu vào và khóa ví người mua.
                prepareVariantCode(context);
                lockWalletAndValidateBalance(connection, context);
                // B3: Khóa tồn kho & credential để đảm bảo đủ hàng bàn giao.
                lockProductAndVerifyInventory(connection, context);
                reserveCredentials(connection, context);
                // B4: Trừ tiền và ghi giao dịch thanh toán.
                chargeWallet(connection, context);
                // B5: Giao credential, cập nhật tồn kho chi tiết & log kiểm toán.
                finalizeFulfillment(connection, context);
                // B6: Hoàn tất đơn hàng và commit transaction.
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.COMPLETED);
                // Mọi thao tác thành công -> chốt giao dịch để ghi xuống DB.
                connection.commit();
            } catch (SQLException ex) {
                // Có lỗi SQL -> rollback để dữ liệu ví/tồn kho không bị lệch.
                connection.rollback();
                throw ex;
            } finally {
                // Khôi phục lại chế độ auto-commit giúp connection pool tái sử dụng an toàn.
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Chuẩn hóa và lưu biến thể tương ứng của đơn hàng (nếu có) vào context.
     */
    private void prepareVariantCode(OrderProcessingContext context) {
        String resolved = context.message().variantCode();
        if (resolved == null || resolved.isBlank()) {
            resolved = context.order().getVariantCode();
        }
        // Chuẩn hóa chuỗi biến thể để các hàm tìm kiếm/so khớp hoạt động nhất quán.
        context.setNormalizedVariantCode(ProductVariantUtils.normalizeCode(resolved));
    }

    /**
     * Khóa ví, kiểm tra trạng thái ví và số dư so với tổng tiền đơn hàng.
     */
    private void lockWalletAndValidateBalance(Connection connection, OrderProcessingContext context) throws SQLException {
        Wallets wallet = walletsDAO.lockWalletForUpdate(connection, context.order().getBuyerId());
        if (wallet == null || Boolean.FALSE.equals(wallet.getStatus())) {
            failWithReason(connection, context.order(), "Ví của người dùng không khả dụng", Level.WARNING);
        }
        BigDecimal totalAmount = context.order().getTotalAmount();
        if (totalAmount == null) {
            failWithReason(connection, context.order(), "Đơn hàng thiếu thông tin tổng tiền", Level.SEVERE);
        }
        BigDecimal balanceBefore = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        if (balanceBefore.compareTo(totalAmount) < 0) {
            failWithReason(connection, context.order(), "Ví người dùng không đủ số dư", Level.INFO);
        }
        context.setWallet(wallet);
        context.setTotalAmount(totalAmount);
        context.setBalanceBefore(balanceBefore);
    }

    /**
     * Khóa tồn kho sản phẩm, kiểm tra tổng tồn kho và tồn kho biến thể.
     */
    private void lockProductAndVerifyInventory(Connection connection, OrderProcessingContext context) throws SQLException {
        ProductDAO.ProductInventoryLock productLock = productDAO.lockProductForUpdate(connection, context.message().productId());
        List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(productLock.variantSchema(), productLock.variantsJson());
        int inventory = calculateAvailableInventory(productLock, variants);
        if (inventory < context.message().qty()) {
            failWithReason(connection, context.order(), "Tổng tồn kho sản phẩm không đủ", Level.WARNING);
        }
        context.setAvailableInventory(inventory);
        if (context.normalizedVariantCode() != null) {
            Optional<ProductVariantOption> variantOpt = ProductVariantUtils.findVariant(variants, context.normalizedVariantCode());
            if (variantOpt.isEmpty() || !variantOpt.get().isAvailable()) {
                failWithReason(connection, context.order(), "Biến thể sản phẩm không khả dụng", Level.WARNING);
            }
            ProductVariantOption variant = variantOpt.get();
            Integer variantInventory = variant.getInventoryCount();
            if (variantInventory == null || variantInventory < context.message().qty()) {
                failWithReason(connection, context.order(), "Biến thể sản phẩm không đủ tồn kho", Level.WARNING);
            }
            context.setVariant(variant);
            context.setVariantInventory(variantInventory);
        }
        context.setVariants(variants);
    }

    /**
     * Khóa credential phù hợp với sản phẩm/biến thể.
     */
    private void reserveCredentials(Connection connection, OrderProcessingContext context) throws SQLException {
        List<Integer> credentialIds = credentialDAO.pickFreeCredentialIds(connection,
                context.message().productId(), context.message().qty(), context.normalizedVariantCode());
        if (credentialIds.size() < context.message().qty()) {
            failWithReason(connection, context.order(), "Không đủ credential sẵn sàng để giao", Level.WARNING);
        }
        context.setCredentialIds(credentialIds);
    }

    /**
     * Trừ tiền khỏi ví người mua và ghi nhận giao dịch thanh toán.
     */
    private void chargeWallet(Connection connection, OrderProcessingContext context) throws SQLException {
        BigDecimal balanceAfter = context.balanceBefore().subtract(context.totalAmount());
        boolean balanceUpdated = walletsDAO.updateBalance(connection, context.wallet().getId(), balanceAfter);
        if (!balanceUpdated) {
            throw new SQLException("Không thể cập nhật số dư ví");
        }
        String note = "Thanh toán đơn hàng #" + context.order().getId();
        int walletTxId = walletTransactionDAO.insertTransaction(connection, context.wallet().getId(), context.order().getId(),
                TransactionType.PURCHASE, context.totalAmount().negate(), context.balanceBefore(), balanceAfter, note);
        // Lưu mã giao dịch vào order để màn hình chi tiết có thể truy ngược dòng tiền.
        orderDAO.assignPaymentTransaction(connection, context.order().getId(), walletTxId);
    }

    /**
     * Cập nhật tồn kho tổng, tồn kho biến thể (nếu có) và bàn giao credential
     * cho khách hàng.
     */
    private void finalizeFulfillment(Connection connection, OrderProcessingContext context) throws SQLException {
        boolean decremented = productDAO.decrementInventory(connection, context.message().productId(), context.message().qty());
        if (!decremented) {
            throw new SQLException("Không thể trừ tồn kho");
        }
        if (context.variant() != null) {
            ProductVariantUtils.decreaseInventory(context.variant(), context.message().qty());
            String updatedJson = ProductVariantUtils.toJson(context.variants());
            productDAO.updateVariantsJson(connection, context.message().productId(), updatedJson);
        }
        credentialDAO.markCredentialsSold(connection, context.message().orderId(), context.credentialIds());
        orderDAO.insertInventoryLog(connection, context.message().productId(), context.message().orderId(), -context.message().qty(), "Sale");
    }

    private void markFailed(int orderId) {
        boolean updated = orderDAO.setStatus(orderId, OrderStatus.FAILED.toDatabaseValue());
        if (!updated) {
            LOGGER.log(Level.SEVERE, "Không thể đánh dấu đơn hàng {0} thất bại", orderId);
        }
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isTerminal(String status) {
        if (status == null) {
            return false;
        }
        OrderStatus current = OrderStatus.fromDatabaseValue(status);
        return current == OrderStatus.COMPLETED || current == OrderStatus.FAILED;
    }

    private int calculateAvailableInventory(ProductDAO.ProductInventoryLock lockedProduct,
            List<ProductVariantOption> variants) {
        Integer rawInventory = lockedProduct.inventoryCount();
        if (rawInventory != null) {
            return rawInventory;
        }
        if (!ProductVariantUtils.hasVariants(lockedProduct.variantSchema())) {
            return 0;
        }
        return variants.stream()
                .filter(option -> option != null && option.isAvailable() && option.getInventoryCount() != null)
                .mapToInt(ProductVariantOption::getInventoryCount)
                .sum();
    }

    /**
     * Đánh dấu đơn hàng thất bại, commit giao dịch rồi ném ngoại lệ để
     * {@link #handle(OrderMessage)} log chi tiết.
     */
    private void failWithReason(Connection connection, Orders order, String reason, Level level) throws SQLException {
        orderDAO.updateStatus(connection, order.getId(), OrderStatus.FAILED);
        connection.commit();
        throw new OrderProcessingException(reason, level);
    }

    /**
     * Ngoại lệ runtime dành riêng cho các tình huống nghiệp vụ khiến đơn hàng
     * thất bại.
     */
    private static final class OrderProcessingException extends RuntimeException {

        private final Level level;

        OrderProcessingException(String message, Level level) {
            this(message, level, null);
        }

        OrderProcessingException(String message, Level level, Throwable cause) {
            super(message, cause);
            this.level = level == null ? Level.WARNING : level;
        }

        Level level() {
            return level;
        }
    }

    /**
     * Context gom thông tin trong suốt vòng đời xử lý đơn hàng để tái sử dụng.
     */
    private static final class OrderProcessingContext {

        private final OrderMessage message;
        private final Orders order;
        private Wallets wallet;
        private BigDecimal balanceBefore;
        private BigDecimal totalAmount;
        private String normalizedVariantCode;
        private List<ProductVariantOption> variants;
        private ProductVariantOption variant;
        private List<Integer> credentialIds;
        private int availableInventory;
        private Integer variantInventory;

        OrderProcessingContext(OrderMessage message, Orders order) {
            this.message = message;
            this.order = order;
        }

        OrderMessage message() {
            return message;
        }

        Orders order() {
            return order;
        }

        Wallets wallet() {
            return wallet;
        }

        void setWallet(Wallets wallet) {
            this.wallet = wallet;
        }

        BigDecimal balanceBefore() {
            return balanceBefore;
        }

        void setBalanceBefore(BigDecimal balanceBefore) {
            this.balanceBefore = balanceBefore;
        }

        BigDecimal totalAmount() {
            return totalAmount;
        }

        void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        String normalizedVariantCode() {
            return normalizedVariantCode;
        }

        void setNormalizedVariantCode(String normalizedVariantCode) {
            this.normalizedVariantCode = normalizedVariantCode;
        }

        List<ProductVariantOption> variants() {
            return variants;
        }

        void setVariants(List<ProductVariantOption> variants) {
            this.variants = variants;
        }

        ProductVariantOption variant() {
            return variant;
        }

        void setVariant(ProductVariantOption variant) {
            this.variant = variant;
        }

        List<Integer> credentialIds() {
            return credentialIds;
        }

        void setCredentialIds(List<Integer> credentialIds) {
            this.credentialIds = credentialIds;
        }

        int availableInventory() {
            return availableInventory;
        }

        void setAvailableInventory(int availableInventory) {
            this.availableInventory = availableInventory;
        }

        Integer variantInventory() {
            return variantInventory;
        }

        void setVariantInventory(Integer variantInventory) {
            this.variantInventory = variantInventory;
        }
    }
}
