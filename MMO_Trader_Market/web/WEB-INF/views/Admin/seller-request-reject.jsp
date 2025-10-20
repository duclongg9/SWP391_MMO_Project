<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Từ chối yêu cầu seller #${sellerRequest.id}</h2>
            <div class="panel__actions">
                <a href="${pageContext.request.contextPath}/admin/seller-requests?action=view&id=${sellerRequest.id}" class="button button--ghost">
                    ← Quay lại chi tiết
                </a>
            </div>
        </div>

        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error">
                    <p><c:out value="${error}" /></p>
                </div>
            </c:if>

            <!-- Thông tin request -->
            <div class="request-summary">
                <h3>Thông tin yêu cầu</h3>
                <div class="summary-grid">
                    <div class="summary-item">
                        <span class="summary-label">Tên doanh nghiệp:</span>
                        <span class="summary-value"><c:out value="${sellerRequest.businessName}" /></span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Ngày gửi:</span>
                        <span class="summary-value">
                            <fmt:formatDate value="${sellerRequest.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                    </div>
                </div>
                <div class="summary-item summary-item--full">
                    <span class="summary-label">Mô tả doanh nghiệp:</span>
                    <div class="summary-value">
                        <p><c:out value="${sellerRequest.businessDescription}" /></p>
                    </div>
                </div>
            </div>

            <!-- Form từ chối -->
            <form method="post" action="${pageContext.request.contextPath}/admin/seller-requests?action=submit-reject" class="form">
                <input type="hidden" name="requestId" value="${sellerRequest.id}">
                
                <div class="form__group">
                    <label for="rejectionReason" class="form__label">
                        Lý do từ chối <span class="form__required">*</span>
                    </label>
                    <textarea id="rejectionReason" 
                              name="rejectionReason" 
                              class="form__textarea" 
                              placeholder="Nhập lý do từ chối yêu cầu một cách chi tiết và rõ ràng..."
                              rows="8"
                              minlength="10"
                              maxlength="500"
                              required><c:out value="${rejectionReason}" /></textarea>
                    <small class="form__help">
                        Lý do từ chối sẽ được gửi tới user để họ hiểu và cải thiện (10-500 ký tự)
                    </small>
                </div>

                <div class="form__actions">
                    <button type="submit" class="button button--danger">
                        Từ chối yêu cầu
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/seller-requests?action=view&id=${sellerRequest.id}" 
                       class="button button--ghost">
                        Hủy
                    </a>
                </div>
            </form>

            <!-- Hướng dẫn -->
            <div class="panel panel--warning" style="margin-top: 2rem;">
                <div class="panel__body">
                    <h3>⚠️ Lưu ý khi từ chối yêu cầu:</h3>
                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                        <li><strong>Lý do rõ ràng:</strong> Đưa ra lý do cụ thể để user hiểu và cải thiện</li>
                        <li><strong>Tôn trọng:</strong> Sử dụng ngôn từ lịch sự và chuyên nghiệp</li>
                        <li><strong>Hướng dẫn:</strong> Đề xuất cách cải thiện để user có thể gửi lại yêu cầu</li>
                        <li><strong>Không thể hoàn tác:</strong> Sau khi từ chối, user sẽ phải gửi yêu cầu mới</li>
                    </ul>
                </div>
            </div>

            <!-- Các lý do từ chối phổ biến -->
            <div class="panel panel--secondary" style="margin-top: 2rem;">
                <div class="panel__body">
                    <h3>💡 Các lý do từ chối phổ biến:</h3>
                    <div class="reason-examples">
                        <div class="reason-example" onclick="insertReason(this.dataset.reason)">
                            <h4>Thiếu kinh nghiệm</h4>
                            <p data-reason="Kinh nghiệm bán hàng chưa đủ để đảm bảo chất lượng dịch vụ. Bạn cần có ít nhất 6 tháng kinh nghiệm bán hàng online hoặc chứng minh được khả năng kinh doanh qua các kênh khác.">
                                Kinh nghiệm bán hàng chưa đủ để đảm bảo chất lượng dịch vụ...
                            </p>
                        </div>
                        <div class="reason-example" onclick="insertReason(this.dataset.reason)">
                            <h4>Thông tin không rõ ràng</h4>
                            <p data-reason="Thông tin về doanh nghiệp và kế hoạch kinh doanh chưa đủ chi tiết. Vui lòng bổ sung thêm về loại sản phẩm cụ thể, target khách hàng, và chiến lược marketing.">
                                Thông tin về doanh nghiệp và kế hoạch kinh doanh chưa đủ chi tiết...
                            </p>
                        </div>
                        <div class="reason-example" onclick="insertReason(this.dataset.reason)">
                            <h4>Thông tin liên hệ không hợp lệ</h4>
                            <p data-reason="Thông tin liên hệ không thể xác minh được. Vui lòng cung cấp số điện thoại và các kênh liên hệ khác có thể xác minh được danh tính.">
                                Thông tin liên hệ không thể xác minh được...
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<style>
.request-summary {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.request-summary h3 {
    margin: 0 0 1rem 0;
    color: #333;
    font-size: 1.125rem;
}

.summary-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
}

.summary-item {
    display: flex;
    gap: 1rem;
    align-items: start;
}

.summary-item--full {
    grid-column: 1 / -1;
    flex-direction: column;
    gap: 0.5rem;
}

.summary-label {
    font-weight: 600;
    color: #495057;
    font-size: 0.9rem;
    min-width: 120px;
}

.summary-value {
    color: #333;
    flex: 1;
}

.summary-value p {
    margin: 0;
    line-height: 1.6;
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

.form__textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.15s ease-in-out;
    resize: vertical;
    min-height: 150px;
    font-family: inherit;
}

.form__textarea:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
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

.reason-examples {
    display: grid;
    gap: 1rem;
}

.reason-example {
    padding: 1rem;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.15s ease-in-out;
}

.reason-example:hover {
    border-color: #007bff;
    background-color: #f8f9fa;
}

.reason-example h4 {
    margin: 0 0 0.5rem 0;
    color: #333;
    font-size: 1rem;
}

.reason-example p {
    margin: 0;
    font-size: 0.875rem;
    color: #666;
    line-height: 1.4;
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

.button--danger {
    background-color: #dc3545;
    border-color: #dc3545;
    color: white;
}

.button--danger:hover {
    background-color: #c82333;
    border-color: #bd2130;
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

.panel--secondary .panel__body {
    background-color: #f8f9fa;
    border: 1px solid #e0e0e0;
}

@media (max-width: 768px) {
    .summary-grid {
        grid-template-columns: 1fr;
    }
    
    .summary-item {
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .summary-label {
        min-width: auto;
    }
    
    .form__actions {
        flex-direction: column;
    }
}
</style>

<script>
function insertReason(reason) {
    const textarea = document.getElementById('rejectionReason');
    if (textarea.value.trim() === '') {
        textarea.value = reason;
    } else {
        textarea.value += '\n\n' + reason;
    }
    textarea.focus();
}
</script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
