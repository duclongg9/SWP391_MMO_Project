package model;

/**
 * Enumeration that describes the current lifecycle state of a buyer order.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    DISPUTED;

    public static OrderStatus fromDatabaseValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "PROCESSING" -> PROCESSING;
            case "COMPLETED" -> COMPLETED;
            case "FAILED" -> FAILED;
            case "REFUNDED" -> REFUNDED;
            case "DISPUTED" -> DISPUTED;
            default -> null;
        };
    }

    public String toDatabaseValue() {
        return switch (this) {
            case PENDING -> "Pending";
            case PROCESSING -> "Processing";
            case COMPLETED -> "Completed";
            case FAILED -> "Failed";
            case REFUNDED -> "Refunded";
            case DISPUTED -> "Disputed";
        };
    }
}
