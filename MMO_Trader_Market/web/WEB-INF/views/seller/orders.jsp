<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Orders" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    List<Orders> orders = (List<Orders>) request.getAttribute("orders");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int totalPages = (Integer) request.getAttribute("totalPages");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <h1>Đơn Hàng Của Shop</h1>
        <div class="orders-table">
            <% if (orders == null || orders.isEmpty()) { %>
                <p>Chưa có đơn hàng nào.</p>
            <% } else { %>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Ngày đặt</th>
                            <th>ID Sản phẩm</th>
                            <th>ID Người mua</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Orders order : orders) { %>
                        <tr>
                            <td><%= order.getId() %></td>
                            <td><%= sdf.format(order.getCreatedAt()) %></td>
                            <td><%= order.getProductId() %></td>
                            <td><%= order.getBuyerId() %></td>
                            <td><%= String.format("%,.0f", order.getTotalAmount()) %> đ</td>
                            <td><%= order.getStatus() %></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>
        <div class="pagination">
            <% for (int i = 1; i <= totalPages; i++) { %>
                <a href="?page=<%= i %>" class="<%= i == currentPage ? "active" : "" %>"><%= i %></a>
            <% } %>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<style>
.container {max-width: 1200px; margin: 0 auto; padding: 20px;}
</style>
