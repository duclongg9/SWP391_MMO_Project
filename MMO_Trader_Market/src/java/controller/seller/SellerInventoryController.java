package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Trang cập nhật kho hàng dành cho người bán.
 */
@WebServlet(name = "SellerInventoryController", urlPatterns = {"/seller/inventory"})
public class SellerInventoryController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object success = session.getAttribute("sellerInventoryFlashSuccess");
            if (success instanceof String) {
                request.setAttribute("flashSuccess", success);
            }
            Object error = session.getAttribute("sellerInventoryFlashError");
            if (error instanceof String) {
                request.setAttribute("flashError", error);
            }
            session.removeAttribute("sellerInventoryFlashSuccess");
            session.removeAttribute("sellerInventoryFlashError");
        }
        request.setAttribute("pageTitle", "Cập nhật kho - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/inventory");
    }
}
