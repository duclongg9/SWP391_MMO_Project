package model;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum liệt kê các trạng thái hợp lệ của khiếu nại trong hệ thống.
 * <p>
 * Mỗi hằng lưu giá trị chính xác dùng trong cơ sở dữ liệu để tránh lỗi gõ sai
 * dẫn tới {@code Data truncated for column 'status'}.
 */
public enum DisputeStatus {

    /** Khiếu nại vừa được tạo bởi người mua. */
    OPEN("Open"),

    /** Khiếu nại đang được admin xem xét. */
    IN_REVIEW("InReview"),

    /** Khiếu nại đã được giải quyết với phương án hoàn tiền. */
    RESOLVED_WITH_REFUND("ResolvedWithRefund"),

    /** Khiếu nại đã được giải quyết nhưng không hoàn tiền. */
    RESOLVED_WITHOUT_REFUND("ResolvedWithoutRefund"),

    /** Khiếu nại đã được đóng lại. */
    CLOSED("Closed"),

    /** Khiếu nại bị hủy bỏ. */
    CANCELLED("Cancelled");

    private static final Map<String, DisputeStatus> LOOKUP = Collections.unmodifiableMap(Stream.of(values())
            .collect(Collectors.toMap(status -> status.databaseValue.toUpperCase(Locale.ROOT), status -> status)));

    private final String databaseValue;

    DisputeStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    /**
     * Lấy giá trị lưu trong cơ sở dữ liệu tương ứng với trạng thái.
     *
     * @return giá trị chuỗi dùng trong bảng {@code disputes.status}
     */
    public String getDatabaseValue() {
        return databaseValue;
    }

    /**
     * Chuyển giá trị trong cơ sở dữ liệu về enum tương ứng (không phân biệt hoa
     * thường).
     *
     * @param value giá trị trạng thái trong DB
     * @return {@link Optional} chứa {@link DisputeStatus} tương ứng nếu hợp lệ
     */
    public static Optional<DisputeStatus> fromDatabaseValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String key = value.trim().toUpperCase(Locale.ROOT);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(key));
    }
}

