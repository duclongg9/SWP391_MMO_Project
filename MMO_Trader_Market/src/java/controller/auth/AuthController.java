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
 * Handles login and logout requests following the MVC pattern.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final String FLASH_SUCCESS = "registerSuccess";
    private static final String FLASH_RESET_SUCCESS = "resetSuccess";
    private static final String FLASH_EMAIL = "newUserEmail";
    private static final String FLASH_ERROR = "oauthError";

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
        Users currentUser = session == null ? null : (Users) session.getAttribute("currentUser");
        if (currentUser != null) {
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(currentUser));
            return;
        }
        if (session != null) {
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
            Users user = userService.authenticate(normalizedEmail, password);
            HttpSession session = renewSession(request);
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoleId());
            handleRememberEmailCookie(request, response, normalizedEmail, rememberMe);
            if (rememberMe) {
                rememberMeService.createRememberMeCookie(request, response, user.getId());
            } else {
                rememberMeService.clearRememberMe(request, response);
            }
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
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
        if (existing != null) {
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

    private void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    private void handleRememberEmailCookie(HttpServletRequest request, HttpServletResponse response,
            String email, boolean rememberMe) {
        String contextPath = request.getContextPath();
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
