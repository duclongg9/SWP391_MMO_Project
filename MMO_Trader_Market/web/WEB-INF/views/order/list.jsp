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
            <a class="button button--primary" href="${pageContext.request.contextPath}/products">Tiếp tục mua hàng</a>
        </div>
        <div class="panel__body">
            <form class="form form--inline" method="get" action="${pageContext.request.contextPath}/orders/my">
                <input type="hidden" name="size" value="${size}" />
                <label class="form__control">
                    <span class="form__label">Trạng thái</span>
                    <select name="status" class="form__select">
                        <option value="">Tất cả</option>
                        <c:forEach var="option" items="${statusOptions}">
                            <option value="${option.key}" <c:if test="${option.key == statusFilter}">selected</c:if>>
                                <c:out value="${option.value}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <button type="submit" class="button button--ghost">Lọc</button>
            </form>
            <c:choose>
                <c:when test="${not empty items}">
                    <table class="table table--interactive">
                        <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th class="table__actions">Chi tiết</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="order" items="${items}">
                            <c:set var="orderId" value="${order.id}" />
                            <tr>
                                <td>#<c:out value="${order.id}" /></td>
                                <td>
                                    <strong><c:out value="${order.product.name}" /></strong>
                                    <br/>
                                    <small>Email nhận: <c:out value="${order.buyerEmail}" /></small>
                                </td>
                                <td><c:out value="${order.quantity != null ? order.quantity : 1}" /></td>
                                <td>
                                    <fmt:formatNumber value="${order.totalAmount}" type="number"
                                                      minFractionDigits="0" maxFractionDigits="0" /> đ
                                </td>
                                <td>
                                    <span class="${statusClasses[orderId]}">
                                        <c:out value="${statusLabels[orderId]}" />
                                    </span>
                                </td>
                                <td class="table__actions">
                                    <c:url value="/orders/detail" var="detailUrl">
                                        <c:param name="orderId" value="${order.id}" />
                                    </c:url>
                                    <a class="button button--ghost" href="${detailUrl}">Xem</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p>Chưa có đơn hàng.</p>
                </c:otherwise>
            </c:choose>
            <c:if test="${totalPages > 1}">
                <nav class="pagination" aria-label="Phân trang đơn hàng">
                    <c:choose>
                        <c:when test="${page == 1}">
                            <span class="pagination__item pagination__item--disabled" aria-disabled="true">«</span>
                        </c:when>
                        <c:otherwise>
                            <c:url var="prevUrl" value="/orders/my">
                                <c:param name="page" value="${page - 1}" />
                                <c:if test="${not empty statusFilter}">
                                    <c:param name="status" value="${statusFilter}" />
                                </c:if>
                                <c:param name="size" value="${size}" />
                            </c:url>
                            <a class="pagination__item" href="${prevUrl}">«</a>
                        </c:otherwise>
                    </c:choose>
                    <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                        <c:url var="pageUrl" value="/orders/my">
                            <c:param name="page" value="${pageNumber}" />
                            <c:if test="${not empty statusFilter}">
                                <c:param name="status" value="${statusFilter}" />
                            </c:if>
                            <c:param name="size" value="${size}" />
                        </c:url>
                        <c:choose>
                            <c:when test="${pageNumber == page}">
                                <a class="pagination__item pagination__item--active" href="${pageUrl}" aria-current="page">${pageNumber}</a>
                            </c:when>
                            <c:otherwise>
                                <a class="pagination__item" href="${pageUrl}">${pageNumber}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:choose>
                        <c:when test="${page >= totalPages}">
                            <span class="pagination__item pagination__item--disabled" aria-disabled="true">»</span>
                        </c:when>
                        <c:otherwise>
                            <c:url var="nextUrl" value="/orders/my">
                                <c:param name="page" value="${page + 1}" />
                                <c:if test="${not empty statusFilter}">
                                    <c:param name="status" value="${statusFilter}" />
                                </c:if>
                                <c:param name="size" value="${size}" />
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
