package service.dto;

/**
 * Bộ lọc dành cho danh sách shop của seller.
 * <p>
 * Cho phép lọc theo từ khóa (tên shop). Service sẽ sử dụng thông tin này để gọi
 * xuống DAO mà không cần controller can thiệp vào logic xây dựng query.
 */
public class ShopFilters {

    private final String keyword;

    private ShopFilters(Builder builder) {
        this.keyword = builder.keyword;
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

    /**
     * Builder pattern đơn giản cho {@link ShopFilters}.
     */
    public static class Builder {
        private String keyword;

        /**
         * Thiết lập từ khóa tìm kiếm tên shop (không phân biệt hoa thường).
         *
         * @param keyword từ khóa cần tìm, có thể null
         * @return builder hiện tại để chaining
         */
        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public ShopFilters build() {
            return new ShopFilters(this);
        }
    }
}
