package controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import units.NavigationBuilder;
import units.RoleHomeResolver;

/**
 * Base servlet that provides convenience helpers for forwarding to JSP views.
 */
public abstract class BaseController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        ensureNavigation(request);
        String jsp = ViewResolver.resolve(view);
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    /**
     * Redirects administrators to the dedicated admin dashboard.
     *
     * @return {@code true} if the response has been committed due to a redirect
     */
    protected boolean redirectAdminHome(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Integer roleId = (Integer) session.getAttribute("userRole");
        if (roleId != null && roleId == 1) {
            response.sendRedirect(request.getContextPath() + RoleHomeResolver.ADMIN_HOME);
            return true;
        }
        return false;
    }

    private void ensureNavigation(HttpServletRequest request) {
        if (request.getAttribute("navItems") == null) {
            request.setAttribute("navItems", NavigationBuilder.build(request));
        }
    }
}
