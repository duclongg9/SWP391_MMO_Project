package model.view;

import java.math.BigDecimal;
import java.util.Date;

/**
 * View model mô tả từng bước liên quan tới ví khi xử lý đơn hàng.
 * <p>
 * Mỗi sự kiện tương ứng với một hành động trong worker: đưa đơn vào hàng
 * đợi, khóa ví, ghi nhận giao dịch... giúp giao diện trình bày luồng tiền dưới
 * dạng timeline dễ hiểu cho người mua.</p>
 */
public class OrderWalletEvent {

    private final String code;
    private final String title;
    private final String description;
    private final Date occurredAt;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String reference;
    private final boolean primary;

    public OrderWalletEvent(String code, String title, String description, Date occurredAt,
            BigDecimal amount, BigDecimal balanceAfter, String reference, boolean primary) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.occurredAt = copyDate(occurredAt);
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reference = reference;
        this.primary = primary;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getOccurredAt() {
        return copyDate(occurredAt);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReference() {
        return reference;
    }

    public boolean isPrimary() {
        return primary;
    }

    private static Date copyDate(Date input) {
        return input == null ? null : new Date(input.getTime());
    }
}
