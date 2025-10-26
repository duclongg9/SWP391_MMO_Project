package controller.guide;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Renders the standalone FAQ page with the existing JSP content.
 */
@WebServlet(name = "FaqController", urlPatterns = {"/faq"})
public class FaqController extends BaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Câu hỏi thường gặp");
        forward(request, response, "faq");
    }
}
