<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Order" %>
<%@ page import="service.OrderService" %>
<%
    request.setAttribute("bodyClass", "layout");
    Order order = (Order) request.getAttribute("order");
    OrderService badgeHelper = (OrderService) request.getAttribute("badgeHelper");
    if (badgeHelper == null) {
        badgeHelper = new OrderService();
    }
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bước 2: Thanh toán thành công</h2>
            <span class="panel__tag">Giao key ngay</span>
        </div>
        <div class="panel__body">
            <% if (order != null) { %>
            <article class="profile-card" style="margin-bottom: 1.5rem;">
                <h4>Mã đơn hàng #<%= order.getId() %></h4>
                <p class="profile-card__subtitle">Thanh toán qua: <%= order.getPaymentMethod() %></p>
                <dl class="profile-card__stats">
                    <div>
                        <dt>Tên sản phẩm</dt>
                        <dd><%= order.getProduct().getName() %></dd>
                    </div>
                    <div>
                        <dt>Email nhận</dt>
                        <dd><%= order.getBuyerEmail() %></dd>
                    </div>
                    <div>
                        <dt>Trạng thái</dt>
                        <dd>
                            <span class="<%= badgeHelper.getStatusBadgeClass(order.getStatus()) %>">
                                <%= badgeHelper.getFriendlyStatus(order.getStatus()) %>
                            </span>
                        </dd>
                    </div>
                </dl>
                <p class="profile-card__note">Thời gian tạo: <%= order.getCreatedAt() %></p>
            </article>
            <div class="panel panel--sub" style="margin-bottom: 1.5rem;">
                <div class="panel__header">
                    <h3 class="panel__title">Thông tin bàn giao</h3>
                </div>
                <div class="panel__body">
                    <% if (order.hasDeliveryInformation()) { %>
                    <p><strong>Activation key:</strong> <code><%= order.getActivationCode() %></code></p>
                    <% if (order.getDeliveryLink() != null) { %>
                    <p><strong>Đường dẫn tải sản phẩm:</strong> <a href="<%= order.getDeliveryLink() %>"><%= order.getDeliveryLink() %></a></p>
                    <% } %>
                    <p class="profile-card__note">Hãy đổi mật khẩu ngay sau khi nhận tài khoản để bảo vệ quyền lợi.</p>
                    <% } else { %>
                    <p>Đơn hàng đang chờ hệ thống kiểm tra. Chúng tôi sẽ gửi thông tin qua email trong giây lát.</p>
                    <% } %>
                </div>
            </div>
            <div style="display: flex; gap: 0.75rem;">
                <a class="button button--primary" href="<%= request.getContextPath() %>/orders">Xem danh sách đơn hàng</a>
                <a class="button button--ghost" href="<%= request.getContextPath() %>/home">Tiếp tục mua sắm</a>
            </div>
            <% } else { %>
            <div class="alert alert--error" role="alert">Không tìm thấy thông tin đơn hàng.</div>
            <a class="button button--primary" href="<%= request.getContextPath() %>/orders">Quay lại danh sách</a>
            <% } %>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
