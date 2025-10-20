package service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

public record WalletOverview(BigDecimal balance, BigDecimal holdBalance, String currency,
                             List<Transaction> transactions) {

    public record Transaction(long id, String type, BigDecimal amount,
                              BigDecimal balanceBefore, BigDecimal balanceAfter,
                              String note, Date createdAt) {
    }
}
