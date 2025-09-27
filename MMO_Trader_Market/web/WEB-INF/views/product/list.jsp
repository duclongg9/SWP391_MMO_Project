<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh sách sản phẩm - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header">
    <h1>Danh sách sản phẩm</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/dashboard">Bảng điều khiển</a>
    </nav>
</header>
<main class="layout__content">
    <table class="table">
        <thead>
        <tr>
            <th>Mã</th>
            <th>Tên sản phẩm</th>
            <th>Mô tả</th>
            <th>Giá</th>
            <th>Trạng thái</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="product" items="${products}">
            <tr>
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>${product.description}</td>
                <td>${product.price} đ</td>
                <td>${product.status}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
