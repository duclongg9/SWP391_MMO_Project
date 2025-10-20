package queue;

/**
 * Immutable message describing an order to be processed asynchronously.
 */
public record OrderMessage(int orderId, String idempotencyKey, int productId, int qty) {
}
