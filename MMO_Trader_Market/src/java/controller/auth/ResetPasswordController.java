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

/**
 * Điều phối luồng "Đặt lại mật khẩu" khi người dùng truy cập từ email quên mật
 * khẩu.
 * <p>
 * - Hiển thị form đặt lại kèm kiểm tra token hợp lệ. - Xác thực mật khẩu mới,
 * cập nhật dữ liệu và thông báo thành công. - Ghi nhận lỗi tiếng Việt khi token
 * sai, mật khẩu không hợp lệ hoặc xảy ra sự cố.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ResetPasswordController", urlPatterns = {"/reset-password"})
public class ResetPasswordController extends BaseController {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordController.class.getName());
    private final UserService userService = new UserService(new UserDAO());
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token == null || token.isBlank()) {
            request.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ");
        } else {
            request.setAttribute("token", token);
        }
        forward(request, response, "auth/reset-password");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token"); 
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        try {
            userService.resetPassword(token, password, confirmPassword);
            HttpSession session = request.getSession();
            session.setAttribute("resetSuccess", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại"); //flash message
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error when resetting password, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
        }
        request.setAttribute("token", token);
        forward(request, response, "auth/reset-password");
    }
}
