<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Product" %>
<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Menu");
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
<main class="layout__content grid-2">
    <section class="panel profile-grid" >
 
      
      

        
        <!--Alerts -->
  <div style="grid-column: 1 / -1;">
    <c:if test="${not empty msg}">
      <div class="alert alert--success" role="status" aria-live="polite">${msg}</div>
    </c:if>
    <c:if test="${not empty emg}">
      <div class="alert alert--danger" role="alert" aria-live="assertive">${emg}</div>
    </c:if>
  </div>
        
        <!--Thông tin ví -->
<form id="profileForm" class="" method="post"
      action="${pageContext.request.contextPath}/profile">
    <input type="hidden" name="action" value="updateProfile">
  <div class="panel__header">
    <h2 class="panel__title">Ví của bạn</h2>
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
                 autocomplete="name" placeholder="${myProfile.username}">
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
          
           <!--Lịch sử ví -->

    <input type="hidden" name="action" value="updateProfile">
  <div class="panel__header">
    <h2 class="panel__title">Lịch sử giao dịch</h2>
  </div>

    <table class="table table--interactive" role="presentation">
        <tr>
            <th scope="col">Mã</th>
            <th scope="col">Loại giao dịch</th>
            <th scope="col">Số tiền</th>
            <th scope="col">Số tiền trước giao dịch</th>
            <th scope="col">Số tiền sau giao dịch</th>
            <th scope="col">ghi chú</th>
            <th scope="col">Thời gian tạo</th>
        </tr>
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
                   autocomplete="name" placeholder="${myProfile.username}">
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



        
    </section>
   
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
