package queue.memory;

import dao.order.CredentialDAO;
import dao.order.OrderDAO;
import dao.product.ProductDAO;
import dao.user.WalletTransactionDAO;
import dao.user.WalletsDAO;
import queue.OrderMessage;
import queue.OrderQueueProducer;
import queue.OrderWorker;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InMemoryOrderQueue implements OrderQueueProducer {

    private static final Logger LOGGER = Logger.getLogger(InMemoryOrderQueue.class.getName());
    private static final InMemoryOrderQueue INSTANCE = new InMemoryOrderQueue();

    private final BlockingQueue<OrderMessage> queue = new LinkedBlockingQueue<>();
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "order-queue-dispatcher");
        thread.setDaemon(true);
        return thread;
    });

    private volatile OrderWorker worker;

    private InMemoryOrderQueue() {
        dispatcher.submit(this::dispatchLoop);
    }

    public static InMemoryOrderQueue getInstance() {
        return INSTANCE;
    }

    public static synchronized void ensureWorkerInitialized(OrderDAO orderDAO, ProductDAO productDAO,
            CredentialDAO credentialDAO, WalletsDAO walletsDAO, WalletTransactionDAO walletTransactionDAO) {
        // Khởi tạo worker duy nhất cho hàng đợi, đảm bảo có đủ DAO phục vụ xử lý ví và đơn hàng.
        if (INSTANCE.worker == null) {
            Objects.requireNonNull(orderDAO, "orderDAO");
            Objects.requireNonNull(productDAO, "productDAO");
            Objects.requireNonNull(credentialDAO, "credentialDAO");
            Objects.requireNonNull(walletsDAO, "walletsDAO");
            Objects.requireNonNull(walletTransactionDAO, "walletTransactionDAO");
            INSTANCE.worker = new AsyncOrderWorker(orderDAO, productDAO, credentialDAO, walletsDAO, walletTransactionDAO);
        }
    }

    @Override
    public void publish(int orderId, String idemKey, int productId, int qty) {
        queue.offer(new OrderMessage(orderId, idemKey, productId, qty));
    }

    private void dispatchLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                OrderMessage message = queue.poll(5, TimeUnit.SECONDS);
                if (message == null) {
                    continue;
                }
                OrderWorker current = worker;
                if (current == null) {
                    queue.offer(message);
                    Thread.sleep(1000);
                    continue;
                }
                try {
                    current.handle(message);
                } catch (RuntimeException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi xử lý thông điệp đơn hàng", ex);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
