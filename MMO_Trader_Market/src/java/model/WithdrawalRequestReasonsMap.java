package model;

public class WithdrawalRequestReasonsMap {
    private Integer requestId;
    private Integer reasonId;

    public WithdrawalRequestReasonsMap(Integer requestId, Integer reasonId) {
        this.requestId = requestId;
        this.reasonId = reasonId;
    }

    public Integer getRequestId() {
        return requestId;
    }
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
    public Integer getReasonId() {
        return reasonId;
    }
    public void setReasonId(Integer reasonId) {
        this.reasonId = reasonId;
    }
}