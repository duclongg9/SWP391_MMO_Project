<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đăng nhập" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="MMO Trader Market" />
<c:set var="headerSubtitle" value="Đăng nhập để quản lý giao dịch" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<style>
  /* Thu gọn khoảng cách giữa các nút hướng dẫn dưới form */
  /* Tùy chọn: thu gọn form cho đều mắt */
  .form-card { gap: 12px; }
  .form-card__field { margin-bottom: 10px; }
</style>
<main class="layout__content auth-page">
    <c:if test="${not empty error}">
        <div class="alert alert--error"><c:out value="${error}" /></div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert--success"><c:out value="${success}" /></div>
    </c:if>
    <form method="post" action="<c:url value='/auth' />" class="form-card">
        <div class="form-card__field">
            <label class="form-card__label" for="email">Email</label>
            <input class="form-card__input" id="email" name="email" type="email"
                   placeholder="example@email.com"
                   value="<c:out value='${prefillEmail}'/>" required>
        </div>
        <div class="form-card__field">
            <label class="form-card__label" for="password">Mật khẩu</label>
            <input class="form-card__input" id="password" name="password" type="password" placeholder="••••••••" required>
        </div>
        <div class="form-card__options-row">
            <label class="form-card__option form-card__option--inline" for="rememberMe">
                <input class="form-card__checkbox" id="rememberMe" name="rememberMe" type="checkbox"
                       <c:if test="${rememberMeChecked}">checked</c:if>>
                <span class="form-card__option-text">Ghi nhớ đăng nhập</span>
            </label>
            <a class="form-card__link" href="<c:url value='/forgot-password' />">Quên mật khẩu?</a>
        </div>

        <button class="button button--primary" type="submit">Đăng nhập</button>
        <div class="form-card__actions-row">
            <a class="button button--secondary" href="<c:url value='/auth/google' />">Đăng nhập bằng Google</a>
            <a class="button button--ghost" href="<c:url value='/register' />">Đăng ký ngay</a>
        </div>
    </form>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>