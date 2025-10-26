<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Shops" %>
<%@ page import="java.math.BigDecimal" %>
<%
    Shops shop = (Shops) request.getAttribute("shop");
    Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
    
    String shopStatus = shop != null ? shop.getStatus() : "Unknown";
    String shopName = shop != null ? shop.getName() : "Shop";
    
    long totalProducts = stats != null ? (Long) stats.getOrDefault("totalProducts", 0L) : 0;
    long availableProducts = stats != null ? (Long) stats.getOrDefault("availableProducts", 0L) : 0;
    long totalOrders = stats != null ? (Long) stats.getOrDefault("totalOrders", 0L) : 0;
    BigDecimal revenue = stats != null ? (BigDecimal) stats.getOrDefault("revenue", BigDecimal.ZERO) : BigDecimal.ZERO;
    
    request.setAttribute("pageTitle", "Dashboard Seller - " + shopName);
    request.setAttribute("bodyClass", "layout seller-dashboard");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <h1>Dashboard Seller</h1>
            <p>Xin chào, <%= shopName %>!</p>
        </div>

        <%-- Thông báo trạng thái shop --%>
        <% if ("Pending".equals(shopStatus)) { %>
            <div class="alert alert--warning">
                ⏳ Shop của bạn đang chờ admin phê duyệt. Vui lòng đợi trong ít phút.
            </div>
        <% } else if ("Suspended".equals(shopStatus)) { %>
            <div class="alert alert--danger">
                ⚠️ Shop của bạn đã bị tạm ngưng. Vui lòng liên hệ admin để biết thêm chi tiết.
            </div>
        <% } else if ("Active".equals(shopStatus)) { %>
            <div class="alert alert--success">
                ✅ Shop của bạn đang hoạt động bình thường.
            </div>
        <% } %>

        <%-- Thông báo thành công/lỗi --%>
        <% 
            String successMessage = (String) session.getAttribute("successMessage");
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (successMessage != null) {
                session.removeAttribute("successMessage");
        %>
            <div class="alert alert--success"><%= successMessage %></div>
        <% 
            }
            if (errorMessage != null) {
                session.removeAttribute("errorMessage");
        %>
            <div class="alert alert--danger"><%= errorMessage %></div>
        <% } %>

        <%-- Thống kê tổng quan --%>
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-card__icon">📦</div>
                <div class="stat-card__content">
                    <div class="stat-card__label">Tổng sản phẩm</div>
                    <div class="stat-card__value"><%= totalProducts %></div>
                    <div class="stat-card__meta"><%= availableProducts %> đang bán</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-card__icon">📋</div>
                <div class="stat-card__content">
                    <div class="stat-card__label">Đơn hàng</div>
                    <div class="stat-card__value"><%= totalOrders %></div>
                    <div class="stat-card__meta">Tổng số đơn</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-card__icon">💰</div>
                <div class="stat-card__content">
                    <div class="stat-card__label">Doanh thu</div>
                    <div class="stat-card__value"><%= String.format("%,.0f", revenue) %> đ</div>
                    <div class="stat-card__meta">Đơn đã hoàn thành</div>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-card__icon">🏪</div>
                <div class="stat-card__content">
                    <div class="stat-card__label">Trạng thái Shop</div>
                    <div class="stat-card__value"><%= shopStatus %></div>
                    <div class="stat-card__meta">
                        <a href="<%= request.getContextPath() %>/seller/shop/view">Xem chi tiết</a>
                    </div>
                </div>
            </div>
        </div>

        <%-- Các action nhanh --%>
        <div class="quick-actions">
            <h2>Thao tác nhanh</h2>
            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/seller/products" class="button button--primary">
                    📦 Quản lý sản phẩm
                </a>
                <a href="<%= request.getContextPath() %>/seller/products/create" class="button button--accent">
                    ➕ Thêm sản phẩm mới
                </a>
                <a href="<%= request.getContextPath() %>/seller/orders" class="button button--secondary">
                    📋 Xem đơn hàng
                </a>
                <a href="<%= request.getContextPath() %>/seller/shop/edit" class="button button--secondary">
                    ⚙️ Cài đặt Shop
                </a>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<style>
.seller-dashboard .container {max-width: 1200px; margin: 0 auto; padding: 20px;}
.page-header {margin-bottom: 30px;}
.page-header h1 {font-size: 2em; margin-bottom: 10px;}
.alert {padding: 15px; margin-bottom: 20px; border-radius: 5px;}
.alert--success {background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;}
.alert--warning {background-color: #fff3cd; color: #856404; border: 1px solid #ffeaa7;}
.alert--danger {background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;}
.stats-grid {display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 40px;}
.stat-card {background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; gap: 15px;}
.stat-card__icon {font-size: 3em;}
.stat-card__label {font-size: 0.9em; color: #666; margin-bottom: 5px;}
.stat-card__value {font-size: 2em; font-weight: bold; color: #333;}
.stat-card__meta {font-size: 0.85em; color: #999; margin-top: 5px;}
.quick-actions {background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}
.quick-actions h2 {margin-bottom: 20px;}
.action-buttons {display: flex; gap: 15px; flex-wrap: wrap;}
</style>
