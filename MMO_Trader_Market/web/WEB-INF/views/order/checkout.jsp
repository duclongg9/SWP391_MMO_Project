<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<%--
    Bước checkout đầu tiên: OrderController truyền attribute "product" (model.Products) để preview thông tin,
    đồng thời có thể gửi "error" nếu tham số không hợp lệ. Khi người dùng submit form, dữ liệu được POST
    lên controller/phần xử lý tiếp theo để tạo đơn và gọi OrderService.placeOrderPending.
--%>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bước 1: Xác nhận thông tin sản phẩm</h2>
            <span class="panel__tag">Checkout 2 bước</span>
        </div>
        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error" role="alert"><c:out value="${error}" /></div>
            </c:if>
            <c:choose>
                <c:when test="${not empty product}">
                    <div class="stat-card" style="margin-bottom: 1.5rem;">
                        <div class="icon icon--accent">🛒</div>
                        <div>
                            <p class="stat-card__label">Sản phẩm chọn mua</p>
                            <p class="stat-card__value"><c:out value="${product.name}" /></p>
                            <p class="profile-card__note">
                                Giá bán:
                                <strong>
                                    <fmt:formatNumber value="${product.price}" type="number" minFractionDigits="0"
                                                     maxFractionDigits="0" /> đ
                                </strong>
                            </p>
                        </div>
                    </div>
                    <ol class="guide__steps" style="margin-bottom: 1.5rem;">
                        <li>Kiểm tra lại mô tả sản phẩm và điều kiện bàn giao.</li>
                        <li>Nhập email nhận sản phẩm và chọn phương thức thanh toán.</li>
                        <li>Hoàn tất thanh toán để nhận key/đường dẫn ngay lập tức.</li>
                    </ol>
                    <form class="form" method="post" action="${pageContext.request.contextPath}/orders/buy">
                        <input type="hidden" name="productId" value="${product.encodedId}">
                        <div class="form__group" style="margin-bottom: 1rem;">
                            <label class="form__label" for="buyerEmail">Email nhận sản phẩm</label>
                            <input class="form__input" type="email" id="buyerEmail" name="buyerEmail"
                                   placeholder="buyer@example.com" required>
                        </div>
                        <div class="form__group" style="margin-bottom: 1rem;">
                            <label class="form__label" for="paymentMethod">Phương thức thanh toán</label>
                            <select class="form__input" id="paymentMethod" name="paymentMethod" required>
                                <option value="">-- Chọn phương thức --</option>
                                <option value="Ví điện tử">Ví điện tử</option>
                                <option value="Chuyển khoản ngân hàng">Chuyển khoản ngân hàng</option>
                                <option value="Thẻ nội địa">Thẻ nội địa</option>
                            </select>
                        </div>
                        <p class="profile-card__note" style="margin-bottom: 1.5rem;">
                            Bằng việc tiếp tục, bạn xác nhận đã đọc quy định bảo vệ người mua và sẵn sàng thanh toán.
                        </p>
                        <div style="display: flex; gap: 0.75rem;">
                            <a class="button button--ghost" href="${pageContext.request.contextPath}/orders">Trở lại</a>
                            <button class="button button--primary" type="submit">Bước 2: Thanh toán</button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <p>Không tìm thấy thông tin sản phẩm. Vui lòng chọn lại.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
