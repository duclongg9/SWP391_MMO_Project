<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<<<<<<< HEAD
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
=======
<%@ page import="java.util.List" %>
<%@ page import="model.Products" %>
<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Bảng điều khiển");
    request.setAttribute("headerSubtitle", "Tổng quan nhanh về thị trường của bạn");
    request.setAttribute("headerModifier", "layout__header--split");
%>
>>>>>>> origin/hoa
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content dashboard">
    <section class="dashboard__row">
        <article class="stat-card">
            <div class="icon icon--primary">📦</div>
            <div>
                <p class="stat-card__label">Tổng sản phẩm</p>
                <p class="stat-card__value"><c:out value="${totalProducts}" /></p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--accent">💰</div>
            <div>
                <p class="stat-card__label">Doanh thu tháng</p>
                <p class="stat-card__value">
                    <fmt:formatNumber value="${monthlyRevenue}" type="currency" currencySymbol="đ"
                                      maxFractionDigits="0" minFractionDigits="0" />
                </p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--muted">⏳</div>
            <div>
                <p class="stat-card__label">Đơn chờ duyệt</p>
                <p class="stat-card__value"><c:out value="${pendingOrders}" /></p>
            </div>
        </article>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sản phẩm nổi bật</h2>
            <c:url var="productsUrl" value="/products" />
            <form class="search-bar" method="get" action="${productsUrl}">
                <label class="search-bar__icon" for="keyword">🔍</label>
                <input class="search-bar__input" type="text" id="keyword" name="q" placeholder="Tìm sản phẩm...">
                <button class="button button--primary" type="submit">Tìm kiếm</button>
            </form>
        </div>
        <ul class="product-grid">
            <c:choose>
                <c:when test="${not empty featuredProducts}">
                    <c:forEach var="p" items="${featuredProducts}">
                        <li class="product-card">
                            <h3><c:out value="${p.name}" /></h3>
                            <p><c:out value="${p.description}" /></p>
                            <span class="product-card__price">
                                <fmt:formatNumber value="${p.price}" type="currency" currencySymbol="đ"
                                                  maxFractionDigits="0" minFractionDigits="0" />
                            </span>
                            <span class="badge"><c:out value="${p.status}" /></span>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li class="product-card product-card--empty">
                        <p>Chưa có sản phẩm nào được duyệt.</p>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
