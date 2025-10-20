package worker;

import dao.order.OrderDAO;
import model.Order;
import model.OrderStatus;
import queue.OrderQueueProducer;
import service.InventoryService;
import service.WalletService;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background worker that simulates consuming {@link OrderQueueProducer.OrderMessage}
 * events. The worker enforces idempotency by checking the order status before
 * attempting to reserve inventory and capture the wallet hold. Failures are
 * retried with a simple exponential backoff and eventually routed to an
 * in-memory dead letter queue.
 */
public final class OrderWorker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(OrderWorker.class.getName());
    private static final int MAX_ATTEMPTS = 3;
    private static final long BACKOFF_BASE_MS = 300L;
    private static final OrderWorker INSTANCE = new OrderWorker();

    private final BlockingQueue<OrderQueueProducer.OrderMessage> queue = new LinkedBlockingQueue<>();
    private final BlockingQueue<OrderQueueProducer.OrderMessage> deadLetterQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    private final OrderDAO orderDAO = new OrderDAO();
    private final InventoryService inventoryService = new InventoryService();
    private final WalletService walletService = new WalletService();

    private OrderWorker() {
    }

    public static OrderWorker getInstance() {
        return INSTANCE;
    }

    public void ensureStarted() {
        if (started.compareAndSet(false, true)) {
            Thread workerThread = new Thread(this, "order-worker-thread");
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    public void enqueue(OrderQueueProducer.OrderMessage message) {
        if (message != null) {
            queue.offer(message);
        }
    }

    public BlockingQueue<OrderQueueProducer.OrderMessage> getDeadLetterQueue() {
        return deadLetterQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                OrderQueueProducer.OrderMessage message = queue.take();
                processWithRetry(message);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void processWithRetry(OrderQueueProducer.OrderMessage message) {
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            try {
                if (process(message)) {
                    return;
                }
                // Terminal failure without exception -> stop retrying
                return;
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Order processing attempt {0} failed for order {1}",
                        new Object[]{attempt, message.orderId()});
                if (attempt >= MAX_ATTEMPTS) {
                    deadLetterQueue.offer(message);
                    return;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(BACKOFF_BASE_MS * attempt);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private boolean process(OrderQueueProducer.OrderMessage message) {
        Optional<Order> optionalOrder = orderDAO.findByIdAndToken(message.orderId(), message.orderToken());
        if (optionalOrder.isEmpty()) {
            LOGGER.log(Level.FINE, "Order {0} not found or token mismatch", message.orderId());
            return true;
        }
        Order order = optionalOrder.get();
        if (order.getStatus() != OrderStatus.PENDING) {
            LOGGER.log(Level.FINE, "Order {0} already processed with status {1}",
                    new Object[]{order.getId(), order.getStatus()});
            return true;
        }

        int quantity = order.getQuantity() == null || order.getQuantity() <= 0 ? 1 : order.getQuantity();
        int productId = order.getProduct() == null ? -1 : order.getProduct().getId();
        if (productId <= 0) {
            LOGGER.log(Level.WARNING, "Order {0} is missing product information", order.getId());
            orderDAO.updateStatus(order.getId(), message.orderToken(), OrderStatus.FAILED);
            safeReleaseHold(message.orderToken());
            return true;
        }

        boolean reserved;
        try {
            reserved = inventoryService.reserve(productId, quantity);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Unable to reserve inventory for order {0}: {1}",
                    new Object[]{order.getId(), ex.getMessage()});
            orderDAO.updateStatus(order.getId(), message.orderToken(), OrderStatus.FAILED);
            safeReleaseHold(message.orderToken());
            return true;
        }

        if (!reserved) {
            LOGGER.log(Level.WARNING, "Inventory not available for order {0}", order.getId());
            orderDAO.updateStatus(order.getId(), message.orderToken(), OrderStatus.FAILED);
            safeReleaseHold(message.orderToken());
            return true;
        }

        try {
            walletService.capture(message.orderToken());
            orderDAO.assignCredentialToOrder(order.getId(), productId);
            if (!orderDAO.updateStatus(order.getId(), message.orderToken(), OrderStatus.CONFIRMED)) {
                throw new RuntimeException("Failed to update order status to confirmed");
            }
            return true;
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Unable to capture wallet hold for order {0}: {1}",
                    new Object[]{order.getId(), ex.getMessage()});
            inventoryService.release(productId, quantity);
            orderDAO.updateStatus(order.getId(), message.orderToken(), OrderStatus.FAILED);
            safeReleaseHold(message.orderToken());
            return true;
        } catch (RuntimeException ex) {
            inventoryService.release(productId, quantity);
            safeReleaseHold(message.orderToken());
            throw ex;
        }
    }

    private void safeReleaseHold(String orderToken) {
        try {
            if (walletService.hasActiveHold(orderToken)) {
                walletService.release(orderToken);
            }
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Failed to release wallet hold for token {0}: {1}",
                    new Object[]{orderToken, ex.getMessage()});
        }
    }
}
