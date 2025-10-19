package model;

/**
 * Enumeration that describes the current lifecycle state of a buyer order.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    CONFIRMED("Confirmed"),
    DELIVERED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    DISPUTED("Disputed");

    private final String databaseValue;

    OrderStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public static OrderStatus fromDatabaseValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "PROCESSING" -> PROCESSING;
            case "CONFIRMED" -> CONFIRMED;
            case "DELIVERED", "COMPLETED" -> DELIVERED;
            case "FAILED" -> FAILED;
            case "REFUNDED" -> REFUNDED;
            case "DISPUTED" -> DISPUTED;
            default -> null;
        };
    }

    public String toDatabaseValue() {
        return databaseValue;
    }
}
