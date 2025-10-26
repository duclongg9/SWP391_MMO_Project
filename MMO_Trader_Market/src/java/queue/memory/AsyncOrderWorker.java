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

    @Override
    public void handle(OrderMessage msg) {
        for (int attempt = 0; attempt < RETRY_DELAYS.length; attempt++) {
            try {
                processMessage(msg);
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
                // 1. Đánh dấu đơn hàng đang xử lý để tránh các luồng khác can thiệp song song.
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.PROCESSING);
                // 2. Khóa ví của người mua ở mức hàng để đảm bảo số dư không bị thay đổi ngoài ý muốn.
                Wallets wallet = walletsDAO.lockWalletForUpdate(connection, order.getBuyerId());
                if (wallet == null || Boolean.FALSE.equals(wallet.getStatus())) {
                    LOGGER.log(Level.WARNING, "Ví của người dùng {0} không khả dụng", order.getBuyerId());
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                BigDecimal totalAmount = order.getTotalAmount();
                if (totalAmount == null) {
                    LOGGER.log(Level.SEVERE, "Đơn hàng {0} thiếu thông tin tổng tiền", order.getId());
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                BigDecimal balanceBefore = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
                if (balanceBefore.compareTo(totalAmount) < 0) {
                    LOGGER.log(Level.INFO, "Ví người dùng {0} không đủ số dư cho đơn {1}",
                            new Object[]{order.getBuyerId(), order.getId()});
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                // 3. Khóa tồn kho và credential của sản phẩm để chắc chắn còn đủ hàng.
                Integer variantId = msg.variantId();
                String variantCode = msg.variantCode();
                if (variantId == null) {
                    variantId = order.getVariantId();
                }
                if (variantCode == null || variantCode.isBlank()) {
                    variantCode = order.getVariantCode();
                }
                ProductDAO.ProductInventoryLock lockedProduct = productDAO.lockProductForUpdate(connection, msg.productId());
                int inventory = lockedProduct.inventoryCount() == null ? 0 : lockedProduct.inventoryCount();
                if (inventory < msg.qty()) {
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                List<ProductVariantOption> variants = ProductVariantUtils.parseVariants(
                        lockedProduct.variantSchema(), lockedProduct.variantsJson());
                ProductVariantOption variant = null;
                if (variantId != null || (variantCode != null && !variantCode.isBlank())) {
                    Optional<ProductVariantOption> variantOpt;
                    if (variantId != null) {
                        variantOpt = ProductVariantUtils.findVariantById(variants, variantId);
                    } else {
                        String normalized = ProductVariantUtils.normalizeCode(variantCode);
                        variantOpt = ProductVariantUtils.findVariant(variants, normalized);
                    }
                    if (variantOpt.isEmpty() || !variantOpt.get().isAvailable()) {
                        orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                        connection.commit();
                        return;
                    }
                    variant = variantOpt.get();
                    variantId = variant.getVariantId();
                    variantCode = variant.getVariantCode();
                    Integer variantInventory = variant.getInventoryCount();
                    if (variantInventory == null || variantInventory < msg.qty()) {
                        orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                        connection.commit();
                        return;
                    }
                }
                List<Integer> credentialIds = credentialDAO.pickFreeCredentialIds(connection, msg.productId(), msg.qty(), variantId, variantCode);
                if (credentialIds.size() < msg.qty()) {
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                Integer variantIdForUpdate = variant == null ? null : variant.getVariantId();
                boolean decremented = productDAO.decrementInventory(connection, msg.productId(), variantIdForUpdate, msg.qty());
                if (!decremented) {
                    throw new SQLException("Không thể trừ tồn kho");
                }
                // 4. Trừ tiền trong ví và ghi nhận giao dịch thanh toán.
                BigDecimal balanceAfter = balanceBefore.subtract(totalAmount);
                boolean balanceUpdated = walletsDAO.updateBalance(connection, wallet.getId(), balanceAfter);
                if (!balanceUpdated) {
                    throw new SQLException("Không thể cập nhật số dư ví");
                }
                String note = "Thanh toán đơn hàng #" + order.getId();
                int walletTxId = walletTransactionDAO.insertTransaction(connection, wallet.getId(), order.getId(),
                        TransactionType.PURCHASE, totalAmount.negate(), balanceBefore, balanceAfter, note);
                orderDAO.assignPaymentTransaction(connection, order.getId(), walletTxId);
                // 5. Giao credential cho người mua và ghi nhận log tồn kho.
                credentialDAO.markCredentialsSold(connection, msg.orderId(), credentialIds);
                orderDAO.insertInventoryLog(connection, msg.productId(), msg.orderId(), -msg.qty(), "Sale");
                // 6. Hoàn tất đơn hàng.
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
}
