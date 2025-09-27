<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Bảng điều khiển - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header">
    <h1>Bảng điều khiển</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/products">Danh sách sản phẩm</a>
        <a href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
    </nav>
</header>
<main class="layout__content">
    <section>
        <h2>Sản phẩm nổi bật</h2>
        <c:if test="${empty products}">
            <p>Chưa có sản phẩm nào được duyệt.</p>
        </c:if>
        <ul class="product-grid">
            <c:forEach var="product" items="${products}">
                <li class="product-card">
                    <h3>${product.name}</h3>
                    <p>${product.description}</p>
                    <span class="product-card__price">${product.price} đ</span>
                </li>
            </c:forEach>
        </ul>
    </section>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
