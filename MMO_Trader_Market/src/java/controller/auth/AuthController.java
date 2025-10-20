package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.RememberMeToken;
import model.Users;
import service.RememberMeService;
import service.UserService;
import units.RoleHomeResolver;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Xử lý đăng nhập, đăng ký, ghi nhớ đăng nhập và đăng xuất.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final String COOKIE_NAME = "remember_token";

    private final UserService userService = new UserService(new UserDAO());
    private final RememberMeService rememberMeService = new RememberMeService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            handleLogout(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        if (tryAutoLogin(request, response)) {
            return;
        }

        populateFlashMessages(request);

        if ("register".equals(action)) {
            forward(request, response, "auth/register");
        } else {
            forward(request, response, "auth/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("register".equals(action)) {
            handleRegister(request, response);
        } else {
            handleLogin(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        String password = request.getParameter("password");
        boolean rememberMe = request.getParameter("rememberMe") != null;

        if (email == null || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ email và mật khẩu");
            request.setAttribute("prefillEmail", email);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
            return;
        }

        try {
            Users user = userService.authenticate(email, password);
            HttpSession session = renewSession(request);
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoleId());

            if (rememberMe) {
                RememberMeToken token = rememberMeService.createToken(user.getId());
                addRememberMeCookie(request, response, token);
            } else {
                clearRememberMeCookie(request, response);
            }

            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("prefillEmail", email);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during login, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            request.setAttribute("prefillEmail", email);
            request.setAttribute("rememberMeChecked", rememberMe);
            forward(request, response, "auth/login");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        String name = trimToNull(request.getParameter("name"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        boolean accepted = request.getParameter("acceptTerms") != null;

        request.setAttribute("email", email);
        request.setAttribute("name", name);
        request.setAttribute("acceptTermsChecked", accepted);

        if (!accepted) {
            request.setAttribute("error", "Vui lòng đồng ý với Điều khoản sử dụng để tiếp tục.");
            forward(request, response, "auth/register");
            return;
        }

        try {
            Users created = userService.registerNewUser(email, name, password, confirmPassword);
            HttpSession session = request.getSession(true);
            session.setAttribute("registerSuccess", "Tạo tài khoản thành công! Vui lòng đăng nhập.");
            session.setAttribute("newUserEmail", created.getEmail());
            response.sendRedirect(request.getContextPath() + "/auth");
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, "auth/register");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during registration, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            forward(request, response, "auth/register");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            if (userId instanceof Integer id) {
                rememberMeService.revokeAll(id);
            }
            session.invalidate();
        }
        clearRememberMeCookie(request, response);
        response.sendRedirect(request.getContextPath() + "/auth");
    }

    private boolean tryAutoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie tokenCookie = findRememberMeCookie(request.getCookies());
        if (tokenCookie == null || tokenCookie.getValue() == null || tokenCookie.getValue().isBlank()) {
            return false;
        }
        String[] parts = tokenCookie.getValue().split(":", 2);
        if (parts.length != 2) {
            clearRememberMeCookie(request, response);
            return false;
        }
        Optional<RememberMeToken> stored = rememberMeService.consume(parts[0], parts[1]);
        if (stored.isEmpty()) {
            clearRememberMeCookie(request, response);
            return false;
        }
        Users user = userDAO.getUserByUserId(stored.get().getUserId());
        if (user == null) {
            clearRememberMeCookie(request, response);
            return false;
        }
        HttpSession session = renewSession(request);
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRoleId());
        RememberMeToken refreshed = rememberMeService.createToken(user.getId());
        addRememberMeCookie(request, response, refreshed);
        response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
        return true;
    }

    private void populateFlashMessages(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        moveFlash(session, request, "registerSuccess", "success");
        moveFlash(session, request, "resetSuccess", "success");
        moveFlash(session, request, "newUserEmail", "prefillEmail");
        moveFlash(session, request, "oauthError", "error");
    }

    private void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }

    private void addRememberMeCookie(HttpServletRequest request, HttpServletResponse response, RememberMeToken token) {
        if (token.getPlainValidator() == null) {
            return;
        }
        String value = token.getSelector() + ":" + token.getPlainValidator();
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(60 * 60 * 24 * 30);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(cookie);
    }

    private void clearRememberMeCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(0);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(cookie);
    }

    private Cookie findRememberMeCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
