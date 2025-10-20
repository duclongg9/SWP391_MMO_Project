package model.view;

import java.time.LocalDateTime;

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

