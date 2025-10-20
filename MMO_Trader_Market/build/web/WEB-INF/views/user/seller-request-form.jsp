<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Yêu cầu trở thành seller</h2>
            <p class="panel__subtitle">Hãy điền đầy đủ thông tin để gửi yêu cầu trở thành seller</p>
        </div>

        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error">
                    <p><c:out value="${error}" /></p>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/user/seller-request" class="form">
                <div class="form__group">
                    <label for="businessName" class="form__label">
                        Tên doanh nghiệp/gian hàng dự kiến <span class="form__required">*</span>
                    </label>
                    <input type="text" 
                           id="businessName" 
                           name="businessName" 
                           class="form__input" 
                           value="<c:out value='${businessName}' />"
                           placeholder="Nhập tên doanh nghiệp hoặc gian hàng bạn muốn tạo"
                           maxlength="100"
                           required>
                    <small class="form__help">
                        Tên này sẽ được sử dụng làm tên gian hàng khi được duyệt (3-100 ký tự)
                    </small>
                </div>

                <div class="form__group">
                    <label for="businessDescription" class="form__label">
                        Mô tả doanh nghiệp <span class="form__required">*</span>
                    </label>
                    <textarea id="businessDescription" 
                              name="businessDescription" 
                              class="form__textarea" 
                              placeholder="Mô tả chi tiết về doanh nghiệp, loại sản phẩm bạn dự định bán, target khách hàng..."
                              rows="6"
                              minlength="20"
                              maxlength="1000"
                              required><c:out value="${businessDescription}" /></textarea>
                    <small class="form__help">
                        Mô tả chi tiết về doanh nghiệp và kế hoạch kinh doanh (20-1000 ký tự)
                    </small>
                </div>

                <div class="form__group">
                    <label for="experience" class="form__label">
                        Kinh nghiệm bán hàng <span class="form__required">*</span>
                    </label>
                    <textarea id="experience" 
                              name="experience" 
                              class="form__textarea" 
                              placeholder="Mô tả kinh nghiệm bán hàng online, offline, các platform đã sử dụng, thành tích đạt được..."
                              rows="5"
                              minlength="10"
                              maxlength="500"
                              required><c:out value="${experience}" /></textarea>
                    <small class="form__help">
                        Chia sẻ kinh nghiệm bán hàng và thành tích của bạn (10-500 ký tự)
                    </small>
                </div>

                <div class="form__group">
                    <label for="contactInfo" class="form__label">
                        Thông tin liên hệ <span class="form__required">*</span>
                    </label>
                    <textarea id="contactInfo" 
                              name="contactInfo" 
                              class="form__textarea" 
                              placeholder="Số điện thoại, Facebook, Zalo, Telegram hoặc các kênh liên hệ khác..."
                              rows="4"
                              minlength="10"
                              maxlength="200"
                              required><c:out value="${contactInfo}" /></textarea>
                    <small class="form__help">
                        Thông tin liên hệ để admin có thể xác minh danh tính (10-200 ký tự)
                    </small>
                </div>

                <div class="form__actions">
                    <button type="submit" class="button button--primary">
                        Gửi yêu cầu
                    </button>
                    <a href="${pageContext.request.contextPath}/home" class="button button--ghost">
                        Hủy
                    </a>
                </div>
            </form>

            <!-- Hướng dẫn và lưu ý -->
            <div class="panel panel--info" style="margin-top: 2rem;">
                <div class="panel__body">
                    <h3>📋 Lưu ý quan trọng:</h3>
                    <ul style="margin: 1rem 0; padding-left: 2rem;">
                        <li><strong>Thời gian duyệt:</strong> Admin sẽ xem xét yêu cầu trong 1-3 ngày làm việc</li>
                        <li><strong>Xác minh thông tin:</strong> Admin có thể liên hệ để xác minh danh tính</li>
                        <li><strong>Tiêu chuẩn duyệt:</strong> Kinh nghiệm bán hàng, kế hoạch kinh doanh rõ ràng</li>
                        <li><strong>Sau khi được duyệt:</strong> Bạn sẽ có thể tạo gian hàng và đăng sản phẩm</li>
                        <li><strong>Tuân thủ quy định:</strong> Seller phải tuân thủ các quy định về sản phẩm và dịch vụ</li>
                    </ul>
                </div>
            </div>

            <!-- Các bước trở thành seller -->
            <div class="panel panel--secondary" style="margin-top: 2rem;">
                <div class="panel__body">
                    <h3>🚀 Quy trình trở thành seller:</h3>
                    <div class="process-steps">
                        <div class="step">
                            <div class="step__number">1</div>
                            <div class="step__content">
                                <h4>Gửi yêu cầu</h4>
                                <p>Điền form với thông tin chi tiết về doanh nghiệp và kinh nghiệm</p>
                            </div>
                        </div>
                        <div class="step">
                            <div class="step__number">2</div>
                            <div class="step__content">
                                <h4>Chờ duyệt</h4>
                                <p>Admin sẽ xem xét và có thể liên hệ để xác minh thông tin</p>
                            </div>
                        </div>
                        <div class="step">
                            <div class="step__number">3</div>
                            <div class="step__content">
                                <h4>Được duyệt</h4>
                                <p>Account được cập nhật role seller, có thể tạo gian hàng</p>
                            </div>
                        </div>
                        <div class="step">
                            <div class="step__number">4</div>
                            <div class="step__content">
                                <h4>Bắt đầu bán</h4>
                                <p>Tạo gian hàng, đăng sản phẩm và bắt đầu kinh doanh</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<style>
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

.process-steps {
    display: grid;
    gap: 1.5rem;
}

.step {
    display: flex;
    gap: 1rem;
    align-items: flex-start;
}

.step__number {
    width: 2.5rem;
    height: 2.5rem;
    border-radius: 50%;
    background-color: #007bff;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    flex-shrink: 0;
}

.step__content h4 {
    margin: 0 0 0.5rem 0;
    color: #333;
}

.step__content p {
    margin: 0;
    color: #666;
    line-height: 1.5;
}

.panel--info .panel__body {
    background-color: #cce7ff;
    border: 1px solid #b3d9ff;
    color: #004085;
}

.panel--secondary .panel__body {
    background-color: #f8f9fa;
    border: 1px solid #e0e0e0;
}
</style>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
