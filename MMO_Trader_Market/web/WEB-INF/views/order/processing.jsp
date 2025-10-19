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
            <h2 class="panel__title">Trạng thái đơn hàng</h2>
            <span class="panel__tag">Xử lý tự động</span>
        </div>
        <div class="panel__body">
            <c:if test="${not empty orderError}">
                <div class="alert alert--error" role="alert">
                    <c:out value="${orderError}" />
                </div>
                <p class="profile-card__note">Bạn có thể quay lại trang sản phẩm để thử lại giao dịch.</p>
                <a class="button button--ghost" href="${pageContext.request.contextPath}/products">Quay lại sản phẩm</a>
            </c:if>

            <c:if test="${empty orderError && not empty orderPlacement}">
                <div class="stat-card" style="margin-bottom: 1.25rem;">
                    <div class="icon icon--accent">⚙️</div>
                    <div>
                        <p class="stat-card__label">Sản phẩm đang xử lý</p>
                        <p class="stat-card__value"><c:out value="${product.name}" /></p>
                        <p class="profile-card__note">
                            Số lượng: <strong><c:out value="${quantity}" /></strong>
                            – Giá: <strong><fmt:formatNumber value="${product.price}" type="number" minFractionDigits="0"/> đ</strong>
                        </p>
                    </div>
                </div>

                <div class="alert alert--info" role="status" id="pollingIndicator">
                    <span class="badge ${statusClass}" id="orderStatusBadge">
                        <c:out value="${statusLabel}" />
                    </span>
                    <span id="orderStatusMessage">Đơn hàng của bạn đang được xử lý. Trang sẽ cập nhật khi hoàn tất.</span>
                </div>

                <div id="deliveryContainer" class="panel" style="display:none; margin-top:1rem;">
                    <div class="panel__header">
                        <h3 class="panel__title">Thông tin bàn giao</h3>
                    </div>
                    <div class="panel__body">
                        <p><strong>Mã kích hoạt:</strong> <span id="deliveryActivation"></span></p>
                        <p id="deliveryLinkWrapper" style="display:none;">
                            <strong>Đường dẫn tải xuống:</strong> <a id="deliveryLink" href="#" target="_blank" rel="noopener">Mở liên kết</a>
                        </p>
                    </div>
                </div>

                <c:if test="${not empty pollUrl}">
                    <c:url value="${pollUrl}" var="pollEndpoint" />
                    <script>
                        (function () {
                            const pollUrl = '${fn:escapeXml(pollEndpoint)}';
                            if (!pollUrl) {
                                return;
                            }
                        const statusBadge = document.getElementById('orderStatusBadge');
                        const statusMessage = document.getElementById('orderStatusMessage');
                        const deliveryContainer = document.getElementById('deliveryContainer');
                        const activationEl = document.getElementById('deliveryActivation');
                        const deliveryWrapper = document.getElementById('deliveryLinkWrapper');
                        const deliveryLink = document.getElementById('deliveryLink');
                        let stopped = false;

                        async function poll() {
                            if (stopped) {
                                return;
                            }
                            try {
                                const response = await fetch(pollUrl, {credentials: 'same-origin'});
                                if (!response.ok) {
                                    throw new Error('Polling failed with status ' + response.status);
                                }
                                const data = await response.json();
                                statusBadge.textContent = data.friendlyStatus;
                                statusBadge.className = 'badge ' + (data.statusClass || '');
                                if (data.deliverable) {
                                    stopped = true;
                                    statusMessage.textContent = 'Đơn hàng đã hoàn tất! Bạn có thể nhận thông tin bên dưới.';
                                    activationEl.textContent = data.activationCode || '';
                                    if (data.deliveryLink) {
                                        deliveryLink.href = data.deliveryLink;
                                        deliveryWrapper.style.display = 'block';
                                    } else {
                                        deliveryWrapper.style.display = 'none';
                                    }
                                    deliveryContainer.style.display = 'block';
                                } else if (data.status === 'FAILED') {
                                    stopped = true;
                                    statusMessage.textContent = 'Thanh toán không thành công. Vui lòng thử lại hoặc liên hệ hỗ trợ.';
                                }
                            } catch (error) {
                                console.error('Order polling error:', error);
                                statusMessage.textContent = 'Không thể cập nhật trạng thái đơn hàng. Trang sẽ thử lại sau.';
                            }
                            if (!stopped) {
                                setTimeout(poll, 2000);
                            }
                        }

                            setTimeout(poll, 1500);
                        })();
                    </script>
                </c:if>
            </c:if>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
