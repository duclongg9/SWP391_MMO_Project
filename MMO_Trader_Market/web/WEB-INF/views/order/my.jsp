<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<%--
    Trang lịch sử đơn hàng.
    Controller OrderController#showMyOrders truyền xuống các attribute:
    - items: List<OrderRow> đã được OrderDAO map từ DB.
    - statusLabels/statusOptions: Map trạng thái -> nhãn tiếng Việt cho cả bảng và select filter.
    - total, page, totalPages, size: thông tin phân trang để render pagination.
--%>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Lịch sử mua hàng</h2>
            <form class="panel__filters" method="get" action="${pageContext.request.contextPath}/orders/my">
                <label class="form-field">
                    <span class="form-field__label">Mã đơn</span>
                    <input class="form-field__input" type="text" name="code" placeholder="VD: 5003"
                           value="<c:out value='${orderCode}'/>" />
                </label>
                <label class="form-field">
                    <span class="form-field__label">Sản phẩm</span>
                    <input class="form-field__input" type="text" name="product" placeholder="Tên sản phẩm"
                           value="<c:out value='${productName}'/>" />
                </label>
                <label class="form-field">
                    <span class="form-field__label">Trạng thái</span>
                    <select class="form-field__input" name="status">
                        <option value="">Tất cả</option>
                        <c:forEach var="entry" items="${statusOptions}">
                            <c:set var="statusKey" value="${entry.key}" />
                            <option value="${statusKey}" <c:if test="${statusKey eq status}">selected</c:if>>
                                <c:out value="${entry.value}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label class="form-field">
                    <span class="form-field__label">Từ ngày</span>
                    <input class="form-field__input" type="date" name="fromDate"
                           max="<c:out value='${todayIso}'/>" value="<c:out value='${fromDate}'/>" />
                </label>
                <label class="form-field">
                    <span class="form-field__label">Đến ngày</span>
                    <input class="form-field__input" type="date" name="toDate"
                           max="<c:out value='${todayIso}'/>" value="<c:out value='${toDate}'/>" />
                </label>
                <label class="form-field">
                    <span class="form-field__label">Mỗi trang</span>
                    <input class="form-field__input" type="number" min="1" name="size" value="${size}" />
                </label>
                <button class="button button--primary" type="submit">Lọc</button>
            </form>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty items}">
                    <%-- Bảng dữ liệu sử dụng trực tiếp OrderRow để giảm bước chuyển đổi ở JSP. --%>
                    <table class="table table--interactive">
                        <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Sản phẩm</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th class="table__actions">Chi tiết</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${items}">
                            <tr>
                                <td>#<c:out value="${item.id}" /></td>
                                <td><c:out value="${item.productName}" /></td>
                                <td>
                                    <fmt:formatNumber value="${item.totalAmount}" type="currency" currencySymbol="" /> đ
                                </td>
                                <td><c:out value="${statusLabels[item.status]}" /></td>
                                <td>
                                    <fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                </td>
                                <td class="table__actions">
                                    <c:url var="detailUrl" value="/orders/detail/${item.encodedId}" />
                                    <a class="button button--ghost" href="${detailUrl}">Xem</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p class="empty">Bạn chưa có đơn hàng nào.</p>
                </c:otherwise>
            </c:choose>
            <%-- Phân trang dựa trên meta được OrderService tính toán (currentPage, totalPages, size). --%>
            <c:if test="${totalPages > 1}">
                <nav class="pagination" aria-label="Phân trang đơn hàng">
                    <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                        <c:url var="pageUrl" value="/orders/my">
                            <c:param name="page" value="${pageNumber}" />
                            <c:if test="${not empty status}">
                                <c:param name="status" value="${status}" />
                            </c:if>
                            <c:param name="size" value="${size}" />
                        </c:url>
                        <c:choose>
                            <c:when test="${pageNumber == page}">
                                <span class="pagination__item pagination__item--active" aria-current="page">
                                    ${pageNumber}
                                </span>
                            </c:when>
                            <c:otherwise>
                                <a class="pagination__item" href="${pageUrl}">${pageNumber}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </nav>
            </c:if>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
