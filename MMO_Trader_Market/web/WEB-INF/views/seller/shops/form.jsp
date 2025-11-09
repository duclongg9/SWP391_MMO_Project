<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page seller-shop-form">
        <section class="panel seller-shop-form__panel">
                <div class="panel__header">
                        <h2 class="panel__title"><c:choose><c:when test="${not empty shopId}">Chỉnh sửa shop</c:when><c:otherwise>Tạo shop</c:otherwise></c:choose></h2>
                        <p class="panel__subtitle">Tên và mô tả cần tối thiểu 20 ký tự, không chứa ký tự đặc biệt.</p>
                </div>
                <div class="panel__body">
                        <c:if test="${not empty errorMessage}">
                                <div class="alert alert--error"><c:out value="${errorMessage}"/></div>
                        </c:if>

                        <form method="post" action="${cPath}/seller/shops/<c:out value='${empty shopId ? "create" : "edit"}'/>" class="shop-form">
                                <c:if test="${not empty shopId}"><input type="hidden" name="id" value="${shopId}"/></c:if>
                                <div class="shop-form__group">
                                        <label class="shop-form__label" for="name">Tên shop</label>
                                        <input class="form-input shop-form__input" type="text" id="name" name="name"
                                               value="${fn:escapeXml(formName)}" maxlength="255" minlength="20"
                                               placeholder="Ví dụ: Studio ACC Game chất lượng" required data-basic-text />
                                </div>
                                <div class="shop-form__group">
                                        <label class="shop-form__label" for="description">Mô tả shop</label>
                                        <textarea class="form-input shop-form__input" id="description" name="description" rows="5"
                                                  minlength="20" maxlength="1000" required data-basic-text
                                                  placeholder="Giới thiệu ngắn gọn về sản phẩm, dịch vụ và cam kết của shop">${fn:escapeXml(formDescription)}</textarea>
                                </div>
                                <p class="shop-form__note">Chỉ sử dụng chữ, số và khoảng trắng. Vui lòng nhập tối thiểu 20 ký tự.</p>
                                <div class="panel__footer shop-form__actions">
                                        <button type="submit" class="button button--primary"><c:out value='${empty shopId ? "Tạo shop" : "Lưu thay đổi"}'/></button>
                                        <a class="button button--ghost" href="${cPath}/seller/shops">Hủy</a>
                                </div>
                        </form>
                </div>
        </section>
</main>

<script>
        document.addEventListener('input', function (event) {
                if (event.target && event.target.matches('[data-basic-text]')) {
                        event.target.value = event.target.value.replace(/[^\p{L}\p{N}\s]/gu, '');
                }
        });
        document.addEventListener('blur', function (event) {
                if (event.target && event.target.matches('[data-basic-text]')) {
                        event.target.value = event.target.value.trim();
                }
        }, true);
</script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>


