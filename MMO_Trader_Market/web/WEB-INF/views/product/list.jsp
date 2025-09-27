<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Product" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh sách sản phẩm - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header layout__header--split">
    <div>
        <h1>Danh sách sản phẩm</h1>
        <p class="subtitle">Quản lý sản phẩm theo mô hình MVC</p>
    </div>
    <nav class="menu menu--horizontal">
        <a class="menu__item" href="${pageContext.request.contextPath}/dashboard">Bảng điều khiển</a>
        <a class="menu__item" href="${pageContext.request.contextPath}/styleguide">Thư viện giao diện</a>
    </nav>
</header>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bộ lọc nhanh</h2>
            <form class="search-bar" method="get" action="${pageContext.request.contextPath}/products">
                <label class="search-bar__icon" for="keyword">🔎</label>
                <input class="search-bar__input" type="search" id="keyword" name="keyword" placeholder="Nhập từ khóa...">
                <button class="button button--ghost" type="submit">Lọc</button>
            </form>
        </div>
        <table class="table table--interactive">
            <thead>
            <tr>
                <th>Mã</th>
                <th>Tên sản phẩm</th>
                <th>Mô tả</th>
                <th>Giá</th>
                <th>Trạng thái</th>
                <th class="table__actions">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <%
                List<Product> products = (List<Product>) request.getAttribute("products");
                if (products != null) {
                    for (Product product : products) {
            %>
            <tr>
                <td><%= product.getId() %></td>
                <td><%= product.getName() %></td>
                <td><%= product.getDescription() %></td>
                <td><%= product.getPrice() %> đ</td>
                <td><span class="badge"><%= product.getStatus() %></span></td>
                <td class="table__actions">
                    <button class="button button--ghost" type="button">Sửa</button>
                    <button class="button button--danger" type="button">Xóa</button>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
        <nav class="pagination" aria-label="Phân trang sản phẩm">
            <a class="pagination__item pagination__item--disabled" href="#" aria-disabled="true">«</a>
            <a class="pagination__item pagination__item--active" href="#">1</a>
            <a class="pagination__item" href="#">2</a>
            <a class="pagination__item" href="#">3</a>
            <a class="pagination__item" href="#">»</a>
        </nav>
    </section>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
