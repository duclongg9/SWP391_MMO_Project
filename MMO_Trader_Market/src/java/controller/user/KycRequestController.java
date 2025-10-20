package controller.user;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.SellerRequest;
import model.Users;
import service.KycRequestService;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho user gửi yêu cầu KYC để trở thành seller
 */
@WebServlet(name = "KycRequestController", urlPatterns = {"/user/kyc-request"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 5,   // 5MB
    maxRequestSize = 1024 * 1024 * 10 // 10MB
)
public class KycRequestController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(KycRequestController.class.getName());
    
    private final KycRequestService kycRequestService = new KycRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Kiểm tra user đã là seller chưa
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == 2) {
            request.setAttribute("message", "Bạn đã là seller rồi");
            response.sendRedirect(request.getContextPath() + "/seller/shop");
            return;
        }

        // Chỉ buyer mới có thể gửi KYC request
        if (currentUser.getRoleId() != 3) {
            request.setAttribute("error", "Chỉ tài khoản buyer mới có thể gửi yêu cầu KYC.");
            request.setAttribute("pageTitle", "Lỗi quyền truy cập");
            forward(request, response, "error/403");
            return;
        }
        
        try {
            // Kiểm tra đã có KYC request chưa
            Optional<SellerRequest> existingRequest = kycRequestService.getMyKycRequest(currentUser);
            
            if (existingRequest.isPresent()) {
                // Có request rồi -> hiển thị trạng thái
                showKycStatus(request, response, existingRequest.get());
            } else {
                // Chưa có request -> hiển thị form KYC
                showKycForm(request, response);
            }
            
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi tải trang KYC request", e);
            request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.setAttribute("pageTitle", "Lỗi hệ thống");
            forward(request, response, "error/500");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        // Kiểm tra quyền
        if (currentUser.getRoleId() != 3) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ tài khoản buyer mới có thể gửi yêu cầu KYC.");
            return;
        }
        
        try {
            // Lấy thông tin từ form
            String fullName = request.getParameter("fullName");
            String dateOfBirth = request.getParameter("dateOfBirth");
            String idNumber = request.getParameter("idNumber");
            String businessType = request.getParameter("businessType");
            String businessName = request.getParameter("businessName");
            String businessDescription = request.getParameter("businessDescription");
            String experience = request.getParameter("experience");
            
            // Lấy thông tin liên hệ
            String phoneNumber = request.getParameter("phoneNumber");
            String email = request.getParameter("email");
            String facebookLink = request.getParameter("facebookLink");
            String zaloNumber = request.getParameter("zaloNumber");
            String otherContacts = request.getParameter("otherContacts");
            
            // Lấy file uploads
            Part frontIdImage = request.getPart("frontIdImage");
            Part backIdImage = request.getPart("backIdImage");
            Part selfieImage = request.getPart("selfieImage");
            
            // Tạo KYC request
            SellerRequest kycRequest = kycRequestService.createKycRequest(currentUser, fullName, dateOfBirth, idNumber,
                    frontIdImage, backIdImage, selfieImage, businessType, businessName, businessDescription, 
                    experience, phoneNumber, email, facebookLink, zaloNumber, otherContacts);
            
            session.setAttribute("success", "Yêu cầu KYC của bạn đã được gửi thành công! Admin sẽ xem xét trong vòng 1-2 ngày làm việc.");
            response.sendRedirect(request.getContextPath() + "/user/kyc-request");
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Validation errors
            handleFormError(request, response, e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi gửi KYC request", e);
            handleFormError(request, response, "Lỗi hệ thống. Vui lòng thử lại sau.");
        }
    }

    private void showKycStatus(HttpServletRequest request, HttpServletResponse response, SellerRequest kycRequest)
            throws ServletException, IOException {
        
        request.setAttribute("kycRequest", kycRequest);
        
        // Set image URLs for display
        if (kycRequest.getFrontIdImagePath() != null) {
            request.setAttribute("frontIdImageUrl", kycRequestService.getImageUrl(
                kycRequest.getFrontIdImagePath(), request.getContextPath()));
        }
        if (kycRequest.getBackIdImagePath() != null) {
            request.setAttribute("backIdImageUrl", kycRequestService.getImageUrl(
                kycRequest.getBackIdImagePath(), request.getContextPath()));
        }
        if (kycRequest.getSelfieImagePath() != null) {
            request.setAttribute("selfieImageUrl", kycRequestService.getImageUrl(
                kycRequest.getSelfieImagePath(), request.getContextPath()));
        }
        
        request.setAttribute("pageTitle", "Trạng thái yêu cầu KYC");
        forward(request, response, "user/kyc-request-status");
    }

    private void showKycForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setAttribute("pageTitle", "Xác minh danh tính (KYC) - Trở thành Seller");
        forward(request, response, "user/kyc-request-form");
    }

    private void handleFormError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        
        request.setAttribute("error", errorMessage);
        
        // Preserve form data
        request.setAttribute("fullName", request.getParameter("fullName"));
        request.setAttribute("dateOfBirth", request.getParameter("dateOfBirth"));
        request.setAttribute("idNumber", request.getParameter("idNumber"));
        request.setAttribute("businessType", request.getParameter("businessType"));
        request.setAttribute("businessName", request.getParameter("businessName"));
        request.setAttribute("businessDescription", request.getParameter("businessDescription"));
        request.setAttribute("experience", request.getParameter("experience"));
        request.setAttribute("phoneNumber", request.getParameter("phoneNumber"));
        request.setAttribute("email", request.getParameter("email"));
        request.setAttribute("facebookLink", request.getParameter("facebookLink"));
        request.setAttribute("zaloNumber", request.getParameter("zaloNumber"));
        request.setAttribute("otherContacts", request.getParameter("otherContacts"));
        
        showKycForm(request, response);
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }
}
