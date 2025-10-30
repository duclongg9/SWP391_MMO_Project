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
import units.IdObfuscator;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Điều phối luồng "Chi tiết sản phẩm" cho từng sản phẩm cụ thể.
 * <p>
 * - Tải đầy đủ thông tin, hình ảnh, mô tả, giá và chi tiết người bán. - Gợi ý
 * sản phẩm tương tự để người dùng tham khảo thêm. - Xác định quyền mua dựa trên
 * trạng thái đăng nhập và tình trạng hàng hóa.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ProductDetailController", urlPatterns = {"/product/detail/*"})
public class ProductDetailController extends BaseController {
    private static final long serialVersionUID = 1L;
    // Dịch vụ sản phẩm giúp truy vấn chi tiết và tìm kiếm sản phẩm liên quan.
    private final ProductService productService = new ProductService();
    // Xử lý yêu cầu hiển thị chi tiết sản phẩm bằng token mã hóa hoặc ID cũ.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = extractTokenFromPath(request); //    // Tách token sản phẩm từ phần path của URL thân thiện.
        if (token == null) {
            int legacyId = parsePositiveInt(request.getParameter("id")); //id phải là số dương
            if (legacyId > 0) {
                // Hỗ trợ URL cũ ?id=... bằng cách chuyển sang đường dẫn mới.
                String redirect = buildProductRedirect(request, legacyId);
                response.sendRedirect(redirect);
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int productId;
        try {
            // Giải mã token thân thiện thành ID sản phẩm thực tế.
            productId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            // Tải dữ liệu chi tiết sản phẩm và danh sách gợi ý tương tự.
            ProductDetailView product = productService.getPublicDetail(productId);
            List<ProductSummaryView> similarProducts = productService.findSimilarProducts(
                    product.getProductType(), product.getId());
            HttpSession session = request.getSession(false);
            boolean isAuthenticated = session != null && session.getAttribute("userId") != null;

            if (session != null) {
                String purchaseError = (String) session.getAttribute("purchaseError");
                if (purchaseError != null && !purchaseError.isBlank()) {
                    // Đưa thông báo lỗi mua hàng trước đó ra giao diện.
                    request.setAttribute("purchaseError", purchaseError);
                }
                if (purchaseError != null) {
                    session.removeAttribute("purchaseError");
                }
            }

            boolean canBuy = isAuthenticated && product.isAvailable() //Kiểm tra người dùng đã đăng nhập chưa 
                    // Chỉ cho phép mua khi sản phẩm còn khả dụng và có dữ liệu bàn giao.
                    && productService.hasDeliverableCredentials(product.getId(), product.getVariants());

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
            request.setAttribute("productToken", IdObfuscator.encode(product.getId()));
            forward(request, response, "product/detail");
        } catch (IllegalArgumentException ex) {
            // Không tìm thấy sản phẩm hoặc sản phẩm bị ẩn => trả 404.
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Đảm bảo chỉ chấp nhận ID số nguyên dương, trả về -1 nếu không hợp lệ.
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

    // Tách token sản phẩm từ phần path của URL thân thiện.
    private String extractTokenFromPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return null;
        }
        String token = pathInfo.charAt(0) == '/' ? pathInfo.substring(1) : pathInfo;
        int slashIndex = token.indexOf('/');
        if (slashIndex >= 0) {
            token = token.substring(0, slashIndex);
        }
        return token.isBlank() ? null : token;
    }

    // Chuyển đổi đường dẫn sử dụng tham số id cũ sang URL mới dựa trên token.
    private String buildProductRedirect(HttpServletRequest request, int productId) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/product/detail/")
                .append(IdObfuscator.encode(productId));
        List<String> queryParts = new ArrayList<>();
        request.getParameterMap().forEach((key, values) -> {
            if ("id".equals(key) || values == null) {
                return;
            }
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                queryParts.add(encodedKey + "=" + encodedValue);
            }
        });
        if (!queryParts.isEmpty()) {
            url.append('?').append(String.join("&", queryParts));
        }
        return url.toString();
    }
}
