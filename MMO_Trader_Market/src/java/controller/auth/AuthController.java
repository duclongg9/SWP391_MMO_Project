package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import model.Users;
import service.InactiveAccountException;
import service.UserService;
import units.RoleHomeResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final String FLASH_SUCCESS = "registerSuccess";
    private static final String FLASH_RESET_SUCCESS = "resetSuccess";
    private static final String FLASH_VERIFICATION_SUCCESS = "verificationSuccess";
    private static final String FLASH_EMAIL = "newUserEmail";
    private static final String FLASH_ERROR = "oauthError";
    private static final String FLASH_PENDING_VERIFICATION_EMAIL = "pendingVerificationEmail";
    private static final String FLASH_PENDING_VERIFICATION_MODAL = "showVerificationModal";
    private static final String FLASH_PENDING_VERIFICATION_NOTICE = "verificationNotice";

    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService(userDAO);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            invalidateSession(request); // xóa ss hiện tại
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        // kiểm tra user đã login chưa
        HttpSession session = request.getSession(false); // lấy ss hiện tại
        Users currentUser = session == null ? null : (Users) session.getAttribute("currentUser"); // !null lấy object đã lưu trong session với key'curu'
        if (currentUser != null) {
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(currentUser)); //redirect về trang home theo role
            return;
        }
        if (session != null) { // hiển thị flash message
            moveFlash(session, request, FLASH_SUCCESS, "success");
            moveFlash(session, request, FLASH_RESET_SUCCESS, "success");
            moveFlash(session, request, FLASH_VERIFICATION_SUCCESS, "success");
            moveFlash(session, request, FLASH_EMAIL, "prefillEmail");
            moveFlash(session, request, FLASH_ERROR, "error");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_EMAIL, "verificationEmail");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_MODAL, "showVerificationModal");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_NOTICE, "verificationNotice");
        }

        forward(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String normalizedEmail = email == null ? null : email.trim(); // xóa khoảng trắng
        String password = request.getParameter("password");
        if (normalizedEmail == null || normalizedEmail.isBlank() || password == null || password.isBlank()) { // thiếu 1 trg 2 thì báo lỗi
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu");
            request.setAttribute("prefillEmail", normalizedEmail);
            forward(request, response, "auth/login");
            return;
        }

        try {
            Users user = userService.authenticate(normalizedEmail, password); //kiểm tra thông tin đăng nhập.
            HttpSession session = renewSession(request);
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoleId());
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        } catch (InactiveAccountException e) { // tai khoan chua kich hoat
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("showVerificationModal", true);
            request.setAttribute("verificationEmail", normalizedEmail);
            request.setAttribute("verificationNotice",
                    "Vui lòng nhập mã xác thực đã được gửi tới " + normalizedEmail + ".");
            try {
                userService.resendVerificationCode(normalizedEmail);
                request.setAttribute("verificationNotice", "Chúng tôi đã gửi lại mã xác thực đến " + normalizedEmail + ".");
            } catch (IllegalArgumentException | IllegalStateException resendEx) {
                request.setAttribute("verificationError", resendEx.getMessage()); //  lỗi nghiệp vụ
            } catch (RuntimeException resendEx) {
                LOGGER.log(Level.SEVERE, "Unable to resend verification code", resendEx); 
                request.setAttribute("verificationError",
                        "Hệ thống không thể gửi lại mã xác thực lúc này. Vui lòng thử lại sau.");
            }
            forward(request, response, "auth/login");
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", normalizedEmail);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during login, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            request.setAttribute("prefillEmail", normalizedEmail);
            forward(request, response, "auth/login");
        }
    }

    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) { // ss đã tạo nma ch có currentUser
            existing.invalidate();
        }
        return request.getSession(true);
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // Di chuyển dữ liệu dạng flash từ session xuống request và xóa khỏi session.
    private void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

}
