package service.dto;

/**
 * Hành động thay đổi trạng thái shop do seller khởi tạo.
 */
public enum ShopAction {
    /** Ẩn shop và chuyển toàn bộ sản phẩm sang trạng thái UNLISTED. */
    HIDE,
    /** Bật lại shop sang trạng thái Active (sản phẩm không tự động thay đổi). */
    UNHIDE
}
