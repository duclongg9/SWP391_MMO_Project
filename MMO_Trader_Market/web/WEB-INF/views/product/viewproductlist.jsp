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
            <h2>Tất cả sản phẩm</h2>
            <p>Xem toàn bộ sản phẩm hiện đang được đăng bán bởi các shop.</p>
        </div>
        <div class="product-list__meta">
            <span>Tổng <strong>${totalItems}</strong> sản phẩm khả dụng.</span>
        </div>
        <c:choose>
            <c:when test="${not empty items}">
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
                    <p>Hiện chưa có sản phẩm khả dụng.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
