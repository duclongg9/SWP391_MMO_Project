package controller.wallet;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Users;
import service.WalletService;
import service.dto.WalletOverview;

import java.io.IOException;

/**
 * Hiển thị ví điện tử dành cho người mua.
 */
@WebServlet(name = "WalletController", urlPatterns = {"/wallet"})
public class WalletController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int TRANSACTION_LIMIT = 10;

    private final WalletService walletService = new WalletService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser = requireAuthenticatedUser(request, response);
        if (currentUser == null) {
            return;
        }

        try {
            WalletOverview overview = walletService.loadOverview(currentUser.getId(), TRANSACTION_LIMIT);

            request.setAttribute("pageTitle", "Ví điện tử - MMO Trader Market");
            request.setAttribute("bodyClass", "layout");
            request.setAttribute("headerTitle", "Ví điện tử");
            request.setAttribute("headerSubtitle", "Theo dõi số dư và giao dịch gần đây");
            request.setAttribute("headerModifier", "layout__header--split");

            request.setAttribute("walletBalance", overview.balance());
            request.setAttribute("walletHold", overview.holdBalance());
            request.setAttribute("walletCurrency", overview.currency());
            request.setAttribute("transactions", overview.transactions());
        } catch (IllegalArgumentException ex) {
            request.setAttribute("pageTitle", "Ví điện tử - MMO Trader Market");
            request.setAttribute("bodyClass", "layout");
            request.setAttribute("headerTitle", "Ví điện tử");
            request.setAttribute("headerSubtitle", "Không tìm thấy ví cho tài khoản hiện tại");
            request.setAttribute("headerModifier", "layout__header--split");
            request.setAttribute("walletBalance", java.math.BigDecimal.ZERO);
            request.setAttribute("walletHold", java.math.BigDecimal.ZERO);
            request.setAttribute("walletCurrency", "VND");
            request.setAttribute("transactions", java.util.List.of());
            request.setAttribute("error", ex.getMessage());
        }

        forward(request, response, "wallet/overview");
    }

    private Users requireAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return null;
        }
        Users currentUser = (Users) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return null;
        }
        return currentUser;
    }
}
