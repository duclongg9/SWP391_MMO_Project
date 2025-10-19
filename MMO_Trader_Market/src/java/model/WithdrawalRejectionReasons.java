package model;

public class WithdrawalRejectionReasons {
    private Integer id;
    private String reasonCode;
    private String description;
    private Boolean isActive;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public WithdrawalRejectionReasons(Integer id, String reasonCode, String description, Boolean isActive) {
        this.id = id;
        this.reasonCode = reasonCode;
        this.description = description;
        this.isActive = isActive;
    }

    public String getReasonCode() {
        return reasonCode;
    }
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}