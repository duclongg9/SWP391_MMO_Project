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
        <div class="product-browser">
            <aside class="product-browser__sidebar">
                <form id="product-subtype-form" class="product-sidebar" method="get" action="${cPath}/products">
                    <input type="hidden" name="type" value="${selectedType}" />
                    <input type="hidden" name="page" value="1" />
                    <input type="hidden" name="pageSize" value="${pageSize}" />
                    <div class="product-sidebar__header">
                        <h2 class="product-sidebar__title"><c:out value="${selectedTypeLabel}" /></h2>
                        <p class="product-sidebar__subtitle">Chọn phân loại chi tiết để lọc kết quả.</p>
                    </div>
                    <div class="product-sidebar__group">
                        <c:choose>
                            <c:when test="${not empty subtypeOptions}">
                                <c:forEach var="option" items="${subtypeOptions}">
                                    <label class="product-sidebar__option">
                                        <input type="checkbox" name="subtype" value="${option.code}"
                                               <c:if test="${selectedSubtypes contains option.code}">checked</c:if> />
                                        <span><c:out value="${option.label}" /></span>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="product-sidebar__empty">Không có phân loại chi tiết cho danh mục này.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <button class="button button--primary product-sidebar__apply" type="submit">Áp dụng lọc</button>
                </form>
            </aside>
            <div class="product-browser__content">
                <header class="product-browser__header">
                    <h2><c:out value="${selectedTypeLabel}" /></h2>
                    <p class="product-browser__description">Có thể chọn nhiều phân loại cùng lúc để thu hẹp danh sách sản phẩm.</p>
                </header>
                <div class="product-list__meta">
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
                            <c:param name="type" value="${selectedType}" />
                            <c:param name="page" value="${page - 1}" />
                            <c:param name="pageSize" value="${pageSize}" />
                            <c:forEach var="subtype" items="${selectedSubtypes}">
                                <c:param name="subtype" value="${subtype}" />
                            </c:forEach>
                        </c:url>
                        <c:url var="nextUrl" value="/products">
                            <c:param name="type" value="${selectedType}" />
                            <c:param name="page" value="${page + 1}" />
                            <c:param name="pageSize" value="${pageSize}" />
                            <c:forEach var="subtype" items="${selectedSubtypes}">
                                <c:param name="subtype" value="${subtype}" />
                            </c:forEach>
                        </c:url>
                        <span class="pagination__summary">Trang ${page} / ${totalPages}</span>
                        <a class="pagination__item ${page le 1 ? 'pagination__item--disabled' : ''}"
                           href="${page le 1 ? '#' : prevUrl}" aria-disabled="${page le 1}">«</a>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <c:url var="pageUrl" value="/products">
                                <c:param name="type" value="${selectedType}" />
                                <c:param name="page" value="${i}" />
                                <c:param name="pageSize" value="${pageSize}" />
                                <c:forEach var="subtype" items="${selectedSubtypes}">
                                    <c:param name="subtype" value="${subtype}" />
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
<script>
  (function() {
    var form = document.getElementById('product-subtype-form');
    if (!form || form.dataset.enhanced === 'true') {
      return;
    }
    form.dataset.enhanced = 'true';
    var checkboxes = form.querySelectorAll('input[type="checkbox"]');
    checkboxes.forEach(function(input) {
      input.addEventListener('change', function() {
        var pageInput = form.querySelector('input[name="page"]');
        if (pageInput) {
          pageInput.value = '1';
        }
        form.submit();
      });
    });
  })();
</script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
