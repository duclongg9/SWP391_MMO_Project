<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page seller-page--centered">
    <div class="shop-form">
        <section class="form-card shop-form__card">
            <header class="shop-form__header">
                <h2 class="shop-form__title">
                    <c:choose>
                        <c:when test="${not empty shopId}">Chỉnh sửa shop</c:when>
                        <c:otherwise>Tạo shop mới</c:otherwise>
                    </c:choose>
                </h2>
                <p class="shop-form__subtitle">
                    Hoàn thiện thông tin rõ ràng, chuyên nghiệp để tăng uy tín và tỷ lệ chuyển đổi đơn hàng.
                </p>
            </header>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert--error"><c:out value="${errorMessage}"/></div>
            </c:if>

            <form class="shop-form__body" method="post" action="${cPath}/seller/shops/<c:out value='${empty shopId ? "create" : "edit"}'/>">
                <c:if test="${not empty shopId}">
                    <input type="hidden" name="id" value="${shopId}" />
                </c:if>
                <c:set var="shopContextName" value="${not empty shop ? shop.name : formName}" />
                <c:if test="${not empty shopContextName}">
                    <div class="shop-form__context">
                        Đang quản lý shop: <strong><c:out value="${shopContextName}"/></strong>
                    </div>
                </c:if>
                <div class="form-card__field">
                    <label class="form-card__label" for="name">Tên shop</label>
                    <input class="form-card__input" type="text" id="name" name="name"
                           value="${fn:escapeXml(formName)}" maxlength="255" minlength="20"
                           data-basic-text
                           placeholder="Ví dụ: Cửa hàng tài khoản game chất lượng"
                           required />
                    <p class="form-note">Tên shop phải tối thiểu 20 ký tự, không chứa ký tự đặc biệt.</p>
                </div>
                <div class="form-card__field">
                    <label class="form-card__label" for="description">Mô tả chi tiết</label>
                    <textarea class="form-card__input shop-form__textarea" id="description" name="description"
                              rows="5" minlength="20" data-basic-text
                              placeholder="Giới thiệu điểm mạnh, cam kết bảo hành, thời gian hỗ trợ..." required>${fn:escapeXml(formDescription)}</textarea>
                    <p class="form-note">Mô tả tối thiểu 20 ký tự, chỉ gồm chữ, số, khoảng trắng và dấu . , -</p>
                </div>
                <div class="shop-form__actions">
                    <button type="submit" class="button button--primary">
                        <c:out value='${empty shopId ? "Tạo shop" : "Lưu thay đổi"}'/>
                    </button>
                    <a class="button button--ghost" href="${cPath}/seller/shops">Hủy</a>
                </div>
            </form>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
    (function () {
        // Lọc ký tự đặc biệt theo yêu cầu seller (chỉ cho phép chữ, số, khoảng trắng và ., -)
        const ALLOWED_PATTERN = /[^\p{L}\p{N}\s.,-]+/gu;
        document.querySelectorAll('[data-basic-text]').forEach(function (field) {
            field.addEventListener('input', function () {
                const original = field.value;
                const sanitized = original.replace(ALLOWED_PATTERN, '');
                if (original !== sanitized) {
                    field.value = sanitized;
                }
            });
        });
    })();
</script>
