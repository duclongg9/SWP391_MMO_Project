package model;

/**
 * Order states supported by the asynchronous checkout pipeline.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    DELIVERED,
    REFUNDED,
    CANCELLED;

    public static OrderStatus fromDatabaseValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "CONFIRMED" -> CONFIRMED;
            case "FAILED" -> FAILED;
            case "DELIVERED" -> DELIVERED;
            case "REFUNDED" -> REFUNDED;
            case "CANCELLED" -> CANCELLED;
            default -> null;
        };
    }

    public String toDatabaseValue() {
        return switch (this) {
            case PENDING -> "PENDING";
            case CONFIRMED -> "CONFIRMED";
            case FAILED -> "FAILED";
            case DELIVERED -> "DELIVERED";
            case REFUNDED -> "REFUNDED";
            case CANCELLED -> "CANCELLED";
        };
    }
}
