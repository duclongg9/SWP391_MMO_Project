package controller.dashboard;

import controller.BaseController;
import service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import units.RoleHomeResolver;
import java.io.IOException;

/**
 * Simple controller that loads the dashboard view.
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/dashboard"})
public class DashboardController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (redirectAdminHomeIfPossible(request, response)) {
            return;
        }
        request.setAttribute("products", productService.homepageHighlights());
        forward(request, response, "dashboard/index");
    }

    private boolean redirectAdminHomeIfPossible(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            return super.redirectAdminHome(request, response);
        } catch (NoSuchMethodError ex) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return false;
            }
            Object role = session.getAttribute("userRole");
            if (role instanceof Integer && (Integer) role == 1) {
                response.sendRedirect(request.getContextPath() + RoleHomeResolver.ADMIN_HOME);
                return true;
            }
            return false;
        }
    }
}
