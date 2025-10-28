<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Products" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="extraStylesheet" scope="request"
       value="${pageContext.request.contextPath}/assets/css/components/filterbar.css" />
<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content grid-2">
    <section class="panel profile-grid" style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:24px;align-items:start">



        <!-- Thời gian cập nhật mới nhất -->
        <c:if test="${not empty myProfile.updatedAt}">
            <fmt:timeZone value="Asia/Ho_Chi_Minh">
                <fmt:formatDate value="${myProfile.updatedAt}"
                                pattern="dd/MM/yyyy HH:mm"
                                var="updatedAtStr" />
            </fmt:timeZone>

            <small class="muted" title="${updatedAtStr}">
                Cập nhật hồ sơ lần cuối: ${updatedAtStr}
            </small>
        </c:if>




        <!--Alerts dùng chung cho cả 2 form-->
        <div style="grid-column: 1 / -1;">
            <c:if test="${not empty msg}">
                <div class="alert alert--success" role="status" aria-live="polite">${msg}</div>
            </c:if>
            <c:if test="${not empty emg}">
                <div class="alert alert--danger" role="alert" aria-live="assertive">${emg}</div>
            </c:if>
        </div>

        <!--Form cập nhật thông tin -->
        <form id="profileForm" class="card" method="post"
              action="${pageContext.request.contextPath}/profile">
            <input type="hidden" name="action" value="updateProfile">
            <div class="panel__header">
                <h2 class="panel__title">Thông tin người dùng</h2>
            </div>

            <table class="table table--interactive" role="presentation">
                <tbody>
                    <tr>
                        <th scope="row"><label>Ảnh đại diện</label></th>
                        <td>
                            <img src="${pageContext.request.contextPath}/assets/img/user-placeholder.svg"
                                 alt="Ảnh đại diện" class="avatar">
                        </td>
                    </tr>

                    <tr>
                        <th scope="row"><label for="uid">ID người dùng</label></th>
                        <td><input id="uid" name="userId" type="text" value="${myProfile.id}" readonly></td>
                    </tr>

                    <tr>
                        <th scope="row"><label for="fullName">Họ và tên</label></th>
                        <td>
                            <input id="fullName" name="fullName" type="text" required maxlength="100"
                                   autocomplete="name" placeholder="${myProfile.name}">
                        </td>
                    </tr>

                    <tr>
                        <th scope="row"><label for="email">Email</label></th>
                        <td>
                            <input id="email" name="email" type="email" autocomplete="email"
                                   placeholder="${myProfile.email}"readonly>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2" class="actions">
                            <input type="hidden" name="csrfToken" value="${csrf}">
                            <button type="submit" class="button button--primary">Update</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>

        <!-- ========== Form đổi mật khẩu ========== -->
        <form id="passwordForm" class="card" method="post"
              action="${pageContext.request.contextPath}/profile" novalidate>
            <input type="hidden" name="action" value="updatePassword">
            <div class="panel__header">
                <h2 class="panel__title">Đổi mật khẩu</h2>
            </div>

            <table class="table table--interactive" role="presentation">
                <tbody>
                    <tr>
                        <th scope="row"><label for="accEmail">Tài khoản </label></th>
                        <td><input id="accEmail" name="email" type="email" value="${myProfile.email}" readonly></td>
                    </tr>

                    <tr>
                        <th scope="row"><label for="oldPass">Mật khẩu cũ</label></th>
                        <td><input id="oldPass" name="oldPass" type="password" required
                                   autocomplete="current-password" minlength="8"></td>
                    </tr>

                    <tr>
                        <th scope="row"><label for="newPass">Mật khẩu mới</label></th>
                        <td><input id="newPass" name="newPass" type="password" required
                                   autocomplete="new-password" minlength="8"
                                   pattern="(?=.*[A-Za-z])(?=.*\\d).{8,}" aria-describedby="pwdHelp"><br>
                            <a style="color: red">Tối thiểu 8 ký tự, có chữ và số.</a>
                        </td>

                    </tr>
                    <tr>
                        <th scope="row"><label for="confirmPass">Nhập lại mật khẩu</label></th>
                        <td><input id="confirmPass" name="confirmPassword" type="password" required
                                   autocomplete="new-password"></td>
                    </tr>
                    <tr>
                        <td colspan="2" class="actions">
                            <input type="hidden" name="csrfToken" value="${csrf}">
                            <button type="submit" class="button button--primary">Change password</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>


    </section>

</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
