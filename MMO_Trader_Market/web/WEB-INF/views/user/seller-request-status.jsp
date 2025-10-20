<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Trạng thái yêu cầu trở thành seller</h2>
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
                <c:when test="${not empty existingRequest}">
                    <!-- Hiển thị thông tin request hiện tại -->
                    <div class="request-status">
                        <div class="request-status__header">
                            <h3>Thông tin yêu cầu hiện tại</h3>
                            <span class="badge badge--${existingRequest.status == 'Approved' ? 'success' : (existingRequest.status == 'Pending' ? 'warning' : 'danger')}">
                                <c:choose>
                                    <c:when test="${existingRequest.status == 'Pending'}">Đang chờ duyệt</c:when>
                                    <c:when test="${existingRequest.status == 'Approved'}">Đã duyệt</c:when>
                                    <c:when test="${existingRequest.status == 'Rejected'}">Bị từ chối</c:when>
                                    <c:otherwise><c:out value="${existingRequest.status}" /></c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <div class="request-details">
                            <div class="detail-row">
                                <span class="detail-label">Tên doanh nghiệp:</span>
                                <span class="detail-value"><c:out value="${existingRequest.businessName}" /></span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">Ngày gửi:</span>
                                <span class="detail-value">
                                    <fmt:formatDate value="${existingRequest.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                </span>
                            </div>

                            <c:if test="${existingRequest.reviewedAt != null}">
                                <div class="detail-row">
                                    <span class="detail-label">Ngày xử lý:</span>
                                    <span class="detail-value">
                                        <fmt:formatDate value="${existingRequest.reviewedAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </span>
                                </div>
                            </c:if>

                            <div class="detail-row detail-row--full">
                                <span class="detail-label">Mô tả doanh nghiệp:</span>
                                <div class="detail-value">
                                    <p><c:out value="${existingRequest.businessDescription}" /></p>
                                </div>
                            </div>

                            <div class="detail-row detail-row--full">
                                <span class="detail-label">Kinh nghiệm:</span>
                                <div class="detail-value">
                                    <p><c:out value="${existingRequest.experience}" /></p>
                                </div>
                            </div>

                            <div class="detail-row detail-row--full">
                                <span class="detail-label">Thông tin liên hệ:</span>
                                <div class="detail-value">
                                    <p><c:out value="${existingRequest.contactInfo}" /></p>
                                </div>
                            </div>

                            <c:if test="${existingRequest.status == 'Rejected' && not empty existingRequest.rejectionReason}">
                                <div class="detail-row detail-row--full rejection-reason">
                                    <span class="detail-label">Lý do từ chối:</span>
                                    <div class="detail-value">
                                        <p><c:out value="${existingRequest.rejectionReason}" /></p>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <!-- Trạng thái và hành động -->
                    <c:choose>
                        <c:when test="${existingRequest.status == 'Pending'}">
                            <div class="panel panel--info" style="margin-top: 2rem;">
                                <div class="panel__body">
                                    <h3>⏳ Yêu cầu đang được xử lý</h3>
                                    <p>Yêu cầu trở thành seller của bạn đang được admin xem xét. Thường mất 1-3 ngày làm việc.</p>
                                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                                        <li>Admin sẽ liên hệ qua thông tin bạn cung cấp nếu cần xác minh</li>
                                        <li>Vui lòng kiểm tra email/điện thoại thường xuyên</li>
                                        <li>Bạn sẽ nhận được thông báo khi có kết quả</li>
                                    </ul>
                                </div>
                            </div>
                        </c:when>

                        <c:when test="${existingRequest.status == 'Approved'}">
                            <div class="panel panel--success" style="margin-top: 2rem;">
                                <div class="panel__body">
                                    <h3>🎉 Chúc mừng! Yêu cầu đã được duyệt</h3>
                                    <p>Bạn đã trở thành seller thành công! Bây giờ bạn có thể:</p>
                                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                                        <li>Tạo gian hàng của riêng mình</li>
                                        <li>Đăng sản phẩm lên marketplace</li>
                                        <li>Bắt đầu bán hàng và kiếm thu nhập</li>
                                    </ul>
                                    <p style="margin-top: 1.5rem;">
                                        <a href="${pageContext.request.contextPath}/seller/shop" 
                                           class="button button--primary">
                                            Tạo gian hàng ngay
                                        </a>
                                    </p>
                                </div>
                            </div>
                        </c:when>

                        <c:when test="${existingRequest.status == 'Rejected'}">
                            <div class="panel panel--error" style="margin-top: 2rem;">
                                <div class="panel__body">
                                    <h3>❌ Yêu cầu bị từ chối</h3>
                                    <p>Rất tiếc, yêu cầu trở thành seller của bạn đã bị từ chối. Bạn có thể:</p>
                                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                                        <li>Xem lý do từ chối ở trên để hiểu rõ vấn đề</li>
                                        <li>Cải thiện thông tin và gửi yêu cầu mới</li>
                                        <li>Liên hệ admin nếu có thắc mắc</li>
                                    </ul>
                                    <p style="margin-top: 1.5rem;">
                                        <a href="${pageContext.request.contextPath}/user/seller-request" 
                                           class="button button--primary">
                                            Gửi yêu cầu mới
                                        </a>
                                    </p>
                                </div>
                            </div>
                        </c:when>
                    </c:choose>
                </c:when>

                <c:otherwise>
                    <!-- Chưa có request nào -->
                    <div class="empty-state">
                        <div class="empty-state__icon">📝</div>
                        <h3 class="empty-state__title">Chưa có yêu cầu nào</h3>
                        <p class="empty-state__description">
                            Bạn chưa gửi yêu cầu trở thành seller. Hãy gửi yêu cầu để bắt đầu bán hàng trên marketplace.
                        </p>
                        <a href="${pageContext.request.contextPath}/user/seller-request" 
                           class="button button--primary">
                            Gửi yêu cầu trở thành seller
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</main>

<style>
.request-status {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.request-status__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #dee2e6;
}

.request-status__header h3 {
    margin: 0;
    font-size: 1.25rem;
    color: #333;
}

.request-details {
    display: grid;
    gap: 1rem;
}

.detail-row {
    display: grid;
    grid-template-columns: 200px 1fr;
    gap: 1rem;
    align-items: start;
}

.detail-row--full {
    grid-template-columns: 1fr;
}

.detail-label {
    font-weight: 600;
    color: #495057;
    font-size: 0.9rem;
}

.detail-value {
    color: #333;
}

.detail-value p {
    margin: 0;
    line-height: 1.6;
}

.rejection-reason {
    background-color: #f8d7da;
    padding: 1rem;
    border-radius: 4px;
    border-left: 4px solid #dc3545;
}

.rejection-reason .detail-label {
    color: #721c24;
}

.rejection-reason .detail-value {
    color: #721c24;
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
    max-width: 500px;
    margin-left: auto;
    margin-right: auto;
}

.badge {
    padding: 0.375rem 0.75rem;
    font-size: 0.875rem;
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

.panel--info .panel__body {
    background-color: #cce7ff;
    border: 1px solid #b3d9ff;
    color: #004085;
}

.panel--success .panel__body {
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
    color: #155724;
}

.panel--error .panel__body {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
}

@media (max-width: 768px) {
    .detail-row {
        grid-template-columns: 1fr;
        gap: 0.5rem;
    }
    
    .request-status__header {
        flex-direction: column;
        align-items: stretch;
        gap: 1rem;
    }
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
