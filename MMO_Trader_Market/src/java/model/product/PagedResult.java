package model.product;

import java.util.List;
import java.util.Objects;

/**
 * <p>Đối tượng bao gói kết quả phân trang cho các luồng duyệt sản phẩm.</p>
 * <p>Dữ liệu được dựng từ các truy vấn DAO trả về danh sách record/raw model và được
 * chuyển thành {@code items} để truyền xuống JSP. Thông tin trang/size/tổng mục giúp
 * controller render phân trang chính xác.</p>
 *
 * @param <T> kiểu phần tử trong danh sách sản phẩm
 * @author longpdhe171902
 */
public class PagedResult<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalItems;

    public PagedResult(List<T> items, int page, int size, int totalPages, long totalItems) {
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }
}
