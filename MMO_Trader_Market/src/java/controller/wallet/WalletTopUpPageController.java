package controller.wallet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Renders the JSP page that allows users to initiate a wallet top-up via VNPAY.
 */
@WebServlet(name = "WalletTopUpPageController", urlPatterns = {"/wallet/deposit"})
public class WalletTopUpPageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/wallet/deposit.jsp").forward(request, response);
    }
}
