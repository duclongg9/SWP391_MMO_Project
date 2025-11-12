<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("pageTitle", "Nạp tiền ví qua VNPAY");
    request.setAttribute("bodyClass", "layout");
    java.util.List<String> extraStylesheets = new java.util.ArrayList<>();
    extraStylesheets.add("https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css");
    request.setAttribute("extraStylesheets", extraStylesheets);
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h1 class="panel__title">Nạp tiền ví qua VNPAY</h1>
        </div>
        <div class="panel__body">
            <div class="topup topup--with-guide">
                <div class="topup__form-card">
                    <!-- Ghi chú UI: bố cục form được cố định chiều rộng để người dùng tập trung vào thao tác nhập liệu. -->
                    <header class="topup__form-header">
                        <p class="topup__intro">Vui lòng nhập số tiền muốn nạp (đơn vị VNĐ). Bạn sẽ được chuyển tới cổng thanh toán VNPAY và số dư ví cập nhật ngay khi IPN thành công.</p>

                        <div class="topup__meta" role="list">
                            <span class="topup__meta-item" role="listitem"><i class="fas fa-clock"></i> Liên kết hết hạn sau 15 phút</span>
                            <span class="topup__meta-item" role="listitem"><i class="fas fa-shield-alt"></i> Giao dịch bảo mật bởi VNPAY</span>
                            <span class="topup__meta-item" role="listitem"><i class="fas fa-receipt"></i> Có hoá đơn trong lịch sử ví</span>
                        </div>
                    </header>

                    <section class="topup__limits" aria-label="Giới hạn giao dịch">
                        <h2 class="topup__limits-title">Giới hạn mỗi giao dịch</h2>
                        <dl class="topup__limits-list">
                            <div class="topup__limit">
                                <dt>Tối thiểu</dt>
                                <dd>1.000 VNĐ</dd>
                            </div>
                            <div class="topup__limit">
                                <dt>Tối đa</dt>
                                <dd>50.000.000 VNĐ</dd>
                            </div>
                            <div class="topup__limit">
                                <dt>Thời gian xử lý</dt>
                                <dd>Tức thì sau khi IPN thành công</dd>
                            </div>
                        </dl>
                    </section>

                    <form action="${pageContext.request.contextPath}/payment" method="post" class="topup__form" novalidate>
                        <div class="topup__field">
                            <label for="amount-display" class="topup__label">Số tiền</label>
                            <div class="topup__control">
                                <span class="topup__control-prefix" aria-hidden="true">VNĐ</span>
                                <input type="text" id="amount-display" class="topup__input" inputmode="numeric" autocomplete="off" required aria-describedby="amount-hint">
                                <input type="hidden" id="amount" name="amount">
                            </div>
                            <p id="amount-hint" class="topup__hint">Tối thiểu 1.000 VNĐ và tối đa 50.000.000 VNĐ cho mỗi giao dịch.</p>
                        </div>

                        <div class="topup__field">
                            <label for="note" class="topup__label">Ghi chú (tuỳ chọn)</label>
                            <div class="topup__control topup__control--textarea">
                                <textarea id="note" name="note" class="topup__input topup__input--textarea" rows="3" maxlength="120" placeholder="Ví dụ: Nạp cho sự kiện tháng 7"></textarea>
                            </div>
                            <p class="topup__hint">Ghi chú giúp bạn nhận diện giao dịch (tối đa 120 ký tự).</p>
                        </div>

                        <div class="topup__actions">
                            <button type="submit" class="btn btn--primary topup__submit">
                                <span class="btn__text">Tạo yêu cầu nạp</span>
                            </button>
                            <a href="${pageContext.request.contextPath}/wallet" class="btn btn--ghost">Quay lại ví</a>
                        </div>
                    </form>

                    <div id="topup-alert" class="alert" role="alert" aria-live="assertive" style="display:none;" tabindex="-1"></div>
                </div>

                <aside class="topup__guide" aria-label="Hướng dẫn nạp tiền VNPAY">
                    <div class="topup__guide-card">
                        <h2 class="topup__guide-title">Cách nạp tiền</h2>
                        <ol class="topup__steps">
                            <li>Nhập số tiền bạn muốn nạp và (tuỳ chọn) ghi chú để dễ nhận biết giao dịch.</li>
                            <li>Nhấn <strong>Tạo yêu cầu nạp</strong> để hệ thống sinh liên kết thanh toán.</li>
                            <li>Kiểm tra thông tin trên cổng VNPAY, quét mã QR hoặc chọn ứng dụng ví ngân hàng để hoàn tất thanh toán.</li>
                            <li>Sau khi VNPAY gửi IPN thành công, số dư ví sẽ tự động cộng thêm và hiển thị trong lịch sử.</li>
                        </ol>

                        <div class="topup__callout" role="note">
                            <h3>Thông tin điền trên trang VNPAY</h3>
                            <ul>
                                <li><strong>Order ID</strong>: Mã hệ thống tự sinh, giữ nguyên để đối soát.</li>
                                <li><strong>Amount</strong>: Chính là số tiền bạn vừa nhập ở bước trên.</li>
                                <li><strong>Order Description</strong>: "Nap vi VNPAY" kèm mã giao dịch, nên đối chiếu trước khi thanh toán.</li>
                            </ul>
                            <p class="topup__callout-note">Nếu trình duyệt chặn cửa sổ mới, hãy bật cho phép hoặc sao chép liên kết thanh toán được hiển thị.</p>
                        </div>

                        <details class="topup__details">
                            <summary>Tự tạo mã QR VNPAY?</summary>
                            <p>Đối với merchant muốn in sẵn QR, đăng nhập Cổng quản trị VNPAY, vào mục <em>Quản lý QR</em> &rarr; <em>Tạo QR</em>, nhập <strong>Terminal ID</strong> (mã điểm bán) và các trường Amount, Order Info giống hệ thống. Bạn có thể tham khảo hướng dẫn chi tiết trong tài liệu nội bộ “Hướng dẫn tạo QR VNPAY”.</p>
                            <ol>
                                <li>Chọn loại QR tĩnh hoặc động theo nhu cầu.</li>
                                <li>Nhập đúng TMN Code/Terminal do VNPAY cấp cho tài khoản của bạn.</li>
                                <li>Điền Amount, Order Info, Expire Date tương tự giao dịch trên hệ thống để đối soát dễ dàng.</li>
                                <li>Tải hình ảnh QR về và sử dụng cho kênh chăm sóc khách hàng hoặc hướng dẫn thanh toán.</li>
                            </ol>
                        </details>
                    </div>
                </aside>
            </div>
        </div>
    </section>
</main>



<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
    // JavaScript: Định dạng số tiền theo từng nhóm nghìn và kiểm tra giới hạn trước khi gửi form.
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.querySelector('.topup__form');
        const amountDisplay = document.getElementById('amount-display');
        const amountHidden = document.getElementById('amount');
        const alertBox = document.getElementById('topup-alert');
        const MIN_AMOUNT = 1000;
        const MAX_AMOUNT = 50000000;

        const formatCurrency = (value) => {
            const numericValue = value.replace(/\D/g, '');

            if (!numericValue) {
                amountHidden.value = '';
                return '';
            }

            const parsed = parseInt(numericValue, 10);
            amountHidden.value = parsed;
            return parsed.toLocaleString('vi-VN');
        };

        const showAlert = (message) => {
            alertBox.textContent = message;
            alertBox.style.display = 'block';
            alertBox.focus();
            amountDisplay.setAttribute('aria-invalid', 'true');
        };

        const hideAlert = () => {
            alertBox.textContent = '';
            alertBox.style.display = 'none';
            amountDisplay.removeAttribute('aria-invalid');
        };

        const validateAmount = () => {
            const currentValue = amountHidden.value ? parseInt(amountHidden.value, 10) : NaN;

            if (!amountHidden.value) {
                showAlert('Vui lòng nhập số tiền cần nạp.');
                return false;
            }

            if (Number.isNaN(currentValue)) {
                showAlert('Số tiền không hợp lệ, vui lòng kiểm tra lại.');
                return false;
            }

            if (currentValue < MIN_AMOUNT) {
                showAlert(`Số tiền tối thiểu là ${MIN_AMOUNT.toLocaleString('vi-VN')} VNĐ.`);
                return false;
            }

            if (currentValue > MAX_AMOUNT) {
                showAlert(`Số tiền tối đa là ${MAX_AMOUNT.toLocaleString('vi-VN')} VNĐ.`);
                return false;
            }

            if (currentValue % 1000 !== 0) {
                showAlert('Số tiền phải là bội số của 1.000 VNĐ.');
                return false;
            }

            hideAlert();
            return true;
        };

        amountDisplay.addEventListener('input', (event) => {
            const formatted = formatCurrency(event.target.value);
            event.target.value = formatted;
            hideAlert();
        });

        amountDisplay.addEventListener('blur', () => {
            amountDisplay.value = formatCurrency(amountDisplay.value);
            validateAmount();
        });

        form.addEventListener('submit', (event) => {
            amountDisplay.value = formatCurrency(amountDisplay.value);

            if (!validateAmount()) {
                event.preventDefault();
            }
        });
    });
</script>
