package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.UserService;

/**
 * Điều phối luồng "Quên mật khẩu" để hỗ trợ người dùng khôi phục truy cập.
 * <p>
 * - Tiếp nhận email khi người dùng quên mật khẩu và sinh đường dẫn đặt lại.
 * - Gửi thông báo xác nhận đã gửi email hướng dẫn đặt lại mật khẩu.
 * - Hiển thị thông báo lỗi tiếng Việt khi thông tin không hợp lệ hoặc hệ thống gặp sự cố.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPasswordController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordController.class.getName());

    private final UserService userService = new UserService(new UserDAO());

    /**
     * Hiển thị form nhập email để khởi động quy trình quên mật khẩu.
     *
     * @param request  yêu cầu HTTP của người dùng
     * @param response phản hồi HTTP để forward tới giao diện tương ứng
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "auth/forgot-password");
    }

    /**
     * Tiếp nhận email, gửi yêu cầu đặt lại mật khẩu và hiển thị kết quả cho người dùng.
     *
     * @param request  yêu cầu HTTP chứa địa chỉ email của người dùng
     * @param response phản hồi HTTP để forward lại trang kèm thông báo
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        try {
            String resetBaseUrl = buildResetBaseUrl(request);
            userService.requestPasswordReset(email, resetBaseUrl);
            request.setAttribute("success", "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn");
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when requesting password reset, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
        }
        request.setAttribute("email", email);
        forward(request, response, "auth/forgot-password");
    }

    /**
     * Dựng URL tuyệt đối tới trang đặt lại mật khẩu dựa trên thông tin của request.
     *
     * @param request yêu cầu HTTP để lấy domain, port và context path
     * @return chuỗi URL dùng chèn vào email đặt lại mật khẩu
     */
    private String buildResetBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder builder = new StringBuilder();
        builder.append(scheme).append("://").append(serverName);
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            builder.append(":").append(port);
        }
        builder.append(contextPath).append("/reset-password");
        return builder.toString();
    }
}
