<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Products" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    Products product = (Products) request.getAttribute("product");
    if (product == null) {
        product = new Products(); // Khởi tạo để tránh NullPointerException
    }
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <h1>Thêm Sản Phẩm Mới</h1>
        </div>

        <% if (errorMessage != null) { %>
            <div class="alert alert--danger"><%= errorMessage %></div>
        <% } %>

        <div class="form-container">
            <form method="post" action="<%= request.getContextPath() %>/seller/products/create" class="form" id="product-form">
                <div class="form-row">
                    <div class="form-group">
                        <label for="name" class="form-label">Tên sản phẩm <span class="required">*</span></label>
                        <input type="text" id="name" name="name" class="form-input" required maxlength="255" value="<%= product.getName() != null ? product.getName() : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="productType" class="form-label">Loại sản phẩm <span class="required">*</span></label>
                        <select id="productType" name="productType" class="form-input" required>
                            <option value="" <%= product.getProductType() == null || product.getProductType().isEmpty() ? "selected" : "" %>>-- Chọn loại --</option>
                            <option value="EMAIL" <%= "EMAIL".equals(product.getProductType()) ? "selected" : "" %>>Tài khoản Mail</option>
                            <option value="SOCIAL" <%= "SOCIAL".equals(product.getProductType()) ? "selected" : "" %>>Tài khoản MXH</option>
                            <option value="SOFTWARE" <%= "SOFTWARE".equals(product.getProductType()) ? "selected" : "" %>>Tài khoản phần mềm</option>
                            <option value="GAME" <%= "GAME".equals(product.getProductType()) ? "selected" : "" %>>Tài khoản Game</option>
                            <option value="OTHER" <%= "OTHER".equals(product.getProductType()) ? "selected" : "" %>>Khác</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="price" class="form-label">Giá (VNĐ) <span class="required">*</span></label>
                        <input type="number" id="price" name="price" class="form-input" required min="0" step="1000" value="<%= product.getPrice() != null ? product.getPrice() : "" %>">
                    </div>

                    <div class="form-group">
                        <label for="inventoryCount" class="form-label">Số lượng <span class="required">*</span></label>
                        <input type="number" id="inventoryCount" name="inventoryCount" class="form-input" required min="0" value="<%= product.getInventoryCount() != null ? product.getInventoryCount() : "" %>">
                    </div>
                </div>

                <div class="form-group">
                    <label for="shortDescription" class="form-label">Mô tả ngắn</label>
                    <input type="text" id="shortDescription" name="shortDescription" class="form-input" maxlength="255" value="<%= product.getShortDescription() != null ? product.getShortDescription() : "" %>">
                </div>

                <div class="form-group">
                    <label for="description" class="form-label">Mô tả chi tiết</label>
                    <textarea id="description" name="description" class="form-textarea" rows="5"><%= product.getDescription() != null ? product.getDescription() : "" %></textarea>
                </div>

                <div class="form-group">
                    <label for="primaryImageUrl" class="form-label">URL ảnh chính</label>
                    <input type="text" id="primaryImageUrl" name="primaryImageUrl" class="form-input" value="<%= product.getPrimaryImageUrl() != null ? product.getPrimaryImageUrl() : "" %>">
                    <small class="form-help">Ví dụ: /assets/images/products/product1.png</small>
                </div>
                
                <hr/>

                <div class="form-group">
                    <label for="productSubtype" class="form-label">Phân loại con (Subtype) <span class="required">*</span></label>
                    <select id="productSubtype" name="productSubtype" class="form-input" required>
                        <option value="OTHER" <%= "OTHER".equals(product.getProductSubtype()) || product.getProductSubtype() == null || product.getProductSubtype().isEmpty() ? "selected" : "" %>>Khác (OTHER)</option>
                        <option value="GMAIL" <%= "GMAIL".equals(product.getProductSubtype()) ? "selected" : "" %>>Gmail</option>
                        <option value="FACEBOOK" <%= "FACEBOOK".equals(product.getProductSubtype()) ? "selected" : "" %>>Facebook</option>
                        <option value="TIKTOK" <%= "TIKTOK".equals(product.getProductSubtype()) ? "selected" : "" %>>TikTok</option>
                        <option value="CANVA" <%= "CANVA".equals(product.getProductSubtype()) ? "selected" : "" %>>Canva</option>
                        <option value="VALORANT" <%= "VALORANT".equals(product.getProductSubtype()) ? "selected" : "" %>>Valorant</option>
                    </select>
                    <small class="form-help">Chọn phân loại phù hợp với sản phẩm. Database chỉ chấp nhận các giá trị ENUM cố định.</small>
                </div>

                <div class="form-group">
                    <label for="status" class="form-label">Trạng thái</label>
                    <select id="status" name="status" class="form-input">
                        <option value="Available" <%= "Available".equals(product.getStatus()) || product.getStatus() == null ? "selected" : "" %>>Đang bán (Available)</option>
                        <option value="OutOfStock" <%= "OutOfStock".equals(product.getStatus()) ? "selected" : "" %>>Hết hàng (Out of Stock)</option>
                        <option value="Unlisted" <%= "Unlisted".equals(product.getStatus()) ? "selected" : "" %>>Ẩn sản phẩm (Unlisted)</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="galleryJson" class="form-label">Gallery (JSON)</label>
                    <textarea id="galleryJson" name="galleryJson" class="form-textarea" rows="3"><%= product.getGalleryJson() != null ? product.getGalleryJson() : "" %></textarea>
                </div>
                <hr/>
                
                <%-- Variant Management UI --%>
                <div class="variant-section">
                    <h2>Quản lý Biến thể (Tùy chọn)</h2>
                    <p class="text-muted">Thêm các tùy chọn cho sản phẩm như Màu sắc, Dung lượng, Server... Nếu sản phẩm của bạn không có tùy chọn, hãy bỏ qua phần này.</p>
                    
                    <div id="variant-schema-builder">
                        <div class="empty-schema">
                             <p>Sản phẩm này chưa có tùy chọn nào.</p>
                        </div>
                    </div>
                    
                    <button type="button" id="add-variant-option" class="button button--secondary">
                        ➕ Thêm Tùy chọn
                    </button>
                </div>

                <div class="variant-section">
                     <h2>Chi tiết Biến thể</h2>
                     <div id="variants-table-container">
                        <p class="text-muted">Thêm tùy chọn để bắt đầu tạo biến thể.</p>
                     </div>
                </div>

                <%-- Hidden inputs to store JSON data --%>
                <input type="hidden" name="variantSchema" id="variantSchema" value="<%= product.getVariantSchema() != null ? product.getVariantSchema().replace("\"", "&quot;") : "" %>">
                <input type="hidden" name="variantsJson" id="variantsJson" value="<%= product.getVariantsJson() != null ? product.getVariantsJson().replace("\"", "&quot;") : "" %>">

                <div class="form-actions">
                    <button type="submit" class="button button--primary">Thêm sản phẩm</button>
                    <a href="<%= request.getContextPath() %>/seller/products" class="button button--secondary">Hủy</a>
                </div>
            </form>
        </div>
    </div>
</main>

<script src="<%= request.getContextPath() %>/assets/Script/variant-manager.js"></script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<%-- Giữ nguyên CSS cũ --%>
<style>
.container {max-width: 900px; margin: 0 auto; padding: 20px;}
.form-container {background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}
.form-row {display: grid; grid-template-columns: 1fr 1fr; gap: 20px;}
.form-group {margin-bottom: 20px;}
.form-label {display: block; font-weight: 500; margin-bottom: 8px;}
.required {color: #dc3545;}
.form-input, .form-textarea, .form-select {width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 1em;}
.form-help {display: block; margin-top: 5px; font-size: 0.85em; color: #666;}
.form-actions {display: flex; gap: 10px; margin-top: 30px;}
.alert--danger {padding: 15px; margin-bottom: 20px; background-color: #f8d7da; color: #721c24; border-radius: 5px;}
.button {padding: 12px 24px; border: none; border-radius: 5px; font-weight: 500; cursor: pointer; text-decoration: none; display: inline-block;}
.button--primary {background-color: #007bff; color: white;}
.button--secondary {background-color: #6c757d; color: white;}
.variant-section {
    background-color: #f8f9fa;
    padding: 20px;
    border-radius: 8px;
    margin-top: 20px;
    border: 1px solid #e9ecef;
}
.variant-section h2 {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 1.2em;
}
.variant-option-group {
    background: #fff;
    padding: 15px;
    border-radius: 5px;
    margin-bottom: 15px;
    border: 1px solid #ddd;
}
.input-with-button {
    display: flex;
    gap: 10px;
}
.input-with-button input {
    flex: 1;
}
.option-values-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px;
    background-color: #f1f3f5;
    border-radius: 5px;
    min-height: 40px;
    margin-bottom: 10px;
}
.option-value-tag {
    background-color: #007bff;
    color: white;
    padding: 5px 10px;
    border-radius: 15px;
    font-size: 0.9em;
    display: flex;
    align-items: center;
    gap: 5px;
}
.remove-value {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    font-weight: bold;
    font-size: 1.1em;
    opacity: 0.7;
}
.remove-value:hover {
    opacity: 1;
}
.variants-table {
    margin-top: 15px;
    width: 100%;
}
.text-muted {
    color: #6c757d;
}
</style>
