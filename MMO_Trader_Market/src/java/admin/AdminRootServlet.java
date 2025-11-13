package admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "AdminRootServlet", urlPatterns = {"/admin"})
public class AdminRootServlet extends AbstractAdminServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Đã check login trong AbstractAdminServlet.service()
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }
}
