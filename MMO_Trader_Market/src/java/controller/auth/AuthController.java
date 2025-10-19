package controller.auth;

import controller.BaseController;
import units.CredentialsValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles login and logout requests following the MVC pattern.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;

    

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
               HttpSession session = request.getSession(false); // không tạo session mới nếu chưa có
        if (session != null) {
            String successMessage = (String) session.getAttribute("registerSuccess"); // 1) Flash message sau khi đăng ký
            if (successMessage != null) {
                request.setAttribute("success", successMessage);
                session.removeAttribute("registerSuccess");// chỉ hiện đúng 1 lần
            }
// 2) Prefill email vào ô username ở trang login
            String emailFromRegister = (String) session.getAttribute("newUserEmail");
            if (emailFromRegister != null) {
                request.setAttribute("prefillUsername", emailFromRegister);
                session.removeAttribute("newUserEmail"); // không lưu lại cho lần sau
            }
        }
        forward(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (!CredentialsValidator.isValid(username, password)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
            forward(request, response, "auth/login");
            return;
        }

//        User user = userService.authenticate(username, password);
//        if (user == null) {
//            request.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
//            forward(request, response, "auth/login");
//            return;
//        }
//        request.getSession().setAttribute("currentUser", user);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
