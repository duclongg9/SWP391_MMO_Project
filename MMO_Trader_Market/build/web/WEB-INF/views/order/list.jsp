<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Order" %>
<%@ page import="service.OrderService" %>
<%
    request.setAttribute("bodyClass", "layout");
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    OrderService badgeHelper = (OrderService) request.getAttribute("badgeHelper");
    if (badgeHelper == null) {
        badgeHelper = new OrderService();
    }
    String errorMessage = (String) request.getAttribute("error");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Lịch sử đơn mua</h2>
            <a class="button button--primary" href="<%= request.getContextPath() %>/home">Tiếp tục mua hàng</a>
        </div>
        <div class="panel__body">
            <% if (errorMessage != null) { %>
            <div class="alert alert--error" role="alert"><%= errorMessage %></div>
            <% } %>
            <% if (orders != null && !orders.isEmpty()) { %>
            <table class="table table--interactive">
                <thead>
                <tr>
                    <th>Mã</th>
                    <th>Sản phẩm</th>
                    <th>Giá</th>
                    <th>Trạng thái</th>
                    <th>Bàn giao</th>
                    <th class="table__actions">Thao tác</th>
                </tr>
                </thead>
                <tbody>
                <% for (Order order : orders) { %>
                <tr>
                    <td>#<%= order.getId() %></td>
                    <td>
                        <strong><%= order.getProduct().getName() %></strong><br>
                        <small>Email nhận: <%= order.getBuyerEmail() %></small>
                    </td>
                    <td><%= order.getProduct().getPrice() %> đ</td>
                    <td>
                        <span class="<%= badgeHelper.getStatusBadgeClass(order.getStatus()) %>">
                            <%= badgeHelper.getFriendlyStatus(order.getStatus()) %>
                        </span>
                    </td>
                    <td>
                        <% if (order.hasDeliveryInformation()) { %>
                        <code><%= order.getActivationCode() %></code><br>
                        <% if (order.getDeliveryLink() != null) { %>
                        <a href="<%= order.getDeliveryLink() %>">Xem link</a>
                        <% } %>
                        <% } else { %>
                        <span class="badge badge--ghost">Đang xử lý</span>
                        <% } %>
                    </td>
                    <td class="table__actions">
                        <a class="button button--ghost" href="<%= request.getContextPath() %>/orders/buy?productId=<%= order.getProduct().getId() %>">Mua lại</a>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
            <% } else { %>
            <p>Bạn chưa có đơn hàng nào. Hãy truy cập trang chủ để chọn sản phẩm phù hợp.</p>
            <% } %>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
