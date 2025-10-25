<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components/kyc.css">
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

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
    <section class="panel profile-grid" style="width: 100%">
        <section class="panel kyc-panel">
        <section class="panel">
            <form method="post"
                  action="${pageContext.request.contextPath}/kycRequest"
                  enctype="multipart/form-data"
                  class="kyc-layout">
                
                <div class="panel__header">
                <h2 class="panel__title">Thông tin cá nhân</h2>
              </div>
              <table class="table table--interactive" role="presentation">
                <tbody>
                  <tr>
                      <td>
                        <div class="form-card__field">
                        <label for="cccd_number" class="form-card__label">CCCD ID:</label>
                        <input style="width: 30%" id="cccd_number" name="cccd_number" type="text" class="form-card__input" required>
                      </div>
                      </td>
                  </tr>
                </tbody>
              </table>
                
                
              <!-- CỘT TRÁI (50%) -->
              <div class="panel__header">
                <h2 class="panel__title">Ảnh đính kèm</h2>
              </div>
              <table class="table table--interactive" role="presentation">
                <tbody>
                  <tr>
                      <td>
                         <div class="form-card__field">
                            <label for="front" class="form-card__label">Chọn ảnh mặt trước CCCD</label>
                            <input id="front" name="front" type="file" accept="image/*" class="form-control" required>
                         </div>
                      </td>
                      <td>
                          <div class="form-card__field">
                            <label for="back" class="form-card__label">Chọn ảnh mặt sau CCCD</label>
                            <input id="back" name="back" type="file" accept="image/*" class="form-control" required>
                          </div>
                      </td>
                  </tr>
                  <tr>
                      <td>
                           <div class="form-card__field">
                            <label for="selfie" class="form-card__label">Chọn ảnh cá nhân</label>
                            <input id="selfie" name="selfie" type="file" accept="image/*" class="form-control" required>
                          </div>
                      </td>
                      <td></td>
                  </tr>
                  <tr>
                      <td></td>
                      <td style="text-align: right">
                        <div class="kyc-actions">
                            <button class="button button--primary" type="submit">Tải lên</button>
                        </div>  
                      </td>
                  </tr>
                    
                </tbody>
              </table>
                

                
              

              <!-- CỘT PHẢI (50%) -->
              <div class="kyc-col">
               

                
              </div>

              
            
            </form>
</section>

</section>

    </section>
   
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
