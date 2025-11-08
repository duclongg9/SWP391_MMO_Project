<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Kiểm tra email" />
<c:set var="bodyClass" value="layout layout--center" />
<c:set var="headerTitle" value="Kiểm tra hộp thư" />
<c:set var="headerSubtitle" value="Chúng tôi đã gửi hướng dẫn cho bạn" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <section class="notice-card">
        <h2>Vui lòng kiểm tra email</h2>
        <p>
            <c:choose>
                <c:when test="${noticeType == 'error'}">
                    <span class="notice-card__error"><c:out value="${noticeMessage}" /></span>
                </c:when>
                <c:otherwise>
                    <c:out value="${empty noticeMessage ? 'Chúng tôi đã gửi hướng dẫn tới địa chỉ email của bạn.' : noticeMessage}" />
                </c:otherwise>
            </c:choose>
        </p>
        <c:if test="${not empty noticeEmail}">
            <p class="notice-card__email">Email: <strong><c:out value="${noticeEmail}" /></strong></p>
        </c:if>
        <p>Vui lòng kiểm tra hộp thư (bao gồm mục spam) và làm theo hướng dẫn.</p>
        <div class="notice-card__actions">
            <a class="button button--primary" href="<c:url value='/auth' />">Quay lại đăng nhập</a>
            <a class="button button--ghost" href="<c:url value='/forgot-password' />">Gửi lại yêu cầu</a>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<style>
.notice-card {
    max-width: 520px;
    margin: 0 auto;
    padding: 2.5rem;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(15, 23, 42, 0.12);
    text-align: center;
}
.notice-card h2 {
    margin-bottom: 1rem;
}
.notice-card__email {
    margin-top: 0.75rem;
    font-size: 1.05rem;
}
.notice-card__error {
    color: #dc2626;
    font-weight: 600;
}
.notice-card__actions {
    margin-top: 2rem;
    display: flex;
    gap: 1rem;
    justify-content: center;
}
.notice-card__actions .button {
    min-width: 160px;
}
@media (max-width: 480px) {
    .notice-card {
        padding: 2rem 1.5rem;
    }
    .notice-card__actions {
        flex-direction: column;
    }
    .notice-card__actions .button {
        width: 100%;
    }
}
</style>
