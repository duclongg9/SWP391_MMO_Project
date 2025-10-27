package model.view;

import java.time.LocalDateTime;

/**
 * <p>
 * View model cho phần "Tin nhắn gần đây" trên homepage.</p>
 * <p>
 * Dữ liệu đến từ {@code ConversationMessageDAO.findLatest} và được
 * {@link service.HomepageService} chuyển thành đối tượng bất biến nhằm dễ dàng
 * hiển thị nội dung, tên người gửi và sản phẩm liên quan.</p>
 *
 * @author longpdhe171902
 */
public class ConversationMessageView {

    private final String senderName;
    private final String content;
    private final String productName;
    private final LocalDateTime createdAt;

    public ConversationMessageView(String senderName, String content, String productName, LocalDateTime createdAt) {
        this.senderName = senderName;
        this.content = content;
        this.productName = productName;
        this.createdAt = createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public String getProductName() {
        return productName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
