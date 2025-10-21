<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="product-list">
        <div class="product-list__header">
            <h2>Khám phá sản phẩm</h2>
            <p>Tìm kiếm, lọc theo shop và mua ngay những sản phẩm bạn cần.</p>
        </div>
        <form class="product-filters" method="get" action="${cPath}/products">
            <div class="product-filters__group">
                <label class="sr-only" for="q">Từ khóa</label>
                <input class="product-filters__input" type="search" id="q" name="q"
                       placeholder="Nhập tên sản phẩm..." value="${fn:escapeXml(query)}" />
            </div>
            <div class="product-filters__group">
                <label class="sr-only" for="shopId">Shop</label>
                <select class="product-filters__select" id="shopId" name="shopId">
                    <option value="">Tất cả shop</option>
                    <c:forEach var="shop" items="${shops}">
                        <c:set var="optionValue" value="${shop.id}" />
                        <option value="${optionValue}"
                                <c:if test="${selectedShopId == optionValue}">selected</c:if>>
                            <c:out value="${shop.name}" />
                        </option>
                    </c:forEach>
                </select>
            </div>
            <input type="hidden" name="size" value="${size}" />
            <button class="button button--primary" type="submit">Áp dụng</button>
        </form>
        <div class="product-list__meta">
            <span>Tổng <strong>${totalItems}</strong> sản phẩm khả dụng.</span>
            <span>Trang ${page} / ${totalPages}</span>
        </div>
        <c:choose>
            <c:when test="${not empty items}">
                <div class="product-grid">
                    <c:forEach var="p" items="${items}">
                        <article class="product-card">
                            <h3 class="product-card__title"><c:out value="${p.name}" /></h3>
                            <p class="product-card__price">
                                <fmt:formatNumber value="${p.price}" type="currency" currencySymbol="đ"
                                                  maxFractionDigits="0" minFractionDigits="0" />
                            </p>
                            <p class="product-card__stock">Tồn kho: <strong>${p.inventoryCount}</strong></p>
                            <p class="product-card__shop">Shop: <c:out value="${p.shopName}" /></p>
                            <div class="product-card__actions">
                                <a class="button button--ghost" href="${cPath}/product/detail?id=${p.id}">Xem</a>
                                <c:choose>
                                    <c:when test="${p.inventoryCount gt 0}">
                                        <form class="product-card__buy" method="post"
                                              action="${cPath}/order/buy-now?productId=${p.id}&amp;qty=1">
                                            <button class="button button--primary" type="submit">Mua ngay</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="product-card__badge">Hết hàng</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-card product-card--empty">
                    <p>Không tìm thấy sản phẩm phù hợp.</p>
                </div>
            </c:otherwise>
        </c:choose>
        <c:if test="${totalPages gt 1}">
            <nav class="pagination" aria-label="Phân trang sản phẩm">
                <c:url var="prevUrl" value="/products">
                    <c:param name="page" value="${page - 1}" />
                    <c:param name="size" value="${size}" />
                    <c:if test="${not empty query}">
                        <c:param name="q" value="${query}" />
                    </c:if>
                    <c:if test="${not empty selectedShopId}">
                        <c:param name="shopId" value="${selectedShopId}" />
                    </c:if>
                </c:url>
                <c:url var="nextUrl" value="/products">
                    <c:param name="page" value="${page + 1}" />
                    <c:param name="size" value="${size}" />
                    <c:if test="${not empty query}">
                        <c:param name="q" value="${query}" />
                    </c:if>
                    <c:if test="${not empty selectedShopId}">
                        <c:param name="shopId" value="${selectedShopId}" />
                    </c:if>
                </c:url>
                <span class="pagination__summary">Trang ${page} / ${totalPages}</span>
                <a class="pagination__item ${page le 1 ? 'pagination__item--disabled' : ''}" href="${page le 1 ? '#' : prevUrl}" aria-disabled="${page le 1}">«</a>
                <c:forEach var="i" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="/products">
                        <c:param name="page" value="${i}" />
                        <c:param name="size" value="${size}" />
                        <c:if test="${not empty query}">
                            <c:param name="q" value="${query}" />
                        </c:if>
                        <c:if test="${not empty selectedShopId}">
                            <c:param name="shopId" value="${selectedShopId}" />
                        </c:if>
                    </c:url>
                    <a class="pagination__item ${i == page ? 'pagination__item--active' : ''}"
                       href="${pageUrl}">${i}</a>
                </c:forEach>
                <a class="pagination__item ${page ge totalPages ? 'pagination__item--disabled' : ''}"
                   href="${page ge totalPages ? '#' : nextUrl}" aria-disabled="${page ge totalPages}">»</a>
            </nav>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
