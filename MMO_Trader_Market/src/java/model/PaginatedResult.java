package model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable container for paginated results.
 *
 * @param <T> element type
 */
public class PaginatedResult<T> {

    private final List<T> items;
    private final int currentPage;
    private final int totalPages;
    private final int pageSize;
    private final int totalItems;

    public PaginatedResult(List<T> items, int currentPage, int totalPages, int pageSize, int totalItems) {
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }
}
