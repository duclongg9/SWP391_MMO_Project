<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chỉnh sửa sản phẩm</h2>
            <div class="panel__actions">
                <a href="${pageContext.request.contextPath}/seller/products" class="button button--ghost">
                    ← Quay lại danh sách
                </a>
            </div>
        </div>

        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error">
                    <p><c:out value="${error}" /></p>
                </div>
            </c:if>

            <!-- Thông tin sản phẩm hiện tại -->
            <div class="product-current-info">
                <h3>Thông tin hiện tại:</h3>
                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">ID:</span>
                        <span class="info-value">#${product.id}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Trạng thái:</span>
                        <span class="badge badge--${product.status == 'Available' ? 'success' : (product.status == 'Pending' ? 'warning' : 'danger')}">
                            <c:choose>
                                <c:when test="${product.status == 'Available'}">Có sẵn</c:when>
                                <c:when test="${product.status == 'Pending'}">Chờ duyệt</c:when>
                                <c:when test="${product.status == 'Suspended'}">Tạm khóa</c:when>
                                <c:otherwise><c:out value="${product.status}" /></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Ngày tạo:</span>
                        <span class="info-value">
                            <fmt:formatDate value="${product.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">Cập nhật lần cuối:</span>
                        <span class="info-value">
                            <fmt:formatDate value="${product.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                    </div>
                </div>
            </div>

            <!-- Form chỉnh sửa -->
            <form method="post" action="${pageContext.request.contextPath}/seller/products?action=update" class="form">
                <input type="hidden" name="productId" value="${product.id}">
                
                <div class="form__group">
                    <label for="name" class="form__label">
                        Tên sản phẩm <span class="form__required">*</span>
                    </label>
                    <input type="text" 
                           id="name" 
                           name="name" 
                           class="form__input" 
                           value="<c:out value='${name != null ? name : product.name}' />"
                           placeholder="Nhập tên sản phẩm (3-100 ký tự)"
                           maxlength="100"
                           required>
                    <small class="form__help">
                        Tên sản phẩm sẽ hiển thị trong danh sách tìm kiếm
                    </small>
                </div>

                <div class="form__group">
                    <label for="description" class="form__label">
                        Mô tả sản phẩm <span class="form__required">*</span>
                    </label>
                    <textarea id="description" 
                              name="description" 
                              class="form__textarea" 
                              placeholder="Mô tả chi tiết về sản phẩm, thông tin đăng nhập, cách sử dụng..."
                              rows="8"
                              minlength="10"
                              maxlength="1000"
                              required><c:out value="${description != null ? description : product.description}" /></textarea>
                    <small class="form__help">
                        Mô tả chi tiết sản phẩm (10-1000 ký tự). Bao gồm thông tin về tài khoản, level, vật phẩm có sẵn...
                    </small>
                </div>

                <div class="form__row">
                    <div class="form__group">
                        <label for="price" class="form__label">
                            Giá bán (VND) <span class="form__required">*</span>
                        </label>
                        <input type="number" 
                               id="price" 
                               name="price" 
                               class="form__input" 
                               value="${price != null ? price : product.price}"
                               placeholder="Nhập giá bán"
                               min="1000"
                               max="100000000"
                               step="1000"
                               required>
                        <small class="form__help">
                            Giá từ 1,000 đến 100,000,000 VND
                        </small>
                    </div>

                    <div class="form__group">
                        <label for="inventoryCount" class="form__label">
                            Số lượng tồn kho
                        </label>
                        <input type="number" 
                               id="inventoryCount" 
                               name="inventoryCount" 
                               class="form__input" 
                               value="${inventoryCount != null ? inventoryCount : (product.inventoryCount != null ? product.inventoryCount : '')}"
                               placeholder="Để trống = không giới hạn"
                               min="0"
                               max="99999">
                        <small class="form__help">
                            Để trống nếu không muốn giới hạn số lượng
                        </small>
                    </div>
                </div>

                <div class="form__actions">
                    <button type="submit" class="button button--primary">
                        Cập nhật sản phẩm
                    </button>
                    <a href="${pageContext.request.contextPath}/seller/products" class="button button--ghost">
                        Hủy
                    </a>
                </div>
            </form>

            <!-- Cảnh báo -->
            <c:if test="${product.status == 'Suspended'}">
                <div class="panel panel--warning" style="margin-top: 2rem;">
                    <div class="panel__body">
                        <h3>⚠️ Sản phẩm bị tạm khóa</h3>
                        <p>Sản phẩm này đang bị tạm khóa và không hiển thị công khai. Vui lòng liên hệ admin để biết thêm chi tiết.</p>
                    </div>
                </div>
            </c:if>

            <c:if test="${product.status == 'Pending'}">
                <div class="panel panel--info" style="margin-top: 2rem;">
                    <div class="panel__body">
                        <h3>⏳ Sản phẩm đang chờ duyệt</h3>
                        <p>Sản phẩm này đang được admin xem xét và chưa hiển thị công khai. Bạn vẫn có thể chỉnh sửa thông tin.</p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</main>

<style>
.product-current-info {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.product-current-info h3 {
    margin: 0 0 1rem 0;
    color: #333;
    font-size: 1.25rem;
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.info-item {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.info-label {
    font-weight: 600;
    color: #333;
    font-size: 0.875rem;
}

.info-value {
    color: #666;
}

.form__row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

@media (max-width: 768px) {
    .form__row {
        grid-template-columns: 1fr;
    }
}

.form__group {
    margin-bottom: 1.5rem;
}

.form__label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #333;
}

.form__required {
    color: #dc3545;
}

.form__input,
.form__textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.15s ease-in-out;
}

.form__input:focus,
.form__textarea:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.form__textarea {
    resize: vertical;
    min-height: 120px;
}

.form__help {
    display: block;
    margin-top: 0.25rem;
    font-size: 0.875rem;
    color: #666;
}

.form__actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
}

.alert {
    padding: 1rem;
    margin-bottom: 1.5rem;
    border-radius: 4px;
}

.alert--error {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
}

.badge {
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    font-weight: 600;
    border-radius: 4px;
}

.badge--success {
    background-color: #d4edda;
    color: #155724;
}

.badge--warning {
    background-color: #fff3cd;
    color: #856404;
}

.badge--danger {
    background-color: #f8d7da;
    color: #721c24;
}

.button {
    padding: 0.75rem 1.5rem;
    border: 1px solid transparent;
    border-radius: 4px;
    font-size: 1rem;
    text-decoration: none;
    text-align: center;
    cursor: pointer;
    transition: all 0.15s ease-in-out;
    display: inline-block;
}

.button--primary {
    background-color: #007bff;
    border-color: #007bff;
    color: white;
}

.button--primary:hover {
    background-color: #0056b3;
    border-color: #0056b3;
}

.button--ghost {
    background-color: transparent;
    border-color: #6c757d;
    color: #6c757d;
}

.button--ghost:hover {
    background-color: #6c757d;
    color: white;
}

.panel--warning .panel__body {
    background-color: #fff3cd;
    border: 1px solid #ffeaa7;
    color: #856404;
}

.panel--info .panel__body {
    background-color: #cce7ff;
    border: 1px solid #b3d9ff;
    color: #004085;
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
