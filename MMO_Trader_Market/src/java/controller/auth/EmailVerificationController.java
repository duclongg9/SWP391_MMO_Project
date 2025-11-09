package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Xử lý xác thực email dựa trên mã được gửi cho người dùng sau khi đăng ký.
 */
@WebServlet(name = "EmailVerificationController", urlPatterns = {"/verify-email"})
public class EmailVerificationController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EmailVerificationController.class.getName());

    private final UserService userService = new UserService(new UserDAO()); //thực thi logic xác thực mã.

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            moveFlash(session, request, "pendingVerificationEmail", "verificationEmail");
            moveFlash(session, request, "verificationNotice", "verificationNotice");
            moveFlash(session, request, "verificationError", "verificationError");
            moveFlash(session, request, "verificationSuccess", "success");
        }

        String emailParam = request.getParameter("email");
        if (emailParam != null && !emailParam.trim().isEmpty()) {
            request.setAttribute("verificationEmail", emailParam.trim().toLowerCase());
        }

        ensureDefaultNotice(request);
        forward(request, response, "auth/verify-email");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String code = request.getParameter("verificationCode");
        String action = request.getParameter("action");
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if ("resend".equals(action)) {
            handleResend(request, response, normalizedEmail);
            return;
        }

        try {
            boolean activated = userService.verifyEmailCode(normalizedEmail, code);
            HttpSession session = request.getSession();
            session.setAttribute("newUserEmail", normalizedEmail); //prefill ô email ở trang login
            session.setAttribute("verificationSuccess",
                    activated
                            ? "Xác thực email thành công! Bạn có thể đăng nhập."
                            : "Email đã được xác thực trước đó. Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/auth");
        } catch (IllegalArgumentException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/verify-email");
        } catch (IllegalStateException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/verify-email");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during email verification, errorId=" + errorId, e);
            prepareErrorState(request, normalizedEmail,
                    "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId,
                    "verificationError");
            forward(request, response, "auth/verify-email");
        }
    }

    /**
     * Xử lý yêu cầu gửi lại mã xác thực từ phía người dùng.
     *
     * @param request         {@link HttpServletRequest} hiện tại.
     * @param response        {@link HttpServletResponse} hiện tại.
     * @param normalizedEmail Email đã được chuẩn hóa.
     * @throws ServletException nếu xảy ra lỗi servlet trong quá trình forward.
     * @throws IOException      nếu xảy ra lỗi I/O khi forward.
     */
    private void handleResend(HttpServletRequest request, HttpServletResponse response, String normalizedEmail)
            throws ServletException, IOException {
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            prepareErrorState(request, normalizedEmail, "Vui lòng nhập email hợp lệ để gửi lại mã.", null);
            forward(request, response, "auth/verify-email");
            return;
        }

        try {
            userService.resendVerificationCode(normalizedEmail);
            request.setAttribute("verificationEmail", normalizedEmail);
            request.setAttribute("success", "Chúng tôi đã gửi lại mã xác thực đến " + normalizedEmail + ".");
            String codeValue = request.getParameter("verificationCode");
            if (codeValue != null && !codeValue.isBlank()) {
                request.setAttribute("enteredVerificationCode", codeValue.trim());
            }
            ensureDefaultNotice(request);
            forward(request, response, "auth/verify-email");
        } catch (IllegalArgumentException | IllegalStateException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/verify-email");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when resending verification code, errorId=" + errorId, e);
            prepareErrorState(request, normalizedEmail,
                    "Hệ thống không thể gửi lại mã xác thực lúc này. Mã lỗi: " + errorId,
                    "verificationError");
            forward(request, response, "auth/verify-email");
        }
    }

    /**
     * Di chuyển dữ liệu từ session xuống request để hiển thị dạng flash message.
     */
    private void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    /**
     * Chuẩn hóa thông điệp hướng dẫn hiển thị trên màn hình xác thực.
     */
    private void ensureDefaultNotice(HttpServletRequest request) {
        Object existing = request.getAttribute("verificationNotice");
        Object email = request.getAttribute("verificationEmail");
        if (existing == null) {
            if (email instanceof String emailStr && !emailStr.isBlank()) {
                request.setAttribute("verificationNotice", "Vui lòng nhập mã xác thực đã gửi tới " + emailStr + ".");
            } else {
                request.setAttribute("verificationNotice", "Vui lòng nhập mã xác thực đã được gửi tới email của bạn.");
            }
        }
    }

    /**
     * Chuẩn bị trạng thái lỗi và giữ lại dữ liệu người dùng vừa nhập để hiển thị lại.
     */
    private void prepareErrorState(HttpServletRequest request, String email, String message, String attributeOverride) {
        String attribute = attributeOverride == null ? "verificationError" : attributeOverride;
        request.setAttribute(attribute, message);
        request.setAttribute("verificationEmail", email);
        ensureDefaultNotice(request);
        String codeValue = request.getParameter("verificationCode");
        if (codeValue != null) {
            request.setAttribute("enteredVerificationCode", codeValue.trim());
        }
    }
}
