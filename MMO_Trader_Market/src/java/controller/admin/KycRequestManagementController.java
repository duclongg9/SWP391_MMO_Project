package controller.admin;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.SellerRequest;
import model.Users;
import model.view.SellerRequestView;
import service.KycRequestService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho admin quản lý KYC requests
 */
@WebServlet(name = "KycRequestManagementController", urlPatterns = {"/admin/kyc-requests"})
public class KycRequestManagementController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(KycRequestManagementController.class.getName());
    
    private final KycRequestService kycRequestService = new KycRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        // Kiểm tra admin permission
        if (!isAdmin(currentUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có thể truy cập trang này.");
            return;
        }
        
        String action = request.getParameter("action");
        
        try {
            switch (action == null ? "list" : action) {
                case "detail":
                    showKycRequestDetail(request, response);
                    break;
                case "reject-form":
                    showRejectForm(request, response);
                    break;
                case "list":
                default:
                    listPendingKycRequests(request, response);
                    break;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            listPendingKycRequests(request, response);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi xử lý KYC request", e);
            request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            listPendingKycRequests(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        Users currentUser = getCurrentUser(session);
        
        // Kiểm tra admin permission
        if (!isAdmin(currentUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có thể thực hiện hành động này.");
            return;
        }
        
        try {
            switch (action == null ? "list" : action) {
                case "approve":
                    handleApproveKycRequest(request, response, currentUser);
                    break;
                case "reject":
                    handleRejectKycRequest(request, response, currentUser);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
                    break;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            if ("approve".equals(action) || "reject".equals(action)) {
                showKycRequestDetail(request, response);
            } else {
                listPendingKycRequests(request, response);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi xử lý KYC request", e);
            request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại sau.");
            listPendingKycRequests(request, response);
        }
    }

    private void listPendingKycRequests(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<SellerRequestView> pendingRequests = kycRequestService.getAllPendingKycRequests();
        
        request.setAttribute("pendingRequests", pendingRequests);
        request.setAttribute("pageTitle", "Quản lý yêu cầu KYC");
        forward(request, response, "admin/kyc-request-list");
    }

    private void showKycRequestDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int requestId = parseRequestId(request.getParameter("id"));
        
        Optional<SellerRequest> requestOpt = kycRequestService.getKycRequestById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu KYC không tồn tại.");
        }
        
        SellerRequest kycRequest = requestOpt.get();
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
        
        request.setAttribute("pageTitle", "Chi tiết yêu cầu KYC");
        forward(request, response, "admin/kyc-request-detail");
    }

    private void showRejectForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int requestId = parseRequestId(request.getParameter("id"));
        
        Optional<SellerRequest> requestOpt = kycRequestService.getKycRequestById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu KYC không tồn tại.");
        }
        
        SellerRequest kycRequest = requestOpt.get();
        if (!"Pending".equals(kycRequest.getStatus())) {
            throw new IllegalStateException("Yêu cầu đã được xử lý rồi.");
        }
        
        request.setAttribute("kycRequest", kycRequest);
        request.setAttribute("pageTitle", "Từ chối yêu cầu KYC");
        forward(request, response, "admin/kyc-request-reject");
    }

    private void handleApproveKycRequest(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = parseRequestId(request.getParameter("id"));
        String adminNotes = request.getParameter("adminNotes");
        
        boolean success = kycRequestService.approveKycRequest(requestId, admin, adminNotes);
        
        if (success) {
            request.getSession().setAttribute("success", 
                "Yêu cầu KYC đã được duyệt thành công! Người dùng đã trở thành seller.");
        } else {
            request.getSession().setAttribute("error", 
                "Có lỗi xảy ra khi duyệt yêu cầu. Vui lòng thử lại.");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/kyc-requests");
    }

    private void handleRejectKycRequest(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = parseRequestId(request.getParameter("id"));
        String rejectionReason = request.getParameter("rejectionReason");
        
        boolean success = kycRequestService.rejectKycRequest(requestId, admin, rejectionReason);
        
        if (success) {
            request.getSession().setAttribute("success", 
                "Yêu cầu KYC đã bị từ chối.");
        } else {
            request.getSession().setAttribute("error", 
                "Có lỗi xảy ra khi từ chối yêu cầu. Vui lòng thử lại.");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/kyc-requests");
    }

    private int parseRequestId(String idStr) {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID yêu cầu không hợp lệ.");
        }
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }

    private boolean isAdmin(Users user) {
        return user != null && user.getRoleId() != null && user.getRoleId() == 1;
    }
}
