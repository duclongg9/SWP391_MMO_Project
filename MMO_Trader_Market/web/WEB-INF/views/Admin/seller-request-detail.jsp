<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết yêu cầu seller #${sellerRequest.id}</h2>
            <div class="panel__actions">
                <a href="${pageContext.request.contextPath}/admin/seller-requests" class="button button--ghost">
                    ← Quay lại danh sách
                </a>
            </div>
        </div>

        <div class="panel__body">
            <!-- Trạng thái request -->
            <div class="request-header">
                <div class="request-header__status">
                    <span class="badge badge--${sellerRequest.status == 'Approved' ? 'success' : (sellerRequest.status == 'Pending' ? 'warning' : 'danger')} badge--large">
                        <c:choose>
                            <c:when test="${sellerRequest.status == 'Pending'}">Chờ duyệt</c:when>
                            <c:when test="${sellerRequest.status == 'Approved'}">Đã duyệt</c:when>
                            <c:when test="${sellerRequest.status == 'Rejected'}">Bị từ chối</c:when>
                            <c:otherwise><c:out value="${sellerRequest.status}" /></c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="request-header__dates">
                    <div class="date-info">
                        <span class="date-info__label">Ngày gửi:</span>
                        <span class="date-info__value">
                            <fmt:formatDate value="${sellerRequest.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                    </div>
                    <c:if test="${sellerRequest.reviewedAt != null}">
                        <div class="date-info">
                            <span class="date-info__label">Ngày xử lý:</span>
                            <span class="date-info__value">
                                <fmt:formatDate value="${sellerRequest.reviewedAt}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Chi tiết thông tin -->
            <div class="request-details">
                <div class="detail-section">
                    <h3 class="detail-section__title">Thông tin doanh nghiệp</h3>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Tên doanh nghiệp:</span>
                            <span class="detail-value"><c:out value="${sellerRequest.businessName}" /></span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h3 class="detail-section__title">Thông tin liên hệ</h3>
                    <div class="detail-item detail-item--full">
                        <span class="detail-label">Thông tin liên hệ:</span>
                        <div class="detail-value">
                            <p class="detail-text"><c:out value="${sellerRequest.contactInfo}" /></p>
                        </div>
                    </div>
                </div>

                <c:if test="${sellerRequest.status == 'Rejected' && not empty sellerRequest.rejectionReason}">
                    <div class="detail-section detail-section--rejection">
                        <h3 class="detail-section__title">Lý do từ chối</h3>
                        <div class="detail-item detail-item--full">
                            <div class="detail-value">
                                <p class="detail-text"><c:out value="${sellerRequest.rejectionReason}" /></p>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>

            <!-- Hành động -->
            <c:if test="${sellerRequest.status == 'Pending'}">
                <div class="action-section">
                    <h3>Xử lý yêu cầu</h3>
                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/admin/seller-requests?action=approve&id=${sellerRequest.id}" 
                           class="button button--success"
                           onclick="return confirm('Bạn có chắc chắn muốn duyệt yêu cầu này? User sẽ được chuyển thành seller.')">
                            ✓ Duyệt yêu cầu
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/seller-requests?action=reject&id=${sellerRequest.id}" 
                           class="button button--danger">
                            ✗ Từ chối yêu cầu
                        </a>
                    </div>
                </div>
            </c:if>

            <c:if test="${sellerRequest.status != 'Pending'}">
                <div class="action-section">
                    <div class="status-info">
                        <c:choose>
                            <c:when test="${sellerRequest.status == 'Approved'}">
                                <div class="status-info--success">
                                    <h3>✅ Yêu cầu đã được duyệt</h3>
                                    <p>User đã được chuyển thành seller và có thể tạo gian hàng.</p>
                                </div>
                            </c:when>
                            <c:when test="${sellerRequest.status == 'Rejected'}">
                                <div class="status-info--danger">
                                    <h3>❌ Yêu cầu đã bị từ chối</h3>
                                    <p>Yêu cầu đã được xử lý và từ chối với lý do như trên.</p>
                                </div>
                            </c:when>
                        </c:choose>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</main>

<style>
.request-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding: 1.5rem;
    background-color: #f8f9fa;
    border-radius: 8px;
}

.request-header__status {
    display: flex;
    align-items: center;
}

.request-header__dates {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.date-info {
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.date-info__label {
    font-size: 0.875rem;
    color: #666;
    font-weight: 500;
}

.date-info__value {
    font-size: 0.875rem;
    color: #333;
    font-weight: 600;
}

.badge--large {
    font-size: 1rem;
    padding: 0.5rem 1rem;
}

.request-details {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.detail-section {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
}

.detail-section--rejection {
    background: #f8d7da;
    border-left: 4px solid #dc3545;
}

.detail-section__title {
    margin: 0 0 1rem 0;
    color: #333;
    font-size: 1.125rem;
    font-weight: 600;
    border-bottom: 1px solid #dee2e6;
    padding-bottom: 0.5rem;
}

.detail-section--rejection .detail-section__title {
    color: #721c24;
}

.detail-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
}

.detail-item {
    display: flex;
    gap: 1rem;
    align-items: start;
}

.detail-item--full {
    grid-column: 1 / -1;
    flex-direction: column;
    gap: 0.5rem;
}

.detail-label {
    font-weight: 600;
    color: #495057;
    font-size: 0.9rem;
    min-width: 150px;
}

.detail-value {
    color: #333;
    flex: 1;
}

.detail-text {
    margin: 0;
    line-height: 1.6;
    white-space: pre-wrap;
}

.action-section {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: #fff;
    border: 1px solid #dee2e6;
    border-radius: 8px;
}

.action-section h3 {
    margin: 0 0 1rem 0;
    color: #333;
    font-size: 1.125rem;
}

.action-buttons {
    display: flex;
    gap: 1rem;
}

.status-info {
    text-align: center;
    padding: 1rem;
}

.status-info--success {
    color: #155724;
}

.status-info--danger {
    color: #721c24;
}

.status-info h3 {
    margin: 0 0 0.5rem 0;
}

.status-info p {
    margin: 0;
    font-size: 0.95rem;
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

.button--ghost {
    background-color: transparent;
    border-color: #6c757d;
    color: #6c757d;
}

.button--ghost:hover {
    background-color: #6c757d;
    color: white;
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

@media (max-width: 768px) {
    .request-header {
        flex-direction: column;
        align-items: stretch;
        gap: 1rem;
    }
    
    .action-buttons {
        flex-direction: column;
    }
    
    .detail-grid {
        grid-template-columns: 1fr;
    }
    
    .detail-item {
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .detail-label {
        min-width: auto;
    }
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
