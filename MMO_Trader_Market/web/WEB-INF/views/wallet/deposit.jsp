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
                    <p class="topup__intro">Vui lòng nhập số tiền muốn nạp (đơn vị VNĐ). Bạn sẽ được chuyển tới cổng thanh toán VNPAY và số dư ví cập nhật ngay khi IPN thành công.</p>

                    <div class="topup__meta" role="list">
                        <span class="topup__meta-item" role="listitem"><i class="fas fa-clock"></i> Liên kết hết hạn sau 15 phút</span>
                        <span class="topup__meta-item" role="listitem"><i class="fas fa-shield-alt"></i> Giao dịch bảo mật bởi VNPAY</span>
                        <span class="topup__meta-item" role="listitem"><i class="fas fa-receipt"></i> Có hoá đơn trong lịch sử ví</span>
                    </div>

                    <form action="${pageContext.request.contextPath}/payment" method="post" class="form topup__form">
                        <div class="form__group">
                            <label for="amount" class="form__label">Số tiền</label>
                            <input type="number" id="amount" name="amount" class="form-control" min="1000" max="50000000" step="1000" required>
                            <small class="form__hint">Tối thiểu 1.000 VNĐ và tối đa 50.000.000 VNĐ cho mỗi giao dịch.</small>
                        </div>

                        <div class="form__group">
                            <label for="note" class="form__label">Ghi chú (tuỳ chọn)</label>
                            <textarea id="note" name="note" class="form-control" rows="3" maxlength="120"></textarea>
                            <small class="form__hint">Ghi chú giúp bạn nhận diện giao dịch (tối đa 120 ký tự).</small>
                        </div>

                        <div class="form__group form__actions">
                            <!-- Button bình thường, submit form -->
                            <button type="submit" class="btn btn--primary">Tạo yêu cầu nạp</button>
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
