    package controller.auth;

import controller.BaseController;
import dao.user.RememberMeTokenDAO;
import dao.user.UserDAO;
import model.Users;
import service.InactiveAccountException;
import service.RememberMeService;
import service.UserService;
import units.RoleHomeResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Điều phối luồng "Đăng nhập" cho người dùng đã có tài khoản.
 * <p>
 * - Hỗ trợ đăng nhập nội bộ bằng email/mật khẩu và ghi nhớ phiên. - Chuyển
 * hướng khách (Role Guest) sang trang đăng nhập của hệ thống khác (Google) khi
 * cần. - Cung cấp chức năng đăng xuất và dọn dẹp phiên, cookie nhớ tài khoản.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
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
    private static final String FLASH_PENDING_VERIFICATION_NOTICE = "verificationNotice";

    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService(userDAO);
    private final RememberMeService rememberMeService = new RememberMeService(new RememberMeTokenDAO(), userDAO);


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            invalidateSession(request);  
            rememberMeService.clearRememberMe(request, response);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        HttpSession session = request.getSession(false); 
        Users currentUser = session == null ? null : (Users) session.getAttribute("currentUser"); // đọc attibute và ép kiểu
        if (currentUser != null) {
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(currentUser));
            return;
        }
        if (session != null) {
            moveFlash(session, request, FLASH_SUCCESS, "success");
            moveFlash(session, request, FLASH_RESET_SUCCESS, "success");
            moveFlash(session, request, FLASH_VERIFICATION_SUCCESS, "success");
            moveFlash(session, request, FLASH_EMAIL, "prefillEmail");
            moveFlash(session, request, FLASH_ERROR, "error");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_EMAIL, "verificationEmail");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_NOTICE, "verificationNotice");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            boolean hasPrefill = request.getAttribute("prefillEmail") != null;
            for (Cookie cookie : cookies) {
                if ("rememberedEmail".equals(cookie.getName()) && cookie.getValue() != null
                        && !cookie.getValue().isBlank()) {
                    if (!hasPrefill) {
                        request.setAttribute("prefillEmail", cookie.getValue());
                    }
                    request.setAttribute("rememberMeChecked", true);
                    break;
                }
            }
        }
        forward(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String normalizedEmail = email == null ? null : email.trim();
        String password = request.getParameter("password");
        boolean rememberMe = request.getParameter("rememberMe") != null; 
        if (normalizedEmail == null || normalizedEmail.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu");
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
            return;
        }

        try {
            // Xác thực thông tin đăng nhập và lưu thông tin người dùng vào session mới.
            Users user = userService.authenticate(normalizedEmail, password);
            HttpSession session = renewSession(request); 
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoleId());
            handleRememberEmailCookie(request, response, normalizedEmail, rememberMe); //điền sẵn email cho lần sau
            if (rememberMe) {
                // Tạo cookie ghi nhớ phiên nếu người dùng yêu cầu.
                rememberMeService.createRememberMeCookie(request, response, user.getId());
            } else {
                rememberMeService.clearRememberMe(request, response);
            }
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        } catch (InactiveAccountException e) { // tài khoản chưa kích hoạt
            HttpSession session = request.getSession();
            session.setAttribute(FLASH_PENDING_VERIFICATION_EMAIL, normalizedEmail);
            session.setAttribute(FLASH_PENDING_VERIFICATION_NOTICE,
                    e.getMessage() + " Nhập mã xác thực đã được gửi tới " + normalizedEmail + ".");
            session.setAttribute(FLASH_EMAIL, normalizedEmail);
            response.sendRedirect(request.getContextPath() + "/verify-email");
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", normalizedEmail); 
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during login, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("rememberMeChecked", rememberMe);
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

    // quản lý cookie, điền sẵn email cho lần sau
    private void handleRememberEmailCookie(HttpServletRequest request, HttpServletResponse response,
            String email, boolean rememberMe) {
        String contextPath = request.getContextPath(); // dg dan goc
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }

        Cookie cookie = new Cookie("rememberedEmail", rememberMe ? email : "");
        cookie.setHttpOnly(true); 
        cookie.setSecure(request.isSecure()); 
        cookie.setPath(contextPath); 
        cookie.setMaxAge(rememberMe ? 60 * 60 * 24 * 30 : 0);
        response.addCookie(cookie);
    }
}
