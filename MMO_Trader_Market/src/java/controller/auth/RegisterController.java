package controller.auth;

import controller.BaseController;
import dao.user.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Users;
import service.UserService;

import java.io.IOException;

/**
 * Handles registration flow for new users.
 */
@WebServlet(name = "RegisterController", urlPatterns = {"/register"})
public class RegisterController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService(new UserDAO());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "auth/register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("email", email == null ? null : email.trim());
        request.setAttribute("name", name == null ? null : name.trim());

        try {
            Users createdUser = userService.registerNewUser(email, name, password, confirmPassword);

            HttpSession session = request.getSession();
            session.setAttribute("registerSuccess", "Tạo tài khoản thành công! Vui lòng đăng nhập.");
            session.setAttribute("newUserEmail", createdUser.getEmail());

            response.sendRedirect(request.getContextPath() + "/auth");
              } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, "auth/register");
        } catch (RuntimeException e) {
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Vui lòng thử lại sau.");
            forward(request, response, "auth/register");
        }
    }
}