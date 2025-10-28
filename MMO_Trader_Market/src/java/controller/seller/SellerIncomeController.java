package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Trang thống kê thu nhập cho người bán.
 */
@WebServlet(name = "SellerIncomeController", urlPatterns = {"/seller/income"})
public class SellerIncomeController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        request.setAttribute("pageTitle", "Thu nhập - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerTitle", "Thu nhập");
        request.setAttribute("headerSubtitle", "Theo dõi doanh thu và dòng tiền");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/income");
    }
}
