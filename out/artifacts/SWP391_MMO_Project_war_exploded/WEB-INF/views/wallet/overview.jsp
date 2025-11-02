<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="page" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content wallet">
    <section class="panel wallet__summary">
        <div class="panel__header">
            <h2 class="panel__title">Tổng quan ví</h2>
            <span class="badge"><c:out value="${walletStatus}" /></span>
        </div>
        <div class="panel__body wallet__stats">
            <article class="wallet-stat">
                <p class="wallet-stat__label">Số dư khả dụng</p>
                <p class="wallet-stat__value">
                    <fmt:formatNumber value="${walletBalance}" type="number" minFractionDigits="0" />
                    <span class="wallet-stat__currency"><c:out value="${walletCurrency}" /></span>
                </p>
            </article>
            <article class="wallet-stat">
                <p class="wallet-stat__label">Số tiền tạm giữ</p>
                <p class="wallet-stat__value wallet-stat__value--muted">
                    <fmt:formatNumber value="${walletHold}" type="number" minFractionDigits="0" />
                    <span class="wallet-stat__currency"><c:out value="${walletCurrency}" /></span>
                </p>
            </article>
            <article class="wallet-stat">
                <p class="wallet-stat__label">Cập nhật gần nhất</p>
                <p class="wallet-stat__meta"><c:out value="${walletUpdatedAt}" /></p>
            </article>
        </div>
    </section>

    <section class="panel wallet__shortcuts">
        <div class="panel__header">
            <h3 class="panel__title">Thao tác nhanh</h3>
        </div>
        <div class="panel__body wallet-shortcuts">
            <c:choose>
                <c:when test="${empty shortcuts}">
                    <p>Không có thao tác nhanh.</p>
                </c:when>
                <c:otherwise>
                    <div class="wallet-shortcuts__grid">
                        <c:forEach var="shortcut" items="${shortcuts}">
                            <a class="wallet-shortcut" href="${shortcut.href}">
                                <span class="wallet-shortcut__icon" aria-hidden="true"><c:out value="${shortcut.icon}" /></span>
                                <div>
                                    <strong class="wallet-shortcut__title"><c:out value="${shortcut.title}" /></strong>
                                    <p class="wallet-shortcut__description"><c:out value="${shortcut.description}" /></p>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="panel wallet__section">
        <div class="panel__header">
            <h3 class="panel__title">Lịch sử giao dịch</h3>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${empty transactions}">
                    <p>Chưa có giao dịch nào được ghi nhận.</p>
                </c:when>
                <c:otherwise>
                    <table class="table table--interactive">
                        <thead>
                            <tr>
                                <th>Nội dung</th>
                                <th>Số tiền</th>
                                <th>Trạng thái</th>
                                <th>Thời gian</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="transaction" items="${transactions}">
                                <tr>
                                    <td><c:out value="${transaction.title}" /></td>
                                    <td><strong><c:out value="${transaction.amount}" /></strong></td>
                                    <td><span class="badge"><c:out value="${transaction.status}" /></span></td>
                                    <td><c:out value="${transaction.time}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
