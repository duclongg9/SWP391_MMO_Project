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
            <h2 class="panel__title">Chi tiết đơn hàng</h2>
            <c:if test="${not empty order}">
                <span class="panel__tag">#<c:out value="${order.id}" /></span>
            </c:if>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty order}">
                    <article class="profile-card" style="margin-bottom: 1.5rem;">
                        <h4>Sản phẩm: <c:out value="${order.product.name}" /></h4>
                        <p class="profile-card__subtitle">Mua bởi: <c:out value="${order.buyerEmail}" /></p>
                        <dl class="profile-card__stats">
                            <div>
                                <dt>Giá</dt>
                                <dd>
                                    <fmt:formatNumber value="${order.product.price}" type="number" minFractionDigits="0"
                                                     maxFractionDigits="0" /> đ
                                </dd>
                            </div>
                            <div>
                                <dt>Thanh toán</dt>
                                <dd><c:out value="${order.paymentMethod}" /></dd>
                            </div>
                            <div>
                                <dt>Trạng thái</dt>
                                <dd>
                                    <span class="${orderStatusClass}">
                                        <c:out value="${orderStatusLabel}" />
                                    </span>
                                </dd>
                            </div>
                            <div>
                                <dt>Thời gian tạo</dt>
                                <dd><c:out value="${orderCreatedAt}" /></dd>
                            </div>
                        </dl>
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
                                    <p class="profile-card__note">Hãy bảo quản thông tin này cẩn thận để tránh lộ tài khoản.</p>
                                </c:when>
                                <c:otherwise>
                                    <p>Đơn hàng đang chờ xử lý. Khi hoàn tất, thông tin bàn giao sẽ hiển thị tại đây và được gửi qua email.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                        <a class="button button--primary" href="${pageContext.request.contextPath}/orders">Quay lại danh sách</a>
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
