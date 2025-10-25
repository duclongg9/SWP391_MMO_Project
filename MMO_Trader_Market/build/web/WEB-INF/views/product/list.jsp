<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="product-browse">
        <div class="product-browse__header">
            <h2><c:out value="${currentTypeLabel}" /></h2>
            <p>Lọc theo phân loại chi tiết để tìm sản phẩm phù hợp nhất với nhu cầu của bạn.</p>
        </div>
        <div class="product-browse__layout">
            <aside class="product-browse__sidebar">
                <form class="product-filter-sidebar" method="get" action="${cPath}/products">
                    <input type="hidden" name="type" value="${selectedType}" />
                    <input type="hidden" name="page" value="1" />
                    <input type="hidden" name="pageSize" value="${pageSize}" />
                    <h3 class="product-filter-sidebar__title">Phân loại chi tiết</h3>
                    <div class="product-filter-sidebar__group">
                        <c:choose>
                            <c:when test="${not empty subtypeOptions}">
                                <c:forEach var="option" items="${subtypeOptions}">
                                    <label class="product-filter-sidebar__option">
                                        <input type="checkbox" name="subtype" value="${option.code}"
<input type="checkbox" name="subtype" value="${option.code}"
  <c:if test="${not empty selectedSubtypes and selectedSubtypes.contains(option.code)}">checked</c:if> />

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
                    <span>Tổng <strong>${totalItems}</strong> sản phẩm khả dụng.</span>
                    <span>Trang ${page} / ${totalPages}</span>
                </div>
                <c:choose>
                    <c:when test="${not empty items}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${items}">
                                <article class="product-card">
                                    <div class="product-card__image">
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
                                                <img src="${productImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(p.name)}" loading="lazy" />
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
                                        <div class="product-card__actions">
                                            <a class="button button--primary" href="${cPath}/product/detail?id=${p.id}">Xem chi tiết</a>
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
                <c:if test="${totalPages gt 1}">
                    <nav class="pagination" aria-label="Phân trang sản phẩm">
                        <c:url var="prevUrl" value="/products">
                            <c:param name="page" value="${page - 1}" />
                            <c:param name="pageSize" value="${pageSize}" />
                            <c:if test="${not empty selectedType}">
                                <c:param name="type" value="${selectedType}" />
                            </c:if>
                            <c:forEach var="code" items="${selectedSubtypes}">
                                <c:param name="subtype" value="${code}" />
                            </c:forEach>
                        </c:url>
                        <c:url var="nextUrl" value="/products">
                            <c:param name="page" value="${page + 1}" />
                            <c:param name="pageSize" value="${pageSize}" />
                            <c:if test="${not empty selectedType}">
                                <c:param name="type" value="${selectedType}" />
                            </c:if>
                            <c:forEach var="code" items="${selectedSubtypes}">
                                <c:param name="subtype" value="${code}" />
                            </c:forEach>
                        </c:url>
                        <span class="pagination__summary">Trang ${page} / ${totalPages}</span>
                        <a class="pagination__item ${page le 1 ? 'pagination__item--disabled' : ''}"
                           href="${page le 1 ? '#' : prevUrl}" aria-disabled="${page le 1}">«</a>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <c:url var="pageUrl" value="/products">
                                <c:param name="page" value="${i}" />
                                <c:param name="pageSize" value="${pageSize}" />
                                <c:if test="${not empty selectedType}">
                                    <c:param name="type" value="${selectedType}" />
                                </c:if>
                                <c:forEach var="code" items="${selectedSubtypes}">
                                    <c:param name="subtype" value="${code}" />
                                </c:forEach>
                            </c:url>
                            <a class="pagination__item ${i == page ? 'pagination__item--active' : ''}"
                               href="${pageUrl}">${i}</a>
                        </c:forEach>
                        <a class="pagination__item ${page ge totalPages ? 'pagination__item--disabled' : ''}"
                           href="${page ge totalPages ? '#' : nextUrl}" aria-disabled="${page ge totalPages}">»</a>
                    </nav>
                </c:if>
            </div>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
