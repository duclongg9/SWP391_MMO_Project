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
import model.Users;
import service.UserService;
import units.RoleHomeResolver;


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
            Users user = userService.resetPassword(token, password, confirmPassword);
            HttpSession session = renewSession(request); // tạo ss mới, xóa ss cũ
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRoleId());
            session.setAttribute("success", "Đổi mật khẩu thành công. Bạn đã được đăng nhập.");
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.resolve(user));
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


    private HttpSession renewSession(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        return request.getSession(true);
    }
}
