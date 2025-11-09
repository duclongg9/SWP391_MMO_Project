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
 * Xử lý xác thực email dựa trên mã OTP được gửi đến hộp thư của người dùng.
 */
@WebServlet(name = "EmailVerificationController", urlPatterns = {"/verify-email"})
public class EmailVerificationController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EmailVerificationController.class.getName());

    private final UserService userService = new UserService(new UserDAO());

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
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if ("resend".equals(action)) {
            handleResend(request, response, normalizedEmail);
            return;
        }

        String code = request.getParameter("verificationCode");
        try {
            boolean activated = userService.verifyEmailCode(normalizedEmail, code);
            HttpSession session = request.getSession();
            session.setAttribute("newUserEmail", normalizedEmail);
            session.setAttribute("verificationSuccess",
                    activated
                            ? "Xác thực email thành công! Bạn có thể đăng nhập."
                            : "Email đã được xác thực trước đó. Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/auth");
        } catch (IllegalArgumentException | IllegalStateException e) {
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
            forward(request, response, "auth/login");
            return;
        }

        try {
            userService.resendVerificationCode(normalizedEmail);
            prepareModalState(request, normalizedEmail);
            request.setAttribute("verificationSuccessMessage",
                    "Chúng tôi đã gửi lại mã xác thực đến " + normalizedEmail + ".");
            retainCodeValue(request);
            forward(request, response, "auth/login");
        } catch (IllegalArgumentException | IllegalStateException e) {
            prepareErrorState(request, normalizedEmail, e.getMessage(), null);
            forward(request, response, "auth/login");
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when resending verification code, errorId=" + errorId, e);
            prepareErrorState(request, normalizedEmail,
                    "Hệ thống không thể gửi lại mã xác thực lúc này. Mã lỗi: " + errorId,
                    "verificationError");
            forward(request, response, "auth/login");
        }
    }

    /**
     * Chuẩn bị trạng thái lỗi và giữ lại dữ liệu người dùng vừa nhập để hiển thị lại.
     *
     * @param request           {@link HttpServletRequest} hiện tại.
     * @param email             địa chỉ email đã chuẩn hóa.
     * @param message           thông báo lỗi cần hiển thị.
     * @param attributeOverride tên thuộc tính lỗi tùy chọn.
     */
    private void prepareErrorState(HttpServletRequest request, String email, String message, String attributeOverride) {
        String attribute = attributeOverride == null ? "verificationError" : attributeOverride;
        request.setAttribute(attribute, message);
        prepareModalState(request, email);
        retainCodeValue(request);
    }

    /**
     * Đảm bảo modal xác thực luôn được mở và hiển thị đúng email hướng dẫn.
     *
     * @param request {@link HttpServletRequest} hiện tại.
     * @param email   địa chỉ email cần hiển thị.
     */
    private void prepareModalState(HttpServletRequest request, String email) {
        request.setAttribute("showVerificationModal", true);
        request.setAttribute("verificationEmail", email);
        request.setAttribute("prefillEmail", email);
        ensureDefaultNotice(request);
    }

    /**
     * Lưu lại giá trị mã xác thực người dùng vừa nhập để tránh nhập lại.
     *
     * @param request {@link HttpServletRequest} hiện tại.
     */
    private void retainCodeValue(HttpServletRequest request) {
        String codeValue = request.getParameter("verificationCode");
        if (codeValue != null) {
            request.setAttribute("enteredVerificationCode", codeValue.trim());
        }
    }

    /**
     * Chuẩn hóa thông điệp hướng dẫn hiển thị trên modal xác thực.
     *
     * @param request {@link HttpServletRequest} hiện tại.
     */
    private void ensureDefaultNotice(HttpServletRequest request) {
        Object notice = request.getAttribute("verificationNotice");
        if (notice != null) {
            return;
        }
        Object email = request.getAttribute("verificationEmail");
        if (email instanceof String) {
            String emailStr = ((String) email).trim();
            if (!emailStr.isEmpty()) {
                request.setAttribute("verificationNotice",
                        "Vui lòng nhập mã xác thực đã gửi tới " + emailStr + ".");
                return;
            }
        }
        request.setAttribute("verificationNotice",
                "Vui lòng nhập mã xác thực đã được gửi tới email của bạn.");
    }
}
