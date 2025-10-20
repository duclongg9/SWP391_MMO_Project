package controller.user;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.SellerRequest;
import model.Users;
import service.SellerRequestService;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho user gửi yêu cầu trở thành seller
 */
@WebServlet(name = "SellerRequestController", urlPatterns = {"/user/seller-request", "/user/seller-request-form"})
public class SellerRequestController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SellerRequestController.class.getName());
    
    private final SellerRequestService sellerRequestService = new SellerRequestService();

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
            request.setAttribute("error", "Bạn đã là seller rồi");
            response.sendRedirect(request.getContextPath() + "/seller/shop");
            return;
        }

        // Kiểm tra user có thể gửi request không
        if (!sellerRequestService.canSubmitSellerRequest(currentUser)) {
            // Lấy thông tin request hiện tại
            Optional<SellerRequest> existingRequest = sellerRequestService.getMySellerRequest(currentUser);
            if (existingRequest.isPresent()) {
                request.setAttribute("existingRequest", existingRequest.get());
            }
            request.setAttribute("pageTitle", "Yêu cầu trở thành seller");
            forward(request, response, "user/seller-request-status");
            return;
        }

        // Chuyển hướng trực tiếp đến trang KYC
        request.setAttribute("pageTitle", "Trở thành Seller - KYC");
        forward(request, response, "user/seller-request-options");
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

        // Kiểm tra user đã là seller chưa
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == 2) {
            response.sendRedirect(request.getContextPath() + "/seller/shop");
            return;
        }

        String businessName = request.getParameter("businessName");
        String businessDescription = request.getParameter("businessDescription");
        String experience = request.getParameter("experience");
        String contactInfo = request.getParameter("contactInfo");

        try {
            SellerRequest newRequest = sellerRequestService.submitSellerRequest(
                    currentUser, businessName, businessDescription, experience, contactInfo);
            
            // Thành công - chuyển hướng với thông báo
            session.setAttribute("successMessage", 
                    "Gửi yêu cầu trở thành seller thành công! Vui lòng chờ admin xem xét trong 1-3 ngày làm việc.");
            response.sendRedirect(request.getContextPath() + "/user/seller-request");
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi validation hoặc business logic
            request.setAttribute("error", e.getMessage());
            request.setAttribute("businessName", businessName);
            request.setAttribute("businessDescription", businessDescription);
            request.setAttribute("experience", experience);
            request.setAttribute("contactInfo", contactInfo);
            request.setAttribute("pageTitle", "Yêu cầu trở thành seller");
            forward(request, response, "user/seller-request-form");
            
        } catch (RuntimeException e) {
            // Lỗi hệ thống
            LOGGER.log(Level.SEVERE, "Lỗi khi gửi seller request cho user ID: " + currentUser.getId(), e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            request.setAttribute("businessName", businessName);
            request.setAttribute("businessDescription", businessDescription);
            request.setAttribute("experience", experience);
            request.setAttribute("contactInfo", contactInfo);
            request.setAttribute("pageTitle", "Yêu cầu trở thành seller");
            forward(request, response, "user/seller-request-form");
        }
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }
}
