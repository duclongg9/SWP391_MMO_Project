<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page">
    <div class="seller-shop-form">
        <div class="seller-shop-form__card">
            <h2 class="seller-shop-form__title">
                <c:choose>
                    <c:when test="${not empty shopId}">Chỉnh sửa shop</c:when>
                    <c:otherwise>Tạo shop mới</c:otherwise>
                </c:choose>
            </h2>
            <p class="seller-shop-form__subtitle">Cập nhật tên và mô tả để khách hàng hiểu rõ cửa hàng của bạn.</p>

            <c:if test="${not empty formError}">
                <div class="alert alert--error"><c:out value="${formError}" /></div>
            </c:if>

            <c:choose>
                <c:when test="${empty shopId}">
                    <c:set var="formAction" value="${cPath}/seller/shops/create" />
                </c:when>
                <c:otherwise>
                    <c:set var="formAction" value="${cPath}/seller/shops/edit" />
                </c:otherwise>
            </c:choose>
            <form method="post" action="${formAction}" class="seller-shop-form__fields">
                <c:if test="${not empty shopId}">
                    <input type="hidden" name="id" value="${shopId}" />
                </c:if>
                <div class="seller-shop-form__row">
                    <div class="seller-shop-form__field">
                        <label for="name">Tên shop</label>
                        <input id="name" name="name" type="text"
                               value="${fn:escapeXml(formName)}"
                               required pattern="[\p{L}\p{N} ]{3,60}" maxlength="60"
                               title="3–60 ký tự: chữ, số, khoảng trắng" />
                        <c:if test="${not empty fieldErrors.name}">
                            <div class="form-error"><c:out value="${fieldErrors.name}" /></div>
                        </c:if>
                    </div>
                    <div class="seller-shop-form__field">
                        <label for="description">Mô tả</label>
                        <textarea id="description" name="description" rows="4" minlength="20" required
                                  title="Tối thiểu 20 ký tự">${fn:escapeXml(formDescription)}</textarea>
                        <c:if test="${not empty fieldErrors.description}">
                            <div class="form-error"><c:out value="${fieldErrors.description}" /></div>
                        </c:if>
                    </div>
                </div>
                <div class="seller-shop-form__actions">
                    <button type="submit" class="button button--primary">Lưu</button>
                    <a class="button button--ghost" href="${cPath}/seller/shops">Hủy</a>
                </div>
            </form>

            <c:if test="${not empty formCreatedAt || not empty formUpdatedAt}">
                <div class="seller-shop-form__meta">
                    <c:if test="${not empty formCreatedAt}">
                        <span>Created: ${formCreatedAt}</span>
                    </c:if>
                    <c:if test="${not empty formUpdatedAt}">
                        <span>Updated: ${formUpdatedAt}</span>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<style>
.seller-shop-form {
    min-height: calc(100vh - 120px);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem 1rem;
}
.seller-shop-form__card {
    width: min(800px, 100%);
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(15, 23, 42, 0.12);
    padding: 2.5rem;
}
.seller-shop-form__title {
    margin: 0;
    font-size: 1.6rem;
    font-weight: 700;
}
.seller-shop-form__subtitle {
    margin-top: 0.5rem;
    margin-bottom: 2rem;
    color: #6b7280;
}
.seller-shop-form__fields {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}
.seller-shop-form__row {
    display: grid;
    grid-template-columns: 2fr 3fr;
    gap: 1.5rem;
}
.seller-shop-form__field label {
    font-weight: 600;
    display: block;
    margin-bottom: 0.5rem;
}
.seller-shop-form__field input,
.seller-shop-form__field textarea {
    width: 100%;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    padding: 0.65rem 0.85rem;
    font-size: 1rem;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.seller-shop-form__field input:focus,
.seller-shop-form__field textarea:focus {
    border-color: #2563eb;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
    outline: none;
}
.seller-shop-form__actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
}
.seller-shop-form__meta {
    margin-top: 1.5rem;
    display: flex;
    gap: 1.5rem;
    font-size: 0.9rem;
    color: #6b7280;
}
.form-error {
    margin-top: 0.4rem;
    color: #dc2626;
    font-size: 0.9rem;
}
@media (max-width: 768px) {
    .seller-shop-form__card {
        padding: 1.75rem;
    }
    .seller-shop-form__row {
        grid-template-columns: 1fr;
    }
    .seller-shop-form__actions {
        flex-direction: column;
        align-items: stretch;
    }
    .seller-shop-form__actions .button {
        width: 100%;
    }
}
</style>
