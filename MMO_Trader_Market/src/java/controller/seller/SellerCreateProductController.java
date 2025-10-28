package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Trang tạo sản phẩm mới cho người bán.
 */
@WebServlet(name = "SellerCreateProductController", urlPatterns = {"/seller/products/create"})
public class SellerCreateProductController extends SellerBaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureSellerAccess(request, response)) {
            return;
        }
        request.setAttribute("pageTitle", "Tạo sản phẩm mới - Quản lý cửa hàng");
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("headerTitle", "Tạo sản phẩm");
        request.setAttribute("headerSubtitle", "Đăng bán sản phẩm MMO nhanh chóng");
        request.setAttribute("headerModifier", "layout__header--split");
        forward(request, response, "seller/create-product");
    }
}
