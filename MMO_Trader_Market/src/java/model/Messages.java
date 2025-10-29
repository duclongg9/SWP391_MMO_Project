package model;

import java.util.Date;

public class Messages {

    private Long id;

    private Integer conversationId;

    private Integer senderId;

    private Date createdAt;

    private String content;

    public Messages(Long id, Integer conversationId, Integer senderId, Date createdAt, String content) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.createdAt = createdAt;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
