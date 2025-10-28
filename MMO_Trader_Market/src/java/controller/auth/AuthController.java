package controller.auth;

import controller.BaseController;
import dao.user.RememberMeTokenDAO;
import dao.user.UserDAO;
import model.Users;
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
    // Khóa flash message thông báo đăng ký thành công sau khi chuyển hướng.
    private static final String FLASH_SUCCESS = "registerSuccess";
    // Khóa flash message thông báo đổi mật khẩu thành công.
    private static final String FLASH_RESET_SUCCESS = "resetSuccess";
    // Khóa flash message dùng để điền sẵn email mới đăng ký.
    private static final String FLASH_EMAIL = "newUserEmail";
    // Khóa flash message thông báo lỗi đến từ đăng nhập OAuth.
    private static final String FLASH_ERROR = "oauthError";

    // DAO thao tác bảng Users để phục vụ quá trình xác thực.
    private final UserDAO userDAO = new UserDAO();
    // Lớp dịch vụ xử lý nghiệp vụ đăng nhập, khóa tài khoản...
    private final UserService userService = new UserService(userDAO);
    // Dịch vụ "Ghi nhớ đăng nhập" để sinh token và cookie tương ứng.
    private final RememberMeService rememberMeService = new RememberMeService(new RememberMeTokenDAO(), userDAO);


    // Xử lý các yêu cầu GET đến trang /auth: hiển thị form đăng nhập hoặc đăng xuất.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            // Người dùng yêu cầu đăng xuất: hủy session và xóa cookie ghi nhớ.
            invalidateSession(request);
            rememberMeService.clearRememberMe(request, response);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        HttpSession session = request.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("currentUser");
        if (currentUser != null) {
            // Nếu đã đăng nhập, chuyển hướng về trang chủ phù hợp với vai trò.
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(currentUser));
            return;
        }
        if (session != null) {
            // Chuyển thông báo tạm thời từ session xuống request để hiển thị.
            moveFlash(session, request, FLASH_SUCCESS, "success");
            moveFlash(session, request, FLASH_RESET_SUCCESS, "success");
            moveFlash(session, request, FLASH_EMAIL, "prefillEmail");
            moveFlash(session, request, FLASH_ERROR, "error");
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
        // Hiển thị giao diện đăng nhập mặc định.
        forward(request, response, "auth/login");
    }

    // Xử lý hành động submit form đăng nhập từ người dùng.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String normalizedEmail = email == null ? null : email.trim();
        String password = request.getParameter("password");
        boolean rememberMe = request.getParameter("rememberMe") != null;
        if (normalizedEmail == null || normalizedEmail.isBlank() || password == null || password.isBlank()) {
            // Báo lỗi nếu thiếu thông tin đăng nhập.
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
            handleRememberEmailCookie(request, response, normalizedEmail, rememberMe);
            if (rememberMe) {
                // Tạo cookie ghi nhớ phiên nếu người dùng yêu cầu.
                rememberMeService.createRememberMeCookie(request, response, user.getId());
            } else {
                rememberMeService.clearRememberMe(request, response);
            }
            // Chuyển hướng đến trang chủ tương ứng với vai trò.
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
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
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }

    // Hủy session hiện tại nếu tồn tại, phục vụ quá trình đăng xuất.
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

    // Thiết lập cookie lưu email đăng nhập để hỗ trợ điền sẵn cho lần sau.
    private void handleRememberEmailCookie(HttpServletRequest request, HttpServletResponse response,
            String email, boolean rememberMe) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }

        // Cookie chỉ lưu email, thiết lập thuộc tính bảo mật và thời hạn phù hợp.
        Cookie cookie = new Cookie("rememberedEmail", rememberMe ? email : "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(contextPath);
        cookie.setMaxAge(rememberMe ? 60 * 60 * 24 * 30 : 0);
        response.addCookie(cookie);
    }
}
