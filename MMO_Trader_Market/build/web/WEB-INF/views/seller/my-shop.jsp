<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Quản lý gian hàng</h2>
            <div class="panel__actions">
                <a href="${pageContext.request.contextPath}/seller/products" class="button button--primary">
                    Quản lý sản phẩm
                </a>
            </div>
        </div>

        <div class="panel__body">
            <c:if test="${not empty successMessage}">
                <div class="alert alert--success">
                    <p><c:out value="${successMessage}" /></p>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert--error">
                    <p><c:out value="${error}" /></p>
                </div>
            </c:if>

            <c:if test="${not empty shop}">
                <!-- Thông tin gian hàng -->
                <div class="shop-info">
                    <div class="shop-info__header">
                        <h3><c:out value="${shop.name}" /></h3>
                        <span class="badge badge--${shop.status == 'Active' ? 'success' : (shop.status == 'Pending' ? 'warning' : 'danger')}">
                            <c:out value="${shop.status}" />
                        </span>
                    </div>
                    
                    <div class="shop-info__body">
                        <p class="shop-info__description">
                            <c:out value="${shop.description}" />
                        </p>
                        
                        <div class="shop-info__meta">
                            <div class="meta-item">
                                <span class="meta-item__label">Ngày tạo:</span>
                                <span class="meta-item__value">
                                    <fmt:formatDate value="${shop.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-item__label">Trạng thái:</span>
                                <span class="meta-item__value"><c:out value="${statusMessage}" /></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Form cập nhật thông tin -->
                <div class="panel panel--secondary" style="margin-top: 2rem;">
                    <div class="panel__header">
                        <h3 class="panel__title">Cập nhật thông tin gian hàng</h3>
                    </div>
                    <div class="panel__body">
                        <form method="post" action="${pageContext.request.contextPath}/seller/shop?action=update" class="form">
                            <div class="form__group">
                                <label for="shopName" class="form__label">
                                    Tên gian hàng <span class="form__required">*</span>
                                </label>
                                <input type="text" 
                                       id="shopName" 
                                       name="shopName" 
                                       class="form__input" 
                                       value="<c:out value='${shop.name}' />"
                                       placeholder="Nhập tên gian hàng"
                                       maxlength="50"
                                       required>
                            </div>

                            <div class="form__group">
                                <label for="description" class="form__label">
                                    Mô tả gian hàng <span class="form__required">*</span>
                                </label>
                                <textarea id="description" 
                                          name="description" 
                                          class="form__textarea" 
                                          rows="5"
                                          minlength="10"
                                          maxlength="500"
                                          required><c:out value="${shop.description}" /></textarea>
                            </div>

                            <div class="form__actions">
                                <button type="submit" class="button button--primary">
                                    Cập nhật thông tin
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Hướng dẫn -->
                <c:if test="${shop.status == 'Pending'}">
                    <div class="panel panel--info" style="margin-top: 2rem;">
                        <div class="panel__body">
                            <h3>⏳ Gian hàng đang chờ duyệt</h3>
                            <p>Gian hàng của bạn đang được admin xem xét. Trong thời gian này:</p>
                            <ul style="margin: 1rem 0; padding-left: 2rem;">
                                <li>Bạn có thể chỉnh sửa thông tin gian hàng</li>
                                <li>Chưa thể đăng sản phẩm mới</li>
                                <li>Thời gian duyệt thường là 1-3 ngày làm việc</li>
                            </ul>
                        </div>
                    </div>
                </c:if>

                <c:if test="${shop.status == 'Active'}">
                    <div class="panel panel--success" style="margin-top: 2rem;">
                        <div class="panel__body">
                            <h3>✅ Gian hàng đang hoạt động</h3>
                            <p>Gian hàng của bạn đã được kích hoạt. Bạn có thể:</p>
                            <ul style="margin: 1rem 0; padding-left: 2rem;">
                                <li>Đăng sản phẩm mới</li>
                                <li>Quản lý sản phẩm hiện có</li>
                                <li>Xem đơn hàng và doanh thu</li>
                            </ul>
                            <p>
                                <a href="${pageContext.request.contextPath}/seller/products?action=create" 
                                   class="button button--primary">
                                    Đăng sản phẩm đầu tiên
                                </a>
                            </p>
                        </div>
                    </div>
                </c:if>
            </c:if>
        </div>
    </div>
</main>

<style>
.shop-info {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.shop-info__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.shop-info__header h3 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
}

.shop-info__description {
    color: #666;
    line-height: 1.6;
    margin-bottom: 1rem;
}

.shop-info__meta {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.meta-item {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
}

.meta-item__label {
    font-weight: 600;
    color: #333;
    font-size: 0.875rem;
}

.meta-item__value {
    color: #666;
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
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
