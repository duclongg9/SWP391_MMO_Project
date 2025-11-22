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
        try {
            String resetBaseUrl = buildResetBaseUrl(request); // để tạo URL gốc cho trang reset
            boolean emailSent = userService.requestPasswordReset(email, resetBaseUrl); //tạo yêu cầu đặt lại mật khẩu
            if (emailSent) {
                request.setAttribute("success", "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn");
            } else {
                request.setAttribute("success", "Bạn đã yêu cầu đặt lại mật khẩu trong 24 giờ qua. Vui lòng kiểm tra email hoặc thử lại sau khi liên kết hết hạn.");
            }
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

//tạo ra phần đầu của URL dùng trong email reset mật khẩu
    private String buildResetBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); //http
        String serverName = request.getServerName(); //localhost
        int port = request.getServerPort(); //8080
        String contextPath = request.getContextPath(); //tên app
        //Tomcat lấy từ HTTP request
        StringBuilder builder = new StringBuilder(); //Dùng StringBuilder để lắp thành URL
        builder.append(scheme).append("://").append(serverName); 
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            builder.append(":").append(port); // phải ghi :port
        }
        builder.append(contextPath).append("/reset-password"); //Thêm contextPath và path /reset-password
        return builder.toString();
    }
}
