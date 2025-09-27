<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.ee/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>MMO Trader Market - Đăng nhập</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header">
    <h1>MMO Trader Market</h1>
    <p>Đăng nhập để quản lý giao dịch</p>
</header>
<main class="layout__content">
    <c:if test="${not empty error}">
        <div class="alert alert--error">${error}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/auth">
        <label for="username">Tên đăng nhập</label>
        <input id="username" name="username" type="text" required>

        <label for="password">Mật khẩu</label>
        <input id="password" name="password" type="password" required>

        <button type="submit">Đăng nhập</button>
    </form>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
