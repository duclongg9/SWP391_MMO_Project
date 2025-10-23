<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bước 2: Thanh toán thành công</h2>
            <span class="panel__tag">Giao key ngay</span>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty order}">
                    <article class="profile-card" style="margin-bottom: 1.5rem;">
                        <h4>Mã đơn hàng #<c:out value="${order.id}" /></h4>
                        <p class="profile-card__subtitle">Thanh toán qua: <c:out value="${order.paymentMethod}" /></p>
                        <dl class="profile-card__stats">
                            <div>
                                <dt>Tên sản phẩm</dt>
                                <dd><c:out value="${order.product.name}" /></dd>
                            </div>
                            <div>
                                <dt>Giá</dt>
                                <dd>
                                    <fmt:formatNumber value="${order.product.price}" type="number" minFractionDigits="0"
                                                     maxFractionDigits="0" /> đ
                                </dd>
                            </div>
                            <div>
                                <dt>Email nhận</dt>
                                <dd><c:out value="${order.buyerEmail}" /></dd>
                            </div>
                            <div>
                                <dt>Trạng thái</dt>
                                <dd>
                                    <span class="${orderStatusClass}">
                                        <c:out value="${orderStatusLabel}" />
                                    </span>
                                </dd>
                            </div>
                        </dl>
                        <p class="profile-card__note">Thời gian tạo: <c:out value="${orderCreatedAt}" /></p>
                    </article>
                    <div class="panel panel--sub" style="margin-bottom: 1.5rem;">
                        <div class="panel__header">
                            <h3 class="panel__title">Thông tin bàn giao</h3>
                        </div>
                        <div class="panel__body">
                            <c:choose>
                                <c:when test="${not empty order.activationCode}">
                                    <p><strong>Activation key:</strong> <code><c:out value="${order.activationCode}" /></code></p>
                                    <c:if test="${not empty order.deliveryLink}">
                                        <p>
                                            <strong>Đường dẫn tải sản phẩm:</strong>
                                            <a href="${fn:escapeXml(order.deliveryLink)}"><c:out value="${order.deliveryLink}" /></a>
                                        </p>
                                    </c:if>
                                    <p class="profile-card__note">Hãy đổi mật khẩu ngay sau khi nhận tài khoản để bảo vệ quyền lợi.</p>
                                </c:when>
                                <c:otherwise>
                                    <p>Đơn hàng đang chờ hệ thống kiểm tra. Chúng tôi sẽ gửi thông tin qua email trong giây lát.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <a class="button button--primary" href="${pageContext.request.contextPath}/orders">Xem danh sách đơn hàng</a>
                        <a class="button button--ghost" href="${pageContext.request.contextPath}/home">Tiếp tục mua sắm</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert--error" role="alert">Không tìm thấy thông tin đơn hàng.</div>
                    <a class="button button--primary" href="${pageContext.request.contextPath}/orders">Quay lại danh sách</a>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
