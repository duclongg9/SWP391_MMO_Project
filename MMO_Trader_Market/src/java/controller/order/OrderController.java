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

import java.io.IOException;
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
    "/orders/detail"
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
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Xử lý các yêu cầu GET cho ba đường dẫn:
     * <ul>
     *     <li><code>/orders</code>: chuyển hướng 302 tới trang lịch sử cá nhân để tái sử dụng logic phân trang.</li>
     *     <li><code>/orders/my</code>: tải danh sách đơn có lọc, gán vào request attribute để JSP dựng bảng.</li>
     *     <li><code>/orders/detail</code>: hiển thị chi tiết kèm credential nếu đã bàn giao.</li>
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
        int productId = parsePositiveInt(request.getParameter("productId"));
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
            String redirectUrl = request.getContextPath() + "/orders/detail?id=" + orderId + "&processing=1";
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
        String statusParam = normalize(request.getParameter("status"));
        int page = parsePositiveIntOrDefault(request.getParameter("page"), DEFAULT_PAGE);
        int size = parsePositiveIntOrDefault(request.getParameter("size"), DEFAULT_PAGE_SIZE);

        var result = orderService.getMyOrders(userId, statusParam, page, size);
        Map<String, String> statusLabels = orderService.getStatusLabels();

        request.setAttribute("items", result.getItems());
        request.setAttribute("total", result.getTotalItems());
        request.setAttribute("page", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("size", result.getPageSize());
        request.setAttribute("status", statusParam == null ? "" : statusParam);
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
        int orderId = parsePositiveInt(request.getParameter("id"));
        if (orderId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Optional<OrderDetailView> detailOpt = orderService.getDetail(orderId, userId);
        if (detailOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        OrderDetailView detail = detailOpt.get();
        Orders order = detail.order();
        Products product = detail.product();
        List<String> credentials = detail.credentials();

        request.setAttribute("order", order);
        request.setAttribute("product", product);
        request.setAttribute("credentials", credentials);
        request.setAttribute("statusLabel", orderService.getStatusLabel(order.getStatus()));
        boolean showProcessingModal = "1".equals(request.getParameter("processing"));
        request.setAttribute("showProcessingModal", showProcessingModal);

        forward(request, response, "order/detail");
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
}
