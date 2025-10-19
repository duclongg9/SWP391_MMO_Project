<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đăng nhập" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="MMO Trader Market" />
<c:set var="headerSubtitle" value="Đăng nhập để quản lý giao dịch" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <c:if test="${not empty error}">
        <div class="alert alert--error"><c:out value="${error}" /></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert--success"><c:out value="${success}" /></div>
    </c:if>
    <form method="post" action="<c:url value='/auth' />" class="form-card">
        <label class="form-card__label" for="email">Email</label>
        <input class="form-card__input" id="email" name="email" type="email"
               placeholder="example@email.com"
               value="<c:out value='${prefillEmail}'/>" required>
        <label class="form-card__label" for="password">Mật khẩu</label>
        <input class="form-card__input" id="password" name="password" type="password" placeholder="••••••••" required>

        <button class="button button--primary" type="submit">Đăng nhập</button>
    </form>
    <section class="guide-link">
        <a class="button button--secondary" href="<c:url value='/oauth2/google/login' />">Đăng nhập bằng Google</a>
    </section>
    <section class="guide-link">
        <a class="button button--ghost" href="<c:url value='/forgot-password' />">Quên mật khẩu</a>
    </section>
    <section class="guide-link">
        <p>Chưa có tài khoản?</p>
        <a class="button button--ghost" href="<c:url value='/register' />">Đăng ký ngay</a>
    </section>
    <section class="guide-link">
        <a class="button button--ghost" href="<c:url value='/styleguide' />">Xem thư viện giao diện</a>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>