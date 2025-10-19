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
            <h2 class="panel__title">Bộ lọc nhanh</h2>
            <c:url var="searchAction" value="/products" />
            <form class="search-bar" method="get" action="${searchAction}">
                <label class="search-bar__icon" for="keyword">🔎</label>
                <input class="search-bar__input" type="search" id="keyword" name="q"
                       placeholder="Nhập từ khóa..." value="${fn:escapeXml(keyword)}">
                <input type="hidden" name="size" value="${pageSize}" />
                <button class="button button--ghost" type="submit">Lọc</button>
            </form>
        </div>
        <div class="panel__body">
            <p class="profile-card__note">Tìm thấy <c:out value="${total}" /> sản phẩm phù hợp.</p>
            <c:choose>
                <c:when test="${total > 0}">
                    <table class="table table--interactive">
                        <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Tên sản phẩm</th>
                            <th>Mô tả</th>
                            <th>Giá</th>
                            <th>Trạng thái</th>
                            <th class="table__actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="product" items="${items}">
                            <tr>
                                <td>#<c:out value="${product.id}" /></td>
                                <td><c:out value="${product.name}" /></td>
                                <td><c:out value="${product.description}" /></td>
                                <td>
                                    <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="đ"
                                                     minFractionDigits="0" maxFractionDigits="0" />
                                </td>
                                <td>
                                    <span class="badge"><c:out value="${product.status}" /></span>
                                </td>
                                <td class="table__actions">
                                    <c:set var="statusUpper"
                                           value="${not empty product.status ? fn:toUpperCase(product.status) : ''}" />
                                    <c:choose>
                                        <c:when test="${statusUpper eq 'APPROVED'}">
                                            <form method="post" action="${pageContext.request.contextPath}/order/buy-now"
                                                  style="display:inline;">
                                                <input type="hidden" name="productId" value="${product.id}" />
                                                <input type="hidden" name="quantity" value="1" />
                                                <button class="button button--primary" type="submit">Mua ngay</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="button button--ghost" type="button" disabled>Đang chờ duyệt</button>
                                        </c:otherwise>
                                    </c:choose>
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
            <nav class="pagination" aria-label="Phân trang sản phẩm">
                <c:choose>
                    <c:when test="${page <= 1}">
                        <span class="pagination__item pagination__item--disabled" aria-disabled="true">«</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="prevUrl" value="/products">
                            <c:param name="page" value="${page - 1}" />
                            <c:param name="size" value="${pageSize}" />
                            <c:if test="${not empty keyword}">
                                <c:param name="q" value="${keyword}" />
                            </c:if>
                        </c:url>
                        <a class="pagination__item" href="${prevUrl}">«</a>
                    </c:otherwise>
                </c:choose>
                <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="/products">
                        <c:param name="page" value="${pageNumber}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:if test="${not empty keyword}">
                            <c:param name="q" value="${keyword}" />
                        </c:if>
                    </c:url>
                    <c:choose>
                        <c:when test="${pageNumber == page}">
                            <a class="pagination__item pagination__item--active" href="${pageUrl}" aria-current="page">
                                ${pageNumber}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="pagination__item" href="${pageUrl}">${pageNumber}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:choose>
                    <c:when test="${page >= totalPages || totalPages == 0}">
                        <span class="pagination__item pagination__item--disabled" aria-disabled="true">»</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="nextUrl" value="/products">
                            <c:param name="page" value="${page + 1}" />
                            <c:param name="size" value="${pageSize}" />
                            <c:if test="${not empty keyword}">
                                <c:param name="q" value="${keyword}" />
                            </c:if>
                        </c:url>
                        <a class="pagination__item" href="${nextUrl}">»</a>
                    </c:otherwise>
                </c:choose>
            </nav>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
