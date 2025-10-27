/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author D E L L
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    PURCHASE("Purchase"),
    WITHDRAWAL("Withdrawal"),
    REFUND("Refund"),
    FEE("Fee"),
    PAYOUT("Payout");

    private final String dbValue;

    TransactionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    private static final Map<String, TransactionType> BY_DB
            = Stream.of(values()).collect(Collectors.toMap(
                    TransactionType::getDbValue, e -> e
            ));

    /**
     * Map từ chuỗi trong DB -> enum
     */
    public static TransactionType fromDbValue(String dbValue) {
        TransactionType t = BY_DB.get(dbValue);
        if (t == null) {
            throw new IllegalArgumentException("Unknown transaction_type: " + dbValue);
        }
        return t;
    }
}
