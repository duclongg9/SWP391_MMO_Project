package model.view;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * Lightweight view model for order listing rows.
 */
public class OrderRow {
    private final int id;
    private final String productName;
    private final BigDecimal totalAmount;
    private final String status;
    private final Date createdAt;
 
    public record OrderRow(int id, String productName, BigDecimal totalAmount, String status, Date createdAt) {

    /**
     * JSP EL resolves bean properties via conventional getters. Records only
     * expose accessor methods matching the component name (for example
     * {@code id()}). Tomcat's EL implementation does not treat those as bean
     * properties, so we provide explicit getter aliases to keep the view logic
     * unchanged.
     */
    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }


    @Override
    public String toString() {
        return "OrderRow{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderRow orderRow)) return false;
        return id == orderRow.id &&
                Objects.equals(productName, orderRow.productName) &&
                Objects.equals(totalAmount, orderRow.totalAmount) &&
                Objects.equals(status, orderRow.status) &&
                Objects.equals(createdAt, orderRow.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productName, totalAmount, status, createdAt);
    }
}
