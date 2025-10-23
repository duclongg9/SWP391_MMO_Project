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
 * Buyer order endpoints: buy now, order history and detail view.
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

    private void redirectToMyOrders(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String target = request.getContextPath() + "/orders/my";
        response.sendRedirect(target);
    }

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
        if (userId == null || productId <= 0 || quantity <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String idemKeyParam = Optional.ofNullable(request.getParameter("idemKey"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(UUID.randomUUID().toString());
        int orderId = orderService.placeOrderPending(userId, productId, quantity, idemKeyParam);
        String redirectUrl = request.getContextPath() + "/orders/detail?id=" + orderId;
        response.sendRedirect(redirectUrl);
    }

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

        forward(request, response, "order/detail");
    }

    private boolean isBuyerOrSeller(HttpSession session) {
        if (session == null) {
            return false;
        }
        Integer role = (Integer) session.getAttribute("userRole");
        return role != null && (ROLE_BUYER == role || ROLE_SELLER == role);
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

    private int parsePositiveIntOrDefault(String value, int defaultValue) {
        int parsed = parsePositiveInt(value);
        return parsed > 0 ? parsed : defaultValue;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
