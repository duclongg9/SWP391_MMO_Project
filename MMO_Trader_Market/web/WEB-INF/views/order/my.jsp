<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="extraStylesheet" scope="request"
       value="${pageContext.request.contextPath}/assets/css/components/filterbar.css" />
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
        <div class="panel__header panel__header--stack">
            <div class="panel__header-text">
                <h2 class="panel__title">Lịch sử mua hàng</h2>
                <p class="panel__subtitle">Theo dõi đơn đã mua và lọc nhanh theo mã đơn, sản phẩm hoặc thời gian.</p>
            </div>
            <form class="filterbar filterbar--flush" method="get"
                  action="${pageContext.request.contextPath}/orders/my">
                <input type="hidden" name="page" value="1" />
                <div class="filterbar__row">
                    <div class="filterbar__field">
                        <label class="filterbar__label" for="orders-code">Mã đơn</label>
                        <input id="orders-code" class="form-control" type="text" name="code"
                               placeholder="#5003" value="<c:out value='${orderCode}'/>" />
                    </div>
                    <div class="filterbar__field">
                        <label class="filterbar__label" for="orders-product">Sản phẩm</label>
                        <input id="orders-product" class="form-control" type="text" name="product"
                               placeholder="Tên sản phẩm" value="<c:out value='${productName}'/>" />
                    </div>
                    <div class="filterbar__field">
                        <label class="filterbar__label" for="orders-status">Trạng thái</label>
                        <select id="orders-status" class="select" name="status">
                            <option value="">Tất cả</option>
                            <c:forEach var="entry" items="${statusOptions}">
                                <c:set var="statusKey" value="${entry.key}" />
                                <option value="${statusKey}" <c:if test="${statusKey eq status}">selected</c:if>>
                                    <c:out value="${entry.value}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filterbar__field">
                        <label class="filterbar__label" for="orders-from">Từ ngày</label>
                        <input id="orders-from" class="form-control" type="date" name="fromDate"
                               max="<c:out value='${todayIso}'/>" value="<c:out value='${fromDate}'/>" />
                    </div>
                    <div class="filterbar__field">
                        <label class="filterbar__label" for="orders-to">Đến ngày</label>
                        <input id="orders-to" class="form-control" type="date" name="toDate"
                               max="<c:out value='${todayIso}'/>" value="<c:out value='${toDate}'/>" />
                    </div>
                    <div class="filterbar__field filterbar__field--small">
                        <label class="filterbar__label" for="orders-size">Mỗi trang</label>
                        <input id="orders-size" class="form-control" type="number" min="1" name="size"
                               value="${size}" />
                    </div>
                    <div class="filterbar__actions">
                        <button class="button button--primary" type="submit">Lọc</button>
                        <a class="button button--ghost" href="${pageContext.request.contextPath}/orders/my">Xóa lọc</a>
                    </div>
                </div>
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
                            <c:if test="${not empty orderCode}">
                                <c:param name="code" value="${orderCode}" />
                            </c:if>
                            <c:if test="${not empty productName}">
                                <c:param name="product" value="${productName}" />
                            </c:if>
                            <c:if test="${not empty fromDate}">
                                <c:param name="fromDate" value="${fromDate}" />
                            </c:if>
                            <c:if test="${not empty toDate}">
                                <c:param name="toDate" value="${toDate}" />
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
