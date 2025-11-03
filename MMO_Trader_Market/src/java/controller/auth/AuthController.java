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

    // Định danh phiên bản của Servlet để đảm bảo khả năng tuần tự hóa ổn định.
    private static final long serialVersionUID = 1L;

    // Bộ ghi log phục vụ theo dõi các lỗi bất ngờ trong quá trình đăng nhập.
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    //thông báo đăng ký thành công sau khi chuyển hướng.
    private static final String FLASH_SUCCESS = "registerSuccess";
    private static final String FLASH_RESET_SUCCESS = "resetSuccess";
    // Khóa flash message thông báo xác thực email thành công.
    private static final String FLASH_VERIFICATION_SUCCESS = "verificationSuccess";
    // Khóa flash message dùng để điền sẵn email mới đăng ký.
    private static final String FLASH_EMAIL = "newUserEmail";
    // thông báo lỗi đến từ đăng nhập OAuth.
    private static final String FLASH_ERROR = "oauthError";
    // Khóa flash chứa email cần hiện modal xác thực.
    private static final String FLASH_PENDING_VERIFICATION_EMAIL = "pendingVerificationEmail";
    private static final String FLASH_PENDING_VERIFICATION_MODAL = "showVerificationModal";
    private static final String FLASH_PENDING_VERIFICATION_NOTICE = "verificationNotice";

    private final UserDAO userDAO = new UserDAO();
    private final UserService userService = new UserService(userDAO);
    private final RememberMeService rememberMeService = new RememberMeService(new RememberMeTokenDAO(), userDAO);


    // Xử lý các yêu cầu GET đến trang /auth: hiển thị form đăng nhập hoặc đăng xuất.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            // Người dùng yêu cầu đăng xuất: hủy session và xóa cookie ghi nhớ.
            invalidateSession(request);
            //xóa cookie remember-me và bản ghi token DB.
            rememberMeService.clearRememberMe(request, response);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        HttpSession session = request.getSession(false); // ss hiện tại, k tạo mới 
        Users currentUser = session == null ? null : (Users) session.getAttribute("currentUser"); // đọc attibute và ép kiểu
        if (currentUser != null) {
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(currentUser));
            return;
        }
        if (session != null) {
            // lấy tb từ session xuống request để hiển thị 1 lần.
            moveFlash(session, request, FLASH_SUCCESS, "success");
            moveFlash(session, request, FLASH_RESET_SUCCESS, "success");
            moveFlash(session, request, FLASH_VERIFICATION_SUCCESS, "success");
            moveFlash(session, request, FLASH_EMAIL, "prefillEmail"); // điền sẵn email vào
            moveFlash(session, request, FLASH_ERROR, "error");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_EMAIL, "verificationEmail");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_MODAL, "showVerificationModal");
            moveFlash(session, request, FLASH_PENDING_VERIFICATION_NOTICE, "verificationNotice");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            boolean hasPrefill = request.getAttribute("prefillEmail") != null;
            for (Cookie cookie : cookies) {
                if ("rememberedEmail".equals(cookie.getName()) && cookie.getValue() != null
                        && !cookie.getValue().isBlank()) {
                    if (!hasPrefill) {
                        // Điền sẵn email được lưu trước đó nếu người dùng chưa nhập.
                        request.setAttribute("prefillEmail", cookie.getValue());
                    }
                    // Đánh dấu checkbox "Ghi nhớ tôi" cho lần đăng nhập kế tiếp.
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
        boolean rememberMe = request.getParameter("rememberMe") != null; // tick ô gh
        if (normalizedEmail == null || normalizedEmail.isBlank() || password == null || password.isBlank()) {
            // Báo lỗi nếu thiếu thông tin đăng nhập, giữ lại email, trạng thái
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu");
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
            return;
        }

        try {
            // Xác thực thông tin đăng nhập và lưu thông tin người dùng vào session mới.
            Users user = userService.authenticate(normalizedEmail, password);
            HttpSession session = renewSession(request); // hủy ss cũ(nếu có), tạo mới -> chống ss fixation.
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
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("rememberMeChecked", rememberMe);
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
            // Thông báo lỗi nghiệp vụ như sai mật khẩu, tài khoản bị khóa.
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", normalizedEmail); 
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            // Ghi log lỗi hệ thống kèm mã nhận diện để hỗ trợ điều tra.
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during login, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            request.setAttribute("prefillEmail", normalizedEmail);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
        }
    }

    // Tạo mới session để tránh tấn công fixation khi đăng nhập thành công.
    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) { // ss đã tạo nma ch có currentUser
            existing.invalidate();
        }
        return request.getSession(true);
    }

    // Hủy session hiện tại nếu tồn tại -> đxuat
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

        // Cookie chỉ lưu email, thiết lập thuộc tính bảo mật và thời hạn phù hợp.
        Cookie cookie = new Cookie("rememberedEmail", rememberMe ? email : "");
        cookie.setHttpOnly(true); // đánh dấu, javascript k đọc dc cookie
        cookie.setSecure(request.isSecure()); //trả về true nếu request hiện tại đi qua HTTPS
        cookie.setPath(contextPath); //Giới hạn phạm vi URL
        cookie.setMaxAge(rememberMe ? 60 * 60 * 24 * 30 : 0);
        response.addCookie(cookie);
    }
}
