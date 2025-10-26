package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.view.product.ProductDetailView;
import model.view.product.ProductSummaryView;
import service.ProductService;

import java.io.IOException;
import java.util.List;

/**
 * Điều phối luồng "Chi tiết sản phẩm" cho từng sản phẩm cụ thể.
 * <p>
 * - Tải đầy đủ thông tin, hình ảnh, mô tả, giá và chi tiết người bán.
 * - Gợi ý sản phẩm tương tự để người dùng tham khảo thêm.
 * - Xác định quyền mua dựa trên trạng thái đăng nhập và tình trạng hàng hóa.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
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
            ProductDetailView product = productService.getPublicDetail(productId);
            List<ProductSummaryView> similarProducts = productService.findSimilarProducts(
                    product.getProductType(), product.getId());
            HttpSession session = request.getSession(false);
            boolean isAuthenticated = session != null && session.getAttribute("userId") != null;

            boolean canBuy = isAuthenticated && product.isAvailable()
                    && productService.hasDeliverableCredentials(product.getId());

            request.setAttribute("pageTitle", product.getName());
            request.setAttribute("headerTitle", product.getName());
            request.setAttribute("headerSubtitle", "Thông tin chi tiết sản phẩm");
            request.setAttribute("product", product);
            request.setAttribute("variantOptions", product.getVariants());
            request.setAttribute("galleryImages", product.getGalleryImages());
            request.setAttribute("priceMin", product.getMinPrice());
            request.setAttribute("priceMax", product.getMaxPrice());
            request.setAttribute("variantSchema", product.getVariantSchema());
            request.setAttribute("variantOptionsJson", product.getVariantsJson());
            request.setAttribute("similarProducts", similarProducts);
            request.setAttribute("canBuy", canBuy);
            request.setAttribute("isAuthenticated", isAuthenticated);
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
