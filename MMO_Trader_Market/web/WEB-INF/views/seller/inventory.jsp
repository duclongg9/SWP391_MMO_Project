<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Cập nhật kho - Quản lý cửa hàng");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sinh credential ảo</h2>
            <p class="panel__subtitle">Tạo nhanh dữ liệu bàn giao để kiểm thử luồng mua hàng trước khi kết nối hệ thống thực tế.</p>
        </div>
        <div class="panel__body">
            <c:if test="${not empty flashSuccess}">
                <div class="alert alert--success" role="status">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="alert alert--danger" role="alert">${flashError}</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/seller/credentials/generate"
                  style="display:flex;flex-direction:column;gap:12px;max-width:560px;">
                <p class="text-muted" style="margin:0;">
                    Nhấn nút bên dưới để hệ thống sinh credential ảo cho <strong>toàn bộ sản phẩm</strong> và các biến thể
                    đã cấu hình tồn kho trong cơ sở dữ liệu. Mỗi SKU sẽ được bổ sung đủ số lượng theo tồn kho hiện tại.
                </p>
                <div>
                    <button type="submit" class="button button--primary">Sinh credential cho toàn bộ sản phẩm</button>
                </div>
            </form>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Tồn kho tổng quan</h2>
            <p class="panel__subtitle">Dữ liệu mô phỏng phục vụ giao diện quản lý, sẽ kết nối với API thực tế trong giai đoạn kế tiếp.</p>
        </div>
        <div class="panel__body">
            <table class="table table--interactive">
                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Loại</th>
                        <th>Tồn kho</th>
                        <th>Đã bán</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Gmail Doanh nghiệp 50GB</td>
                        <td>Email</td>
                        <td>19</td>
                        <td>128</td>
                        <td><span class="badge">Available</span></td>
                    </tr>
                    <tr>
                        <td>Tài khoản TikTok Pro</td>
                        <td>Mạng xã hội</td>
                        <td>40</td>
                        <td>58</td>
                        <td><span class="badge">Available</span></td>
                    </tr>
                    <tr>
                        <td>Valorant VP (top-up)</td>
                        <td>Game</td>
                        <td>100</td>
                        <td>121</td>
                        <td><span class="badge badge--ghost">Pending</span></td>
                    </tr>
                    <tr>
                        <td>Canva Pro chính chủ</td>
                        <td>Phần mềm</td>
                        <td>60</td>
                        <td>187</td>
                        <td><span class="badge">Available</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Lịch sử điều chỉnh gần đây</h2>
        </div>
        <div class="panel__body">
            <ul class="guide-list">
                <li>29/01: +5 tồn kho cho Spotify Premium (restock thủ công).</li>
                <li>26/01: -1 tồn kho sau đơn hàng #5002 (Disputed).</li>
                <li>20/01: -1 tồn kho sau đơn hàng #5001 (Completed).</li>
            </ul>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
