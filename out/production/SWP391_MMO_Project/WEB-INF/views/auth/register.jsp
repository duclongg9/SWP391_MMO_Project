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
                           <a href="#" class="terms-link" id="termsLink">Điều khoản sử dụng Tap Hóa MMO</a>
                       </label>
                </div>
                <button class="button button--primary" type="submit">Đăng ký</button>
                <div class="form-card__actions-row">
                    <a class="button button--ghost" href="<c:url value='/auth' />">Quay lại trang đăng nhập</a>
                    <!--<a class="button button--secondary" href="<c:url value='/auth/google' />">Đăng ký bằng Google</a>-->
            </div>
        </form>
        <div class="terms-modal" id="termsModal" aria-hidden="true" role="dialog" aria-labelledby="termsTitle">
            <div class="terms-modal__backdrop" data-dismiss="termsModal"></div>
            <div class="terms-modal__content">
                <header class="terms-modal__header">
                    <h2 class="terms-modal__title" id="termsTitle">Điều khoản sử dụng Tap Hóa MMO</h2>
                    <button type="button" class="terms-modal__close" data-dismiss="termsModal" aria-label="Đóng">×</button>
                </header>
                <div class="terms-modal__body">
                    <p>Trước khi đăng ký, vui lòng đọc nhanh các lưu ý quan trọng để đảm bảo quyền lợi của bạn:</p>
                    <ul>
                        <li>Tuân thủ danh sách mặt hàng cấm và chính sách bảo hành trong <a href="<c:url value='/faq'/>" target="_blank" rel="noopener">FAQ</a>.</li>
                        <li>MMO Trader Market giữ tiền 3 ngày để bảo vệ người mua; khiếu nại có thể được mở trong thời gian này.</li>
                        <li>Người bán cần đảm bảo nội dung mô tả minh bạch, không spam và không vi phạm pháp luật.</li>
                        <li>Email xác thực chỉ có hiệu lực ngắn hạn; không chia sẻ mã cho bất kỳ ai.</li>
                        <li>Vi phạm chính sách có thể dẫn đến khóa tài khoản mà không cần thông báo trước.</li>
                    </ul>
                    <p>Chi tiết đầy đủ được trình bày trong trang FAQ. Bạn có thể cuộn để đọc thêm nội dung tóm tắt này.</p>
                </div>
                <footer class="terms-modal__footer">
                    <a class="button button--ghost" href="<c:url value='/faq'/>" target="_blank" rel="noopener">Xem FAQ đầy đủ</a>
                    <button type="button" class="button button--primary" data-dismiss="termsModal">Đã hiểu</button>
                </footer>
            </div>
        </div>
    </div>
</main>

<script>
    (function () {
        const termsLink = document.getElementById('termsLink');
        const modal = document.getElementById('termsModal');
        const dismissTriggers = modal ? modal.querySelectorAll('[data-dismiss="termsModal"]') : [];

        const openModal = (event) => {
            event.preventDefault();
            if (!modal) return;
            modal.classList.add('terms-modal--open');
            modal.setAttribute('aria-hidden', 'false');
        };

        const closeModal = () => {
            if (!modal) return;
            modal.classList.remove('terms-modal--open');
            modal.setAttribute('aria-hidden', 'true');
        };

        if (termsLink) {
            termsLink.addEventListener('click', openModal);
        }

        dismissTriggers.forEach((trigger) => {
            trigger.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    })();
</script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
