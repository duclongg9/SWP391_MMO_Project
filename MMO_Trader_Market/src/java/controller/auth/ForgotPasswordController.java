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
import service.UserService;

@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPasswordController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordController.class.getName());
    private final UserService userService = new UserService(new UserDAO());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "auth/forgot-password");
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        HttpSession session = request.getSession();
        try {
            String resetBaseUrl = buildResetBaseUrl(request);
            userService.requestPasswordReset(email, resetBaseUrl);
            session.setAttribute("emailNoticeMessage", "Chúng tôi đã gửi liên kết đặt lại mật khẩu tới " + email + ".");
            session.setAttribute("emailNoticeType", "success");
            session.setAttribute("emailNoticeEmail", email);
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("emailNoticeMessage", e.getMessage());
            session.setAttribute("emailNoticeType", "error");
            session.setAttribute("emailNoticeEmail", email);
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when requesting password reset, errorId=" + errorId, e);
            session.setAttribute("emailNoticeMessage", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            session.setAttribute("emailNoticeType", "error");
            session.setAttribute("emailNoticeEmail", email);
        }
        response.sendRedirect(request.getContextPath() + "/auth/email-sent");
    }

//trả về chuỗi URL, lấy request để lấy thông tin host, cổng, context path hiện tại.
    private String buildResetBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder builder = new StringBuilder();
        builder.append(scheme).append("://").append(serverName); 
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            // Bổ sung port nếu không sử dụng cổng mặc định của HTTP/HTTPS.
            builder.append(":").append(port);
        }
        builder.append(contextPath).append("/reset-password");
        return builder.toString();
    }
}
