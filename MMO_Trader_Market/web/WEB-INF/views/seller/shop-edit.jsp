<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Shops" %>
<%
    Shops shop = (Shops) request.getAttribute("shop");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    String shopName = shop != null ? shop.getName() : "";
    String shopDescription = shop != null ? shop.getDescription() : "";
    
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <h1>Chỉnh Sửa Shop</h1>
            <p>Cập nhật thông tin shop của bạn</p>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert--danger"><%= errorMessage %></div>
        <% } %>

        <div class="form-container">
            <form method="post" action="<%= request.getContextPath() %>/seller/shop/edit" class="form">
                <div class="form-group">
                    <label for="name" class="form-label">Tên Shop <span class="required">*</span></label>
                    <input type="text" id="name" name="name" class="form-input" value="<%= shopName %>" required maxlength="255" placeholder="Nhập tên shop của bạn">
                </div>

                <div class="form-group">
                    <label for="description" class="form-label">Mô tả Shop</label>
                    <textarea id="description" name="description" class="form-textarea" rows="5" placeholder="Mô tả về shop của bạn"><%= shopDescription %></textarea>
                </div>

                <div class="form-actions">
                    <button type="submit" class="button button--primary">Lưu thay đổi</button>
                    <a href="<%= request.getContextPath() %>/seller/shop/view" class="button button--secondary">Hủy</a>
                </div>
            </form>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<style>
.container {max-width: 800px; margin: 0 auto; padding: 20px;}
.form-container {background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}
</style>
