package controller.order;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Order;
import model.OrderStatus;
import model.PaginatedResult;
import model.Products;
import model.Users;
import service.OrderService;
import service.dto.OrderDetailView;
import service.dto.OrderPlacementResult;
import service.dto.OrderStatusView;

import java.io.IOException;
<<<<<<< HEAD
import java.io.PrintWriter;
import java.util.ArrayList;
=======
import java.time.format.DateTimeFormatter;
>>>>>>> origin/hoa
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Servlet responsible for order placement, polling and listing.
 */
@WebServlet(name = "OrderController", urlPatterns = {
        "/order/buy-now", "/order/status", "/orders/my", "/orders/detail"
})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String BODY_CLASS = "layout";

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/order/buy-now".equals(servletPath)) {
            handleBuyNow(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        switch (servletPath) {
            case "/order/status" -> handleStatusPoll(request, response);
            case "/orders/my" -> showOrderHistory(request, response);
            case "/orders/detail" -> showOrderDetail(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleBuyNow(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser = requireAuthenticatedUser(request, response);
        if (currentUser == null) {
            return;
        }

        int productId;
        int quantity;
        try {
            productId = parseIntegerParameter(request, "productId");
            quantity = parseQuantity(request.getParameter("qty"));
        } catch (IllegalArgumentException ex) {
            request.setAttribute("orderError", ex.getMessage());
            applyPageDefaults(request);
            forward(request, response, "order/processing");
            return;
        }

        String orderToken = request.getParameter("orderToken");

        try {
            OrderPlacementResult placement = orderService.placeOrder(currentUser, productId, quantity, orderToken);
            Products product = placement.getProduct();
            renderProcessingView(request, response, placement, product);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("orderError", ex.getMessage());
            applyPageDefaults(request);
            forward(request, response, "order/processing");
        }
    }

    private void renderProcessingView(HttpServletRequest request, HttpServletResponse response,
            OrderPlacementResult placement, Products product) throws ServletException, IOException {
        applyPageDefaults(request);
        OrderStatus status = placement.getStatus();
        request.setAttribute("orderPlacement", placement);
        request.setAttribute("product", product);
        request.setAttribute("quantity", placement.getQuantity());
        request.setAttribute("statusLabel", orderService.getFriendlyStatus(status));
        request.setAttribute("statusClass", orderService.getStatusBadgeClass(status));
        request.setAttribute("pollUrl", buildPollUrl(request, placement));
        forward(request, response, "order/processing");
    }

    private void showOrderHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser = requireAuthenticatedUser(request, response);
        if (currentUser == null) {
            return;
        }
        int page = resolvePage(request.getParameter("page"));
<<<<<<< HEAD
        int size = resolveSize(request.getParameter("size"));
        OrderStatus status = parseStatus(request.getParameter("status"));
        prepareNavigation(request);
        PaginatedResult<Order> result = orderService.listOrders(currentUser.getId(), page, size, status);
        List<Order> orders = result.getItems();
        request.setAttribute("items", orders);
        request.setAttribute("statusClasses", buildStatusClassMap(orders));
        request.setAttribute("statusLabels", buildStatusLabelMap(orders));
        request.setAttribute("statusOptions", buildStatusOptions());
        request.setAttribute("statusFilter", status == null ? "" : status.name());
        request.setAttribute("page", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("size", result.getPageSize());
        request.setAttribute("total", result.getTotalItems());
=======
        request.setAttribute("bodyClass", BODY_CLASS);
>>>>>>> origin/hoa
        request.setAttribute("pageTitle", "Đơn hàng đã mua");
        request.setAttribute("headerTitle", "Lịch sử đơn mua");
        request.setAttribute("headerSubtitle", "Theo dõi trạng thái và thông tin bàn giao sản phẩm");
        request.setAttribute("bodyClass", BODY_CLASS);
        forward(request, response, "order/list");
    }

<<<<<<< HEAD
    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser = requireAuthenticatedUser(request, response);
        if (currentUser == null) {
=======
    private void prepareCheckoutPage(HttpServletRequest request, Products product, String error)
            throws ServletException {
        request.setAttribute("bodyClass", BODY_CLASS);
        request.setAttribute("pageTitle", "Mua ngay sản phẩm");
        request.setAttribute("headerTitle", "Hoàn tất đơn hàng");
        request.setAttribute("headerSubtitle", "Bước 1: Kiểm tra thông tin trước khi thanh toán");
        request.setAttribute("product", product);
        if (error != null) {
            request.setAttribute("error", error);
        }
    }

    private Order createOrder(String productIdParam, String buyerEmail, String paymentMethod) {
        int productId = Integer.parseInt(productIdParam);
        return orderService.createOrder(productId, buyerEmail, paymentMethod);
    }

    private void showConfirmation(HttpServletRequest request, HttpServletResponse response, Order order)
            throws ServletException, IOException {
        request.setAttribute("bodyClass", BODY_CLASS);
        request.setAttribute("pageTitle", "Thanh toán thành công");
        request.setAttribute("headerTitle", "Đơn hàng đã tạo");
        request.setAttribute("headerSubtitle", "Bước 2: Nhận thông tin bàn giao sản phẩm");
        request.setAttribute("orderStatusClass", orderService.getStatusBadgeClass(order.getStatus()));
        request.setAttribute("orderStatusLabel", orderService.getFriendlyStatus(order.getStatus()));
        request.setAttribute("orderCreatedAt", formatOrderDate(order));
        request.setAttribute("order", order);
        forward(request, response, "order/confirmation");
    }

    private void handleCheckoutValidationError(HttpServletRequest request, HttpServletResponse response,
            String productIdParam, String errorMessage) throws ServletException, IOException {
        int productId = parseProductIdSafely(productIdParam);
        if (productId <= 0) {
            request.setAttribute("error", errorMessage);
            showOrderHistory(request, response);
>>>>>>> origin/hoa
            return;
        }
        int orderId;
        try {
            orderId = parseIntegerParameter(request, "orderId");
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }
        try {
            OrderDetailView detail = orderService.getOrderDetail(orderId, currentUser);
            LOGGER.info(() -> "User " + currentUser.getId() + " viewed credentials of order " + orderId);
            prepareNavigation(request);
            request.setAttribute("detail", detail);
            request.setAttribute("statusLabel", orderService.getFriendlyStatus(detail.getOrder().getStatus()));
            request.setAttribute("statusClass", orderService.getStatusBadgeClass(detail.getOrder().getStatus()));
            request.setAttribute("bodyClass", BODY_CLASS);
            forward(request, response, "order/detail");
        } catch (SecurityException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        }
    }

    private void handleStatusPoll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Users currentUser = requireAuthenticatedUser(request, response);
        if (currentUser == null) {
            return;
        }

        int orderId;
        try {
            orderId = parseIntegerParameter(request, "orderId");
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }
        String orderToken = request.getParameter("token");

        try {
            OrderStatusView statusView = orderService.getOrderStatus(orderId, orderToken, currentUser);
            writeStatusResponse(response, statusView);
        } catch (SecurityException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
        }
    }

    private Users requireAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return null;
        }
        Users currentUser = (Users) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return null;
        }
        return currentUser;
    }

    private int parseIntegerParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Thiếu tham số " + name);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tham số " + name + " không hợp lệ");
        }
    }

    private int parseQuantity(String rawQuantity) {
        if (rawQuantity == null || rawQuantity.isBlank()) {
            return 1;
        }
        try {
            int quantity = Integer.parseInt(rawQuantity.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }
            return quantity;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
    }

    private String buildPollUrl(HttpServletRequest request, OrderPlacementResult placement) {
        String contextPath = request.getContextPath();
        StringBuilder builder = new StringBuilder(contextPath)
                .append("/order/status?orderId=")
                .append(placement.getOrderId())
                .append("&token=")
                .append(placement.getOrderToken());
        return builder.toString();
    }

    private void writeStatusResponse(HttpServletResponse response, OrderStatusView statusView) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.append('{')
                    .append("\"orderId\":").append(String.valueOf(statusView.getOrderId())).append(',')
                    .append("\"status\":\"").append(escapeJson(statusView.getStatus().name())).append('\"').append(',')
                    .append("\"friendlyStatus\":\"")
                    .append(escapeJson(orderService.getFriendlyStatus(statusView.getStatus()))).append('\"').append(',')
                    .append("\"statusClass\":\"")
                    .append(escapeJson(orderService.getStatusBadgeClass(statusView.getStatus()))).append('\"').append(',')
                    .append("\"deliverable\":").append(Boolean.toString(statusView.isDeliverable()));
            if (statusView.isDeliverable()) {
                writer.append(',')
                        .append("\"activationCode\":\"")
                        .append(escapeJson(Objects.toString(statusView.getActivationCode(), ""))).append('\"');
                if (statusView.getDeliveryLink() != null) {
                    writer.append(',')
                            .append("\"deliveryLink\":\"")
                            .append(escapeJson(statusView.getDeliveryLink())).append('\"');
                }
            }
            writer.append('}');
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void applyPageDefaults(HttpServletRequest request) {
        request.setAttribute("bodyClass", BODY_CLASS);
        request.setAttribute("pageTitle", "Xử lý đơn hàng");
        request.setAttribute("headerTitle", "Đơn hàng đang được xử lý");
        request.setAttribute("headerSubtitle", "Chúng tôi sẽ hoàn tất đơn trong giây lát, vui lòng đợi...");
    }

    private int resolvePage(String pageParam) {
        if (pageParam == null) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return page > 0 ? page : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private int resolveSize(String sizeParam) {
        if (sizeParam == null) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int size = Integer.parseInt(sizeParam);
            return size > 0 ? size : DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException ex) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    private OrderStatus parseStatus(String statusParam) {
        if (statusParam == null || statusParam.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusParam.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Map<Integer, String> buildStatusClassMap(List<Order> orders) {
        Map<Integer, String> map = new HashMap<>();
        for (Order order : orders) {
            map.put(order.getId(), orderService.getStatusBadgeClass(order.getStatus()));
        }
        return map;
    }

    private Map<Integer, String> buildStatusLabelMap(List<Order> orders) {
        Map<Integer, String> map = new HashMap<>();
        for (Order order : orders) {
            map.put(order.getId(), orderService.getFriendlyStatus(order.getStatus()));
        }
        return map;
    }

    private Map<String, String> buildStatusOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            options.put(status.name(), orderService.getFriendlyStatus(status));
        }
        return options;
    }

<<<<<<< HEAD
    private void prepareNavigation(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        List<Map<String, String>> navItems = new ArrayList<>();

        Map<String, String> homeLink = new HashMap<>();
        homeLink.put("href", contextPath + "/home");
        homeLink.put("label", "Trang chủ");
        navItems.add(homeLink);

        Map<String, String> productLink = new HashMap<>();
        productLink.put("href", contextPath + "/products");
        productLink.put("label", "Sản phẩm");
        navItems.add(productLink);

        Map<String, String> orderLink = new HashMap<>();
        orderLink.put("href", contextPath + "/orders/my");
        orderLink.put("label", "Đơn đã mua");
        navItems.add(orderLink);

        request.setAttribute("navItems", navItems);
=======
    private Map<Integer, String> buildStatusClassMap(List<Order> orders) {
        Map<Integer, String> statusClasses = new HashMap<>();
        for (Order order : orders) {
            statusClasses.put(order.getId(), orderService.getStatusBadgeClass(order.getStatus()));
        }
        return statusClasses;
    }

    private Map<Integer, String> buildStatusLabelMap(List<Order> orders) {
        Map<Integer, String> statusLabels = new HashMap<>();
        for (Order order : orders) {
            statusLabels.put(order.getId(), orderService.getFriendlyStatus(order.getStatus()));
        }
        return statusLabels;
    }

    private String formatOrderDate(Order order) {
        return ORDER_TIME_FORMATTER.format(order.getCreatedAt());
    }

    private int resolvePage(String pageParam) {
        if (pageParam == null) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return page >= 1 ? page : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
>>>>>>> origin/hoa
    }
}
