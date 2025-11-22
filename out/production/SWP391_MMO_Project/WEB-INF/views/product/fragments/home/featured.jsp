<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />

<div class="panel__header">
    <h3 class="panel__title">Sản phẩm nổi bật</h3>
    <span class="panel__tag">Dữ liệu trực tiếp</span>
</div>

<div class="landing__products">
    <c:choose>
        <c:when test="${empty featuredProducts}">
            <p>Chưa có dữ liệu.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="product" items="${featuredProducts}">
                <article class="product-card product-card--featured product-card--grid">
                    <div class="product-card__image product-card__media">
                        <c:choose>
                            <c:when test="${not empty product.primaryImageUrl}">
                                <c:set var="featuredImageSource" value="${product.primaryImageUrl}" />
                                <c:choose>
                                    <c:when test="${fn:startsWith(featuredImageSource, 'http://')
                                                    or fn:startsWith(featuredImageSource, 'https://')
                                                    or fn:startsWith(featuredImageSource, '//')
                                                    or fn:startsWith(featuredImageSource, 'data:')
                                                    or fn:startsWith(featuredImageSource, cPath)}">
                                        <c:set var="featuredImageUrl" value="${featuredImageSource}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="featuredImageUrl" value="${featuredImageSource}" />
                                    </c:otherwise>
                                </c:choose>
                                <img class="product-card__img" src="${featuredImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(product.name)}" loading="lazy" />
                            </c:when>
                            <c:otherwise>
                                <div class="product-card__placeholder">Không có ảnh</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="product-card__body product-card__body--stack">
                        <header class="product-card__header">
                            <h4><c:out value="${product.name}" /></h4>
                        </header>
                        <p class="product-card__meta">
                            <span><c:out value="${product.productTypeLabel}" /> • <c:out value="${product.productSubtypeLabel}" /></span>
                            <span>Shop: <strong><a class="product-card__shop" href="${cPath}/shops/${product.shopEncodedId}"><c:out value="${product.shopName}" /></a></strong></span>
                        </p>
                        <p class="product-card__description"><c:out value="${product.shortDescription}" /></p>
                        <p class="product-card__price">
                            <c:choose>
                                <c:when test="${product.minPrice eq product.maxPrice}">
                                    Giá
                                    <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                </c:when>
                                <c:otherwise>
                                    Giá từ
                                    <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                    –
                                    <fmt:formatNumber value="${product.maxPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <ul class="product-card__meta product-card__meta--stats">
                            <li>Tồn kho: <strong><c:out value="${product.inventoryCount}" /></strong></li>
                            <li>Đã bán: <strong><c:out value="${product.soldCount}" /></strong></li>
                        </ul>
                        <footer class="product-card__footer">
                            <c:url var="detailUrl" value="/product/detail/${product.encodedId}" />
                            <div class="product-card__actions product-card__actions--justify">
                                <a class="button button--primary product-card__cta" href="${detailUrl}">Xem chi tiết</a>
                            </div>
                        </footer>
                    </div>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>
