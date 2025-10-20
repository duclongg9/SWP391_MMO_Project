package model.view;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Lightweight view model for order listing rows.
 */
public record OrderRow(int id, String productName, BigDecimal totalAmount, String status, Date createdAt) {
}
