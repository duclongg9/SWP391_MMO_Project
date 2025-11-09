<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Xác thực email" />
<c:set var="bodyClass" value="layout layout--auth" />
<c:set var="headerModifier" value="layout__header--auth" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content auth-page">
    <style>
        .form-card__hint {
            margin-top: 8px;
            font-size: 0.95rem;
            color: #0f172a;
        }

        .form-card__link-button {
            background: transparent;
            border: none;
            color: #2563eb;
            cursor: pointer;
            padding: 0;
            font: inherit;
            text-decoration: underline;
        }

        .form-card__link-button:hover,
        .form-card__link-button:focus {
            color: #1d4ed8;
        }
    </style>
    <div class="auth-page__inner">
        <header class="auth-page__header">
            <h1>Xác thực email</h1>
            <p>Nhập mã gồm 6 chữ số đã được gửi tới hộp thư của bạn để kích hoạt tài khoản.</p>
        </header>
        <c:if test="${not empty success}">
            <div class="alert alert--success auth-page__alert"><c:out value="${success}" /></div>
        </c:if>
        <c:if test="${not empty verificationError}">
            <div class="alert alert--error auth-page__alert"><c:out value="${verificationError}" /></div>
        </c:if>
        <c:if test="${not empty verificationNotice}">
            <div class="alert alert--info auth-page__alert"><c:out value="${verificationNotice}" /></div>
        </c:if>
        <form method="post" action="<c:url value='/verify-email' />" class="form-card auth-page__form" id="verificationForm">
            <div class="form-card__field">
                <label class="form-card__label" for="verificationEmail">Email đã đăng ký</label>
                <input class="form-card__input" id="verificationEmail" name="email" type="email"
                       placeholder="example@email.com" value="<c:out value='${verificationEmail}'/>" required>
            </div>
            <div class="form-card__field">
                <label class="form-card__label" for="verificationCode">Mã xác thực</label>
                <input class="form-card__input" id="verificationCode" name="verificationCode" type="text"
                       inputmode="numeric" pattern="[0-9]{6}" maxlength="6"
                       value="<c:out value='${enteredVerificationCode}'/>" placeholder="Nhập mã 6 chữ số" required>
            </div>
            <p class="form-card__hint">
                Hệ thống đã gửi mã! Nếu chưa hãy
                <button class="form-card__link-button" type="submit" name="action" value="resend" formnovalidate>
                    gửi lại
                </button>.
            </p>
            <div class="form-card__actions-row">
                <button class="button button--primary" type="submit">Xác thực</button>
            </div>
        </form>
        <section class="guide-link">
            <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại đăng nhập</a>
        </section>
    </div>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<script>
    (function () {
        const codeInput = document.getElementById('verificationCode');
        if (codeInput) {
            codeInput.focus();
        }
    })();
</script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
