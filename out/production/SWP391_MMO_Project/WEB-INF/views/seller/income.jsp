<%@ page contentType="text/html;charset=UTF-8" language="java" errorPage="" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="page" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <c:if test="${not empty errorMessage}">
        <div class="alert alert--error">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Hiệu suất tháng này</h2>
        </div>
        <div class="panel__body dashboard__row">
            <article class="stat-card">
                <div class="icon icon--primary">💵</div>
                <div>
                    <p class="stat-card__label">Doanh thu đã giải ngân</p>
                    <p class="stat-card__value">
                        <c:choose>
                            <c:when test="${not empty thisMonthRevenue}">
                                <fmt:formatNumber value="${thisMonthRevenue}" type="number" minFractionDigits="0" maxFractionDigits="0" groupingUsed="true" /> đ
                            </c:when>
                            <c:otherwise>0 đ</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </article>
            <article class="stat-card">
                <div class="icon icon--accent">📈</div>
                <div>
                    <p class="stat-card__label">Tăng trưởng</p>
                    <p class="stat-card__value">
                        <c:choose>
                            <c:when test="${not empty growthText}">
                                <c:out value="${growthText}" />
                            </c:when>
                            <c:otherwise>0% so với tháng trước</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </article>
            <article class="stat-card">
                <div class="icon icon--muted">⏱️</div>
                <div>
                    <p class="stat-card__label">Đơn chờ giải ngân</p>
                    <p class="stat-card__value">
                        <c:choose>
                            <c:when test="${not empty pendingDisbursementCount}">
                                <c:out value="${pendingDisbursementCount}" />
                            </c:when>
                            <c:otherwise>0</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </article>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết dòng tiền</h2>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty cashFlowTransactions && !empty cashFlowTransactions}">
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
                            <c:forEach var="tx" items="${cashFlowTransactions}">
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty tx.createdAt}">
                                                <fmt:formatDate value="${tx.createdAt}" pattern="dd/MM" timeZone="Asia/Ho_Chi_Minh" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty tx.note}">
                                                <c:out value="${tx.note}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:choose>
                                                    <c:when test="${tx.transactionType == 'Payout'}">Giải ngân đơn #<c:out value="${tx.relatedEntityId}" /></c:when>
                                                    <c:when test="${tx.transactionType == 'Withdrawal'}">Rút tiền</c:when>
                                                    <c:when test="${tx.transactionType == 'Deposit'}">Nạp tiền</c:when>
                                                    <c:when test="${tx.transactionType == 'Purchase'}">Thanh toán đơn hàng</c:when>
                                                    <c:when test="${tx.transactionType == 'Refund'}">Hoàn tiền</c:when>
                                                    <c:when test="${tx.transactionType == 'Fee'}">Phí giao dịch</c:when>
                                                    <c:otherwise>Giao dịch</c:otherwise>
                                                </c:choose>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${tx.transactionType == 'Payout' || tx.transactionType == 'Deposit' || tx.transactionType == 'Refund'}">
                                                +<fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="0" maxFractionDigits="0" groupingUsed="true" /> đ
                                            </c:when>
                                            <c:when test="${tx.amount < 0}">
                                                <fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="0" maxFractionDigits="0" groupingUsed="true" /> đ
                                            </c:when>
                                            <c:otherwise>
                                                -<fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="0" maxFractionDigits="0" groupingUsed="true" /> đ
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge">Đã nhận</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p class="empty">Chưa có giao dịch nào.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Thống kê sản phẩm đã bán</h2>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty productStats && !empty productStats}">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Tên sản phẩm</th>
                                <th>Trạng thái</th>
                                <th>Doanh thu</th>
                                <th>Số đơn</th>
                                <th>Số lượng bán</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="stat" items="${productStats}" varStatus="loop">
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td><strong><c:out value="${stat.productName}" /></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${stat.status == 'Pending'}">
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #fff3cd; color: #856404; border-radius: 4px; font-size: 0.875rem;">
                                                    Đơn đang chờ
                                                </span>
                                            </c:when>
                                            <c:when test="${stat.status == 'Completed'}">
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #d4edda; color: #155724; border-radius: 4px; font-size: 0.875rem;">
                                                    Hoàn thành
                                                </span>
                                            </c:when>
                                            <c:when test="${stat.status == 'Failed'}">
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #f8d7da; color: #721c24; border-radius: 4px; font-size: 0.875rem;">
                                                    Thất bại
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #e2e3e5; color: #383d41; border-radius: 4px; font-size: 0.875rem;">
                                                    <c:out value="${stat.status}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${stat.revenue != null ? stat.revenue : 0}" type="number" minFractionDigits="0" maxFractionDigits="0" groupingUsed="true" /> đ
                                    </td>
                                    <td>${stat.orderCount != null ? stat.orderCount : 0}</td>
                                    <td>${stat.quantitySold != null ? stat.quantitySold : 0}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p class="empty">Chưa có dữ liệu thống kê.</p>
                </c:otherwise>
            </c:choose>
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
