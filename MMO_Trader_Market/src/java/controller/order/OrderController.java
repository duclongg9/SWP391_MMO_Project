package controller.order;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Orders;
import model.Products;
import model.view.OrderDetailView;
import service.OrderService;
import units.IdObfuscator;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>Servlet điều phối toàn bộ luồng mua sản phẩm của người mua từ lúc gửi yêu cầu "Mua ngay"
 * tới khi người dùng truy cập lịch sử đơn và xem dữ liệu bàn giao.</p>
 * <p>Controller này chịu trách nhiệm:</p>
 * <ul>
 *     <li>Chuẩn hóa và xác thực tham số HTTP trước khi ủy quyền cho tầng dịch vụ xử lý nghiệp vụ.</li>
 *     <li>Định tuyến tới đúng trang JSP, truyền dữ liệu view model (OrderRow, OrderDetailView)</li>
 *     <li>Gắn kết với hàng đợi xử lý bất đồng bộ thông qua {@link service.OrderService#placeOrderPending}</li>
 * </ul>
 *
 * @author longpdhe171902
 */
@WebServlet(name = "OrderController", urlPatterns = {
    "/order/buy-now",
    "/orders",
    "/orders/my",
    "/orders/detail/*",
    "/orders/unlock"
})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int ROLE_SELLER = 2;
    private static final int ROLE_BUYER = 3;

    private final OrderService orderService = new OrderService();

    /**
     * Xử lý các yêu cầu POST. Ở thời điểm hiện tại chỉ có một entry point duy nhất là
     * <code>/order/buy-now</code>. Dòng chảy cụ thể:
     * <ol>
     *     <li>Đọc {@code servletPath} để xác định hành động.</li>
     *     <li>Nếu là "buy-now" thì chuyển cho {@link #handleBuyNow(HttpServletRequest, HttpServletResponse)}.</li>
     *     <li>Nếu không khớp, trả về HTTP 405 để thông báo phương thức không được hỗ trợ.</li>
     * </ol>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/order/buy-now".equals(path)) {
            handleBuyNow(request, response);
            return;
        } else if ("/orders/unlock".equals(path)) {
            handleUnlockCredentials(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Xử lý các yêu cầu GET cho ba đường dẫn:
     * <ul>
     *     <li><code>/orders</code>: chuyển hướng 302 tới trang lịch sử cá nhân để tái sử dụng logic phân trang.</li>
     *     <li><code>/orders/my</code>: tải danh sách đơn có lọc, gán vào request attribute để JSP dựng bảng.</li>
     *     <li><code>/orders/detail/&lt;token&gt;</code>: hiển thị chi tiết kèm credential nếu đã bàn giao.</li>
     * </ul>
     * Nếu đường dẫn không khớp sẽ phản hồi HTTP 404.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/orders" ->
                redirectToMyOrders(request, response);
            case "/orders/my" ->
                showMyOrders(request, response);
            case "/orders/detail" ->
                showOrderDetail(request, response);
            default ->
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Chuyển hướng người dùng tới trang danh sách đơn cá nhân.
     */
    private void redirectToMyOrders(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String target = request.getContextPath() + "/orders/my";
        response.sendRedirect(target);
    }

    /**
     * Tiếp nhận yêu cầu mua ngay từ trang chi tiết sản phẩm. Hàm này xử lý toàn bộ phần
     * đầu luồng cho tới khi đơn được đưa vào hàng đợi:
     * <ol>
     *     <li>Kiểm tra người dùng đăng nhập và có vai trò buyer/seller để được phép mua.</li>
     *     <li>Đọc các tham số {@code productId}, {@code qty}, {@code variantCode} do form gửi lên.</li>
     *     <li>Chuẩn hóa khóa idempotent {@code idemKey} (nếu không gửi thì sinh ngẫu nhiên) để chống double-submit.</li>
     *     <li>Ủy quyền cho {@link OrderService#placeOrderPending(int, int, int, String, String)}.</li>
     *     <li>Nếu thành công, redirect sang trang chi tiết đơn; nếu lỗi nghiệp vụ -> HTTP 400/409.</li>
     * </ol>
     */
    private void handleBuyNow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        int productId = decodeIdentifier(request.getParameter("productId"));
        int quantity = parsePositiveInt(request.getParameter("qty"));
        String variantCode = normalize(request.getParameter("variantCode"));
        if (userId == null || productId <= 0 || quantity <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String idemKeyParam = Optional.ofNullable(request.getParameter("idemKey"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(UUID.randomUUID().toString());
        try {
            int orderId = orderService.placeOrderPending(userId, productId, quantity, variantCode, idemKeyParam);
            String token = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + token + "?processing=1";
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
        }
    }

    /**
     * Hiển thị danh sách đơn hàng của người mua kèm phân trang và lọc trạng thái.
     * Tầng controller chịu trách nhiệm thu thập tham số và chuyển dữ liệu xuống JSP:
     * <ol>
     *     <li>Lấy trạng thái filter, số trang, kích thước trang từ query string.</li>
     *     <li>Gọi {@link OrderService#getMyOrders(int, String, int, int)} để truy vấn DB qua DAO.</li>
     *     <li>Đổ danh sách {@code OrderRow} và meta phân trang vào request attribute "items", "total", ...</li>
     *     <li>Forward tới view <code>/WEB-INF/views/order/my.jsp</code> để dựng giao diện.</li>
     * </ol>
     */
    private void showMyOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String rawCodeParam = request.getParameter("code");
        String statusParam = normalize(request.getParameter("status"));
        String productParam = normalize(request.getParameter("product"));
        LocalDate today = LocalDate.now();
        LocalDate fromDate = normalizeDate(request.getParameter("fromDate"), today);
        LocalDate toDate = normalizeDate(request.getParameter("toDate"), today);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            fromDate = toDate;
        }
        Integer orderIdFilter = extractOrderId(rawCodeParam);
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);

        var result = orderService.getMyOrders(userId, statusParam, orderIdFilter, productParam, fromDate, toDate, page, size);
        Map<String, String> statusLabels = orderService.getStatusLabels();

        request.setAttribute("items", result.getItems());
        request.setAttribute("total", result.getTotalItems());
        request.setAttribute("page", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("size", result.getPageSize());
        request.setAttribute("status", statusParam == null ? "" : statusParam);
        request.setAttribute("orderCode", rawCodeParam == null ? "" : rawCodeParam.trim());
        request.setAttribute("productName", productParam == null ? "" : productParam);
        request.setAttribute("fromDate", fromDate == null ? "" : fromDate.toString());
        request.setAttribute("toDate", toDate == null ? "" : toDate.toString());
        request.setAttribute("todayIso", today.toString());
        request.setAttribute("statusLabels", statusLabels);
        request.setAttribute("statusOptions", statusLabels);

        forward(request, response, "order/my");
    }

    /**
     * Hiển thị chi tiết một đơn hàng cụ thể nếu thuộc sở hữu người dùng.
     * Sau khi qua bước kiểm tra quyền truy cập, controller sẽ:
     * <ol>
     *     <li>Đọc {@code id} của đơn từ query string và validate.</li>
     *     <li>Gọi {@link OrderService#getDetail(int, int)} để load đơn, sản phẩm và credential.</li>
     *     <li>Đưa các đối tượng domain vào request attribute cho JSP: {@code order}, {@code product}, {@code credentials}.</li>
     *     <li>Tính sẵn nhãn trạng thái tiếng Việt thông qua {@link OrderService#getStatusLabel(String)}.</li>
     * </ol>
     */
    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String token = extractTokenFromPath(request);
        if (token == null) {
            int legacyId = parsePositiveInt(request.getParameter("id"));
            if (legacyId > 0) {
                String redirectUrl = buildOrderDetailRedirect(request, legacyId);
                response.sendRedirect(redirectUrl);
                return;
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int orderId;
        try {
            orderId = IdObfuscator.decode(token);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Optional<OrderDetailView> detailOpt = orderService.getDetail(orderId, userId);
        if (detailOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String unlockSuccess = null;
        String unlockError = null;
        if (session != null) {
            unlockSuccess = (String) session.getAttribute("orderUnlockSuccess");
            unlockError = (String) session.getAttribute("orderUnlockError");
            session.removeAttribute("orderUnlockSuccess");
            session.removeAttribute("orderUnlockError");
        }
        boolean unlocked = orderService.hasUnlockedCredentials(orderId, userId);
        boolean includeCredentials = unlocked || unlockSuccess != null;
        OrderDetailView detail = includeCredentials
                ? orderService.getDetail(orderId, userId, true).orElse(detailOpt.get())
                : detailOpt.get();
        Orders order = detail.order();
        Products product = detail.product();
        List<String> credentials = detail.credentials();

        request.setAttribute("order", order);
        request.setAttribute("product", product);
        request.setAttribute("credentials", credentials);
        request.setAttribute("statusLabel", orderService.getStatusLabel(order.getStatus()));
        boolean showProcessingModal = "1".equals(request.getParameter("processing"));
        request.setAttribute("showProcessingModal", showProcessingModal);
        request.setAttribute("credentialsUnlocked", includeCredentials);
        request.setAttribute("unlockSuccessMessage", unlockSuccess);
        request.setAttribute("unlockErrorMessage", unlockError);
        request.setAttribute("unlockJustConfirmed", unlockSuccess != null);
        request.setAttribute("orderToken", IdObfuscator.encode(orderId));

        forward(request, response, "order/detail");
    }

    /**
     * Xác nhận mở khóa thông tin bàn giao.
     */
    private void handleUnlockCredentials(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (!isBuyerOrSeller(session)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String token = normalize(request.getParameter("orderToken"));
        int orderId = decodeIdentifier(token);
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String clientIp = resolveClientIp(request);
        try {
            OrderService.CredentialUnlockResult result = orderService.unlockCredentials(orderId, userId, clientIp);
            String message = result.firstView()
                    ? "Thông tin bàn giao đã được mở khóa và ghi nhận lượt xem đầu tiên."
                    : "Bạn đã mở khóa thông tin bàn giao trước đó, hệ thống cập nhật thời gian truy cập mới.";
            session.setAttribute("orderUnlockSuccess", message);
            String canonicalToken = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + canonicalToken + "?unlocked=1";
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException ex) {
            session.setAttribute("orderUnlockError", ex.getMessage());
            String canonicalToken = IdObfuscator.encode(orderId);
            String redirectUrl = request.getContextPath() + "/orders/detail/" + canonicalToken;
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Kiểm tra quyền truy cập của phiên người dùng (buyer hoặc seller).
     */
    private boolean isBuyerOrSeller(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && (ROLE_BUYER == role || ROLE_SELLER == role);
    }

    /**
     * Chuyển đổi chuỗi sang số nguyên dương, trả về -1 nếu không hợp lệ.
     */
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

    /**
     * Chuyển chuỗi sang số nguyên dương, trả về giá trị mặc định khi không hợp lệ.
     */
    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        int parsed = parsePositiveInt(value);
        return parsed > 0 ? parsed : defaultValue;
    }

    /**
     * Chuẩn hóa chuỗi: cắt khoảng trắng và trả về {@code null} nếu rỗng.
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer extractOrderId(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String digits = normalized.startsWith("#") ? normalized.substring(1) : normalized;
        int parsed = parsePositiveInt(digits);
        return parsed > 0 ? parsed : null;
    }

    private LocalDate normalizeDate(String value, LocalDate today) {
        LocalDate parsed = parseDate(value);
        if (parsed == null) {
            return null;
        }
        return parsed.isAfter(today) ? today : parsed;
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

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

    private String buildOrderDetailRedirect(HttpServletRequest request, int orderId) {
        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/orders/detail/")
                .append(IdObfuscator.encode(orderId));
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

    private int decodeIdentifier(String value) {
        if (value == null) {
            return -1;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return -1;
        }
        try {
            return IdObfuscator.decode(normalized);
        } catch (IllegalArgumentException ex) {
            return parsePositiveInt(normalized);
        }
    }

    /**
     * Ưu tiên đọc IP thực tế từ header proxy (X-Forwarded-For) trước khi fallback về địa chỉ kết nối.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}
