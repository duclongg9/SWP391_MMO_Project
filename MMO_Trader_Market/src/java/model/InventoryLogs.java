package model;

import java.util.Date;

public class InventoryLogs {

    private Long id;
    private Integer productId;
    private Integer relatedOrderId;
    private Integer changeAmount;
    private String reason;
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventoryLogs(Long id, Integer productId, Integer relatedOrderId, Integer changeAmount, String reason, Date createdAt) {
        this.id = id;
        this.productId = productId;
        this.relatedOrderId = relatedOrderId;
        this.changeAmount = changeAmount;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Integer relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
