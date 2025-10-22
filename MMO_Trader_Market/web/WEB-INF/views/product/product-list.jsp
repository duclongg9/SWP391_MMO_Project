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
        <c:set var="filterFormAction" value="${cPath}/products" />
        <c:set var="filterIncludeSize" value="${true}" />
        <c:set var="filterPageSize" value="${pageSize}" />
        <c:set var="filterQuery" value="${q}" />
        <c:set var="filterType" value="${selectedType}" />
        <c:set var="filterSubtype" value="${selectedSubtype}" />
        <%@ include file="/WEB-INF/views/product/fragments/filter-form.jspf" %>
        <div class="product-list__meta">
            <span>Tổng <strong>${totalItems}</strong> sản phẩm khả dụng.</span>
            <span>Trang ${page} / ${totalPages}</span>
        </div>
        <c:if test="${not empty items}">
            <div class="product-grid">
                <c:forEach var="p" items="${items}">
                    <article class="product-card">
                            <div class="product-card__image">
                                <c:choose>
                                    <c:when test="${not empty p.primaryImageUrl}">
                                        <img src="${p.primaryImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(p.name)}" loading="lazy" />
                                    </c:when>
                                    <c:otherwise>
                                        <div class="product-card__placeholder">Không có ảnh</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-card__body">
                                <h3 class="product-card__title"><c:out value="${p.name}" /></h3>
                                <p class="product-card__meta">
                                    <span><c:out value="${p.productTypeLabel}" /> • <c:out value="${p.productSubtypeLabel}" /></span>
                                    <span>Shop: <strong><c:out value="${p.shopName}" /></strong></span>
                                </p>
                                <p class="product-card__description"><c:out value="${p.shortDescription}" /></p>
                                <p class="product-card__price">
                                    <c:choose>
                                        <c:when test="${p.minPrice eq p.maxPrice}">
                                            Giá
                                            <fmt:formatNumber value="${p.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:when>
                                        <c:otherwise>
                                            Từ
                                            <fmt:formatNumber value="${p.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                            –
                                            <fmt:formatNumber value="${p.maxPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <ul class="product-card__meta product-card__meta--stats">
                                    <li>Còn: <strong><c:out value="${p.inventoryCount}" default="0" /></strong></li>
                                    <li>Đã bán: <strong><c:out value="${p.soldCount}" default="0" /></strong></li>
                                </ul>
                                <div class="product-card__actions">
                                    <c:url var="detailUrl" value="/product/detail">
                                        <c:param name="id" value="${p.id}" />
                                    </c:url>
                                    <a class="button button--primary" href="${detailUrl}">Xem chi tiết</a>
                                </div>
                            </div>
                    </article>
                </c:forEach>
            </div>
        </c:if>
        <c:if test="${empty items}">
            <div class="product-card product-card--empty">
                <p>Không tìm thấy sản phẩm phù hợp.</p>
            </div>
        </c:if>
        <c:if test="${totalPages gt 1}">
            <nav class="pagination" aria-label="Phân trang sản phẩm">
                <c:url var="prevUrl" value="/products">
                    <c:param name="page" value="${page - 1}" />
                    <c:param name="pageSize" value="${pageSize}" />
                    <c:if test="${not empty query}">
                        <c:param name="q" value="${query}" />
                    </c:if>
                    <c:if test="${not empty selectedType}">
                        <c:param name="type" value="${selectedType}" />
                    </c:if>
                    <c:if test="${not empty selectedSubtype}">
                        <c:param name="subtype" value="${selectedSubtype}" />
                    </c:if>
                </c:url>
                <c:url var="nextUrl" value="/products">
                    <c:param name="page" value="${page + 1}" />
                    <c:param name="pageSize" value="${pageSize}" />
                    <c:if test="${not empty query}">
                        <c:param name="q" value="${query}" />
                    </c:if>
                    <c:if test="${not empty selectedType}">
                        <c:param name="type" value="${selectedType}" />
                    </c:if>
                    <c:if test="${not empty selectedSubtype}">
                        <c:param name="subtype" value="${selectedSubtype}" />
                    </c:if>
                </c:url>
                <span class="pagination__summary">Trang ${page} / ${totalPages}</span>
                <a class="pagination__item ${page le 1 ? 'pagination__item--disabled' : ''}" href="${page le 1 ? '#' : prevUrl}" aria-disabled="${page le 1}">«</a>
                <c:forEach var="i" begin="1" end="${totalPages}">
                    <c:url var="pageUrl" value="/products">
                        <c:param name="page" value="${i}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:if test="${not empty query}">
                            <c:param name="q" value="${query}" />
                        </c:if>
                        <c:if test="${not empty selectedType}">
                            <c:param name="type" value="${selectedType}" />
                        </c:if>
                        <c:if test="${not empty selectedSubtype}">
                            <c:param name="subtype" value="${selectedSubtype}" />
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
