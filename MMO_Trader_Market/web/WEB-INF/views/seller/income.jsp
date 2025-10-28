<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Thu nhập - Quản lý cửa hàng");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Thu nhập & doanh thu");
    request.setAttribute("headerSubtitle", "Nắm bắt sức khỏe tài chính của cửa hàng trong nháy mắt");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Hiệu suất tháng này</h2>
        </div>
        <div class="panel__body dashboard__row">
            <article class="stat-card">
                <div class="icon icon--primary">💵</div>
                <div>
                    <p class="stat-card__label">Doanh thu đã giải ngân</p>
                    <p class="stat-card__value">68.250.000 đ</p>
                </div>
            </article>
            <article class="stat-card">
                <div class="icon icon--accent">📈</div>
                <div>
                    <p class="stat-card__label">Tăng trưởng</p>
                    <p class="stat-card__value">+18% so với tháng trước</p>
                </div>
            </article>
            <article class="stat-card">
                <div class="icon icon--muted">⏱️</div>
                <div>
                    <p class="stat-card__label">Đơn chờ giải ngân</p>
                    <p class="stat-card__value">4</p>
                </div>
            </article>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết dòng tiền</h2>
            <p class="panel__subtitle">Số liệu minh hoạ dùng để mô tả giao diện báo cáo thu nhập.</p>
        </div>
        <div class="panel__body">
            <table class="table">
                <thead>
                    <tr>
                        <th>Ngày</th>
                        <th>Diễn giải</th>
                        <th>Số tiền</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>05/02</td>
                        <td>Giải ngân đơn #5012</td>
                        <td>+12.500.000 đ</td>
                        <td><span class="badge">Đã nhận</span></td>
                    </tr>
                    <tr>
                        <td>04/02</td>
                        <td>Rút tiền về VCB</td>
                        <td>-8.000.000 đ</td>
                        <td><span class="badge badge--ghost">Đang xử lý</span></td>
                    </tr>
                    <tr>
                        <td>02/02</td>
                        <td>Giải ngân đơn #5008</td>
                        <td>+6.750.000 đ</td>
                        <td><span class="badge">Đã nhận</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Gợi ý tối ưu doanh thu</h2>
        </div>
        <div class="panel__body">
            <ul class="guide-list">
                <li>Kích hoạt mã giảm giá cho nhóm khách hàng thân thiết.</li>
                <li>Theo dõi đơn chờ giải ngân và xử lý tranh chấp kịp thời.</li>
                <li>Đăng thêm sản phẩm hot theo mùa (game, dịch vụ streaming).</li>
            </ul>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
