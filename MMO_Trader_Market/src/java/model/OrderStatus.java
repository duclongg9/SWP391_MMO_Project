package model;

/**
 * Enumeration that describes the current lifecycle state of a buyer order.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PROCESSING,
    COMPLETED,
    DISPUTED
}
