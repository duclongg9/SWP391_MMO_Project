<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Quản lý sản phẩm</h2>
            <div class="panel__actions">
                <a href="${pageContext.request.contextPath}/seller/products?action=create" class="button button--primary">
                    + Đăng sản phẩm mới
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

            <c:choose>
                <c:when test="${empty products}">
                    <div class="empty-state">
                        <div class="empty-state__icon">📦</div>
                        <h3 class="empty-state__title">Chưa có sản phẩm nào</h3>
                        <p class="empty-state__description">
                            Hãy đăng sản phẩm đầu tiên để bắt đầu bán hàng
                        </p>
                        <a href="${pageContext.request.contextPath}/seller/products?action=create" 
                           class="button button--primary">
                            Đăng sản phẩm đầu tiên
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Bảng danh sách sản phẩm -->
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Tên sản phẩm</th>
                                    <th>Giá</th>
                                    <th>Tồn kho</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày tạo</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="product" items="${products}">
                                    <tr>
                                        <td>#${product.id}</td>
                                        <td>
                                            <div class="product-info">
                                                <div class="product-info__name">
                                                    <c:out value="${product.name}" />
                                                </div>
                                                <div class="product-info__description">
                                                    <c:choose>
                                                        <c:when test="${product.description.length() > 50}">
                                                            <c:out value="${product.description.substring(0, 50)}..." />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${product.description}" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${product.price}" type="number" minFractionDigits="0" /> đ
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${product.inventoryCount == null}">
                                                    <span class="badge badge--info">Không giới hạn</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inventory-count ${product.inventoryCount <= 5 ? 'inventory-count--low' : ''}">
                                                        ${product.inventoryCount}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="badge badge--${product.status == 'Available' ? 'success' : (product.status == 'Pending' ? 'warning' : 'danger')}">
                                                <c:choose>
                                                    <c:when test="${product.status == 'Available'}">Có sẵn</c:when>
                                                    <c:when test="${product.status == 'Pending'}">Chờ duyệt</c:when>
                                                    <c:when test="${product.status == 'Suspended'}">Tạm khóa</c:when>
                                                    <c:otherwise><c:out value="${product.status}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${product.createdAt}" pattern="dd/MM/yyyy" />
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="${pageContext.request.contextPath}/seller/products?action=edit&id=${product.id}" 
                                                   class="button button--small button--secondary">
                                                    Sửa
                                                </a>
                                                <a href="${pageContext.request.contextPath}/seller/products?action=delete&id=${product.id}" 
                                                   class="button button--small button--danger"
                                                   onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')">
                                                    Xóa
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Phân trang -->
                    <c:if test="${pagination.totalPages > 1}">
                        <div class="pagination">
                            <c:if test="${pagination.currentPage > 1}">
                                <a href="?page=${pagination.currentPage - 1}&size=${pagination.pageSize}" 
                                   class="pagination__link">← Trước</a>
                            </c:if>
                            
                            <c:forEach begin="1" end="${pagination.totalPages}" var="pageNum">
                                <c:choose>
                                    <c:when test="${pageNum == pagination.currentPage}">
                                        <span class="pagination__current">${pageNum}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="?page=${pageNum}&size=${pagination.pageSize}" 
                                           class="pagination__link">${pageNum}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <c:if test="${pagination.currentPage < pagination.totalPages}">
                                <a href="?page=${pagination.currentPage + 1}&size=${pagination.pageSize}" 
                                   class="pagination__link">Sau →</a>
                            </c:if>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<style>
.empty-state {
    text-align: center;
    padding: 4rem 2rem;
}

.empty-state__icon {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.empty-state__title {
    margin: 0 0 1rem 0;
    color: #333;
}

.empty-state__description {
    color: #666;
    margin-bottom: 2rem;
}

.table-container {
    overflow-x: auto;
    margin-bottom: 2rem;
}

.table {
    width: 100%;
    border-collapse: collapse;
    border: 1px solid #e0e0e0;
}

.table th,
.table td {
    padding: 0.75rem;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.table th {
    background-color: #f8f9fa;
    font-weight: 600;
    color: #333;
}

.table tbody tr:hover {
    background-color: #f8f9fa;
}

.product-info__name {
    font-weight: 600;
    margin-bottom: 0.25rem;
}

.product-info__description {
    font-size: 0.875rem;
    color: #666;
}

.inventory-count {
    font-weight: 600;
}

.inventory-count--low {
    color: #dc3545;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
}

.button--small {
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
}

.pagination {
    display: flex;
    justify-content: center;
    gap: 0.5rem;
    margin-top: 2rem;
}

.pagination__link,
.pagination__current {
    padding: 0.5rem 0.75rem;
    border: 1px solid #dee2e6;
    text-decoration: none;
    color: #495057;
}

.pagination__link:hover {
    background-color: #e9ecef;
}

.pagination__current {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
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

.badge--info {
    background-color: #cce7ff;
    color: #004085;
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
