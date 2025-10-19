<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đăng ký" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="MMO Trader Market" />
<c:set var="headerSubtitle" value="Tạo tài khoản mới" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <c:if test="${not empty error}">
        <div class="alert alert--error"><c:out value="${error}" /></div>
    </c:if>
    <form method="post" action="<c:url value='/register' />" class="form-card">
        <label class="form-card__label" for="name">Tên hiển thị</label>
        <input class="form-card__input" id="name" name="name" type="text"
               placeholder="VD: Nguyễn Văn A" value="<c:out value='${name}'/>" required>

        <label class="form-card__label" for="email">Email</label>
        <input class="form-card__input" id="email" name="email" type="email"
               placeholder="example@email.com" value="<c:out value='${email}'/>" required>

        <label class="form-card__label" for="password">Mật khẩu</label>
        <input class="form-card__input" id="password" name="password" type="password" placeholder="Tối thiểu 8 ký tự gồm chữ và số" required>

        <label class="form-card__label" for="confirmPassword">Xác nhận mật khẩu</label>
        <input class="form-card__input" id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required>

        <button class="button button--primary" type="submit">Đăng ký</button>
    </form>
    <section class="guide-link">
        <a class="button button--secondary" href="<c:url value='/oauth2/google/login' />">Đăng ký bằng Google</a>
    </section>
    <section class="guide-link">
        <p>Đã có tài khoản?</p>
        <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại trang đăng nhập</a>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>