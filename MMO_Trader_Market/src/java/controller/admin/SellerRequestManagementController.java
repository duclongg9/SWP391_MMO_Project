package controller.admin;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.PaginatedResult;
import model.SellerRequest;
import model.Users;
import model.view.SellerRequestView;
import service.SellerRequestService;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller cho admin quản lý seller requests
 */
@WebServlet(name = "SellerRequestManagementController", urlPatterns = {"/admin/seller-requests"})
public class SellerRequestManagementController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SellerRequestManagementController.class.getName());
    
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

        // Kiểm tra quyền admin
        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 1) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có thể truy cập trang này");
            return;
        }

        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "list") {
                case "view" -> showRequestDetail(request, response, currentUser);
                case "approve" -> handleApproveRequest(request, response, currentUser);
                case "reject" -> showRejectForm(request, response, currentUser);
                case "submit-reject" -> handleRejectRequest(request, response, currentUser);
                default -> showRequestList(request, response, currentUser);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            showRequestList(request, response, currentUser);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong SellerRequestManagementController", e);
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại sau.");
            showRequestList(request, response, currentUser);
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

        // Kiểm tra quyền admin
        if (currentUser.getRoleId() == null || currentUser.getRoleId() != 1) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ admin mới có thể thực hiện thao tác này");
            return;
        }

        String action = request.getParameter("action");
        
        if ("submit-reject".equals(action)) {
            handleRejectRequest(request, response, currentUser);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
        }
    }

    private void showRequestList(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        // Lấy tham số phân trang và lọc
        int page = getIntParameter(request, "page", 1);
        int pageSize = getIntParameter(request, "size", 10);
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "all";
        }
        
        PaginatedResult<SellerRequestView> result = sellerRequestService.getAllSellerRequests(
                "all".equals(status) ? null : status, page, pageSize);
        
        request.setAttribute("requests", result.getItems());
        request.setAttribute("pagination", result);
        request.setAttribute("currentStatus", status);
        request.setAttribute("pageTitle", "Quản lý yêu cầu seller");
        
        // Kiểm tra thông báo thành công từ session
        HttpSession session = request.getSession();
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        
        forward(request, response, "admin/seller-request-list");
    }

    private void showRequestDetail(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = getIntParameter(request, "id", 0);
        if (requestId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID yêu cầu không hợp lệ");
            return;
        }
        
        Optional<SellerRequest> requestOpt = sellerRequestService.getSellerRequestById(requestId);
        if (requestOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Yêu cầu không tồn tại");
            return;
        }
        
        request.setAttribute("sellerRequest", requestOpt.get());
        request.setAttribute("pageTitle", "Chi tiết yêu cầu seller");
        forward(request, response, "admin/seller-request-detail");
    }

    private void handleApproveRequest(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = getIntParameter(request, "id", 0);
        if (requestId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID yêu cầu không hợp lệ");
            return;
        }
        
        boolean approved = sellerRequestService.approveSellerRequest(requestId, admin);
        
        HttpSession session = request.getSession();
        if (approved) {
            session.setAttribute("successMessage", "Duyệt yêu cầu seller thành công! User đã được cập nhật role.");
        } else {
            session.setAttribute("errorMessage", "Không thể duyệt yêu cầu");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/seller-requests");
    }

    private void showRejectForm(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = getIntParameter(request, "id", 0);
        if (requestId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID yêu cầu không hợp lệ");
            return;
        }
        
        Optional<SellerRequest> requestOpt = sellerRequestService.getSellerRequestById(requestId);
        if (requestOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Yêu cầu không tồn tại");
            return;
        }
        
        request.setAttribute("sellerRequest", requestOpt.get());
        request.setAttribute("pageTitle", "Từ chối yêu cầu seller");
        forward(request, response, "admin/seller-request-reject");
    }

    private void handleRejectRequest(HttpServletRequest request, HttpServletResponse response, Users admin)
            throws ServletException, IOException {
        
        int requestId = getIntParameter(request, "requestId", 0);
        if (requestId <= 0) {
            throw new IllegalArgumentException("ID yêu cầu không hợp lệ");
        }
        
        String rejectionReason = request.getParameter("rejectionReason");

        try {
            boolean rejected = sellerRequestService.rejectSellerRequest(requestId, admin, rejectionReason);
            
            if (rejected) {
                HttpSession session = request.getSession();
                session.setAttribute("successMessage", "Từ chối yêu cầu seller thành công!");
                response.sendRedirect(request.getContextPath() + "/admin/seller-requests");
            } else {
                request.setAttribute("error", "Không thể từ chối yêu cầu");
                showRejectForm(request, response, admin);
            }
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("rejectionReason", rejectionReason);
            showRejectForm(request, response, admin);
        }
    }

    private Users getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Users) session.getAttribute("currentUser");
    }

    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
