<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<%--
    Trang chi tiết đơn hàng.
    Attribute do OrderController#showOrderDetail cung cấp:
    - order: model.Orders lấy từ DB -> hiển thị tổng tiền, trạng thái, số lượng.
    - product: model.Products để cho biết thông tin mô tả sản phẩm.
    - credentials: List<String> chỉ được populate khi đơn Completed (worker đã trừ tiền + gán mã).
    - statusLabel: chuỗi tiếng Việt do OrderService map từ enum.
--%>
<main class="layout__content">
    <c:if test="${showProcessingModal}">
        <c:url var="orderDetailUrl" value="/orders/detail/${orderToken}" />
        <style>
            /* Modal thông báo xử lý đơn hàng */
            .order-modal {
                position: fixed;
                inset: 0;
                display: none;
                align-items: center;
                justify-content: center;
                z-index: 1000;
            }

            .order-modal.is-visible {
                display: flex;
            }

            .order-modal__backdrop {
                position: absolute;
                inset: 0;
                background: rgba(15, 23, 42, 0.55);
            }

            .order-modal__dialog {
                position: relative;
                background: #fff;
                border-radius: 12px;
                padding: 2rem;
                max-width: 440px;
                width: calc(100% - 2rem);
                box-shadow: 0 20px 45px rgba(15, 23, 42, 0.25);
                z-index: 1;
            }

            .order-modal__dialog h3 {
                margin-top: 0;
                margin-bottom: 0.75rem;
                font-size: 1.25rem;
                font-weight: 600;
            }

            .order-modal__dialog p {
                margin: 0 0 1.5rem 0;
                color: #475569;
                line-height: 1.6;
            }

            .order-modal__actions {
                display: flex;
                justify-content: flex-end;
                gap: 0.75rem;
            }
        </style>
        <div class="order-modal" id="orderProcessingModal">
            <div class="order-modal__backdrop" id="orderProcessingBackdrop"></div>
            <div class="order-modal__dialog" role="dialog" aria-modal="true"
                 aria-labelledby="orderProcessingTitle" aria-describedby="orderProcessingMessage">
                <h3 id="orderProcessingTitle">Đơn hàng đang được xử lý</h3>
                <p id="orderProcessingMessage">
                    Hệ thống đang khóa ví, kiểm tra tồn kho, nạp credential bàn giao và ghi nhận giao dịch trừ tiền.
                    Vui lòng bấm "Xem chi tiết đơn hàng" để theo dõi trạng thái cập nhật theo thời gian thực cùng thông tin giao dịch ví.
                </p>
                <div class="order-modal__actions">
                    <button type="button" class="button button--ghost" id="orderProcessingLater">Ở lại trang</button>
                    <button type="button" class="button button--primary" id="orderProcessingGo"
                            data-target="${orderDetailUrl}">Xem chi tiết đơn hàng</button>
                </div>
            </div>
        </div>
        <script>
            // Hiển thị pop-up nhắc người mua chuyển sang trang chi tiết đơn hàng để theo dõi tiến độ.
            document.addEventListener('DOMContentLoaded', function () {
                const modal = document.getElementById('orderProcessingModal');
                if (!modal) {
                    return;
                }
                const backdrop = document.getElementById('orderProcessingBackdrop');
                const stayButton = document.getElementById('orderProcessingLater');
                const goButton = document.getElementById('orderProcessingGo');

                function closeModal() {
                    modal.classList.remove('is-visible');
                }

                backdrop.addEventListener('click', closeModal);
                stayButton.addEventListener('click', closeModal);
                goButton.addEventListener('click', function () {
                    const target = goButton.getAttribute('data-target');
                    if (target) {
                        window.location.href = target;
                    }
                });

                modal.classList.add('is-visible');
            });
        </script>
    </c:if>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết đơn hàng #<c:out value="${order.id}" /></h2>
        </div>
        <div class="panel__body">
            <style>
                .order-detail__info-grid {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 2rem;
                }

                .order-detail__info-column {
                    flex: 1 1 260px;
                    min-width: 0;
                }

                .order-detail__info-column--wallet {
                    border-left: 1px solid #e2e8f0;
                    padding-left: 1.5rem;
                }

                .order-detail__info-title {
                    margin: 0 0 1rem 0;
                    font-size: 1.1rem;
                    font-weight: 600;
                    color: #0f172a;
                }

                .order-detail__info-subtitle {
                    margin: 1.75rem 0 0.75rem;
                    font-size: 1rem;
                    font-weight: 600;
                    color: #0f172a;
                }

                .wallet-events {
                    display: flex;
                    flex-direction: column;
                    gap: 1.25rem;
                }

                .wallet-event {
                    display: flex;
                    gap: 1rem;
                    align-items: stretch;
                    padding: 1.25rem;
                    border-radius: 16px;
                    border: 1px solid #e2e8f0;
                    background: #f8fafc;
                    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
                }

                .wallet-event--primary {
                    border-color: #2563eb;
                    box-shadow: 0 16px 28px rgba(37, 99, 235, 0.12);
                    background: linear-gradient(180deg, rgba(37, 99, 235, 0.08), rgba(248, 250, 252, 1));
                }

                .wallet-event__index {
                    width: 2.5rem;
                    height: 2.5rem;
                    border-radius: 50%;
                    background: #e2e8f0;
                    color: #0f172a;
                    font-weight: 700;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    flex-shrink: 0;
                }

                .wallet-event--primary .wallet-event__index {
                    background: #2563eb;
                    color: #fff;
                }

                .wallet-event__body {
                    display: flex;
                    flex-direction: column;
                    gap: 0.75rem;
                    flex: 1;
                    min-width: 0;
                }

                .wallet-event__header {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 0.5rem 1rem;
                    align-items: center;
                }

                .wallet-event__title {
                    font-weight: 600;
                    color: #0f172a;
                    font-size: 1rem;
                }

                .wallet-event__time {
                    color: #475569;
                    font-size: 0.92rem;
                }

                .wallet-event__description {
                    color: #334155;
                    line-height: 1.6;
                }

                .wallet-event__meta {
                    display: grid;
                    gap: 0.35rem;
                }

                .wallet-event__meta-row {
                    display: flex;
                    justify-content: space-between;
                    gap: 0.75rem;
                    color: #0f172a;
                    font-size: 0.95rem;
                }

                .wallet-event__meta-label {
                    font-weight: 600;
                }

                .wallet-event__meta-value {
                    color: #1e293b;
                }

                .wallet-events__placeholder,
                .wallet-events__error {
                    padding: 1.25rem;
                    border-radius: 12px;
                    border: 1px dashed #cbd5f5;
                    background: #eef2ff;
                    color: #3730a3;
                }

                @media (max-width: 768px) {
                    .order-detail__info-grid {
                        flex-direction: column;
                    }

                    .order-detail__info-column--wallet {
                        border-left: none;
                        border-top: 1px solid #e2e8f0;
                        padding-left: 0;
                        padding-top: 1.5rem;
                    }

                    .wallet-event {
                        flex-direction: column;
                    }

                    .wallet-event__meta-row {
                        flex-direction: column;
                        align-items: flex-start;
                    }
                }
            </style>
            <div class="grid grid--two-columns">
                <div class="card">
                    <h3 class="card__title">Thông tin đơn</h3>
                    <div class="order-detail__info-grid">
                        <div class="order-detail__info-column">
                            <h4 class="order-detail__info-title">Đơn hàng</h4>
                            <ul class="definition-list">
                                <li><span>Mã đơn:</span> #<c:out value="${order.id}" /></li>
                                <li><span>Sản phẩm:</span> <c:out value="${product.name}" /></li>
                                <c:if test="${not empty order.variantCode}">
                                    <li><span>Biến thể:</span> <c:out value="${order.variantCode}" /></li>
                                </c:if>
                                <c:if test="${order.quantity ne null}">
                                    <li><span>Số lượng:</span> <c:out value="${order.quantity}" /></li>
                                </c:if>
                                <c:if test="${order.unitPrice ne null}">
                                    <li><span>Đơn giá:</span>
                                        <fmt:formatNumber value="${order.unitPrice}" type="currency" currencySymbol="" /> đ
                                    </li>
                                </c:if>
                                <li><span>Tổng tiền:</span>
                                    <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="" /> đ
                                </li>
                                <li><span>Trạng thái:</span> <c:out value="${statusLabel}" /></li>
                                <li><span>Ngày tạo:</span>
                                    <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                </li>
                            </ul>
                        </div>
                        <div class="order-detail__info-column order-detail__info-column--wallet">
                            <h4 class="order-detail__info-title">Giao dịch ví</h4>
                            <c:url var="walletEventsUrl" value="/orders/detail/${orderToken}/wallet-events" />
                            <div class="wallet-events" id="walletEvents" data-endpoint="${walletEventsUrl}">
                                <div class="wallet-events__placeholder">Đang tải dữ liệu giao dịch ví...</div>
                            </div>
                            <c:if test="${not empty paymentTransaction}">
                                <h5 class="order-detail__info-subtitle">Chi tiết giao dịch</h5>
                                <ul class="definition-list">
                                    <li><span>Mã giao dịch ví:</span> #<c:out value="${paymentTransaction.id}" /></li>
                                    <li><span>Loại giao dịch:</span> <c:out value="${paymentTransactionTypeLabel}" /></li>
                                    <li><span>Số tiền trừ:</span>
                                        <c:choose>
                                            <c:when test="${not empty paymentTransactionAmountAbs}">
                                                -<fmt:formatNumber value="${paymentTransactionAmountAbs}" type="currency" currencySymbol="" /> đ
                                            </c:when>
                                            <c:when test="${not empty paymentTransaction.amount}">
                                                <fmt:formatNumber value="${paymentTransaction.amount}" type="currency" currencySymbol="" /> đ
                                            </c:when>
                                            <c:otherwise>
                                                Không xác định
                                            </c:otherwise>
                                        </c:choose>
                                    </li>
                                    <li><span>Số dư trước:</span>
                                        <fmt:formatNumber value="${paymentTransaction.balanceBefore}" type="currency" currencySymbol="" /> đ
                                    </li>
                                    <li><span>Số dư sau:</span>
                                        <fmt:formatNumber value="${paymentTransaction.balanceAfter}" type="currency" currencySymbol="" /> đ
                                    </li>
                                    <li><span>Thời gian thanh toán:</span>
                                        <fmt:formatDate value="${paymentTransaction.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </li>
                                    <c:if test="${not empty paymentTransaction.note}">
                                        <li><span>Ghi chú:</span> <c:out value="${paymentTransaction.note}" /></li>
                                    </c:if>
                                </ul>
                            </c:if>
                        </div>
                    </div>
                </div>
                <div class="card">
                    <h3 class="card__title">Sản phẩm</h3>
                    <p><strong>Tên:</strong> <c:out value="${product.name}" /></p>
                    <p><strong>Mô tả:</strong> <c:out value="${product.description}" /></p>
                    <p><strong>Tồn kho hiện tại:</strong> <c:out value="${product.inventoryCount}" /></p>
                </div>
            </div>
            <div class="panel panel--nested">
                <div class="panel__header">
                    <h3 class="panel__title">Thông tin bàn giao</h3>
                </div>
                <div class="panel__body">
                    <c:if test="${not empty unlockSuccessMessage}">
                        <div class="alert alert--success" role="status" aria-live="polite">
                            <c:out value="${unlockSuccessMessage}" />
                        </div>
                    </c:if>
                    <c:if test="${not empty unlockErrorMessage}">
                        <div class="alert alert--error" role="alert" aria-live="assertive">
                            <c:out value="${unlockErrorMessage}" />
                        </div>
                    </c:if>
                    <style>
                        /* Modal xác nhận mở khóa thông tin bàn giao và ghi log người xem */
                        .unlock-modal {
                            position: fixed;
                            inset: 0;
                            display: none;
                            align-items: center;
                            justify-content: center;
                            z-index: 1100;
                        }

                        .unlock-modal.is-visible {
                            display: flex;
                        }

                        .unlock-modal__backdrop {
                            position: absolute;
                            inset: 0;
                            background: rgba(15, 23, 42, 0.55);
                        }

                        .unlock-modal__dialog {
                            position: relative;
                            background: #fff;
                            border-radius: 12px;
                            padding: 2rem;
                            max-width: 460px;
                            width: calc(100% - 2rem);
                            box-shadow: 0 20px 45px rgba(15, 23, 42, 0.25);
                            z-index: 1;
                        }

                        .unlock-modal__dialog h3 {
                            margin: 0 0 0.75rem 0;
                            font-size: 1.2rem;
                            font-weight: 600;
                        }

                        .unlock-modal__dialog p {
                            margin: 0 0 1.5rem 0;
                            color: #475569;
                            line-height: 1.6;
                        }

                        .unlock-modal__actions {
                            display: flex;
                            justify-content: flex-end;
                            gap: 0.75rem;
                        }

                        .credential-unlock__helper {
                            margin-bottom: 1.5rem;
                            color: #475569;
                            line-height: 1.6;
                        }
                    </style>
                    <%-- Nếu đơn đã hoàn thành, credential được worker mark sold và OrderService nạp kèm để hiển thị. --%>
                    <c:choose>
                        <c:when test="${order.status eq 'Completed'}">
                            <c:choose>
                                <c:when test="${credentialsUnlocked and not empty credentials}">
                                    <ul class="list">
                                        <c:forEach var="cred" items="${credentials}">
                                            <li><c:out value="${cred}" /></li>
                                            </c:forEach>
                                    </ul>
                                </c:when>
                                <c:when test="${credentialsUnlocked and empty credentials}">
                                    <p class="empty">Đơn hàng đã hoàn thành nhưng chưa có dữ liệu bàn giao.</p>
                                </c:when>
                                <c:otherwise>
                                    <p class="credential-unlock__helper">
                                        Vì lý do bảo mật, hệ thống cần bạn xác nhận mở khóa trước khi hiển thị tài khoản
                                        và mật khẩu. Hành động này sẽ được ghi lại để admin có thể kiểm tra lịch sử xem.
                                    </p>
                                    <c:url var="unlockActionUrl" value="/orders/unlock" />
                                    <form id="credentialUnlockForm" method="post" action="${unlockActionUrl}" style="display:none;">
                                        <input type="hidden" name="orderToken" value="${orderToken}" />
                                    </form>
                                    <button type="button" class="button button--primary" id="credentialUnlockTrigger">
                                        Xác nhận mở khóa thông tin bàn giao
                                    </button>
                                    <div class="unlock-modal" id="credentialUnlockModal">
                                        <div class="unlock-modal__backdrop" id="credentialUnlockBackdrop"></div>
                                        <div class="unlock-modal__dialog" role="dialog" aria-modal="true"
                                             aria-labelledby="credentialUnlockTitle" aria-describedby="credentialUnlockMessage">
                                            <h3 id="credentialUnlockTitle">Xác nhận mở khóa</h3>
                                            <p id="credentialUnlockMessage">
                                                Khi mở khóa, hệ thống sẽ lưu lại thời gian và địa chỉ IP của bạn để đối soát.
                                                Vui lòng không chia sẻ thông tin bàn giao cho bên thứ ba nếu chưa có sự đồng ý của shop.
                                            </p>
                                            <div class="unlock-modal__actions">
                                                <button type="button" class="button button--ghost" id="credentialUnlockCancel">Hủy</button>
                                                <button type="button" class="button button--primary" id="credentialUnlockConfirm">Tôi đồng ý</button>
                                            </div>
                                        </div>
                                    </div>
                                    <script>
                                        // Hiển thị modal xác nhận và submit form khi người dùng đồng ý mở khóa.
                                        document.addEventListener('DOMContentLoaded', function () {
                                            const trigger = document.getElementById('credentialUnlockTrigger');
                                            const modal = document.getElementById('credentialUnlockModal');
                                            const backdrop = document.getElementById('credentialUnlockBackdrop');
                                            const cancelBtn = document.getElementById('credentialUnlockCancel');
                                            const confirmBtn = document.getElementById('credentialUnlockConfirm');
                                            const form = document.getElementById('credentialUnlockForm');
                                            if (!trigger || !modal || !form) {
                                                return;
                                            }
                                            const closeModal = () => modal.classList.remove('is-visible');
                                            trigger.addEventListener('click', () => {
                                                modal.classList.add('is-visible');
                                            });
                                            if (backdrop) {
                                                backdrop.addEventListener('click', closeModal);
                                            }
                                            if (cancelBtn) {
                                                cancelBtn.addEventListener('click', closeModal);
                                            }
                                            if (confirmBtn) {
                                                confirmBtn.addEventListener('click', () => {
                                                    form.submit();
                                                });
                                            }
                                        });
                                    </script>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <p class="empty">Đơn đang được xử lý, vui lòng chờ...</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>
</main>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const container = document.getElementById('walletEvents');
        if (!container) {
            return;
        }
        const endpoint = container.dataset.endpoint;
        if (!endpoint) {
            showError('Không tìm thấy nguồn dữ liệu ví.');
            return;
        }

        const formatter = new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0,
            minimumFractionDigits: 0
        });
        const dateFormatter = new Intl.DateTimeFormat('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });

        fetch(endpoint, {headers: {'Accept': 'application/json'}})
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể tải dữ liệu');
                }
                return response.json();
            })
            .then(payload => {
                if (!payload || !Array.isArray(payload.events)) {
                    showError('Dữ liệu ví không hợp lệ.');
                    return;
                }
                renderEvents(payload.events);
            })
            .catch(() => {
                showError('Không thể tải dữ liệu ví. Vui lòng thử lại sau.');
            });

        function renderEvents(events) {
            container.innerHTML = '';
            if (!events.length) {
                const empty = document.createElement('div');
                empty.className = 'wallet-events__placeholder';
                empty.textContent = 'Chưa có dữ liệu ví để hiển thị.';
                container.appendChild(empty);
                return;
            }
            events.forEach(event => {
                const item = document.createElement('div');
                item.className = 'wallet-event';
                if (event.primary === true) {
                    item.classList.add('wallet-event--primary');
                }

                const index = document.createElement('div');
                index.className = 'wallet-event__index';
                const sequenceValue = Number.isFinite(Number(event.sequence)) ? Number(event.sequence) : 0;
                index.textContent = sequenceValue > 0 ? String(sequenceValue) : '#';
                item.appendChild(index);

                const body = document.createElement('div');
                body.className = 'wallet-event__body';

                const header = document.createElement('div');
                header.className = 'wallet-event__header';
                const title = document.createElement('span');
                title.className = 'wallet-event__title';
                title.textContent = event.title || 'Sự kiện ví';
                header.appendChild(title);

                if (event.occurredAt) {
                    const time = document.createElement('span');
                    time.className = 'wallet-event__time';
                    const parsedTime = new Date(event.occurredAt);
                    if (!isNaN(parsedTime.getTime())) {
                        time.textContent = dateFormatter.format(parsedTime);
                        header.appendChild(time);
                    }
                }

                body.appendChild(header);

                if (event.description) {
                    const description = document.createElement('div');
                    description.className = 'wallet-event__description';
                    description.textContent = event.description;
                    body.appendChild(description);
                }

                const meta = document.createElement('div');
                meta.className = 'wallet-event__meta';

                if (event.reference) {
                    meta.appendChild(buildMetaRow('Mã tham chiếu', event.reference));
                }

                if (event.amount) {
                    const amountNumber = Number(event.amount);
                    if (Number.isFinite(amountNumber)) {
                        meta.appendChild(buildMetaRow('Số tiền', formatter.format(amountNumber)));
                    }
                }

                if (event.balanceAfter) {
                    const balanceNumber = Number(event.balanceAfter);
                    if (Number.isFinite(balanceNumber)) {
                        meta.appendChild(buildMetaRow('Số dư sau', formatter.format(balanceNumber)));
                    }
                }

                if (meta.children.length > 0) {
                    body.appendChild(meta);
                }

                item.appendChild(body);
                container.appendChild(item);
            });
        }

        function buildMetaRow(label, value) {
            const row = document.createElement('div');
            row.className = 'wallet-event__meta-row';
            const labelEl = document.createElement('span');
            labelEl.className = 'wallet-event__meta-label';
            labelEl.textContent = label;
            const valueEl = document.createElement('span');
            valueEl.className = 'wallet-event__meta-value';
            valueEl.textContent = value;
            row.append(labelEl, valueEl);
            return row;
        }

        function showError(message) {
            container.innerHTML = '';
            const error = document.createElement('div');
            error.className = 'wallet-events__error';
            error.textContent = message;
            container.appendChild(error);
        }
    });
</script>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
