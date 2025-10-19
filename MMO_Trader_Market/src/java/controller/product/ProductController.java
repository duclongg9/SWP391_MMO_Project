package controller.product;

import controller.BaseController;
import service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller hiển thị danh sách sản phẩm trong marketplace.
 */
@WebServlet(name = "ProductController", urlPatterns = {"/products"})
public class ProductController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy toàn bộ sản phẩm (hoặc có thể lọc theo trạng thái)
        request.setAttribute("products", productService.findAll());

        // Forward đến trang JSP trong thư mục /WEB-INF/views/product/list.jsp
        forward(request, response, "product/list");
    }
}
