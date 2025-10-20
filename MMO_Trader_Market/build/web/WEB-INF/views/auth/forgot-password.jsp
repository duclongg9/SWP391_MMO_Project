<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Quên mật khẩu" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="MMO Trader Market" />
<c:set var="headerSubtitle" value="Khôi phục mật khẩu" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <c:if test="${not empty error}">
        <div class="alert alert--error"><c:out value="${error}" /></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert--success"><c:out value="${success}" /></div>
    </c:if>
    <form method="post" action="<c:url value='/forgot-password' />" class="form-card">
        <label class="form-card__label" for="email">Email đã đăng ký</label>
        <input class="form-card__input" id="email" name="email" type="email"
               placeholder="example@email.com" value="<c:out value='${email}'/>" required>
        <button class="button button--primary" type="submit">Gửi link đặt lại mật khẩu</button>
    </form>
    <section class="guide-link">
        <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại đăng nhập</a>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
