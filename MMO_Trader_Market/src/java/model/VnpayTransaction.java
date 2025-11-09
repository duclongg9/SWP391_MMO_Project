package model;

import java.util.Date;

/**
 * Domain model representing the {@code vnpay_transaction} audit table.
 */
public class VnpayTransaction {

    private Integer id;
    private Integer depositRequestId;
    private String linkData;
    private Date createdAt;
    private String vnpayStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepositRequestId() {
        return depositRequestId;
    }

    public void setDepositRequestId(Integer depositRequestId) {
        this.depositRequestId = depositRequestId;
    }

    public String getLinkData() {
        return linkData;
    }

    public void setLinkData(String linkData) {
        this.linkData = linkData;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getVnpayStatus() {
        return vnpayStatus;
    }

    public void setVnpayStatus(String vnpayStatus) {
        this.vnpayStatus = vnpayStatus;
    }
}
