package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter bảo vệ các trang seller, đảm bảo chỉ seller mới có thể truy cập.
 * 
 * @version 1.0
 * @author AI Assistant
 */
@WebFilter(urlPatterns = {"/seller/*"})
public class SellerAuthFilter implements Filter {

    private static final int ROLE_SELLER = 2;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo filter nếu cần
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Lấy session
        HttpSession session = httpRequest.getSession(false);

        // Kiểm tra xem user đã đăng nhập chưa
        if (session == null || session.getAttribute("userId") == null) {
            // Chưa đăng nhập, redirect về trang login
            String contextPath = httpRequest.getContextPath();
            String loginUrl = contextPath + "/auth?redirect=" + httpRequest.getRequestURI();
            httpResponse.sendRedirect(loginUrl);
            return;
        }

        // Kiểm tra role
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != ROLE_SELLER) {
            // Không phải seller, redirect về home hoặc hiển thị lỗi 403
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                "Bạn không có quyền truy cập trang này. Chỉ dành cho Seller.");
            return;
        }

        // Cho phép tiếp tục
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup nếu cần
    }
}
