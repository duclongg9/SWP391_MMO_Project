package model;

import java.util.Date;

public class Conversations {

    private Integer id;
    private Integer relatedOrderId;
    private Integer relatedProductId;
    private Date createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Conversations(Integer id, Integer relatedOrderId, Integer relatedProductId, Date createdAt) {
        this.id = id;
        this.relatedOrderId = relatedOrderId;
        this.relatedProductId = relatedProductId;
        this.createdAt = createdAt;
    }

    public Integer getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Integer relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public Integer getRelatedProductId() {
        return relatedProductId;
    }

    public void setRelatedProductId(Integer relatedProductId) {
        this.relatedProductId = relatedProductId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
