package model.view;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kết quả kiểm tra nhanh trước khi tạo đơn hàng để giao diện quyết định có cho phép
 * người mua tiếp tục quy trình thanh toán hay không.
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
