package model;

/**
 * Order states supported by the asynchronous checkout pipeline.
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
            case PENDING -> "PENDING";
            case PROCESSING -> "PROCESSING";
            case COMPLETED -> "COMPLETED";
            case FAILED -> "FAILED";
            case REFUNDED -> "REFUNDED";
            case DISPUTED -> "DISPUTED";
        };
    }
}
