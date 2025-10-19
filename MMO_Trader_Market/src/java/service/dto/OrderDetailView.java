package service.dto;

import model.Order;

import java.util.List;

/**
 * View model for order detail page.
 */
public class OrderDetailView {

    private final Order order;
    private final List<String> credentials;

    public OrderDetailView(Order order, List<String> credentials) {
        this.order = order;
        this.credentials = List.copyOf(credentials);
    }

    public Order getOrder() {
        return order;
    }

    public List<String> getCredentials() {
        return credentials;
    }
}
