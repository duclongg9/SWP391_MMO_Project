package model.view;

import model.SellerRequest;
import model.Users;

/**
 * View model cho hiển thị seller request kèm thông tin user
 */
public class SellerRequestView {
    
    private final SellerRequest request;
    private final Users user;
    
    public SellerRequestView(SellerRequest request, Users user) {
        this.request = request;
        this.user = user;
    }
    
    public SellerRequest getRequest() {
        return request;
    }
    
    public Users getUser() {
        return user;
    }
    
    // Convenience methods
    public Integer getId() {
        return request.getId();
    }
    
    public String getUserName() {
        return user != null ? user.getName() : "Unknown";
    }
    
    public String getUserEmail() {
        return user != null ? user.getEmail() : "Unknown";
    }
    
    public String getBusinessName() {
        return request.getBusinessName();
    }
    
    public String getStatus() {
        return request.getStatus();
    }
    
    public java.util.Date getCreatedAt() {
        return request.getCreatedAt();
    }
}
