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
            <span class="badge">Đang hoạt động</span>
        </div>
        <div class="panel__body wallet__stats">
            <c:if test="${not empty error}">
                <div class="alert alert--error"><c:out value="${error}" /></div>
            </c:if>
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
                            <th>Loại giao dịch</th>
                            <th>Số tiền</th>
                            <th>Số dư sau giao dịch</th>
                            <th>Ghi chú</th>
                            <th>Thời gian</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="transaction" items="${transactions}">
                            <tr>
                                <td><c:out value="${transaction.type}" /></td>
                                <td>
                                    <fmt:formatNumber value="${transaction.amount}" type="number" minFractionDigits="0" />
                                    <span class="wallet-stat__currency"><c:out value="${walletCurrency}" /></span>
                                </td>
                                <td>
                                    <fmt:formatNumber value="${transaction.balanceAfter}" type="number" minFractionDigits="0" />
                                    <span class="wallet-stat__currency"><c:out value="${walletCurrency}" /></span>
                                </td>
                                <td><c:out value="${transaction.note}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty transaction.createdAt}">
                                            <fmt:formatDate value="${transaction.createdAt}" type="both" />
                                        </c:when>
                                        <c:otherwise>--</c:otherwise>
                                    </c:choose>
                                </td>
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
