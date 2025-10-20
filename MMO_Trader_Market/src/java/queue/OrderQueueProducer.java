package queue;

public interface OrderQueueProducer {
    void publish(int orderId, String idemKey, int productId, int qty);
}
