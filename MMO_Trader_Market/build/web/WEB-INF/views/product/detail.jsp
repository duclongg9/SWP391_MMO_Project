<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="product-detail">
        <header class="product-detail__header">
            <h2><c:out value="${product.name}" /></h2>
            <div class="product-detail__price">
                <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="đ"
                                  maxFractionDigits="0" minFractionDigits="0" />
            </div>
        </header>
        <div class="product-detail__meta">
            <span>Shop: <strong><c:out value="${product.shopName}" /></strong></span>
            <span>Tồn kho: <strong>${product.inventoryCount}</strong></span>
        </div>
        <section class="product-detail__description">
            <h3>Mô tả sản phẩm</h3>
            <c:choose>
                <c:when test="${not empty product.description}">
                    <p><c:out value="${product.description}" /></p>
                </c:when>
                <c:otherwise>
                    <p>Chưa có mô tả chi tiết.</p>
                </c:otherwise>
            </c:choose>
        </section>
        <section class="product-detail__actions">
            <c:choose>
                <c:when test="${canBuy}">
                    <form class="product-detail__form" method="post" action="${cPath}/order/buy-now">
                        <input type="hidden" name="productId" value="${product.id}" />
                        <label class="product-detail__label" for="qty">Số lượng</label>
                        <input class="product-detail__input" type="number" id="qty" name="qty" min="1"
                               value="1" max="${product.inventoryCount}" />
                        <button class="button button--primary" type="submit">Mua ngay</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="product-detail__soldout">Hết hàng</div>
                </c:otherwise>
            </c:choose>
        </section>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
