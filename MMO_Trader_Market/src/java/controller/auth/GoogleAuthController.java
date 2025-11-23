package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Users;
import service.GoogleOAuthService;
import service.GoogleOAuthService.GoogleProfile;
import service.UserService;
import units.RoleHomeResolver;


@WebServlet(name = "GoogleAuthController", urlPatterns = {"/auth/google"})
public class GoogleAuthController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GoogleAuthController.class.getName());
    private static final String SESSION_STATE = "googleOauthState"; //key để lưu chuỗi state vào session (phòng chống CSRF trong OAuth).
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    private final UserService userService = new UserService(new UserDAO());


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code"); //code gg trả về khi ng dùng đồng ý cấp quyền.
        String state = request.getParameter("state"); 
        if (code == null || code.isBlank()) { 
            startAuthentication(request, response); // Sinh state ngẫu nhiên, lưu vào session, ủy quyền
            return;
        }
        try {
            // Có mã và state => xử lý callback từ Google.
            handleCallback(request, response, code, state);
        } catch (SQLException ex) {
            Logger.getLogger(GoogleAuthController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void startAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute(SESSION_STATE, state); // lấy ss hiện tại, lưu state vào ss
        String authorizationUrl = googleOAuthService.buildAuthorizationUrl(state);    // Tạo URL điều hướng.
        response.sendRedirect(authorizationUrl); 
    }

   //xử lý khi Google redirect về
    private void handleCallback(HttpServletRequest request, HttpServletResponse response, String code, String state)
            throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        String expectedState = session == null ? null : (String) session.getAttribute(SESSION_STATE); //lấy từ session. Nếu session null hoặc không có state ⇒ null.
        if (expectedState == null || !expectedState.equals(state)) { //Nếu expectedState là null hoặc không bằng state trong request:
            sendErrorFlash(request, response, "Phiên đăng nhập Google không hợp lệ. Vui lòng thử lại.");
            return;
        }
        session.removeAttribute(SESSION_STATE); //óa state sau khi dùng
        try {
            // Lấy thông tin hồ sơ từ Google và đăng nhập (hoặc tạo mới) tài khoản nội bộ.
            GoogleProfile profile = googleOAuthService.fetchUserProfile(code);
            Users user = userService.loginWithGoogle(profile.getGoogleId(), profile.getEmail(), profile.getName());
            HttpSession newSession = renewSession(request);
            newSession.setAttribute("currentUser", user); //gắn các thuộc tính phiên dùng cho app
            newSession.setAttribute("userId", user.getId());
            newSession.setAttribute("userRole", user.getRoleId());
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorFlash(request, response, e.getMessage());
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during Google OAuth, errorId=" + errorId, e);
            sendErrorFlash(request, response, "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
        }
    }

    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false); // lấy ss hiện có, k tạo mới
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }


    private void sendErrorFlash(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        HttpSession session = request.getSession();
        // Lưu thông báo lỗi vào session để hiển thị ở trang đăng nhập.
       session.setAttribute("oauthError", message);
        response.sendRedirect(request.getContextPath() + "/auth");
    }
}
