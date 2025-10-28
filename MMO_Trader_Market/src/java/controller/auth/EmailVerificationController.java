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

    private final UserService userService = new UserService(new UserDAO());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String code = request.getParameter("verificationCode");
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        try {
            boolean activated = userService.verifyEmailCode(normalizedEmail, code);
            HttpSession session = request.getSession();
            session.setAttribute("newUserEmail", normalizedEmail);
            session.setAttribute("verificationSuccess",
                    activated
                            ? "Xác thực email thành công! Bạn có thể đăng nhập."
                            : "Email đã được xác thực trước đó. Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/auth");
        } catch (IllegalArgumentException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/login");
        } catch (IllegalStateException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during email verification, errorId=" + errorId, e);
            prepareErrorState(request, normalizedEmail,
                    "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId,
                    "verificationError");
            forward(request, response, "auth/login");
        }
    }

    private void prepareErrorState(HttpServletRequest request, String email, String message, String attributeOverride) {
        String attribute = attributeOverride == null ? "verificationError" : attributeOverride;
        request.setAttribute(attribute, message);
        request.setAttribute("showVerificationModal", true);
        request.setAttribute("verificationEmail", email);
        String notice = (email == null || email.isBlank())
                ? "Vui lòng nhập mã xác thực đã được gửi tới email của bạn."
                : "Vui lòng nhập mã xác thực đã gửi tới " + email + ".";
        request.setAttribute("verificationNotice", notice);
        request.setAttribute("prefillEmail", email);
        String codeValue = request.getParameter("verificationCode");
        if (codeValue != null) {
            request.setAttribute("enteredVerificationCode", codeValue.trim());
        }
    }
}
