<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "Tạo sản phẩm mới - Quản lý cửa hàng");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Tạo sản phẩm mới");
    request.setAttribute("headerSubtitle", "Điền thông tin cơ bản để đưa sản phẩm lên sàn");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Thông tin sản phẩm</h2>
            <p class="panel__subtitle">Bổ sung mô tả chi tiết, giá bán và ảnh minh hoạ trước khi đăng bán.</p>
        </div>
        <div class="form-grid">
            <div class="form-field">
                <label class="form-label" for="product-name">Tên sản phẩm</label>
                <input class="form-input" type="text" id="product-name" placeholder="Ví dụ: Gmail Doanh nghiệp 100GB" disabled>
                <p class="form-note">Tính năng nhập liệu sẽ được kích hoạt trong các phiên bản tiếp theo.</p>
            </div>
            <div class="form-field">
                <label class="form-label" for="product-type">Loại sản phẩm</label>
                <input class="form-input" type="text" id="product-type" value="Email / Gmail" disabled>
            </div>
            <div class="form-field">
                <label class="form-label" for="product-price">Giá bán (VNĐ)</label>
                <input class="form-input" type="number" id="product-price" value="250000" disabled>
            </div>
            <div class="form-field">
                <label class="form-label" for="product-gallery">Ảnh minh hoạ</label>
                <input class="form-input" type="text" id="product-gallery" placeholder="Tải lên nhiều ảnh" disabled>
            </div>
        </div>
        <div class="panel__footer">
            <button class="button button--primary" type="button" disabled>
                Lưu bản nháp
            </button>
            <button class="button button--ghost" type="button" disabled>
                Gửi duyệt sản phẩm
            </button>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Mẹo đăng sản phẩm hiệu quả</h2>
        </div>
        <ul class="guide-list">
            <li>Chuẩn bị ít nhất 3 hình ảnh minh hoạ chất lượng cao.</li>
            <li>Nhập mô tả rõ ràng về quyền lợi bàn giao và chính sách bảo hành.</li>
            <li>Cập nhật tồn kho thực tế để hệ thống tự động khóa đơn khi bán hết.</li>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
