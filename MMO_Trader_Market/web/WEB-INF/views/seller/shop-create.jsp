<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String name = (String) request.getAttribute("name");
    String description = (String) request.getAttribute("description");
    
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <h1>Tạo Shop Mới</h1>
            <p>Vui lòng điền thông tin shop của bạn</p>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert--danger"><%= errorMessage %></div>
        <% } %>

        <div class="form-container">
            <form method="post" action="<%= request.getContextPath() %>/seller/shop/create" class="form">
                <div class="form-group">
                    <label for="name" class="form-label">Tên Shop <span class="required">*</span></label>
                    <input type="text" id="name" name="name" class="form-input" value="<%= name != null ? name : "" %>" required maxlength="255" placeholder="Nhập tên shop của bạn">
                    <small class="form-help">Tên shop sẽ hiển thị công khai cho người mua</small>
                </div>

                <div class="form-group">
                    <label for="description" class="form-label">Mô tả Shop</label>
                    <textarea id="description" name="description" class="form-textarea" rows="5" placeholder="Mô tả về shop của bạn, các sản phẩm chính, chính sách bán hàng..."><%= description != null ? description : "" %></textarea>
                    <small class="form-help">Mô tả chi tiết giúp người mua hiểu rõ hơn về shop của bạn</small>
                </div>

                <div class="alert alert--info">
                    ℹ️ <strong>Lưu ý:</strong> Shop của bạn sẽ cần được admin phê duyệt trước khi có thể bắt đầu bán hàng.
                </div>

                <div class="form-actions">
                    <button type="submit" class="button button--primary">Tạo Shop</button>
                    <a href="<%= request.getContextPath() %>/home" class="button button--secondary">Hủy</a>
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
.form-group {margin-bottom: 20px;}
.form-label {display: block; font-weight: 500; margin-bottom: 8px;}
.required {color: #dc3545;}
.form-input, .form-textarea {width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 1em;}
.form-help {display: block; margin-top: 5px; font-size: 0.85em; color: #666;}
.form-actions {display: flex; gap: 10px; margin-top: 30px;}
.alert--info {background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; padding: 15px; border-radius: 5px; margin-bottom: 20px;}
</style>
