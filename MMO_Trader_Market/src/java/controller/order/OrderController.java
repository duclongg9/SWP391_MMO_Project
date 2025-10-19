package controller.order;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Order;
import model.PaginatedResult;
import model.Products;
import model.OrderStatus;
import service.OrderService;
import service.dto.OrderPlacementResult;
import service.dto.OrderStatusView;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet responsible for handling the asynchronous checkout entry point and
 * the polling endpoint used by the client to keep track of the background
 * processing performed by {@code OrderWorker}.
 */
@WebServlet(name = "OrderController", urlPatterns = {"/orders/my", "/orders/buy"})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;

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
        if ("/order/status".equals(servletPath)) {
            handleStatusPoll(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void handleBuyNow(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser;
        try {
            currentUser = requireAuthenticatedUser(request);
        } catch (IllegalStateException ex) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        applyPageDefaults(request);

        int productId;
        int quantity;
        try {
            productId = parseIntegerParameter(request, "productId");
            quantity = parseQuantity(request.getParameter("quantity"));
        } catch (IllegalArgumentException ex) {
            request.setAttribute("orderError", ex.getMessage());
            forward(request, response, "order/processing");
            return;
        }

        try {
            OrderPlacementResult placement = orderService.placeOrder(currentUser, productId, quantity);
            renderProcessingView(request, response, placement);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("orderError", ex.getMessage());
            forward(request, response, "order/processing");
        }
    }

    private void showOrderHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = resolveUserId(request);
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int page = resolvePage(request.getParameter("page"));
        int size = resolveSize(request.getParameter("size"));
        OrderStatus status = parseStatus(request.getParameter("status"));
        prepareNavigation(request);
        request.setAttribute("pageTitle", "Đơn hàng đã mua");
        request.setAttribute("headerTitle", "Lịch sử đơn mua");
        request.setAttribute("headerSubtitle", "Theo dõi trạng thái và thông tin bàn giao sản phẩm");
        PaginatedResult<Order> result = orderService.listOrders(userId, page, size, status);
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
        forward(request, response, "order/list");
    }

        applyPageDefaults(request);
        request.setAttribute("orderPlacement", placement);
        request.setAttribute("product", product);
        request.setAttribute("quantity", placement.getQuantity());
        request.setAttribute("statusLabel", orderService.getFriendlyStatus(status));
        request.setAttribute("statusClass", orderService.getStatusBadgeClass(status));
        request.setAttribute("pollUrl", buildPollUrl(request, placement));
        forward(request, response, "order/processing");
    }

    private void handleStatusPoll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Users currentUser;
        try {
            currentUser = requireAuthenticatedUser(request);
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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

    private Users requireAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("Session expired");
        }
        Users currentUser = (Users) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Session expired");
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
        List<Map<String, String>> navItems = new ArrayList<>();

        request.setAttribute("bodyClass", BODY_CLASS);
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
        request.setAttribute("bodyClass", "layout");
        request.setAttribute("pageTitle", "Xử lý đơn hàng");
        request.setAttribute("headerTitle", "Đơn hàng đang được xử lý");
        request.setAttribute("headerSubtitle", "Chúng tôi sẽ hoàn tất đơn trong giây lát, vui lòng đợi...");
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

    private Integer resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute("userId");
    }

    private OrderStatus parseStatus(String statusParam) {
        if (statusParam == null || statusParam.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusParam.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OrderStatus.fromDatabaseValue(statusParam.trim());
        }
    }

    private Map<String, String> buildStatusOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            options.put(status.name(), orderService.getFriendlyStatus(status));
        }
        return options;
    }
}
