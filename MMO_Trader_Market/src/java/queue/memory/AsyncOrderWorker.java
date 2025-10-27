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
 * <p>Thuật toán chính nằm trong {@link #processMessage(queue.OrderMessage)} và bao gồm 6 bước:
 * trích đơn -> khóa trạng thái -> khóa ví -> khóa tồn kho/credential -> trừ tiền + ghi giao dịch -> đánh dấu hoàn thành.</p>
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
     * Nhận thông điệp từ hàng đợi và xử lý với cơ chế retry tuyến tính (5s, 15s, 30s).
     * Nếu hết số lần retry mà vẫn lỗi SQL sẽ đánh dấu đơn Failed để controller/JSP thông báo cho người mua.
     */
    @Override
    public void handle(OrderMessage msg) {
        for (int attempt = 0; attempt < RETRY_DELAYS.length; attempt++) {
            try {
                processMessage(msg);
                return;
            } catch (OrderProcessingException ex) {
                LOGGER.log(ex.level(), "Đơn hàng " + msg.orderId() + " thất bại: " + ex.getMessage(), ex);
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
            return;
        }
        try (Connection connection = orderDAO.openConnection()) {
            connection.setAutoCommit(false);
            try {
                // B1: Đánh dấu đơn hàng đang xử lý để đảm bảo các worker khác không xử lý trùng.
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.PROCESSING);
                // B2: Khóa ví của người mua ở mức hàng (SELECT ... FOR UPDATE) để cố định số dư.
                Wallets wallet = walletsDAO.lockWalletForUpdate(connection, order.getBuyerId());
                if (wallet == null || Boolean.FALSE.equals(wallet.getStatus())) {
                    failWithReason(connection, order, "Ví của người dùng không khả dụng", Level.WARNING);
                }
                BigDecimal totalAmount = order.getTotalAmount();
                if (totalAmount == null) {
                    failWithReason(connection, order, "Đơn hàng thiếu thông tin tổng tiền", Level.SEVERE);
                }
                BigDecimal balanceBefore = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
                if (balanceBefore.compareTo(totalAmount) < 0) {
                    failWithReason(connection, order, "Ví người dùng không đủ số dư", Level.INFO);
                }
                // B3: Khóa tồn kho sản phẩm và credential để đảm bảo đủ hàng bàn giao.
                String resolvedVariantCode = msg.variantCode();
                if (resolvedVariantCode == null || resolvedVariantCode.isBlank()) {
                    resolvedVariantCode = order.getVariantCode();
                }
                String normalizedVariantCode = ProductVariantUtils.normalizeCode(resolvedVariantCode);
                ProductDAO.ProductInventoryLock lockedProduct = productDAO.lockProductForUpdate(connection, msg.productId());
                List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
                        lockedProduct.variantSchema(), lockedProduct.variantsJson());
                int inventory = calculateAvailableInventory(lockedProduct, variants);
                if (inventory < msg.qty()) {
                    failWithReason(connection, order, "Tổng tồn kho sản phẩm không đủ", Level.WARNING);
                }
                ProductVariantOption variant = null;
                if (normalizedVariantCode != null) {
                    Optional<ProductVariantOption> variantOpt = ProductVariantUtils.findVariant(variants, normalizedVariantCode);
                    if (variantOpt.isEmpty() || !variantOpt.get().isAvailable()) {
                        failWithReason(connection, order, "Biến thể sản phẩm không khả dụng", Level.WARNING);
                    }
                    variant = variantOpt.get();
                    Integer variantInventory = variant.getInventoryCount();
                    if (variantInventory == null || variantInventory < msg.qty()) {
                        failWithReason(connection, order, "Biến thể sản phẩm không đủ tồn kho", Level.WARNING);
                    }
                }
                List<Integer> credentialIds = credentialDAO.pickFreeCredentialIds(connection, msg.productId(), msg.qty(), normalizedVariantCode);
                if (credentialIds.size() < msg.qty()) {
                    failWithReason(connection, order, "Không đủ credential sẵn sàng để giao", Level.WARNING);
                }
                boolean decremented = productDAO.decrementInventory(connection, msg.productId(), msg.qty());
                if (!decremented) {
                    throw new SQLException("Không thể trừ tồn kho");
                }
                if (variant != null) {
                    ProductVariantUtils.decreaseInventory(variant, msg.qty());
                    String updatedJson = ProductVariantUtils.toJson(variants);
                    productDAO.updateVariantsJson(connection, msg.productId(), updatedJson);
                }
                // B4: Trừ tiền trong ví và ghi nhận giao dịch thanh toán để log lại luồng tiền.
                BigDecimal balanceAfter = balanceBefore.subtract(totalAmount);
                boolean balanceUpdated = walletsDAO.updateBalance(connection, wallet.getId(), balanceAfter);
                if (!balanceUpdated) {
                    throw new SQLException("Không thể cập nhật số dư ví");
                }
                String note = "Thanh toán đơn hàng #" + order.getId();
                int walletTxId = walletTransactionDAO.insertTransaction(connection, wallet.getId(), order.getId(),
                        TransactionType.PURCHASE, totalAmount.negate(), balanceBefore, balanceAfter, note);
                orderDAO.assignPaymentTransaction(connection, order.getId(), walletTxId);
                // B5: Giao credential cho người mua và ghi nhận log tồn kho phục vụ kiểm toán.
                credentialDAO.markCredentialsSold(connection, msg.orderId(), credentialIds);
                orderDAO.insertInventoryLog(connection, msg.productId(), msg.orderId(), -msg.qty(), "Sale");
                // B6: Hoàn tất đơn hàng và commit transaction.
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.COMPLETED);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
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
     * Đánh dấu đơn hàng thất bại, commit giao dịch rồi ném ngoại lệ để {@link #handle(OrderMessage)} log chi tiết.
     */
    private void failWithReason(Connection connection, Orders order, String reason, Level level) throws SQLException {
        orderDAO.updateStatus(connection, order.getId(), OrderStatus.FAILED);
        connection.commit();
        throw new OrderProcessingException(reason, level);
    }

    /**
     * Ngoại lệ runtime dành riêng cho các tình huống nghiệp vụ khiến đơn hàng thất bại.
     */
    private static final class OrderProcessingException extends RuntimeException {

        private final Level level;

        OrderProcessingException(String message, Level level) {
            super(message);
            this.level = level == null ? Level.WARNING : level;
        }

        Level level() {
            return level;
        }
    }
}
