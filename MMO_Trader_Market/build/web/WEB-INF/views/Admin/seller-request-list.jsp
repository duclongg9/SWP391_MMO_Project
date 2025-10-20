<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Quản lý yêu cầu seller</h2>
        </div>

        <div class="panel__body">
            <c:if test="${not empty successMessage}">
                <div class="alert alert--success">
                    <p><c:out value="${successMessage}" /></p>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert--error">
                    <p><c:out value="${errorMessage}" /></p>
                </div>
            </c:if>

            <!-- Bộ lọc -->
            <div class="filter-bar">
                <form method="get" action="${pageContext.request.contextPath}/admin/seller-requests" class="filter-form">
                    <div class="filter-form__group">
                        <label for="status">Trạng thái:</label>
                        <select name="status" id="status" class="filter-form__select">
                            <option value="all" ${currentStatus == 'all' ? 'selected' : ''}>Tất cả</option>
                            <option value="Pending" ${currentStatus == 'Pending' ? 'selected' : ''}>Chờ duyệt</option>
                            <option value="Approved" ${currentStatus == 'Approved' ? 'selected' : ''}>Đã duyệt</option>
                            <option value="Rejected" ${currentStatus == 'Rejected' ? 'selected' : ''}>Bị từ chối</option>
                        </select>
                    </div>
                    <button type="submit" class="button button--secondary">Lọc</button>
                </form>
            </div>

            <c:choose>
                <c:when test="${empty requests}">
                    <div class="empty-state">
                        <div class="empty-state__icon">📋</div>
                        <h3 class="empty-state__title">Không có yêu cầu nào</h3>
                        <p class="empty-state__description">
                            Chưa có yêu cầu trở thành seller nào được gửi.
                        </p>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Bảng danh sách -->
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>User</th>
                                    <th>Tên doanh nghiệp</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày gửi</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="requestView" items="${requests}">
                                    <tr>
                                        <td>#${requestView.request.id}</td>
                                        <td>
                                            <div class="user-info">
                                                <div class="user-info__name">${requestView.userName}</div>
                                                <div class="user-info__email">${requestView.userEmail}</div>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="business-info">
                                                <div class="business-info__name">
                                                    <c:out value="${requestView.request.businessName}" />
                                                </div>
                                                <div class="business-info__description">
                                                    <c:choose>
                                                        <c:when test="${requestView.request.businessDescription.length() > 100}">
                                                            <c:out value="${requestView.request.businessDescription.substring(0, 100)}..." />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${requestView.request.businessDescription}" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge badge--${requestView.request.status == 'Approved' ? 'success' : (requestView.request.status == 'Pending' ? 'warning' : 'danger')}">
                                                <c:choose>
                                                    <c:when test="${requestView.request.status == 'Pending'}">Chờ duyệt</c:when>
                                                    <c:when test="${requestView.request.status == 'Approved'}">Đã duyệt</c:when>
                                                    <c:when test="${requestView.request.status == 'Rejected'}">Bị từ chối</c:when>
                                                    <c:otherwise><c:out value="${requestView.request.status}" /></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${requestView.request.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="${pageContext.request.contextPath}/admin/seller-requests?action=view&id=${requestView.request.id}" 
                                                   class="button button--small button--secondary">
                                                    Chi tiết
                                                </a>
                                                
                                                <c:if test="${requestView.request.status == 'Pending'}">
                                                    <a href="${pageContext.request.contextPath}/admin/seller-requests?action=approve&id=${requestView.request.id}" 
                                                       class="button button--small button--success"
                                                       onclick="return confirm('Bạn có chắc chắn muốn duyệt yêu cầu này?')">
                                                        Duyệt
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/admin/seller-requests?action=reject&id=${requestView.request.id}" 
                                                       class="button button--small button--danger">
                                                        Từ chối
                                                    </a>
                                                </c:if>
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
                                <a href="?page=${pagination.currentPage - 1}&size=${pagination.pageSize}&status=${currentStatus}" 
                                   class="pagination__link">← Trước</a>
                            </c:if>
                            
                            <c:forEach begin="1" end="${pagination.totalPages}" var="pageNum">
                                <c:choose>
                                    <c:when test="${pageNum == pagination.currentPage}">
                                        <span class="pagination__current">${pageNum}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="?page=${pageNum}&size=${pagination.pageSize}&status=${currentStatus}" 
                                           class="pagination__link">${pageNum}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            
                            <c:if test="${pagination.currentPage < pagination.totalPages}">
                                <a href="?page=${pagination.currentPage + 1}&size=${pagination.pageSize}&status=${currentStatus}" 
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
.filter-bar {
    margin-bottom: 2rem;
    padding: 1rem;
    background-color: #f8f9fa;
    border-radius: 4px;
}

.filter-form {
    display: flex;
    gap: 1rem;
    align-items: end;
}

.filter-form__group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.filter-form__group label {
    font-size: 0.875rem;
    font-weight: 600;
    color: #495057;
}

.filter-form__select {
    padding: 0.5rem;
    border: 1px solid #ced4da;
    border-radius: 4px;
    font-size: 0.875rem;
}

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

.user-info__name {
    font-weight: 600;
    margin-bottom: 0.25rem;
}

.user-info__email {
    font-size: 0.875rem;
    color: #666;
}

.business-info__name {
    font-weight: 600;
    margin-bottom: 0.25rem;
}

.business-info__description {
    font-size: 0.875rem;
    color: #666;
    line-height: 1.4;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.button--small {
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
}

.button--success {
    background-color: #28a745;
    border-color: #28a745;
    color: white;
}

.button--success:hover {
    background-color: #218838;
    border-color: #1e7e34;
}

.button--danger {
    background-color: #dc3545;
    border-color: #dc3545;
    color: white;
}

.button--danger:hover {
    background-color: #c82333;
    border-color: #bd2130;
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

.alert {
    padding: 1rem;
    margin-bottom: 1.5rem;
    border-radius: 4px;
}

.alert--success {
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
    color: #155724;
}

.alert--error {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
}

.button {
    padding: 0.5rem 1rem;
    border: 1px solid transparent;
    border-radius: 4px;
    font-size: 0.875rem;
    text-decoration: none;
    text-align: center;
    cursor: pointer;
    transition: all 0.15s ease-in-out;
    display: inline-block;
}

.button--secondary {
    background-color: #6c757d;
    border-color: #6c757d;
    color: white;
}

.button--secondary:hover {
    background-color: #545b62;
    border-color: #4e555b;
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
