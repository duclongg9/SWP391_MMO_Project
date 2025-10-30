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
 * - Tiếp nhận email khi người dùng quên mật khẩu và sinh đường dẫn đặt lại. -
 * Gửi thông báo xác nhận đã gửi email hướng dẫn đặt lại mật khẩu. - Hiển thị
 * thông báo lỗi tiếng Việt khi thông tin không hợp lệ hoặc hệ thống gặp sự cố.
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
     * @param request yêu cầu HTTP của người dùng
     * @param response phản hồi HTTP để forward tới giao diện tương ứng
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "auth/forgot-password");
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        try {
            // Tạo đường dẫn gốc cho liên kết đặt lại mật khẩu gửi qua email.
            String resetBaseUrl = buildResetBaseUrl(request);
            // Gọi dịch vụ để sinh token và gửi email hướng dẫn đặt lại mật khẩu.
            userService.requestPasswordReset(email, resetBaseUrl);
            request.setAttribute("success", "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Thông báo lỗi nghiệp vụ (email trống, không tồn tại...).
            request.setAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            // Ghi log lỗi hệ thống bất thường và trả về mã lỗi tra soát.
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when requesting password reset, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
        }
        request.setAttribute("email", email);
        forward(request, response, "auth/forgot-password");
    }

//trả về chuỗi URL, lấy request để lấy thông tin host, cổng, context path hiện tại.
    private String buildResetBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder builder = new StringBuilder();
        // Tạo StringBuilder để ghép các phần của URL hiệu quả hơn nối chuỗi thông thường.
        builder.append(scheme).append("://").append(serverName); 
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            // Bổ sung port nếu không sử dụng cổng mặc định của HTTP/HTTPS.
            builder.append(":").append(port);
        }
        // Gắn context path và đường dẫn cố định của trang đặt lại mật khẩu.
        builder.append(contextPath).append("/reset-password");
        return builder.toString();
    }
}
