package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.product.ProductDetail;
import service.ProductService;

import java.io.IOException;

/**
 * Displays detail information for a single product.
 */
@WebServlet(name = "ProductDetailController", urlPatterns = {"/product/detail"})
public class ProductDetailController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int productId = parsePositiveInt(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            ProductDetail product = productService.getDetail(productId);
            request.setAttribute("pageTitle", product.getName());
            request.setAttribute("headerTitle", product.getName());
            request.setAttribute("headerSubtitle", "Thông tin chi tiết sản phẩm");
            request.setAttribute("product", product);
            request.setAttribute("canBuy", product.getInventoryCount() != null && product.getInventoryCount() > 0);
            forward(request, response, "product/detail");
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private int parsePositiveInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
