<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>MMO Trader Market - Đăng nhập</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout layout--center">
<header class="layout__header">
    <h1>MMO Trader Market</h1>
    <p>Đăng nhập để quản lý giao dịch</p>
</header>
<main class="layout__content">
    <%
        String errorMessage = (String) request.getAttribute("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
    %>
    <div class="alert alert--error"><%= errorMessage %></div>
    <%
        }
    %>
    <form method="post" action="${pageContext.request.contextPath}/auth" class="form-card">
        <label class="form-card__label" for="username">Tên đăng nhập</label>
        <input class="form-card__input" id="username" name="username" type="text" placeholder="nhập email hoặc username" required>

        <label class="form-card__label" for="password">Mật khẩu</label>
        <input class="form-card__input" id="password" name="password" type="password" placeholder="••••••••" required>

        <button class="button button--primary" type="submit">Đăng nhập</button>
    </form>
    <section class="guide-link">
        <p>Mới tham gia dự án?</p>
        <a class="button button--ghost" href="${pageContext.request.contextPath}/styleguide">Xem thư viện giao diện</a>
    </section>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
