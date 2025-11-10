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
 * Bo loc khoi phuc phien dang nhap dua tren cookie "ghi nho toi" neu ton tai.
 */
@WebFilter("/*")
public class RememberMeFilter extends HttpFilter {

    // Dich vu xu ly logic ghi nho dang nhap.
    private RememberMeService rememberMeService;

    // Khoi tao filter va tao instance RememberMeService.
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
    }

    // Truoc moi request, thu khoi phuc phien tu cookie neu nguoi dung chua dang nhap.
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (rememberMeService == null) {
            // Truong hop filter duoc khoi tao lai ma chua kip goi init.
            rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            // Chua co phien hop le nen thu tu dong dang nhap bang token.
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
