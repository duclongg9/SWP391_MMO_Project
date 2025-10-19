<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đặt lại mật khẩu" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="Đặt lại mật khẩu" />
<c:set var="headerSubtitle" value="Nhập mật khẩu mới" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <c:if test="${not empty error}">
        <div class="alert alert--error"><c:out value="${error}" /></div>
    </c:if>
    <c:choose>
        <c:when test="${empty token}">
            <p>Vui lòng sử dụng liên kết đặt lại mật khẩu hợp lệ được gửi qua email.</p>
        </c:when>
        <c:otherwise>
            <form method="post" action="<c:url value='/reset-password' />" class="form-card">
                <input type="hidden" name="token" value="<c:out value='${token}'/>" />
                <label class="form-card__label" for="password">Mật khẩu mới</label>
                <input class="form-card__input" id="password" name="password" type="password"
                       placeholder="Tối thiểu 8 ký tự gồm chữ và số" required>
                <label class="form-card__label" for="confirmPassword">Xác nhận mật khẩu</label>
                <input class="form-card__input" id="confirmPassword" name="confirmPassword" type="password"
                       placeholder="Nhập lại mật khẩu" required>
                <button class="button button--primary" type="submit">Cập nhật mật khẩu</button>
            </form>
        </c:otherwise>
    </c:choose>
    <section class="guide-link">
        <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại đăng nhập</a>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
