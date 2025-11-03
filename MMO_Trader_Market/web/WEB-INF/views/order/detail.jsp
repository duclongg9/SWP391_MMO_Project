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

                .order-wallet-timeline {
                    list-style: none;
                    margin: 0;
                    padding: 0;
                    display: flex;
                    flex-direction: column;
                    gap: 1.25rem;
                }

                .order-wallet-timeline__item {
                    display: flex;
                    gap: 1rem;
                    align-items: flex-start;
                }

                .order-wallet-timeline__badge {
                    width: 2rem;
                    height: 2rem;
                    border-radius: 50%;
                    background: #e2e8f0;
                    color: #0f172a;
                    font-weight: 600;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    flex-shrink: 0;
                }

                .order-wallet-timeline__item--primary .order-wallet-timeline__badge {
                    background: #2563eb;
                    color: #fff;
                }

                .order-wallet-timeline__content {
                    flex: 1;
                    min-width: 0;
                    padding: 0.75rem 1rem;
                    background: #f8fafc;
                    border-radius: 12px;
                    border: 1px solid #e2e8f0;
                    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
                }

                .order-wallet-timeline__item--primary .order-wallet-timeline__content {
                    border-color: #2563eb;
                    box-shadow: 0 16px 30px rgba(37, 99, 235, 0.12);
                }

                .order-wallet-timeline__header {
                    display: flex;
                    flex-wrap: wrap;
                    align-items: center;
                    gap: 0.5rem 1rem;
                    margin-bottom: 0.5rem;
                }

                .order-wallet-timeline__title {
                    font-weight: 600;
                    color: #0f172a;
                }

                .order-wallet-timeline__time {
                    font-size: 0.9rem;
                    color: #475569;
                }

                .order-wallet-timeline__description {
                    margin: 0 0 0.5rem 0;
                    color: #475569;
                    line-height: 1.6;
                }

                .order-wallet-timeline__reference {
                    margin: 0 0 0.5rem 0;
                    color: #0f172a;
                    font-size: 0.95rem;
                }

                .order-wallet-timeline__reference span {
                    font-weight: 600;
                }

                .order-wallet-timeline__metrics {
                    margin-top: 0;
                }

                .order-wallet-timeline__metrics li span {
                    min-width: 130px;
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

                    .order-wallet-timeline__item {
                        flex-direction: column;
                    }

                    .order-wallet-timeline__badge {
                        margin-bottom: 0.5rem;
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
                            <c:if test="${not empty walletTimeline}">
                                <ol class="order-wallet-timeline">
                                    <c:forEach var="event" items="${walletTimeline}" varStatus="loop">
                                        <li class="order-wallet-timeline__item${event.primary ? ' order-wallet-timeline__item--primary' : ''}">
                                            <div class="order-wallet-timeline__badge">${loop.index + 1}</div>
                                            <div class="order-wallet-timeline__content">
                                                <div class="order-wallet-timeline__header">
                                                    <span class="order-wallet-timeline__title"><c:out value="${event.title}" /></span>
                                                    <c:if test="${not empty event.occurredAt}">
                                                        <time class="order-wallet-timeline__time">
                                                            <fmt:formatDate value="${event.occurredAt}" pattern="dd/MM/yyyy HH:mm" />
                                                        </time>
                                                    </c:if>
                                                </div>
                                                <p class="order-wallet-timeline__description"><c:out value="${event.description}" /></p>
                                                <c:if test="${not empty event.reference}">
                                                    <p class="order-wallet-timeline__reference">Mã tham chiếu: <span><c:out value="${event.reference}" /></span></p>
                                                </c:if>
                                                <c:if test="${not empty event.amount or not empty event.balanceAfter}">
                                                    <ul class="definition-list order-wallet-timeline__metrics">
                                                        <c:if test="${not empty event.amount}">
                                                            <li><span>Số tiền:</span>
                                                                <fmt:formatNumber value="${event.amount}" type="currency" currencySymbol="" /> đ
                                                            </li>
                                                        </c:if>
                                                        <c:if test="${not empty event.balanceAfter}">
                                                            <li><span>Số dư sau:</span>
                                                                <fmt:formatNumber value="${event.balanceAfter}" type="currency" currencySymbol="" /> đ
                                                            </li>
                                                        </c:if>
                                                    </ul>
                                                </c:if>
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ol>
                            </c:if>
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
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
