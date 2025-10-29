package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Users;
import service.GoogleOAuthService;
import service.GoogleOAuthService.GoogleProfile;
import service.UserService;
import units.RoleHomeResolver;

/**
 * Điều phối luồng "Đăng nhập bằng Google" cho người dùng khách và hiện hữu.
 * <p>
 * - Khởi tạo yêu cầu OAuth tới Google và lưu lại state để chống giả mạo. - Nhận
 * kết quả callback, ánh xạ hồ sơ Google sang tài khoản nội bộ và tạo phiên mới.
 * - Thông báo lỗi tiếng Việt cho các tình huống token không hợp lệ hoặc sự cố
 * hệ thống.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "GoogleAuthController", urlPatterns = {"/auth/google"})
public class GoogleAuthController extends BaseController {

    // Mã phiên bản phục vụ tuần tự hóa servlet.
    private static final long serialVersionUID = 1L;
    // Logger để ghi nhận lỗi phát sinh trong quá trình OAuth.
    private static final Logger LOGGER = Logger.getLogger(GoogleAuthController.class.getName());
    // Khóa lưu trữ giá trị state trong session nhằm chống giả mạo callback.
    private static final String SESSION_STATE = "googleOauthState";

    // Dịch vụ giao tiếp với Google OAuth để lấy mã và hồ sơ người dùng.
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    // Dịch vụ người dùng để ánh xạ tài khoản Google vào hệ thống nội bộ.
    private final UserService userService = new UserService(new UserDAO());

    /**
     * Xử lý cả hai bước của Google OAuth gồm khởi tạo yêu cầu và đón callback.
     *
     * @param request yêu cầu HTTP chứa tham số {@code code} và {@code state}
     * (nếu có)
     * @param response phản hồi HTTP để điều hướng trình duyệt
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if (code == null || code.isBlank()) {
            // Không có mã trả về => bắt đầu quy trình ủy quyền với Google.
            startAuthentication(request, response);
            return;
        }
        // Có mã và state => xử lý callback từ Google.
        handleCallback(request, response, code, state);
    }

    /**
     * Tạo state ngẫu nhiên, lưu vào phiên và chuyển hướng người dùng tới trang
     * cho phép của Google.
     *
     * @param request yêu cầu HTTP hiện tại để lấy phiên
     * @param response phản hồi để điều hướng sang URL uỷ quyền của Google
     */
    private void startAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        // Lưu state vào session để xác thực lại khi nhận callback.
        request.getSession().setAttribute(SESSION_STATE, state);
        String authorizationUrl = googleOAuthService.buildAuthorizationUrl(state);
        // Điều hướng người dùng sang trang xác thực của Google.
        response.sendRedirect(authorizationUrl);
    }

    /**
     * Kiểm tra state, đổi mã Google lấy thông tin người dùng và tạo phiên đăng
     * nhập nội bộ.
     *
     * @param request yêu cầu HTTP chứa phiên hiện tại
     * @param response phản hồi để điều hướng người dùng tới trang phù hợp theo
     * vai trò
     * @param code mã Google trả về sau khi người dùng cấp quyền
     * @param state giá trị state để kiểm chứng nguồn gốc callback
     */
    private void handleCallback(HttpServletRequest request, HttpServletResponse response, String code, String state)
            throws IOException {
        HttpSession session = request.getSession(false);
        String expectedState = session == null ? null : (String) session.getAttribute(SESSION_STATE);
        if (expectedState == null || !expectedState.equals(state)) {
            // State không khớp => nghi ngờ giả mạo nên thông báo lỗi và dừng lại.
            sendErrorFlash(request, response, "Phiên đăng nhập Google không hợp lệ. Vui lòng thử lại.");
            return;
        }
        session.removeAttribute(SESSION_STATE);
        try {
            // Lấy thông tin hồ sơ từ Google và đăng nhập (hoặc tạo mới) tài khoản nội bộ.
            GoogleProfile profile = googleOAuthService.fetchUserProfile(code);
            Users user = userService.loginWithGoogle(profile.getGoogleId(), profile.getEmail(), profile.getName());
            HttpSession newSession = renewSession(request);
            newSession.setAttribute("currentUser", user);
            newSession.setAttribute("userId", user.getId());
            newSession.setAttribute("userRole", user.getRoleId());
            // Đăng nhập thành công thì đưa người dùng về trang chủ phù hợp.
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Báo lỗi nghiệp vụ như tài khoản bị khóa, email chưa xác thực.
            sendErrorFlash(request, response, e.getMessage());
        } catch (RuntimeException e) {
            // Lỗi hệ thống bất ngờ thì ghi log và thông báo mã lỗi.
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during Google OAuth, errorId=" + errorId, e);
            sendErrorFlash(request, response, "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
        }
    }

    /**
     * Hủy phiên cũ (nếu có) và tạo phiên mới nhằm tránh tấn công cố định phiên.
     *
     * @param request yêu cầu HTTP cần làm mới phiên
     * @return phiên mới sau khi đăng nhập bằng Google thành công
     */
    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            // Hủy bỏ session cũ để đảm bảo an toàn phiên đăng nhập.
            existing.invalidate();
        }
        return request.getSession(true);
    }

    /**
     * Ghi nhận thông báo lỗi vào flash session và chuyển hướng về trang đăng
     * nhập.
     *
     * @param request yêu cầu HTTP để đặt thông báo lỗi
     * @param response phản hồi HTTP dùng để chuyển hướng
     * @param message nội dung lỗi hiển thị cho người dùng
     */
    private void sendErrorFlash(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        HttpSession session = request.getSession();
        // Lưu thông báo lỗi vào session để hiển thị ở trang đăng nhập.
       session.setAttribute("oauthError", message);
        response.sendRedirect(request.getContextPath() + "/auth");
    }
}
