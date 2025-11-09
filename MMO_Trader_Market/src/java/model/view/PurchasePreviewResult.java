package model.view;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ket qua kiem tra nhanh truoc khi tao don hang de giao dien quyet dinh co cho phep
 * nguoi mua tiep tuc quy trinh thanh toan hay khong.
 */
public record PurchasePreviewResult(
        boolean ok,
        boolean canPurchase,
        boolean productAvailable,
        boolean variantValid,
        boolean hasInventory,
        boolean hasCredentials,
        boolean walletExists,
        boolean walletActive,
        boolean walletHasBalance,
        int availableInventory,
        int availableCredentials,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        BigDecimal walletBalance,
        List<String> blockers
) {
}
