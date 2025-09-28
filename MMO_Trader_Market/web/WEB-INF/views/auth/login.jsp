<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "MMO Trader Market - Đăng nhập");
    request.setAttribute("bodyClass", "layout layout--center");
    request.setAttribute("headerTitle", "MMO Trader Market");
    request.setAttribute("headerSubtitle", "Đăng nhập để quản lý giao dịch");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <%
        String errorMessage = (String) request.getAttribute("error");
        if (errorMessage != null && !errorMessage.isEmpty()) {
    %>
    <div class="alert alert--error"><%= errorMessage %></div>
    <%
        }
    %>
    <form method="post" action="<%= request.getContextPath() %>/auth" class="form-card">
        <label class="form-card__label" for="username">Tên đăng nhập</label>
        <input class="form-card__input" id="username" name="username" type="text" placeholder="nhập email hoặc username" required>

        <label class="form-card__label" for="password">Mật khẩu</label>
        <input class="form-card__input" id="password" name="password" type="password" placeholder="••••••••" required>

        <button class="button button--primary" type="submit">Đăng nhập</button>
    </form>
    <section class="guide-link">
        <p>Mới tham gia dự án?</p>
        <a class="button button--ghost" href="<%= request.getContextPath() %>/styleguide">Xem thư viện giao diện</a>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

