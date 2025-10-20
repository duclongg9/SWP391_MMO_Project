package queue.memory;

import dao.order.CredentialDAO;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import model.OrderStatus;
import model.Orders;
import queue.OrderMessage;
import queue.OrderWorker;

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

    public AsyncOrderWorker(OrderDAO orderDAO, ProductDAO productDAO, CredentialDAO credentialDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.credentialDAO = credentialDAO;
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
                orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.PROCESSING);
                int inventory = productDAO.lockInventoryForUpdate(connection, msg.productId());
                if (inventory < msg.qty()) {
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                List<Integer> credentialIds = credentialDAO.pickFreeCredentialIds(connection, msg.productId(), msg.qty());
                if (credentialIds.size() < msg.qty()) {
                    orderDAO.updateStatus(connection, msg.orderId(), OrderStatus.FAILED);
                    connection.commit();
                    return;
                }
                boolean decremented = productDAO.decrementInventory(connection, msg.productId(), msg.qty());
                if (!decremented) {
                    throw new SQLException("Không thể trừ tồn kho");
                }
                credentialDAO.markCredentialsSold(connection, msg.orderId(), credentialIds);
                orderDAO.insertInventoryLog(connection, msg.productId(), msg.orderId(), -msg.qty(), "Sale");
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
