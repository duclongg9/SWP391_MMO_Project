<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("pageTitle", "MMO Trader Market - Đăng ký");
    request.setAttribute("bodyClass", "layout layout--center");
    request.setAttribute("headerTitle", "MMO Trader Market");
    request.setAttribute("headerSubtitle", "Tạo tài khoản mới");
    String errorMessage = (String) request.getAttribute("error");
    String emailValue = (String) request.getAttribute("email");
    String nameValue = (String) request.getAttribute("name");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <%
        if (errorMessage != null && !errorMessage.isBlank()) {
    %>
    <div class="alert alert--error"><%= errorMessage %></div>
    <%
        }
    %>
    <form method="post" action="<%= request.getContextPath() %>/register" class="form-card">
        <label class="form-card__label" for="name">Tên hiển thị</label>
        <input class="form-card__input" id="name" name="name" type="text" placeholder="VD: Nguyễn Văn A" value="<%= nameValue != null ? nameValue : "" %>" required>

        <label class="form-card__label" for="email">Email</label>
        <input class="form-card__input" id="email" name="email" type="email" placeholder="example@email.com" value="<%= emailValue != null ? emailValue : "" %>" required>

        <label class="form-card__label" for="password">Mật khẩu</label>
        <input class="form-card__input" id="password" name="password" type="password" placeholder="Tối thiểu 8 ký tự gồm chữ và số" required>

        <label class="form-card__label" for="confirmPassword">Xác nhận mật khẩu</label>
        <input class="form-card__input" id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required>

        <button class="button button--primary" type="submit">Đăng ký</button>
    </form>
    <section class="guide-link">
        <p>Đã có tài khoản?</p>
        <a class="button button--ghost" href="<%= request.getContextPath() %>/auth">Quay lại trang đăng nhập</a>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>