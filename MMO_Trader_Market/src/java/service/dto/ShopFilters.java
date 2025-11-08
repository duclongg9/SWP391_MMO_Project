package service.dto;

import java.time.LocalDate;

/**
 * Bộ lọc dành cho danh sách shop của seller.
 * <p>
 * Cho phép lọc theo từ khóa (tên shop) và khoảng thời gian tạo. Service sẽ sử
 * dụng thông tin này để gọi xuống DAO mà không cần controller can thiệp vào
 * logic xây dựng query.
 */
public class ShopFilters {

    private final String keyword;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    private ShopFilters(Builder builder) {
        this.keyword = builder.keyword;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
    }

    /**
     * Tạo builder giúp controller/service dễ dàng cấu hình các tham số tùy chọn.
     *
     * @return builder mới với giá trị mặc định null cho toàn bộ trường
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    /**
     * Builder pattern đơn giản cho {@link ShopFilters}.
     */
    public static class Builder {
        private String keyword;
        private LocalDate fromDate;
        private LocalDate toDate;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public ShopFilters build() {
            return new ShopFilters(this);
        }
    }
}
