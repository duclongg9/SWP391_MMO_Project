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

@WebServlet(name = "GoogleAuthController", urlPatterns = {"/oauth2/google/login", "/oauth2/google/callback"})
public class GoogleAuthController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GoogleAuthController.class.getName());
    private static final String SESSION_STATE = "googleOauthState";
    private static final String LOGIN_PATH = "/oauth2/google/login";
    private static final String CALLBACK_PATH = "/oauth2/google/callback";

    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    private final UserService userService = new UserService(new UserDAO());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (LOGIN_PATH.equals(servletPath)) {
            startAuthentication(request, response);
            return;
        }
        if (CALLBACK_PATH.equals(servletPath)) {
            String code = request.getParameter("code");
            String state = request.getParameter("state");
            if (code == null || code.isBlank()) {
                sendErrorFlash(request, response, "Google không cung cấp mã xác thực. Vui lòng thử lại.");
                return;
            }
            handleCallback(request, response, code, state);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void startAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute(SESSION_STATE, state);
        String authorizationUrl = googleOAuthService.buildAuthorizationUrl(state);
        response.sendRedirect(authorizationUrl);
    }

    private void handleCallback(HttpServletRequest request, HttpServletResponse response, String code, String state)
            throws IOException {
        HttpSession session = request.getSession(false);
        String expectedState = session == null ? null : (String) session.getAttribute(SESSION_STATE);
        if (expectedState == null || !expectedState.equals(state)) {
            sendErrorFlash(request, response, "Phiên đăng nhập Google không hợp lệ. Vui lòng thử lại.");
            return;
        }
        session.removeAttribute(SESSION_STATE);
        try {
            GoogleProfile profile = googleOAuthService.fetchUserProfile(code);
            Users user = userService.loginWithGoogle(profile.getGoogleId(), profile.getEmail(), profile.getName());
            HttpSession newSession = renewSession(request);
            newSession.setAttribute("currentUser", user);
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
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }

    private void sendErrorFlash(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("oauthError", message);
        response.sendRedirect(request.getContextPath() + "/auth");
    }
}
