package model;

import java.util.Date;

/**
 * Model cho yêu cầu KYC (Know Your Customer) để trở thành seller
 */
public class SellerRequest {
    
    private Integer id;
    private Integer userId;
    
    // Thông tin cá nhân từ CCCD
    private String fullName;
    private Date dateOfBirth;
    private String idNumber; // Số CCCD
    
    // File uploads
    private String frontIdImagePath; // Ảnh CCCD mặt trước
    private String backIdImagePath;  // Ảnh CCCD mặt sau
    private String selfieImagePath;  // Ảnh selfie
    
    // Thông tin kinh doanh
    private String businessName;
    private String businessDescription;
    private String experience;
    private String contactInfo;
    
    // Trạng thái và xử lý
    private String status; // Pending, Approved, Rejected
    private String rejectionReason;
    private Integer reviewedBy; // Admin ID
    private Date createdAt;
    private Date reviewedAt;
    
    // Constructors
    public SellerRequest() {
    }
    
    public SellerRequest(Integer userId, String fullName, Date dateOfBirth, String idNumber,
                        String frontIdImagePath, String backIdImagePath, String selfieImagePath,
                        String businessName, String businessDescription, String experience, String contactInfo) {
        this.userId = userId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.idNumber = idNumber;
        this.frontIdImagePath = frontIdImagePath;
        this.backIdImagePath = backIdImagePath;
        this.selfieImagePath = selfieImagePath;
        this.businessName = businessName;
        this.businessDescription = businessDescription;
        this.experience = experience;
        this.contactInfo = contactInfo;
        this.status = "Pending";
        this.createdAt = new Date();
    }

    // Getters and Setters
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getFrontIdImagePath() {
        return frontIdImagePath;
    }

    public void setFrontIdImagePath(String frontIdImagePath) {
        this.frontIdImagePath = frontIdImagePath;
    }

    public String getBackIdImagePath() {
        return backIdImagePath;
    }

    public void setBackIdImagePath(String backIdImagePath) {
        this.backIdImagePath = backIdImagePath;
    }

    public String getSelfieImagePath() {
        return selfieImagePath;
    }

    public void setSelfieImagePath(String selfieImagePath) {
        this.selfieImagePath = selfieImagePath;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
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

    @Override
    public String toString() {
        return "SellerRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", businessName='" + businessName + '\'' +
                ", businessDescription='" + businessDescription + '\'' +
                ", experience='" + experience + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", reviewedBy=" + reviewedBy +
                ", createdAt=" + createdAt +
                ", reviewedAt=" + reviewedAt +
                '}';
    }
}
