package model;

import java.util.Date;

public class KycRequests {

    private Integer id;

    private Integer userId;

    private Integer statusId;

    private String frontImageUrl;

    private String backImageUrl;

    private String selfieImageUrl;
    private String userEmail;
    private String idNumber;

    private String userName;
    private String statusName;
    private Date createdAt;

    private Date reviewedAt;

    private String adminFeedback;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getFrontImageUrl() {
        return frontImageUrl;
    }

    public void setFrontImageUrl(String frontImageUrl) {
        this.frontImageUrl = frontImageUrl;
    }

    public String getBackImageUrl() {
        return backImageUrl;
    }

    public void setBackImageUrl(String backImageUrl) {
        this.backImageUrl = backImageUrl;
    }

    public String getSelfieImageUrl() {
        return selfieImageUrl;
    }

    public void setSelfieImageUrl(String selfieImageUrl) {
        this.selfieImageUrl = selfieImageUrl;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public KycRequests(Integer id, Integer userId, Integer statusId, String frontImageUrl, String backImageUrl, String selfieImageUrl, String idNumber, Date createdAt, Date reviewedAt, String adminFeedback) {
        this.id = id;
        this.userId = userId;
        this.statusId = statusId;
        this.frontImageUrl = frontImageUrl;
        this.backImageUrl = backImageUrl;
        this.selfieImageUrl = selfieImageUrl;
        this.idNumber = idNumber;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.adminFeedback = adminFeedback;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Date reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
