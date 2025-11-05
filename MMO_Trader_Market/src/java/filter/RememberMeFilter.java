package filter;

import dao.user.RememberMeTokenDAO;
import dao.user.UserDAO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Users;
import service.RememberMeService;

import java.io.IOException;

/**
 * Bộ lọc khôi phục phiên đăng nhập dựa trên cookie "ghi nhớ tôi" nếu tồn tại.
 */
@WebFilter("/*")
public class RememberMeFilter extends HttpFilter {

    // Dịch vụ xử lý logic ghi nhớ đăng nhập.
    private RememberMeService rememberMeService;

    // Khởi tạo filter và tạo instance RememberMeService.
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
    }

    // Trước mỗi request, thử khôi phục phiên từ cookie nếu người dùng chưa đăng nhập.
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (rememberMeService == null) {
            // Trường hợp filter được khởi tạo lại mà chưa kịp gọi init.
            rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            // Chưa có phiên hợp lệ nên thử tự động đăng nhập bằng token.
            Users user = rememberMeService.autoLogin(request, response);
            if (user != null) {
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("currentUser", user);
                newSession.setAttribute("userId", user.getId());
                newSession.setAttribute("userRole", user.getRoleId());
            }
        }
        chain.doFilter(request, response);
    }
}
