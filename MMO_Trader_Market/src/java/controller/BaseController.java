package controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import units.NavigationBuilder;

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

    private void ensureNavigation(HttpServletRequest request) {
        if (request.getAttribute("navItems") == null) {
            request.setAttribute("navItems", NavigationBuilder.build(request));
        }
    }
}
