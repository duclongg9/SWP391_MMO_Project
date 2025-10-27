package controller.guide;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cung cấp trang styleguide để các thành viên mới có thể xem nhanh các template
 * UI.
 */
@WebServlet(name = "UiGuideController", urlPatterns = {"/styleguide"})
public class UiGuideController extends BaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "styleguide/index");
    }
}
