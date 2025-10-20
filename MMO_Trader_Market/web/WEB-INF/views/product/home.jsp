<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content landing">
    <c:set var="summary" value="${summary}" />
    <section class="panel landing__hero">
        <div class="landing__hero-main">
            <h2>MMO Trader Market</h2>
            <p class="landing__lead">Số liệu dưới đây được lấy trực tiếp từ cơ sở dữ liệu sản phẩm và đơn hàng.</p>
            <ul class="landing__metrics">
                <li>
                    <strong><fmt:formatNumber value="${summary.availableProductCount}" type="number" /></strong>
                    sản phẩm đang mở bán
                </li>
                <li>
                    <strong><fmt:formatNumber value="${summary.pendingOrderCount}" type="number" /></strong>
                    đơn hàng chờ xử lý
                </li>
                <li>
                    <strong><fmt:formatNumber value="${summary.completedOrderCount}" type="number" /></strong>
                    đơn hàng đã hoàn tất
                </li>
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.totalRevenue}" type="currency" currencySymbol="" /> đ
                    </strong>
                    doanh thu ghi nhận
                </li>
            </ul>
            <div class="landing__cta">
                <a class="button button--primary" href="${pageContext.request.contextPath}/auth">Đăng nhập</a>
                <a class="button button--ghost" href="${pageContext.request.contextPath}/products">Quản lý sản phẩm</a>
            </div>
        </div>
    </section>

    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm nổi bật</h3>
            <span class="panel__tag">Trạng thái 'Available'</span>
        </div>
        <c:choose>
            <c:when test="${empty featuredProducts}">
                <p>Chưa có sản phẩm khả dụng trong hệ thống.</p>
            </c:when>
            <c:otherwise>
                <div class="landing__products">
                    <c:forEach var="product" items="${featuredProducts}">
                        <article class="product-card">
                            <header class="product-card__header">
                                <h4><c:out value="${product.name}" /></h4>
                                <span class="badge"><c:out value="${product.status}" /></span>
                            </header>
                            <p class="product-card__description"><c:out value="${product.description}" /></p>
                            <ul class="product-card__meta">
                                <li>Mã sản phẩm: <strong>#<c:out value="${product.id}" /></strong></li>
                                <li>Tồn kho: <strong><c:out value="${product.inventoryCount}" /></strong></li>
                                <li>
                                    Giá bán: <strong>
                                    <fmt:formatNumber value="${product.price}" type="number" minFractionDigits="0" /> đ
                                </strong>
                                </li>
                            </ul>
                            <footer class="product-card__footer">
                                <a class="button button--ghost" href="${pageContext.request.contextPath}/products">Chi tiết</a>
                                <c:choose>
                                    <c:when test="${product.inventoryCount > 0 && product.status eq 'Available'}">
                                        <form method="post" action="${pageContext.request.contextPath}/order/buy-now" style="display:inline;">
                                            <input type="hidden" name="productId" value="${product.id}" />
                                            <input type="hidden" name="qty" value="1" />
                                            <button class="button button--primary" type="submit">Mua ngay</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge--warning">Không khả dụng</span>
                                    </c:otherwise>
                                </c:choose>
                            </footer>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Shop hoạt động</h3>
        </div>
        <c:choose>
            <c:when test="${empty shops}">
                <p>Chưa có shop nào đang hoạt động.</p>
            </c:when>
            <c:otherwise>
                <ul class="category-menu category-menu--grid">
                    <c:forEach var="shop" items="${shops}">
                        <li class="category-menu__item">
                            <span class="category-menu__icon">🏬</span>
                            <div>
                                <strong><c:out value="${shop.name}" /></strong>
                                <p><c:out value="${shop.description}" /></p>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
