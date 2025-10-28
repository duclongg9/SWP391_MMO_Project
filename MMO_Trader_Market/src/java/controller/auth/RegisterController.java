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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Điều khiển luồng "Đăng ký tài khoản" cho khách truy cập chưa có tài khoản.
 * <p>
 * - Thu thập thông tin email, họ tên, mật khẩu và xác nhận điều khoản. - Tạo
 * mới tài khoản khách (Role Guest) để người dùng có thể đăng nhập vào hệ thống.
 * - Hiển thị thông báo thành công và chuyển hướng về trang đăng nhập.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "RegisterController", urlPatterns = {"/register"})
public class RegisterController extends BaseController {

    // Mã phiên bản phục vụ tuần tự hóa servlet.
    private static final long serialVersionUID = 1L;

    // Bộ ghi log dùng theo dõi các lỗi hệ thống trong quá trình đăng ký.
    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    // Lớp dịch vụ xử lý nghiệp vụ đăng ký người dùng mới.
    private final UserService userService = new UserService(new UserDAO());

    // Hiển thị biểu mẫu đăng ký cho khách truy cập.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "auth/register");
    }

    // Tiếp nhận thông tin đăng ký và tạo tài khoản mới.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        boolean acceptedTerms = request.getParameter("acceptTerms") != null;

        request.setAttribute("email", email == null ? null : email.trim());
        request.setAttribute("name", name == null ? null : name.trim());
        request.setAttribute("acceptTermsChecked", acceptedTerms);

        if (!acceptedTerms) {
            // Bắt buộc người dùng chấp nhận điều khoản sử dụng.
            request.setAttribute("error", "Vui lòng đồng ý với Điều khoản sử dụng để tiếp tục.");
            forward(request, response, "auth/register");
            return;
        }

        try {
            // Gọi dịch vụ để tạo tài khoản mới dựa trên thông tin từ form.
            Users createdUser = userService.registerNewUser(email, name, password, confirmPassword);

            HttpSession session = request.getSession();
            // Lưu thông báo và email để hiển thị sau khi chuyển hướng sang trang đăng nhập.
            session.setAttribute("registerSuccess",
                    "Tạo tài khoản thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
            session.setAttribute("newUserEmail", createdUser.getEmail());
            session.setAttribute("pendingVerificationEmail", createdUser.getEmail());
            session.setAttribute("showVerificationModal", Boolean.TRUE);
            session.setAttribute("verificationNotice",
                    "Chúng tôi đã gửi mã xác thực đến " + createdUser.getEmail() + ". Vui lòng kiểm tra hộp thư.");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi nghiệp vụ (email đã tồn tại, mật khẩu không hợp lệ...).
            request.setAttribute("error", e.getMessage());
            forward(request, response, "auth/register");
        } catch (RuntimeException e) {
            // Ghi log lỗi bất thường và trả lại mã lỗi cho người dùng.
            String errorId = UUID.randomUUID().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error during registration, errorId=" + errorId, e);
            request.setAttribute("error", "Hệ thống đang gặp sự cố. Mã lỗi: " + errorId);
            forward(request, response, "auth/register");
        }
    }
}
