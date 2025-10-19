package queue;

import worker.OrderWorker;

/**
 * Lightweight producer that forwards order events to {@link OrderWorker}. A
 * real implementation would integrate with a message broker but a background
 * worker thread is sufficient for the application.
 */
public class OrderQueueProducer {

    private static final OrderQueueProducer INSTANCE = new OrderQueueProducer();
    private final OrderWorker worker = OrderWorker.getInstance();

    private OrderQueueProducer() {
        worker.ensureStarted();
    }

    public static OrderQueueProducer getInstance() {
        return INSTANCE;
    }

    public void publish(int orderId, String orderToken) {
        worker.enqueue(new OrderMessage(orderId, orderToken));
    }

    public record OrderMessage(int orderId, String orderToken) {
    }
}
