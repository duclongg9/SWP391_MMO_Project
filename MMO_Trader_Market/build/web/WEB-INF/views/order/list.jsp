<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Lịch sử đơn mua</h2>
            <a class="button button--primary" href="${pageContext.request.contextPath}/home">Tiếp tục mua hàng</a>
        </div>
        <div class="panel__body">
            <c:if test="${not empty error}">
                <div class="alert alert--error" role="alert"><c:out value="${error}" /></div>
            </c:if>
            <p class="profile-card__note">Có tổng cộng ${totalItems} đơn hàng trong hệ thống.</p>
            <c:choose>
                <c:when test="${not empty orders}">
                    <table class="table table--interactive">
                        <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Sản phẩm</th>
                            <th>Giá</th>
                            <th>Trạng thái</th>
                            <th>Bàn giao</th>
                            <th class="table__actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="order" items="${orders}">
                            <c:set var="orderId" value="${order.id}" />
                            <tr>
                                <td>#<c:out value="${order.id}" /></td>
                                <td>
                                    <strong><c:out value="${order.product.name}" /></strong><br>
                                    <small>Email nhận: <c:out value="${order.buyerEmail}" /></small>
                                </td>
                                <td>
                                    <fmt:formatNumber value="${order.product.price}" type="number" minFractionDigits="0"
                                                     maxFractionDigits="0" /> đ
                                </td>
                                <td>
                                    <span class="${statusClasses[orderId]}">
                                        <c:out value="${statusLabels[orderId]}" />
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty order.activationCode}">
                                            <span class="badge">Sẵn sàng</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge--ghost">Đang xử lý</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="table__actions">
                                    <c:url var="detailUrl" value="/orders/detail">
                                        <c:param name="id" value="${order.id}" />
                                    </c:url>
                                    <c:url var="buyAgainUrl" value="/orders/buy">
                                        <c:param name="productId" value="${order.product.id}" />
                                    </c:url>
                                    <a class="button button--primary" href="${detailUrl}">Xem chi tiết</a>
                                    <a class="button button--ghost" href="${buyAgainUrl}">Mua lại</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p>Chưa có dữ liệu.</p>
                </c:otherwise>
            </c:choose>
            <c:if test="${totalPages > 1}">
                <nav class="pagination" aria-label="Phân trang đơn hàng">
                    <c:choose>
                        <c:when test="${currentPage == 1}">
                            <span class="pagination__item pagination__item--disabled" aria-disabled="true">«</span>
                        </c:when>
                        <c:otherwise>
                            <c:url var="prevUrl" value="/orders">
                                <c:param name="page" value="${currentPage - 1}" />
                            </c:url>
                            <a class="pagination__item" href="${prevUrl}">«</a>
                        </c:otherwise>
                    </c:choose>
                    <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                        <c:url var="pageUrl" value="/orders">
                            <c:param name="page" value="${pageNumber}" />
                        </c:url>
                        <c:choose>
                            <c:when test="${pageNumber == currentPage}">
                                <a class="pagination__item pagination__item--active" href="${pageUrl}" aria-current="page">${pageNumber}</a>
                            </c:when>
                            <c:otherwise>
                                <a class="pagination__item" href="${pageUrl}">${pageNumber}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:choose>
                        <c:when test="${currentPage >= totalPages}">
                            <span class="pagination__item pagination__item--disabled" aria-disabled="true">»</span>
                        </c:when>
                        <c:otherwise>
                            <c:url var="nextUrl" value="/orders">
                                <c:param name="page" value="${currentPage + 1}" />
                            </c:url>
                            <a class="pagination__item" href="${nextUrl}">»</a>
                        </c:otherwise>
                    </c:choose>
                </nav>
            </c:if>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
