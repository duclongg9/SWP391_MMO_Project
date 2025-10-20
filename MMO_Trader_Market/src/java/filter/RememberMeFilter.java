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
 * Filter that restores user sessions from remember-me cookies when applicable.
 */
@WebFilter("/*")
public class RememberMeFilter extends HttpFilter {

    private RememberMeService rememberMeService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (rememberMeService == null) {
            rememberMeService = new RememberMeService(new RememberMeTokenDAO(), new UserDAO());
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
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
