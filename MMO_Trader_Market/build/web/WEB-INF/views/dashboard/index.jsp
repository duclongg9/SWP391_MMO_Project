<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Product" %>
<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Bảng điều khiển");
    request.setAttribute("headerSubtitle", "Tổng quan nhanh về thị trường của bạn");
    request.setAttribute("headerModifier", "layout__header--split");

    List<Map<String, String>> navItems = new ArrayList<>();
    String contextPath = request.getContextPath();

    Map<String, String> productLink = new HashMap<>();
    productLink.put("href", contextPath + "/products");
    productLink.put("label", "Danh sách sản phẩm");
    navItems.add(productLink);

    Map<String, String> guideLink = new HashMap<>();
    guideLink.put("href", contextPath + "/styleguide");
    guideLink.put("label", "Thư viện giao diện");
    navItems.add(guideLink);

    Map<String, String> logoutLink = new HashMap<>();
    logoutLink.put("href", contextPath + "/auth?action=logout");
    logoutLink.put("label", "Đăng xuất");
    logoutLink.put("modifier", "menu__item--danger");
    navItems.add(logoutLink);

    request.setAttribute("navItems", navItems);
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content dashboard">
    <section class="dashboard__row">
        <article class="stat-card">
            <div class="icon icon--primary">📦</div>
            <div>
                <p class="stat-card__label">Tổng sản phẩm</p>
                <p class="stat-card__value">128</p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--accent">💰</div>
            <div>
                <p class="stat-card__label">Doanh thu tháng</p>
                <p class="stat-card__value">86.500.000 đ</p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--muted">⏳</div>
            <div>
                <p class="stat-card__label">Đơn chờ duyệt</p>
                <p class="stat-card__value">12</p>
            </div>
        </article>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sản phẩm nổi bật</h2>
            <form class="search-bar" method="get" action="<%= request.getContextPath() %>/products">
                <label class="search-bar__icon" for="keyword">🔍</label>
                <input class="search-bar__input" type="text" id="keyword" name="keyword" placeholder="Tìm sản phẩm...">
                <button class="button button--primary" type="submit">Tìm kiếm</button>
            </form>
        </div>
        <ul class="product-grid">
            <%
                List<Product> products = (List<Product>) request.getAttribute("products");
                if (products == null || products.isEmpty()) {
            %>
            <li class="product-card product-card--empty">
                <p>Chưa có sản phẩm nào được duyệt.</p>
            </li>
            <%
                } else {
                    for (Product product : products) {
            %>
            <li class="product-card">
                <h3><%= product.getName() %></h3>
                <p><%= product.getDescription() %></p>
                <span class="product-card__price"><%= product.getPrice() %> đ</span>
                <span class="badge"><%= product.getStatus() %></span>
            </li>
            <%
                    }
                }
            %>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
