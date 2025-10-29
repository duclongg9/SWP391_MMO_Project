<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<c:set var="type" value="${not empty param.type ? param.type : (not empty selectedType ? selectedType : '')}" />
<c:set var="keyword" value="${not empty requestScope.keyword ? requestScope.keyword : (not empty param.keyword ? param.keyword : '')}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="product-browse">
        <div class="product-browse__header">
            <h2><c:out value="${currentTypeLabel}" /></h2>
            <form class="product-search" method="get" action="${cPath}/products">
                <label class="sr-only" for="product-search-keyword" id="product-search-label">Tìm sản phẩm</label>
                <input type="hidden" name="type" value="${fn:escapeXml(type)}" />
                <input type="hidden" name="page" value="1" />
                <input type="hidden" name="pageSize" value="${pageSize}" />
                <c:forEach var="code" items="${selectedSubtypes}">
                    <input type="hidden" name="subtype" value="${fn:escapeXml(code)}" />
                </c:forEach>
                <div class="product-search__field">
                    <input class="product-search__input" type="search" name="keyword" id="product-search-keyword"
                           placeholder="Tìm kiếm sản phẩm..." value="${fn:escapeXml(keyword)}" />
                    <button class="button button--primary product-search__submit" type="submit">Tìm</button>
                </div>
            </form>
        </div>
        <div class="product-browse__layout">
            <aside class="product-browse__sidebar">
                <form class="product-filter-sidebar" method="get" action="${cPath}/products">
                    <input type="hidden" name="type" value="${fn:escapeXml(type)}" />
                    <input type="hidden" name="page" value="1" />
                    <input type="hidden" name="pageSize" value="${pageSize}" />
                    <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}" />
                    <h3 class="product-filter-sidebar__title">Bộ Lọc</h3>
                    <div class="product-filter-sidebar__group">
                        <c:choose>
                            <c:when test="${not empty subtypeOptions}">
                                <c:forEach var="option" items="${subtypeOptions}">
                                    <label class="product-filter-sidebar__option">
                                        <input type="checkbox" name="subtype" value="${fn:escapeXml(option.code)}"
                                               <c:if test="${not empty selectedSubtypes and selectedSubtypes.contains(option.code)}">checked</c:if>
                                                   onchange="this.form.submit()" />
                                               <span><c:out value="${option.label}" /></span>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="product-filter-sidebar__empty">Không có phân loại chi tiết cho danh mục này.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <noscript>
                    <button class="button button--primary button--block" type="submit">Áp dụng bộ lọc</button>
                    </noscript>
                </form>
            </aside>
            <div class="product-browse__results">
                <div class="product-list__meta product-browse__meta">
                    <div class="product-list__stats">
                        <span>Tổng <strong>${totalItems}</strong> sản phẩm khả dụng.</span>
                        <span>Trang ${page} / ${totalPages}</span>
                    </div>
                    <form class="product-list__page-size" method="get" action="${cPath}/products">
                        <input type="hidden" name="type" value="${fn:escapeXml(type)}" />
                        <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}" />
                        <input type="hidden" name="page" value="1" />
                        <c:forEach var="code" items="${selectedSubtypes}">
                            <input type="hidden" name="subtype" value="${fn:escapeXml(code)}" />
                        </c:forEach>
                        <label class="product-list__page-size-label" for="product-page-size">Hiển thị</label>
                        <select class="select" name="pageSize" id="product-page-size" onchange="this.form.submit()">
                            <c:forEach var="option" items="${pageSizeOptions}">
                                <option value="${option}" <c:if test="${option == pageSize}">selected</c:if>>${option} / trang</option>
                            </c:forEach>
                        </select>
                    </form>
                </div>
                <c:choose>
                    <c:when test="${not empty items}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${items}">
                                <article class="product-card product-card--grid">
                                    <div class="product-card__image product-card__media">
                                        <c:choose>
                                            <c:when test="${not empty p.primaryImageUrl}">
                                                <c:set var="productImageSource" value="${p.primaryImageUrl}" />
                                                <c:choose>
                                                    <c:when test="${fn:startsWith(productImageSource, 'http://')
                                                                    or fn:startsWith(productImageSource, 'https://')
                                                                    or fn:startsWith(productImageSource, '//')
                                                                    or fn:startsWith(productImageSource, 'data:')
                                                                    or fn:startsWith(productImageSource, cPath)}">
                                                        <c:set var="productImageUrl" value="${productImageSource}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:url var="productImageUrl" value="${productImageSource}" />
                                                    </c:otherwise>
                                                </c:choose>
                                                <img class="product-card__img" src="${productImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(p.name)}" loading="lazy" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="product-card__placeholder">Không có ảnh</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="product-card__body product-card__body--stack">
                                        <h3 class="product-card__title clamp-2"><c:out value="${p.name}" /></h3>
                                        <p class="product-card__meta">
                                            <span><c:out value="${p.productTypeLabel}" /> • <c:out value="${p.productSubtypeLabel}" /></span>
                                            <span>Shop: <strong><c:out value="${p.shopName}" /></strong></span>
                                        </p>
                                        <p class="product-card__description"><c:out value="${p.shortDescription}" /></p>
                                        <p class="product-card__price">
                                            <c:choose>
                                                <c:when test="${p.minPrice eq p.maxPrice}">
                                                    Giá
                                                    <fmt:formatNumber value="${p.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                                </c:when>
                                                <c:otherwise>
                                                    Giá từ
                                                    <fmt:formatNumber value="${p.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                                    –
                                                    <fmt:formatNumber value="${p.maxPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <ul class="product-card__meta product-card__meta--stats">
                                            <li>Tồn kho: <strong><c:out value="${p.inventoryCount}" /></strong></li>
                                            <li>Đã bán: <strong><c:out value="${p.soldCount}" /></strong></li>
                                        </ul>
                                        <div class="product-card__actions product-card__actions--justify">
                                            <a class="button button--primary product-card__cta" href="${cPath}/product/detail/${p.encodedId}">Xem chi tiết</a>
                                        </div>
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
                <c:if test="${totalPages > 1}">
                    <nav class="pagination" aria-label="Phân trang sản phẩm">
                        <c:choose>
                            <c:when test="${page == 1}">
                                <span class="pagination__item pagination__item--disabled" aria-disabled="true">«</span>
                            </c:when>
                            <c:otherwise>
                                <c:url var="prevUrl" value="/products">
                                    <c:param name="type" value="${type}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                    <c:forEach var="s" items="${selectedSubtypes}">
                                        <c:param name="subtype" value="${s}" />
                                    </c:forEach>
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="page" value="${page - 1}" />
                                </c:url>
                                <a class="pagination__item" href="${prevUrl}">«</a>
                            </c:otherwise>
                        </c:choose>
                        <c:forEach var="pageNumber" begin="1" end="${totalPages}">
                            <c:url var="pageUrl" value="/products">
                                <c:param name="type" value="${type}" />
                                <c:if test="${not empty keyword}">
                                    <c:param name="keyword" value="${keyword}" />
                                </c:if>
                                <c:forEach var="s" items="${selectedSubtypes}">
                                    <c:param name="subtype" value="${s}" />
                                </c:forEach>
                                <c:param name="pageSize" value="${pageSize}" />
                                <c:param name="page" value="${pageNumber}" />
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
                                <c:url var="nextUrl" value="/products">
                                    <c:param name="type" value="${type}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                    <c:forEach var="s" items="${selectedSubtypes}">
                                        <c:param name="subtype" value="${s}" />
                                    </c:forEach>
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="page" value="${page + 1}" />
                                </c:url>
                                <a class="pagination__item" href="${nextUrl}">»</a>
                            </c:otherwise>
                        </c:choose>
                    </nav>
                </c:if>
            </div>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
