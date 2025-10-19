package controller.order;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Order;
import model.Products;
import service.OrderService;

/**
 * Handles the buyer checkout flow: confirm information, create the order and
 * list historical purchases.
 * @version 1.0 21/05/2024
 * @author gpt-5-codex
 */
@WebServlet(name = "OrderController", urlPatterns = {"/orders", "/orders/buy"})
public class OrderController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
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

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productIdParam = request.getParameter("productId");
        try {
            int productId = Integer.parseInt(productIdParam);
            Products product = orderService.validatePurchasableProduct(productId);
            prepareNavigation(request);
            request.setAttribute("pageTitle", "Mua ngay sản phẩm");
            request.setAttribute("headerTitle", "Hoàn tất đơn hàng");
            request.setAttribute("headerSubtitle", "Bước 1: Kiểm tra thông tin trước khi thanh toán");
            request.setAttribute("product", product);
            forward(request, response, "order/checkout");
        } catch (NumberFormatException | IllegalArgumentException | IllegalStateException ex) {
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
            int productId = Integer.parseInt(productIdParam);
            Order order = orderService.createOrder(productId, buyerEmail, paymentMethod);
            prepareNavigation(request);
            request.setAttribute("pageTitle", "Thanh toán thành công");
            request.setAttribute("headerTitle", "Đơn hàng đã tạo");
            request.setAttribute("headerSubtitle", "Bước 2: Nhận thông tin bàn giao sản phẩm");
            request.setAttribute("badgeHelper", orderService);
            request.setAttribute("order", order);
            forward(request, response, "order/confirmation");
        } catch (NumberFormatException ex) {
            request.setAttribute("error", "Mã sản phẩm không hợp lệ");
            showCheckout(request, response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            showCheckout(request, response);
        }
    }

    private void showOrderHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareNavigation(request);
        request.setAttribute("pageTitle", "Đơn hàng đã mua");
        request.setAttribute("headerTitle", "Lịch sử đơn mua");
        request.setAttribute("headerSubtitle", "Theo dõi trạng thái và thông tin bàn giao sản phẩm");
        request.setAttribute("orders", orderService.findAll());
        request.setAttribute("badgeHelper", orderService);
        forward(request, response, "order/list");
    }

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
        orderLink.put("href", contextPath + "/orders");
        orderLink.put("label", "Đơn đã mua");
        navItems.add(orderLink);

        request.setAttribute("navItems", navItems);
    }
}
