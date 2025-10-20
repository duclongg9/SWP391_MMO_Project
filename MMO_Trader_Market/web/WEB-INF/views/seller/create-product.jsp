<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Đăng sản phẩm mới</h2>
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

            <form method="post" action="${pageContext.request.contextPath}/seller/products?action=create" class="form">
                <div class="form__group">
                    <label for="name" class="form__label">
                        Tên sản phẩm <span class="form__required">*</span>
                    </label>
                    <input type="text" 
                           id="name" 
                           name="name" 
                           class="form__input" 
                           value="<c:out value='${name}' />"
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
                              required><c:out value="${description}" /></textarea>
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
                               value="<c:out value='${price}' />"
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
                               value="<c:out value='${inventoryCount}' />"
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
                        Đăng sản phẩm
                    </button>
                    <a href="${pageContext.request.contextPath}/seller/products" class="button button--ghost">
                        Hủy
                    </a>
                </div>
            </form>

            <!-- Hướng dẫn -->
            <div class="panel panel--info" style="margin-top: 2rem;">
                <div class="panel__body">
                    <h3>📋 Hướng dẫn đăng sản phẩm:</h3>
                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                        <li><strong>Tên sản phẩm:</strong> Nên bao gồm tên game, level, server để dễ tìm kiếm</li>
                        <li><strong>Mô tả:</strong> Ghi rõ thông tin tài khoản, vật phẩm có sẵn, điều kiện bán</li>
                        <li><strong>Giá:</strong> Tham khảo giá thị trường để đảm bảo cạnh tranh</li>
                        <li><strong>Tồn kho:</strong> Nếu chỉ có 1 tài khoản duy nhất, hãy nhập số 1</li>
                        <li>Sản phẩm sẽ được admin duyệt trước khi hiển thị công khai</li>
                        <li>Tuân thủ quy định về nội dung và không bán tài khoản bị hack</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</main>

<style>
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
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
