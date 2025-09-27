<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Product" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Bảng điều khiển - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header layout__header--split">
    <div>
        <h1>Bảng điều khiển</h1>
        <p class="subtitle">Tổng quan nhanh về thị trường của bạn</p>
    </div>
    <nav class="menu menu--horizontal">
        <a class="menu__item" href="${pageContext.request.contextPath}/products">Danh sách sản phẩm</a>
        <a class="menu__item" href="${pageContext.request.contextPath}/styleguide">Thư viện giao diện</a>
        <a class="menu__item menu__item--danger" href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
    </nav>
</header>
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
            <form class="search-bar" method="get" action="${pageContext.request.contextPath}/products">
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
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
