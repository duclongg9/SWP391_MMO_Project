<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đăng ký" />
<c:set var="bodyClass" value="layout layout--auth" />
<c:set var="headerModifier" value="layout__header--auth" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content auth-page">
    <div class="auth-page__inner">
        <header class="auth-page__header">
            <h1>Đăng ký</h1>
            <p>Tạo tài khoản MMO Trader Market để bắt đầu giao dịch</p>
        </header>
        <c:if test="${not empty error}">
            <div class="alert alert--error auth-page__alert"><c:out value="${error}" /></div>
        </c:if>
        <form method="post" action="<c:url value='/register' />" class="form-card auth-page__form">
            <div class="form-card__field">
                <label class="form-card__label" for="name">Tài khoản</label>
                <input class="form-card__input" id="name" name="name" type="text"
                       placeholder="VD: username123" value="<c:out value='${name}'/>" required>
            </div>
            <div class="form-card__field">
                <label class="form-card__label" for="email">Email</label>
                <input class="form-card__input" id="email" name="email" type="email"
                       placeholder="example@email.com" value="<c:out value='${email}'/>" required>
            </div>
            <div class="form-card__field">
                <label class="form-card__label" for="password">Mật khẩu</label>
                <input class="form-card__input" id="password" name="password" type="password" placeholder="Tối thiểu 8 ký tự gồm chữ và số" required>
            </div>
            <div class="form-card__field">
                <label class="form-card__label" for="confirmPassword">Nhập lại mật khẩu</label>
                <input class="form-card__input" id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required>
            </div>
            <div class="form-card__option">
                <input class="form-card__checkbox" id="acceptTerms" name="acceptTerms" type="checkbox"
                       <c:if test="${acceptTermsChecked}">checked</c:if> required>
                <label class="form-card__option-text" for="acceptTerms">
                    Tôi đã đọc và đồng ý với
                    <a href="#" target="_blank" rel="noopener">Điều khoản sử dụng Tap Hóa MMO</a>
                </label>
            </div>
            <button class="button button--primary" type="submit">Đăng ký</button>
            <div class="form-card__actions-row">
                <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại trang đăng nhập</a>
                <!--<a class="button button--secondary" href="<c:url value='/auth/google' />">Đăng ký bằng Google</a>-->
            </div>
        </form>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
