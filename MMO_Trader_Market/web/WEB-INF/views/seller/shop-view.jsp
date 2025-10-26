<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Shops" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    Shops shop = (Shops) request.getAttribute("shop");
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    String shopName = shop != null ? shop.getName() : "";
    String shopDescription = shop != null ? shop.getDescription() : "";
    String shopStatus = shop != null ? shop.getStatus() : "";
    String createdAt = shop != null && shop.getCreatedAt() != null ? sdf.format(shop.getCreatedAt()) : "";
    
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <h1>Thông Tin Shop</h1>
            <div class="header-actions">
                <a href="<%= request.getContextPath() %>/seller/shop/edit" class="button button--primary">
                    ✏️ Chỉnh sửa
                </a>
                <a href="<%= request.getContextPath() %>/seller/dashboard" class="button button--secondary">
                    ← Quay lại Dashboard
                </a>
            </div>
        </div>

        <div class="shop-info-card">
            <div class="info-row">
                <div class="info-label">Tên Shop:</div>
                <div class="info-value"><strong><%= shopName %></strong></div>
            </div>

            <div class="info-row">
                <div class="info-label">Trạng thái:</div>
                <div class="info-value">
                    <% if ("Active".equals(shopStatus)) { %>
                        <span class="badge badge--success">✅ Đang hoạt động</span>
                    <% } else if ("Pending".equals(shopStatus)) { %>
                        <span class="badge badge--warning">⏳ Chờ phê duyệt</span>
                    <% } else if ("Suspended".equals(shopStatus)) { %>
                        <span class="badge badge--danger">⚠️ Tạm ngưng</span>
                    <% } else { %>
                        <span class="badge badge--default"><%= shopStatus %></span>
                    <% } %>
                </div>
            </div>

            <div class="info-row">
                <div class="info-label">Ngày tạo:</div>
                <div class="info-value"><%= createdAt %></div>
            </div>

            <div class="info-row">
                <div class="info-label">Mô tả:</div>
                <div class="info-value">
                    <% if (shopDescription != null && !shopDescription.isEmpty()) { %>
                        <p class="description-text"><%= shopDescription %></p>
                    <% } else { %>
                        <p class="text-muted">Chưa có mô tả</p>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<style>
.container {max-width: 900px; margin: 0 auto; padding: 20px;}
.page-header {display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px;}
.shop-info-card {background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}
.info-row {display: grid; grid-template-columns: 200px 1fr; padding: 15px 0; border-bottom: 1px solid #eee;}
.info-row:last-child {border-bottom: none;}
.info-label {font-weight: 500; color: #666;}
.badge {display: inline-block; padding: 5px 12px; border-radius: 4px; font-size: 0.9em; font-weight: 500;}
.badge--success {background-color: #d4edda; color: #155724;}
.badge--warning {background-color: #fff3cd; color: #856404;}
.badge--danger {background-color: #f8d7da; color: #721c24;}
</style>
