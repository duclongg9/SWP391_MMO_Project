<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <c:if test="${not empty errorMessage}">
        <div style="background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
            ${errorMessage}
        </div>
    </c:if>
    
    <c:if test="${not empty errors}">
        <div style="background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
            <ul style="margin: 0; padding-left: 1.5rem;">
                <c:forEach var="error" items="${errors}">
                    <li>${error}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/seller/products/create" method="post" enctype="multipart/form-data">
        <input type="hidden" name="shopId" value="${shopId}" />
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Thông tin sản phẩm</h2>
            <p class="panel__subtitle">Bổ sung mô tả chi tiết, giá bán và ảnh minh hoạ trước khi đăng bán.</p>
        </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; padding: 1.5rem;">
                <!-- Cột trái -->
                <div>
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-name">
                            Tên sản phẩm <span style="color: red;">*</span>
                        </label>
                        <input class="form-input" type="text" id="product-name" name="productName" 
                               placeholder="Ví dụ: Gmail Doanh nghiệp 100GB" 
                               value="${productName}" required maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-type">
                            Loại sản phẩm <span style="color: red;">*</span>
                        </label>
                        <select class="form-input" id="product-type" name="productType" required
                                style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                            <option value="">-- Chọn loại --</option>
                            <option value="EMAIL" ${productType == 'EMAIL' ? 'selected' : ''}>Email</option>
                            <option value="SOCIAL" ${productType == 'SOCIAL' ? 'selected' : ''}>Mạng xã hội</option>
                            <option value="GAME" ${productType == 'GAME' ? 'selected' : ''}>Game</option>
                            <option value="SOFTWARE" ${productType == 'SOFTWARE' ? 'selected' : ''}>Phần mềm</option>
                            <option value="OTHER" ${productType == 'OTHER' ? 'selected' : ''}>Khác</option>
                        </select>
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-subtype">
                            Phân loại chi tiết <span style="color: red;">*</span>
                        </label>
                        <select class="form-input" id="product-subtype" name="productSubtype" required
                                style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                            <option value="">-- Chọn phân loại --</option>
                            <option value="GMAIL" ${productSubtype == 'GMAIL' ? 'selected' : ''}>Gmail</option>
                            <option value="FACEBOOK" ${productSubtype == 'FACEBOOK' ? 'selected' : ''}>Facebook</option>
                            <option value="TIKTOK" ${productSubtype == 'TIKTOK' ? 'selected' : ''}>TikTok</option>
                            <option value="CANVA" ${productSubtype == 'CANVA' ? 'selected' : ''}>Canva</option>
                            <option value="VALORANT" ${productSubtype == 'VALORANT' ? 'selected' : ''}>Valorant</option>
                            <option value="OTHER" ${productSubtype == 'OTHER' ? 'selected' : ''}>Khác</option>
                        </select>
                        <small style="color: #666; font-size: 0.875rem;">Chọn phân loại phù hợp với sản phẩm</small>
                    </div>
                </div>
                
                <!-- Cột phải -->
                <div>
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-price">
                            Giá bán (VNĐ) <span style="color: red;">*</span>
                        </label>
                        <input class="form-input" type="number" id="product-price" name="price" 
                               placeholder="250000" 
                               value="${price}" required min="0" step="1000"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-inventory">
                            Số lượng
                        </label>
                        <input class="form-input" type="number" id="product-inventory" name="inventory" 
                               placeholder="0" 
                               value="${inventory}" min="0"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                        <small style="color: #666; font-size: 0.875rem;">Để 0 nếu chưa có hàng.</small>
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-image">
                            Ảnh sản phẩm
                        </label>
                        <input class="form-input" type="file" id="product-image" name="productImage" 
                               accept="image/*"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                        <small style="color: #666; font-size: 0.875rem;">Chọn ảnh từ máy (JPG, PNG, GIF, WEBP - tối đa 10MB)</small>
                    </div>
            </div>
                
                <!-- Phần mô tả - full width -->
                <div style="grid-column: 1 / -1;">
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="short-description">
                            Mô tả ngắn
                        </label>
                        <textarea class="form-input" id="short-description" name="shortDescription" 
                                  rows="2" placeholder="Mô tả ngắn về sản phẩm"
                                  style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; resize: vertical;">${shortDescription}</textarea>
            </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="description">
                            Mô tả chi tiết
                        </label>
                        <textarea class="form-input" id="description" name="description" 
                                  rows="5" placeholder="Mô tả chi tiết, chính sách bảo hành..."
                                  style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; resize: vertical;">${description}</textarea>
            </div>
            </div>
        </div>
        <div class="panel__footer">
                <button class="button button--primary" type="submit">
                    Đăng sản phẩm ngay
            </button>
        </div>
    </section>
    </form>
    
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Mẹo đăng sản phẩm hiệu quả</h2>
        </div>
        <ul class="guide-list">
            <li>Chuẩn bị ít nhất 3 hình ảnh minh hoạ chất lượng cao.</li>
            <li>Nhập mô tả rõ ràng về quyền lợi bàn giao và chính sách bảo hành.</li>
            <li>Cập nhật tồn kho thực tế để hệ thống tự động khóa đơn khi bán hết.</li>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
