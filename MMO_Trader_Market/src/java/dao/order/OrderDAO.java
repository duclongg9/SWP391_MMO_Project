package dao.order;

import dao.BaseDAO;
import dao.product.ProductDAO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import model.Order;
import model.OrderStatus;
import model.Product;

/**
 * Provides persistence-like operations for buyer orders using an in-memory store.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public class OrderDAO extends BaseDAO {

    private static final List<Order> ORDERS = new CopyOnWriteArrayList<>();
    private static final AtomicInteger SEQUENCE = new AtomicInteger(2000);

    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Returns a snapshot of all orders. The first call seeds demo data.
     * @return list of stored orders
     */
    public List<Order> findAll() {
        seedSampleOrders();
        return new ArrayList<>(ORDERS);
    }

    /**
     * Persists a new order into the in-memory store.
     * @param product product being purchased
     * @param buyerEmail email used for delivery
     * @param paymentMethod selected payment channel
     * @return created order entity
     */
    public Order save(Product product, String buyerEmail, String paymentMethod) {
        int id = SEQUENCE.incrementAndGet();
        Order order = new Order(id, product, buyerEmail, paymentMethod,
                OrderStatus.PROCESSING, LocalDateTime.now());
        ORDERS.add(order);
        return order;
    }

    /**
     * Finds an order by id.
     * @param orderId order identifier
     * @return optional order instance
     */
    public Optional<Order> findById(int orderId) {
        return ORDERS.stream()
                .filter(order -> order.getId() == orderId)
                .findFirst();
    }

    private synchronized void seedSampleOrders() {
        if (!ORDERS.isEmpty()) {
            return;
        }
        List<Product> products = productDAO.findAll();
        if (products.isEmpty()) {
            return;
        }
        if (products.size() >= 2) {
            Order completed = new Order(SEQUENCE.incrementAndGet(), products.get(1),
                    "buyer.demo@example.com", "Ví điện tử",
                    OrderStatus.COMPLETED, LocalDateTime.now().minusDays(2));
            completed.markCompleted(generateActivationCode(completed),
                    generateDeliveryLink(completed));
            ORDERS.add(completed);
        }
        if (!products.isEmpty()) {
            Order processing = new Order(SEQUENCE.incrementAndGet(), products.get(0),
                    "pro.buyer@example.com", "Chuyển khoản ngân hàng",
                    OrderStatus.PROCESSING, LocalDateTime.now().minusHours(6));
            ORDERS.add(processing);
        }
        if (products.size() >= 3) {
            Order disputed = new Order(SEQUENCE.incrementAndGet(), products.get(2),
                    "claim@example.com", "Thẻ nội địa",
                    OrderStatus.DISPUTED, LocalDateTime.now().minusDays(5));
            disputed.markDisputed(generateActivationCode(disputed));
            ORDERS.add(disputed);
        }
    }

    /**
     * Generates a readable activation code for digital products.
     * @param order order that needs an activation code
     * @return generated activation key
     */
    public String generateActivationCode(Order order) {
        return String.format(Locale.ROOT, "%s-%04d",
                order.getProduct().getName().replaceAll("\\s+", "").toUpperCase(Locale.ROOT),
                order.getId() % 10000);
    }

    /**
     * Generates a placeholder download link for the purchased item.
     * @param order order that needs a download link
     * @return delivery URL
     */
    public String generateDeliveryLink(Order order) {
        return String.format(Locale.ROOT,
                "https://demo.mmo-trader.local/orders/%d/delivery", order.getId());
    }
}
