package service;

import dao.order.CredentialDAO;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.support.DisputeDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import model.DisputeStatus;
import model.Disputes;
import model.Orders;
import model.TransactionType;
import model.Wallets;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * Dịch vụ dành cho admin xử lý vòng đời khiếu nại: chuyển trạng thái, hoàn tiền
 * hoặc tiếp tục escrow.
 */
public class DisputeResolutionService {

    /**
     * Hành động xử lý dispute từ phía admin.
     */
    public enum ResolutionAction {
        IN_REVIEW,
        ACCEPT,
        REJECT
    }

    private final DisputeDAO disputeDAO;
    private final OrderDAO orderDAO;
    private final WalletsDAO walletsDAO;
    private final WalletTransactionDAO walletTransactionDAO;
    private final CredentialDAO credentialDAO;
    private final ProductDAO productDAO;

    public DisputeResolutionService() {
        this(new DisputeDAO(), new OrderDAO(), new WalletsDAO(), new WalletTransactionDAO(), new CredentialDAO(),
                new ProductDAO());
    }

    public DisputeResolutionService(DisputeDAO disputeDAO, OrderDAO orderDAO, WalletsDAO walletsDAO,
                                    WalletTransactionDAO walletTransactionDAO, CredentialDAO credentialDAO, ProductDAO productDAO) {
        this.disputeDAO = disputeDAO;
        this.orderDAO = orderDAO;
        this.walletsDAO = walletsDAO;
        this.walletTransactionDAO = walletTransactionDAO;
        this.credentialDAO = credentialDAO;
        this.productDAO = productDAO;
    }

    /**
     * Xử lý khiếu nại theo hành động được lựa chọn.
     *
     * @param disputeId mã khiếu nại cần xử lý
     * @param action    hành động (IN_REVIEW/ACCEPT/REJECT)
     * @param adminId   admin đang thao tác (có thể null)
     * @param note      ghi chú xử lý
     */
    public void handle(int disputeId, ResolutionAction action, Integer adminId, String note) {
        try (Connection connection = disputeDAO.createConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Disputes dispute = disputeDAO.findByIdForUpdate(connection, disputeId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khiếu nại #" + disputeId));
                ensureNotResolved(dispute);
                switch (action) {
                    case IN_REVIEW -> markInReview(connection, dispute, adminId, note);
                    case ACCEPT -> resolveWithRefund(connection, dispute, adminId, note);
                    case REJECT -> resolveWithoutRefund(connection, dispute, adminId, note);
                    default -> throw new IllegalArgumentException("Hành động không hợp lệ");
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof RuntimeException runtime) {
                    throw runtime;
                }
                String message = ex.getMessage();
                if (message == null || message.isBlank()) {
                    Throwable cause = ex.getCause();
                    while (cause != null && (cause.getMessage() == null || cause.getMessage().isBlank())) {
                        cause = cause.getCause();
                    }
                    message = (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank())
                            ? cause.getMessage()
                            : "Không thể xử lý khiếu nại";
                }
                throw new IllegalStateException(message, ex);
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException ex) {
            String message = ex.getMessage();
            if (message == null || message.isBlank()) {
                Throwable cause = ex.getCause();
                while (cause != null && (cause.getMessage() == null || cause.getMessage().isBlank())) {
                    cause = cause.getCause();
                }
                message = (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank())
                        ? cause.getMessage()
                        : "Không thể xử lý khiếu nại";
            }
            throw new IllegalStateException(message, ex);
        }
    }

    private void markInReview(Connection connection, Disputes dispute, Integer adminId, String note) throws SQLException {
        disputeDAO.updateStatus(connection, dispute.getId(), DisputeStatus.IN_REVIEW, adminId, trimToNull(note), null);
    }

    private void resolveWithRefund(Connection connection, Disputes dispute, Integer adminId, String note)
            throws SQLException {
        Orders order = orderDAO.findByIdForUpdate(connection, dispute.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đơn hàng liên quan"));

        Wallets wallet = walletsDAO.lockWalletForUpdate(connection, order.getBuyerId());
        if (wallet == null) {
            throw new IllegalStateException("Không thể khóa ví của người mua");
        }

        BigDecimal totalAmount = Optional.ofNullable(order.getTotalAmount())
                .orElseThrow(() -> new IllegalStateException("Đơn hàng thiếu thông tin tổng tiền"));

        BigDecimal balanceBefore = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(totalAmount);

        boolean balanceUpdated = walletsDAO.updateBalance(connection, wallet.getId(), balanceAfter);
        if (!balanceUpdated) {
            throw new IllegalStateException("Không thể cập nhật số dư ví");
        }

        String transactionNote = "Hoàn tiền tranh chấp đơn #" + order.getId();
        walletTransactionDAO.insertTransaction(connection, wallet.getId(), order.getId(), TransactionType.REFUND,
                totalAmount, balanceBefore, balanceAfter, transactionNote);

        // Trả lại/thu hồi credential & restock tồn kho
        credentialDAO.releaseCredentialsForOrder(connection, order.getId());
        Integer quantity = order.getQuantity();
        if (quantity != null && quantity > 0) {
            productDAO.incrementInventoryWithVariant(connection, order.getProductId(), order.getVariantCode(), quantity);
            orderDAO.insertInventoryLog(connection, order.getProductId(), order.getId(), quantity,
                    "Restock after dispute refund");
        }

        boolean cancelled = orderDAO.cancelEscrowForRefund(connection, order.getId());
        if (!cancelled) {
            throw new IllegalStateException("Không thể cập nhật trạng thái escrow của đơn hàng");
        }
        orderDAO.insertEscrowCancelledEvent(connection, order.getId(), adminId, dispute.getId(),
                "{\"action\":\"ACCEPT\"}");

        Timestamp resolvedAt = Timestamp.from(Instant.now());
        disputeDAO.updateStatus(connection, dispute.getId(), DisputeStatus.RESOLVED_WITH_REFUND, adminId,
                trimToNull(note), resolvedAt);
    }

    private void resolveWithoutRefund(Connection connection, Disputes dispute, Integer adminId, String note)
            throws SQLException {
        Orders order = orderDAO.findByIdForUpdate(connection, dispute.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đơn hàng liên quan"));

        Integer remainingSeconds = resolveEscrowSeconds(order, dispute);

        Timestamp resumedAt = Timestamp.from(Instant.now());
        Timestamp releaseAt = null;
        if (remainingSeconds != null && remainingSeconds > 0) {
            releaseAt = new Timestamp(resumedAt.getTime() + remainingSeconds * 1000L);
        }

        boolean resumed = orderDAO.resumeEscrowAfterDispute(connection, order.getId(), resumedAt, releaseAt);
        if (!resumed) {
            throw new IllegalStateException("Không thể khôi phục escrow cho đơn hàng");
        }
        orderDAO.insertEscrowResumedEvent(connection, order.getId(), resumedAt, releaseAt, remainingSeconds, adminId,
                dispute.getId(), "{\"action\":\"REJECT\"}");

        Timestamp resolvedAt = Timestamp.from(Instant.now());
        disputeDAO.updateStatus(connection, dispute.getId(), DisputeStatus.RESOLVED_WITHOUT_REFUND, adminId,
                trimToNull(note), resolvedAt);
    }

    private Integer resolveEscrowSeconds(Orders order, Disputes dispute) {
        if (dispute != null && dispute.getEscrowRemainingSeconds() != null
                && dispute.getEscrowRemainingSeconds() > 0) {
            return dispute.getEscrowRemainingSeconds();
        }
        if (order.getEscrowRemainingSeconds() != null && order.getEscrowRemainingSeconds() > 0) {
            return order.getEscrowRemainingSeconds();
        }
        return order.getEscrowHoldSeconds();
    }

    private void ensureNotResolved(Disputes dispute) {
        if (dispute == null) {
            return;
        }
        String status = dispute.getStatus();
        if (status == null) {
            return;
        }
        DisputeStatus.fromDatabaseValue(status).ifPresentOrElse(current -> {
            if (current == DisputeStatus.RESOLVED_WITH_REFUND
                    || current == DisputeStatus.RESOLVED_WITHOUT_REFUND
                    || current == DisputeStatus.CLOSED
                    || current == DisputeStatus.CANCELLED) {
                throw new IllegalStateException("Khiếu nại đã được xử lý trước đó");
            }
        }, () -> {
            String normalized = status.trim().toUpperCase(Locale.ROOT);
            if ("RESOLVED".equals(normalized)) {
                throw new IllegalStateException("Khiếu nại đã được xử lý trước đó");
            }
        });
    }

    /** Helper: cắt chuỗi, trả về null nếu rỗng */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
