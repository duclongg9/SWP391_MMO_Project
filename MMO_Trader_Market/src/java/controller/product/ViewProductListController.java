package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.view.product.ProductSummaryView;
import service.ProductService;

import java.io.IOException;
import java.util.List;

/**
 * Displays a simplified list of all available products.
 */
@WebServlet(name = "ViewProductListController", urlPatterns = {"/viewproductlist"})
public class ViewProductListController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProductSummaryView> products = productService.getAllAvailableProducts();

        request.setAttribute("pageTitle", "Danh sách sản phẩm");
        request.setAttribute("headerTitle", "Danh sách sản phẩm");
        request.setAttribute("headerSubtitle", "Xem tất cả sản phẩm đang được bán trên MMO Trader Market.");
        request.setAttribute("items", products);
        request.setAttribute("totalItems", products.size());

        forward(request, response, "product/viewproductlist");
    }
}
