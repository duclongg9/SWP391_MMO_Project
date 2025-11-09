<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("pageTitle", "Nạp tiền ví qua VNPAY");
    request.setAttribute("bodyClass", "layout");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h1 class="panel__title">Nạp tiền ví qua VNPAY</h1>
        </div>
        <div class="panel__body">
            <p>Vui lòng nhập số tiền muốn nạp (đơn vị VNĐ). Hệ thống sẽ chuyển bạn tới cổng thanh toán VNPAY và cập nhật số dư khi nhận được IPN thành công.</p>

            <form id="topup-form" class="form">
                <div class="form__group">
                    <label for="amount" class="form__label">Số tiền</label>
                    <input type="number" id="amount" name="amount" class="form-control" min="1000" step="1000" required aria-describedby="amount-help">
                    <small id="amount-help" class="form__hint">Tối thiểu 1.000 VNĐ. VNPAY sẽ quy đổi sang đơn vị đồng x100.</small>
                </div>

                <div class="form__group">
                    <label for="note" class="form__label">Ghi chú (tuỳ chọn)</label>
                    <textarea id="note" name="note" class="form-control" rows="3" maxlength="120" placeholder="Thông tin giúp bạn ghi nhớ giao dịch"></textarea>
                </div>

                <div class="form__group">
                    <button type="submit" class="btn btn--primary">Tạo yêu cầu nạp</button>
                    <a href="${pageContext.request.contextPath}/wallet" class="btn btn--ghost">Quay lại ví</a>
                </div>
            </form>

            <div id="topup-alert" class="alert" role="alert" style="display:none;"></div>
        </div>
    </section>
</main>

<script>
    (function () {
        const form = document.getElementById('topup-form');
        const alertBox = document.getElementById('topup-alert');

        function showAlert(message, type) {
            alertBox.textContent = message;
            alertBox.className = 'alert alert--' + (type === 'error' ? 'danger' : 'success');
            alertBox.style.display = 'block';
        }

        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            alertBox.style.display = 'none';

            const amountInput = document.getElementById('amount');
            const noteInput = document.getElementById('note');

            const amountValue = parseInt(amountInput.value, 10);
            if (Number.isNaN(amountValue) || amountValue <= 0) {
                showAlert('Số tiền không hợp lệ.', 'error');
                return;
            }

            const payload = { amount: amountValue };
            const note = noteInput.value.trim();
            if (note.length > 0) {
                payload.note = note;
            }

            try {
                const response = await fetch('${pageContext.request.contextPath}/wallet/deposit/create', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const data = await response.json();
                if (!response.ok) {
                    throw new Error(data.error || 'Không thể tạo yêu cầu nạp.');
                }
                if (data.paymentUrl) {
                    window.location.href = data.paymentUrl;
                } else {
                    showAlert('Không nhận được đường dẫn thanh toán.', 'error');
                }
            } catch (error) {
                showAlert(error.message, 'error');
            }
        });
    })();
</script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
