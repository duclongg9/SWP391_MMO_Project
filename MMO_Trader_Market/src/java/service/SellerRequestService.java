package service;

import dao.user.SellerRequestDAO;
import dao.user.UserDAO;
import model.PaginatedResult;
import model.SellerRequest;
import model.Users;
import model.view.SellerRequestView;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service layer cho quản lý seller requests
 */
public class SellerRequestService {

    private static final Pattern BUSINESS_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_.()]{3,100}$");
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MIN_EXPERIENCE_LENGTH = 10;
    private static final int MAX_EXPERIENCE_LENGTH = 500;

    private final SellerRequestDAO sellerRequestDAO;
    private final UserDAO userDAO;

    public SellerRequestService() {
        this.sellerRequestDAO = new SellerRequestDAO();
        this.userDAO = new UserDAO();
    }

    public SellerRequestService(SellerRequestDAO sellerRequestDAO, UserDAO userDAO) {
        this.sellerRequestDAO = sellerRequestDAO;
        this.userDAO = userDAO;
    }

    /**
     * User gửi yêu cầu trở thành seller
     */
    public SellerRequest submitSellerRequest(Users user, String businessName, String businessDescription,
                                           String experience, String contactInfo) {
        validateUser(user);
        validateSellerRequestInput(businessName, businessDescription, experience, contactInfo);
        
        // Kiểm tra user đã là seller chưa
        if (user.getRoleId() != null && user.getRoleId() == 2) {
            throw new IllegalStateException("Bạn đã là seller rồi");
        }
        
        // Kiểm tra đã có request pending chưa
        Optional<SellerRequest> existingRequest = sellerRequestDAO.findByUserId(user.getId());
        if (existingRequest.isPresent() && "Pending".equals(existingRequest.get().getStatus())) {
            throw new IllegalStateException("Bạn đã có yêu cầu đang chờ duyệt. Vui lòng chờ admin xem xét.");
        }
        
        try {
            return sellerRequestDAO.createSellerRequest(user.getId(), businessName.trim(), 
                    businessDescription.trim(), experience.trim(), contactInfo.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Không thể gửi yêu cầu lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Lấy thông tin seller request của user
     */
    public Optional<SellerRequest> getMySellerRequest(Users user) {
        validateUser(user);
        return sellerRequestDAO.findByUserId(user.getId());
    }

    /**
     * Kiểm tra user có thể gửi seller request không
     */
    public boolean canSubmitSellerRequest(Users user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        
        // Chỉ buyer (role 3) mới có thể gửi request
        if (user.getRoleId() != 3) {
            return false;
        }
        
        // Kiểm tra đã có request pending chưa
        Optional<SellerRequest> existingRequest = sellerRequestDAO.findByUserId(user.getId());
        return existingRequest.isEmpty() || !"Pending".equals(existingRequest.get().getStatus());
    }

    /**
     * Admin lấy danh sách seller requests với phân trang
     */
    public PaginatedResult<SellerRequestView> getAllSellerRequests(String status, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 50), 5);
        
        long totalItems = sellerRequestDAO.countRequests(status);
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / safeSize);
        int currentPage = Math.min(safePage, totalPages);
        int offset = (currentPage - 1) * safeSize;
        
        List<SellerRequestView> requests = sellerRequestDAO.findAllWithPagination(status, safeSize, offset);
        
        return new PaginatedResult<>(requests, currentPage, totalPages, 
                safeSize, Math.toIntExact(totalItems));
    }

    /**
     * Admin duyệt seller request
     */
    public boolean approveSellerRequest(int requestId, Users admin) {
        validateAdmin(admin);
        
        try {
            // Lấy thông tin request
            Optional<SellerRequest> requestOpt = sellerRequestDAO.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new IllegalArgumentException("Yêu cầu không tồn tại");
            }
            
            SellerRequest request = requestOpt.get();
            if (!"Pending".equals(request.getStatus())) {
                throw new IllegalStateException("Yêu cầu đã được xử lý rồi");
            }
            
            // Approve request
            boolean requestUpdated = sellerRequestDAO.approveRequest(requestId, admin.getId());
            if (!requestUpdated) {
                return false;
            }
            
            // Cập nhật role của user thành seller (role 2)
            boolean roleUpdated = userDAO.updateUserRole(request.getUserId(), 2);
            
            return roleUpdated;
            
        } catch (SQLException e) {
            throw new RuntimeException("Không thể duyệt yêu cầu lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Admin từ chối seller request
     */
    public boolean rejectSellerRequest(int requestId, Users admin, String rejectionReason) {
        validateAdmin(admin);
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
        }
        
        if (rejectionReason.trim().length() < 10 || rejectionReason.trim().length() > 500) {
            throw new IllegalArgumentException("Lý do từ chối phải từ 10-500 ký tự");
        }
        
        Optional<SellerRequest> requestOpt = sellerRequestDAO.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu không tồn tại");
        }
        
        SellerRequest request = requestOpt.get();
        if (!"Pending".equals(request.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã được xử lý rồi");
        }
        
        return sellerRequestDAO.rejectRequest(requestId, admin.getId(), rejectionReason.trim());
    }

    /**
     * Lấy seller request theo ID (cho admin)
     */
    public Optional<SellerRequest> getSellerRequestById(int requestId) {
        return sellerRequestDAO.findById(requestId);
    }

    private void validateUser(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("Thông tin user không hợp lệ");
        }
        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đang bị khóa");
        }
    }

    private void validateAdmin(Users admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Thông tin admin không hợp lệ");
        }
        if (admin.getRoleId() == null || admin.getRoleId() != 1) {
            throw new IllegalArgumentException("Chỉ admin mới có thể thực hiện thao tác này");
        }
        if (Boolean.FALSE.equals(admin.getStatus())) {
            throw new IllegalStateException("Tài khoản admin đang bị khóa");
        }
    }

    private void validateSellerRequestInput(String businessName, String businessDescription, 
                                         String experience, String contactInfo) {
        // Validate business name
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên doanh nghiệp/gian hàng");
        }
        
        String trimmedBusinessName = businessName.trim();
        if (!BUSINESS_NAME_PATTERN.matcher(trimmedBusinessName).matches()) {
            throw new IllegalArgumentException("Tên doanh nghiệp phải từ 3-100 ký tự, chỉ chứa chữ, số và ký tự đặc biệt: - _ . ( )");
        }

        // Validate business description
        if (businessDescription == null || businessDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả doanh nghiệp");
        }
        
        String trimmedDesc = businessDescription.trim();
        if (trimmedDesc.length() < MIN_DESCRIPTION_LENGTH || trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Mô tả doanh nghiệp phải từ " + MIN_DESCRIPTION_LENGTH + 
                    " đến " + MAX_DESCRIPTION_LENGTH + " ký tự");
        }

        // Validate experience
        if (experience == null || experience.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập kinh nghiệm bán hàng");
        }
        
        String trimmedExp = experience.trim();
        if (trimmedExp.length() < MIN_EXPERIENCE_LENGTH || trimmedExp.length() > MAX_EXPERIENCE_LENGTH) {
            throw new IllegalArgumentException("Kinh nghiệm phải từ " + MIN_EXPERIENCE_LENGTH + 
                    " đến " + MAX_EXPERIENCE_LENGTH + " ký tự");
        }

        // Validate contact info
        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin liên hệ");
        }
        
        String trimmedContact = contactInfo.trim();
        if (trimmedContact.length() < 10 || trimmedContact.length() > 200) {
            throw new IllegalArgumentException("Thông tin liên hệ phải từ 10-200 ký tự");
        }
    }
}
