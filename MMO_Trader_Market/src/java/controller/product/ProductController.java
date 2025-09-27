package controller.product;

import controller.BaseController;
import service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller responsible for listing products in the marketplace.
 */
@WebServlet(name = "ProductController", urlPatterns = {"/products"})
public class ProductController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("products", productService.findAll());
        forward(request, response, "product/list");
    }
}
