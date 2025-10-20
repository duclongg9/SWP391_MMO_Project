package service;

import dao.user.SellerRequestDAO;
import dao.user.UserDAO;
import model.SellerRequest;
import model.Users;
import model.view.SellerRequestView;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý logic nghiệp vụ cho KYC (Know Your Customer) request
 */
public class KycRequestService {

    private static final Logger LOGGER = Logger.getLogger(KycRequestService.class.getName());
    
    // Validation patterns
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("^[0-9]{9,12}$");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}\\s]{2,50}$");
    private static final Pattern BUSINESS_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_.()]{3,100}$");
    
    // Date format for parsing
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private final SellerRequestDAO sellerRequestDAO;
    private final UserDAO userDAO;
    private final FileUploadService fileUploadService;

    public KycRequestService() {
        this(new SellerRequestDAO(), new UserDAO(), new FileUploadService());
    }
    
    public KycRequestService(SellerRequestDAO sellerRequestDAO, UserDAO userDAO, FileUploadService fileUploadService) {
        this.sellerRequestDAO = Objects.requireNonNull(sellerRequestDAO);
        this.userDAO = Objects.requireNonNull(userDAO);
        this.fileUploadService = Objects.requireNonNull(fileUploadService);
    }

    /**
     * Tạo KYC request mới với file upload và thông tin liên hệ đầy đủ
     */
    public SellerRequest createKycRequest(Users user, String fullName, String dateOfBirthStr, String idNumber,
                                         Part frontIdImage, Part backIdImage, Part selfieImage,
                                         String businessType, String businessName, String phoneNumber, 
                                         String email, String facebookLink, String zaloNumber, String otherContacts) {
        validateUser(user);
        validateKycInfo(fullName, dateOfBirthStr, idNumber);
        validateBusinessInfo(businessType, businessName);
        validateContactInfo(phoneNumber, email, facebookLink, zaloNumber, otherContacts);
        validateImageFiles(frontIdImage, backIdImage, selfieImage);
        
        try {
            // Kiểm tra user đã có request chưa
            Optional<SellerRequest> existingRequest = sellerRequestDAO.findByUserId(user.getId());
            if (existingRequest.isPresent() && "Pending".equals(existingRequest.get().getStatus())) {
                throw new IllegalStateException("Bạn đã có một yêu cầu đang chờ xử lý");
            }
            
            // Parse date of birth
            Date dateOfBirth = parseDate(dateOfBirthStr);
            
            // Upload files
            String frontIdPath = fileUploadService.uploadKycImage(frontIdImage, user.getId(), "front_id");
            String backIdPath = fileUploadService.uploadKycImage(backIdImage, user.getId(), "back_id");
            String selfiePath = fileUploadService.uploadKycImage(selfieImage, user.getId(), "selfie");
            
            // Combine contact info
            String combinedContactInfo = buildContactInfo(phoneNumber, email, facebookLink, zaloNumber, otherContacts);
            
            return sellerRequestDAO.createKycRequest(user.getId(), fullName.trim(), dateOfBirth, idNumber.trim(),
                    frontIdPath, backIdPath, selfiePath, businessName.trim(), "Sẽ cập nhật sau khi tạo shop", 
                    "Sẽ chia sẻ sau", combinedContactInfo);
            
        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo KYC request", ex);
            throw new RuntimeException("Không thể tạo yêu cầu. Vui lòng thử lại sau.", ex);
        }
    }

    /**
     * Lấy thông tin KYC request của user
     */
    public Optional<SellerRequest> getMyKycRequest(Users user) {
        validateUser(user);
        return sellerRequestDAO.findByUserId(user.getId());
    }

    /**
     * Kiểm tra user có thể gửi KYC request không
     */
    public boolean canSubmitKycRequest(Users user) {
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
     * Admin lấy danh sách pending KYC requests
     */
    public List<SellerRequestView> getAllPendingKycRequests() {
        return sellerRequestDAO.findAllWithPagination("Pending", 100, 0);
    }

    /**
     * Admin duyệt KYC request
     */
    public boolean approveKycRequest(int requestId, Users admin, String adminNotes) {
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
            
            // Approve request và cập nhật role của user thành seller (role 2)
            boolean requestUpdated = sellerRequestDAO.approveRequest(requestId, admin.getId());
            if (!requestUpdated) {
                return false;
            }
            
            boolean roleUpdated = userDAO.updateUserRole(request.getUserId(), 2);
            
            return roleUpdated;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi duyệt KYC request", e);
            throw new RuntimeException("Không thể duyệt yêu cầu lúc này. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Admin từ chối KYC request
     */
    public boolean rejectKycRequest(int requestId, Users admin, String rejectionReason) {
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
        
        // Xóa các file đã upload khi reject
        try {
            if (request.getFrontIdImagePath() != null) {
                fileUploadService.deleteKycImage(request.getFrontIdImagePath());
            }
            if (request.getBackIdImagePath() != null) {
                fileUploadService.deleteKycImage(request.getBackIdImagePath());
            }
            if (request.getSelfieImagePath() != null) {
                fileUploadService.deleteKycImage(request.getSelfieImagePath());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể xóa file KYC khi reject", e);
            // Không throw exception vì đây không phải lỗi critical
        }
        
        return sellerRequestDAO.rejectRequest(requestId, admin.getId(), rejectionReason.trim());
    }

    /**
     * Lấy KYC request theo ID (cho admin)
     */
    public Optional<SellerRequest> getKycRequestById(int requestId) {
        return sellerRequestDAO.findById(requestId);
    }

    /**
     * Lấy URL ảnh để hiển thị
     */
    public String getImageUrl(String imagePath, String contextPath) {
        return fileUploadService.getImageUrl(imagePath, contextPath);
    }

    private void validateUser(Users user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Thông tin user không hợp lệ");
        }
        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đang bị khóa");
        }
        if (user.getRoleId() != 3) {
            throw new IllegalStateException("Chỉ tài khoản buyer mới có thể gửi yêu cầu KYC");
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

    private void validateKycInfo(String fullName, String dateOfBirthStr, String idNumber) {
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên đầy đủ");
        }
        
        String trimmedName = fullName.trim();
        if (!FULL_NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new IllegalArgumentException("Họ tên chỉ được chứa chữ cái và khoảng trắng, từ 2-50 ký tự");
        }
        
        // Validate date of birth
        if (dateOfBirthStr == null || dateOfBirthStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ngày sinh");
        }
        
        Date dateOfBirth = parseDate(dateOfBirthStr);
        Date now = new Date();
        long ageInMillis = now.getTime() - dateOfBirth.getTime();
        long ageInYears = ageInMillis / (365L * 24 * 60 * 60 * 1000);
        
        if (ageInYears < 18 || ageInYears > 100) {
            throw new IllegalArgumentException("Tuổi phải từ 18 đến 100");
        }
        
        // Validate ID number
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số CCCD/CMND");
        }
        
        String trimmedId = idNumber.trim();
        if (!ID_NUMBER_PATTERN.matcher(trimmedId).matches()) {
            throw new IllegalArgumentException("Số CCCD/CMND phải từ 9-12 chữ số");
        }
    }

    private void validateBusinessInfo(String businessType, String businessName) {
        // Validate business type
        if (businessType == null || businessType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn loại hình kinh doanh.");
        }
        
        if (!"individual".equals(businessType) && !"company".equals(businessType)) {
            throw new IllegalArgumentException("Loại hình kinh doanh không hợp lệ.");
        }
        
        // Validate business name
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên cửa hàng/doanh nghiệp.");
        }
        
        String trimmedBusinessName = businessName.trim();
        if (!BUSINESS_NAME_PATTERN.matcher(trimmedBusinessName).matches()) {
            throw new IllegalArgumentException("Tên cửa hàng phải từ 3-100 ký tự, chỉ chứa chữ, số và ký tự đặc biệt: - _ . ( )");
        }
    }

    private void validateImageFiles(Part frontIdImage, Part backIdImage, Part selfieImage) {
        if (frontIdImage == null || frontIdImage.getSize() == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh CCCD mặt trước");
        }
        
        if (backIdImage == null || backIdImage.getSize() == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh CCCD mặt sau");
        }
        
        if (selfieImage == null || selfieImage.getSize() == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh selfie");
        }
    }

    private void validateContactInfo(String phoneNumber, String email, String facebookLink, String zaloNumber, String otherContacts) {
        // Validate phone number (bắt buộc)
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại");
        }
        
        String trimmedPhone = phoneNumber.trim();
        if (!PHONE_NUMBER_PATTERN.matcher(trimmedPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (10-11 chữ số)");
        }

        // Validate email (bắt buộc)
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }
        
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ");
        }

        // Validate Facebook link (tùy chọn) - chỉ check format nếu có điền
        if (facebookLink != null && !facebookLink.trim().isEmpty()) {
            String trimmedFb = facebookLink.trim();
            if (!URL_PATTERN.matcher(trimmedFb).matches()) {
                throw new IllegalArgumentException("Link Facebook không đúng định dạng URL");
            }
        }

        // Zalo và other contacts đều tùy chọn - không cần validation
    }

    private String buildContactInfo(String phoneNumber, String email, String facebookLink, String zaloNumber, String otherContacts) {
        StringBuilder contactInfo = new StringBuilder();
        
        // Thông tin bắt buộc
        contactInfo.append("📞 SĐT: ").append(phoneNumber.trim()).append("\n");
        contactInfo.append("📧 Email: ").append(email.trim()).append("\n");
        
        // Thông tin tùy chọn - chỉ thêm nếu có
        if (facebookLink != null && !facebookLink.trim().isEmpty()) {
            contactInfo.append("👥 Facebook: ").append(facebookLink.trim()).append("\n");
        }
        
        if (zaloNumber != null && !zaloNumber.trim().isEmpty()) {
            contactInfo.append("💬 Zalo: ").append(zaloNumber.trim()).append("\n");
        }
        
        if (otherContacts != null && !otherContacts.trim().isEmpty()) {
            contactInfo.append("📝 Khác: ").append(otherContacts.trim()).append("\n");
        }
        
        return contactInfo.toString();
    }

    private Date parseDate(String dateStr) {
        try {
            DATE_FORMAT.setLenient(false);
            return DATE_FORMAT.parse(dateStr.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD");
        }
    }
}
