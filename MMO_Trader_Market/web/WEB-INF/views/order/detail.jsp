<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết đơn hàng #<c:out value="${detail.order.id}" /></h2>
            <span class="${statusClass}"><c:out value="${statusLabel}" /></span>
        </div>
        <div class="panel__body">
            <div class="stat-card" style="margin-bottom: 1rem;">
                <div>
                    <p class="stat-card__label">Sản phẩm</p>
                    <p class="stat-card__value"><c:out value="${detail.order.product.name}" /></p>
                    <p class="profile-card__note">
                        Tổng: <fmt:formatNumber value="${detail.order.totalAmount}" type="number" minFractionDigits="0"/> đ –
                        Số lượng: <strong><c:out value="${detail.order.quantity != null ? detail.order.quantity : 1}" /></strong>
                    </p>
                </div>
            </div>
            <c:choose>
                <c:when test="${not empty detail.credentials}">
                    <h3>Thông tin bàn giao</h3>
                    <ul class="list">
                        <c:forEach var="credential" items="${detail.credentials}">
                            <li><code><c:out value="${credential}" /></code></li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p class="profile-card__note">Đơn hàng chưa sẵn sàng bàn giao. Vui lòng kiểm tra lại sau.</p>
                </c:otherwise>
            </c:choose>
            <p>
                <small>Ngày tạo: <c:out value="${detail.order.createdAt}" /></small>
            </p>
            <a class="button button--ghost" href="${pageContext.request.contextPath}/orders/my">Quay lại danh sách</a>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
