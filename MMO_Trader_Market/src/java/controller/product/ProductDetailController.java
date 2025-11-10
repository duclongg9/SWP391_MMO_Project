package controller.product;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.view.product.ProductDetailView;
import model.view.product.ProductSummaryView;
import model.view.PurchasePreviewResult;
import service.OrderService;
import service.ProductService;
import units.IdObfuscator;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Dieu phoi luong "Chi tiet san pham" cho tung san pham cu the.
 * <p>
 * - Tai day du thong tin, hinh anh, mo ta, gia va chi tiet nguoi ban. - Goi y
 * san pham tuong tu de nguoi dung tham khao them. - Xac dinh quyen mua dua tren
 * trang thai dang nhap va tinh trang hang hoa.
 *
 * @version 1.0 27/05/2024
 * @author hoaltthe176867
 */
@WebServlet(name = "ProductDetailController", urlPatterns = {"/product/detail/*"})
public class ProductDetailController extends BaseController {
    private static final long serialVersionUID = 1L;
    // Dich vu san pham giup truy van chi tiet va tim kiem san pham lien quan.

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    // Xử lý yêu cầu hiển thị chi tiết sản phẩm bằng token mã hóa hoặc ID cũ.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.endsWith("/availability")) {
            handleAvailabilityCheck(request, response);
            return;
        }

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

    private void handleAvailabilityCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String token = extractTokenFromPath(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int productId;
        try {
            productId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        int quantity = parsePositiveInt(request.getParameter("quantity"));
        if (quantity <= 0) {
            quantity = 1;
        }
        String variantCode = normalize(request.getParameter("variantCode"));
        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        PurchasePreviewResult result = orderService.previewPurchase(userId, productId, quantity, variantCode);
        response.getWriter().write(buildAvailabilityPayload(result, productId, quantity, variantCode));
    }

    private String buildAvailabilityPayload(PurchasePreviewResult result, int productId, int quantity, String variantCode) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        appendBoolean(builder, "ok", result.ok());
        appendBoolean(builder, "canPurchase", result.canPurchase());
        appendBoolean(builder, "productAvailable", result.productAvailable());
        appendBoolean(builder, "variantValid", result.variantValid());
        appendBoolean(builder, "hasInventory", result.hasInventory());
        appendBoolean(builder, "hasCredentials", result.hasCredentials());
        appendBoolean(builder, "walletExists", result.walletExists());
        appendBoolean(builder, "walletActive", result.walletActive());
        appendBoolean(builder, "walletHasBalance", result.walletHasBalance());
        appendNumber(builder, "productId", productId);
        appendNumber(builder, "quantity", quantity);
        appendNumber(builder, "availableInventory", result.availableInventory());
        appendNumber(builder, "availableCredentials", result.availableCredentials());
        appendString(builder, "variantCode", variantCode);
        appendDecimal(builder, "unitPrice", result.unitPrice());
        appendDecimal(builder, "totalPrice", result.totalPrice());
        appendDecimal(builder, "walletBalance", result.walletBalance());
        appendStringArray(builder, "blockers", result.blockers());
        trimTrailingComma(builder);
        builder.append('}');
        return builder.toString();
    }

    private void appendBoolean(StringBuilder builder, String name, boolean value) {
        builder.append('"').append(name).append('"').append(':').append(value).append(',');
    }

    private void appendNumber(StringBuilder builder, String name, int value) {
        builder.append('"').append(name).append('"').append(':').append(value).append(',');
    }

    private void appendString(StringBuilder builder, String name, String value) {
        if (value == null) {
            return;
        }
        builder.append('"').append(name).append('"').append(':').append('"').append(escapeJson(value)).append('"').append(',');
    }

    private void appendDecimal(StringBuilder builder, String name, java.math.BigDecimal value) {
        if (value == null) {
            return;
        }
        builder.append('"').append(name).append('"').append(':')
                .append('"').append(value.stripTrailingZeros().toPlainString()).append('"').append(',');
    }

    private void appendStringArray(StringBuilder builder, String name, List<String> values) {
        builder.append('"').append(name).append('"').append(':');
        builder.append('[');
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append('"').append(escapeJson(values.get(i))).append('"');
            }
        }
        builder.append(']').append(',');
    }

    private void trimTrailingComma(StringBuilder builder) {
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == ',') {
            builder.setLength(length - 1);
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length() + 8);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private String normalize(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
