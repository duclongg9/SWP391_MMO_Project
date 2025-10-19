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
            <form class="search-bar" method="get" action="${pageContext.request.contextPath}/products">
                <label class="search-bar__icon" for="keyword">🔎</label>
                <input class="search-bar__input" type="search" id="keyword" name="keyword"
                       placeholder="Nhập từ khóa..." value="${fn:escapeXml(searchKeyword)}">
                <button class="button button--ghost" type="submit">Lọc</button>
            </form>
        </div>
        <div class="panel__body">
            <p class="profile-card__note">Tìm thấy ${totalItems} sản phẩm phù hợp.</p>
            <c:choose>
                <c:when test="${not empty products}">
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
                        <c:forEach var="product" items="${products}">
                            <tr>
                                <td>#<c:out value="${product.id}" /></td>
                                <td><c:out value="${product.name}" /></td>
                                <td><c:out value="${product.description}" /></td>
                                <td>
                                    <fmt:formatNumber value="${product.price}" type="number" minFractionDigits="0"
                                                     maxFractionDigits="0" /> đ
                                </td>
                                <td>
                                    <span class="badge"><c:out value="${product.status}" /></span>
                                </td>
                                <td class="table__actions">
                                    <c:set var="statusUpper" value="${not empty product.status ? fn:toUpperCase(product.status) : ''}" />
                                    <c:choose>
                                        <c:when test="${statusUpper eq 'APPROVED'}">
                                            <c:url var="buyUrl" value="/orders/buy">
                                                <c:param name="productId" value="${product.id}" />
                                            </c:url>
                                            <c:url var="ordersUrl" value="/orders" />
                                            <a class="button button--primary" href="${buyUrl}">Mua ngay</a>
                                            <a class="button button--ghost" href="${ordersUrl}">Đơn đã mua</a>
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
                    <p>Bạn chưa có sản phẩm nào khớp từ khóa. Vui lòng thử bộ lọc khác.</p>
                </c:otherwise>
            </c:choose>
            <nav class="pagination" aria-label="Phân trang sản phẩm">
                <c:choose>
                    <c:when test="${currentPage == 1}">
                        <span class="pagination__item pagination__item--disabled" aria-disabled="true">«</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="prevUrl" value="/products">
                            <c:param name="page" value="${currentPage - 1}" />
                            <c:if test="${not empty searchKeyword}">
                                <c:param name="keyword" value="${searchKeyword}" />
                            </c:if>
                        </c:url>
                        <a class="pagination__item" href="${prevUrl}">«</a>
                    </c:otherwise>
                </c:choose>
                <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="/products">
                        <c:param name="page" value="${pageNumber}" />
                        <c:if test="${not empty searchKeyword}">
                            <c:param name="keyword" value="${searchKeyword}" />
                        </c:if>
                    </c:url>
                    <c:choose>
                        <c:when test="${pageNumber == currentPage}">
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
                    <c:when test="${currentPage >= totalPages}">
                        <span class="pagination__item pagination__item--disabled" aria-disabled="true">»</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="nextUrl" value="/products">
                            <c:param name="page" value="${currentPage + 1}" />
                            <c:if test="${not empty searchKeyword}">
                                <c:param name="keyword" value="${searchKeyword}" />
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
