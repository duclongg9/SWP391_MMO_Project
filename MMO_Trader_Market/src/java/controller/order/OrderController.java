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
import model.Users;
import service.OrderService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the buyer checkout flow: confirm information, create the order and
 * list historical purchases.
 * @version 1.0 21/05/2024
 */
@WebServlet(name = "OrderController", urlPatterns = {"/orders", "/orders/buy", "/orders/detail"})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;
    private static final String BODY_CLASS = "layout";
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/orders/detail".equals(servletPath)) {
            showOrderDetail(request, response);
            return;
        }
        if ("/orders/buy".equals(servletPath)) {
            showCheckout(request, response);
            return;
        }
        showOrderHistory(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/orders/buy".equals(servletPath)) {
            processCheckout(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users currentUser = resolveCurrentUser(request);
        if (currentUser == null || currentUser.getId() == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        String orderIdParam = request.getParameter("id");
        if (orderIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã đơn hàng");
            return;
        }
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã đơn hàng không hợp lệ");
            return;
        }
        try {
            Order order = orderService.loadOrderDetail(orderId, currentUser);
            prepareNavigation(request);
            request.setAttribute("pageTitle", "Chi tiết đơn hàng");
            request.setAttribute("headerTitle", "Thông tin đơn hàng #" + order.getId());
            request.setAttribute("headerSubtitle", "Theo dõi trạng thái và thông tin bàn giao");
            request.setAttribute("orderStatusClass", orderService.getStatusBadgeClass(order.getStatus()));
            request.setAttribute("orderStatusLabel", orderService.getFriendlyStatus(order.getStatus()));
            request.setAttribute("orderCreatedAt", formatOrderDate(order));
            request.setAttribute("order", order);
            LOGGER.info(() -> String.format("Buyer %s (%d) viewed order #%d", currentUser.getEmail(),
                    currentUser.getId(), order.getId()));
            forward(request, response, "order/detail");
        } catch (SecurityException ex) {
            LOGGER.log(Level.WARNING, "Unauthorized access to order detail #{0} by user {1}",
                    new Object[]{orderId, currentUser.getId()});
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productIdParam = request.getParameter("productId");
        try {
            int productId = Integer.parseInt(productIdParam);
            Products product = orderService.validatePurchasableProduct(productId);
            prepareCheckoutPage(request, product, null);
            forward(request, response, "order/checkout");
        } catch (NumberFormatException ex) {
            request.setAttribute("error", "Mã sản phẩm không hợp lệ");
            showOrderHistory(request, response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            showOrderHistory(request, response);
        }
    }

    private void processCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productIdParam = request.getParameter("productId");
        String buyerEmail = request.getParameter("buyerEmail");
        String paymentMethod = request.getParameter("paymentMethod");
        try {
            Order order = createOrder(productIdParam, buyerEmail, paymentMethod);
            showConfirmation(request, response, order);
        } catch (NumberFormatException ex) {
            request.setAttribute("error", "Mã sản phẩm không hợp lệ");
            showOrderHistory(request, response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            handleCheckoutValidationError(request, response, productIdParam, ex.getMessage());
        }
    }

    private void showOrderHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = resolvePage(request.getParameter("page"));
        prepareNavigation(request);
        request.setAttribute("pageTitle", "Đơn hàng đã mua");
        request.setAttribute("headerTitle", "Lịch sử đơn mua");
        request.setAttribute("headerSubtitle", "Theo dõi trạng thái và thông tin bàn giao sản phẩm");
        PaginatedResult<Order> result = orderService.listOrders(page, DEFAULT_PAGE_SIZE);
        List<Order> orders = result.getItems();
        request.setAttribute("orders", orders);
        request.setAttribute("statusClasses", buildStatusClassMap(orders));
        request.setAttribute("statusLabels", buildStatusLabelMap(orders));
        request.setAttribute("currentPage", result.getCurrentPage());
        request.setAttribute("totalPages", result.getTotalPages());
        request.setAttribute("pageSize", result.getPageSize());
        request.setAttribute("totalItems", result.getTotalItems());
        forward(request, response, "order/list");
    }

    private void prepareCheckoutPage(HttpServletRequest request, Products product, String error)
            throws ServletException {
        prepareNavigation(request);
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
        prepareNavigation(request);
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
            return;
        }
        Products product = null;
        try {
            product = orderService.validatePurchasableProduct(productId);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // giữ nguyên thông báo lỗi ban đầu
        }
        prepareCheckoutPage(request, product, errorMessage);
        forward(request, response, "order/checkout");
    }

    private int parseProductIdSafely(String productIdParam) {
        try {
            return Integer.parseInt(productIdParam);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void prepareNavigation(HttpServletRequest request) {
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
        orderLink.put("href", contextPath + "/orders");
        orderLink.put("label", "Đơn đã mua");
        navItems.add(orderLink);

        request.setAttribute("navItems", navItems);
    }

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
    }

    private Users resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object principal = session.getAttribute("currentUser");
        if (principal instanceof Users) {
            return (Users) principal;
        }
        return null;
    }
}
