<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="MMO Trader Market - Đăng nhập" />
<c:set var="bodyClass" value="layout layout--auth" />
<c:set var="headerModifier" value="layout__header--auth" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content auth-page">
    <style>
        .verification-modal {
            position: fixed;
            inset: 0;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 16px;
            z-index: 999;
        }

        .verification-modal.is-visible {
            display: flex;
        }

        .verification-modal__backdrop {
            position: absolute;
            inset: 0;
            background: rgba(15, 23, 42, 0.55);
        }

        .verification-modal__dialog {
            position: relative;
            background: #ffffff;
            border-radius: 16px;
            padding: 32px;
            width: min(440px, 100%);
            box-shadow: 0 24px 48px rgba(15, 23, 42, 0.25);
            z-index: 1;
        }

        .verification-modal__close {
            position: absolute;
            top: 12px;
            right: 12px;
            border: none;
            background: transparent;
            font-size: 1.5rem;
            line-height: 1;
            cursor: pointer;
            color: #475569;
        }

        .verification-modal__title {
            margin: 0 0 12px;
            font-size: 1.5rem;
            font-weight: 600;
            color: #0f172a;
        }

        .verification-modal__description {
            margin: 0 0 16px;
            color: #4b5563;
            line-height: 1.5;
        }

        .verification-modal__notice,
        .verification-modal__error,
        .verification-modal__success {
            margin-bottom: 16px;
            padding: 12px 14px;
            border-radius: 10px;
            font-size: 0.95rem;
        }

        .verification-modal__notice {
            background: #eff6ff;
            color: #1d4ed8;
        }

        .verification-modal__error {
            background: #fee2e2;
            color: #b91c1c;
        }

        .verification-modal__success {
            background: #dcfce7;
            color: #15803d;
        }

        .verification-modal__email {
            margin: 0 0 8px;
            color: #0f172a;
            font-weight: 600;
        }

        .verification-modal__form {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .verification-modal__field label {
            display: block;
            font-weight: 600;
            color: #0f172a;
            margin-bottom: 6px;
        }

        .verification-modal__input {
            width: 100%;
            padding: 12px 14px;
            border-radius: 10px;
            border: 1px solid #cbd5f5;
            font-size: 1rem;
            color: #0f172a;
        }

        .verification-modal__input:focus {
            outline: none;
            border-color: #6366f1;
            box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
        }

        .verification-modal__actions {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
            margin-top: 8px;
            flex-wrap: wrap;
        }

        .verification-modal__actions .button {
            flex: 1;
            min-width: 140px;
        }

        @media (max-width: 480px) {
            .verification-modal__dialog {
                padding: 24px;
            }

            .verification-modal__actions {
                flex-direction: column;
            }

            .verification-modal__actions .button {
                width: 100%;
            }
        }
    </style>
    <div class="auth-page__inner">
        <header class="auth-page__header">
            <h1>Đăng nhập</h1>
            <p>Đăng nhập để quản lý giao dịch của bạn trên MMO Trader Market</p>
        </header>
        <c:if test="${not empty error}">
            <div class="alert alert--error auth-page__alert"><c:out value="${error}" /></div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert--success auth-page__alert"><c:out value="${success}" /></div>
        </c:if>
        <form method="post" action="<c:url value='/auth' />" class="form-card auth-page__form">
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
                           <span class="form-card__option-text">Ghi nhớ tài khoản</span>
                    </label>
                    <a class="form-card__link" href="<c:url value='/forgot-password' />">Quên mật khẩu?</a>
            </div>

            <button class="button button--primary" type="submit">Đăng nhập</button>
            <div class="form-card__actions-row">
                <a class="button button--secondary" href="<c:url value='/auth/google' />">Đăng nhập bằng Google</a>
                <a class="button button--ghost" href="<c:url value='/register' />">Đăng ký ngay</a>
            </div>
        </form>
    </div>
    <div class="verification-modal${showVerificationModal ? ' is-visible' : ''}" id="emailVerificationModal">
        <div class="verification-modal__backdrop" data-modal-close></div>
        <div class="verification-modal__dialog" role="dialog" aria-modal="true" aria-labelledby="verificationModalTitle">
            <button type="button" class="verification-modal__close" data-modal-close aria-label="Đóng">&times;</button>
            <h2 class="verification-modal__title" id="verificationModalTitle">Xác thực email</h2>
            <p class="verification-modal__description">Nhập mã xác thực đã được gửi đến hộp thư của bạn để kích hoạt tài khoản.</p>
            <c:if test="${not empty verificationNotice}">
                <div class="verification-modal__notice"><c:out value="${verificationNotice}" /></div>
            </c:if>
            <c:if test="${not empty verificationError}">
                <div class="verification-modal__error"><c:out value="${verificationError}" /></div>
            </c:if>
            <c:if test="${not empty verificationSuccessMessage}">
                <div class="verification-modal__success"><c:out value="${verificationSuccessMessage}" /></div>
            </c:if>
            <p class="verification-modal__email">Email: <strong><c:out value="${verificationEmail}" /></strong></p>
            <form method="post" action="<c:url value='/verify-email' />" class="verification-modal__form">
                <input type="hidden" name="email" value="<c:out value='${verificationEmail}' />">
                <div class="verification-modal__field">
                    <label for="verificationCode">Mã xác thực</label>
                    <input class="verification-modal__input" id="verificationCode" name="verificationCode" type="text"
                           value="<c:out value='${enteredVerificationCode}' />"
                           placeholder="Nhập mã 6 chữ số" required>
                </div>
                <div class="verification-modal__actions">
                    <button class="button button--ghost" type="submit" name="action" value="resend" formnovalidate>Gửi lại mã</button>
                    <button class="button button--primary" type="submit">Xác thực</button>
                </div>
            </form>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<script>
    (function () {
        const modal = document.getElementById('emailVerificationModal');
        if (!modal) {
            return;
        }
        const closeButtons = modal.querySelectorAll('[data-modal-close]');
        closeButtons.forEach(function (btn) {
            btn.addEventListener('click', function () {
                modal.classList.remove('is-visible');
            });
        });

        if (modal.classList.contains('is-visible')) {
            const codeInput = modal.querySelector('#verificationCode');
            if (codeInput) {
                window.requestAnimationFrame(function () {
                    codeInput.focus();
                    codeInput.select();
                });
            }
        }
    })();
</script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
