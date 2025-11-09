<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page">
	<section class="panel">
		<div class="panel__header">
			<h2 class="panel__title"><c:choose><c:when test="${not empty shopId}">Chỉnh sửa shop</c:when><c:otherwise>Tạo shop</c:otherwise></c:choose></h2>
			<c:if test="${empty shopId}">
				<p class="panel__subtitle">Tạo shop mới để bắt đầu bán sản phẩm. Bạn có thể tạo tối đa 5 shop.</p>
			</c:if>
		</div>
		<div class="panel__body">
			<c:if test="${not empty errorMessage}">
				<div class="alert alert--error"><c:out value="${errorMessage}"/></div>
			</c:if>

			<form method="post" action="${cPath}/seller/shops/<c:out value='${empty shopId ? "create" : "edit"}'/>">
				<c:if test="${not empty shopId}"><input type="hidden" name="id" value="${shopId}"/></c:if>
				<div class="form-grid">
					<div class="form-field">
						<label class="form-label" for="name">Tên Shop</label>
						<input class="form-input" type="text" id="name" name="name" value="${fn:escapeXml(formName)}" maxlength="255" placeholder="Nhập tên shop" required />
						<p class="form-note">Tên shop phải từ 3 đến 255 ký tự.</p>
					</div>
					<div class="form-field">
						<label class="form-label" for="description">Mô tả</label>
						<textarea class="form-input" id="description" name="description" rows="4" placeholder="Mô tả về shop của bạn (tùy chọn)">${fn:escapeXml(formDescription)}</textarea>
					</div>
				</div>
				<div class="panel__footer">
					<button type="submit" class="button button--primary"><c:out value='${empty shopId ? "Tạo shop" : "Lưu thay đổi"}'/></button>
					<a class="button button--ghost" href="${cPath}/seller/shops">Hủy</a>
				</div>
			</form>
		</div>
	</section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>


