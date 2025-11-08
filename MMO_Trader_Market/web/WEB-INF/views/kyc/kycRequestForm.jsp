<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css" type="text/css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/components/image-preview.css"/>
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

    Map<String, String> homeLink = new HashMap<>();
    homeLink.put("href", contextPath + "/home");
    homeLink.put("label", "Trở về trang chủ");
    navItems.add(homeLink);

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
                <!--Alerts -->
          <div style="grid-column: 1 / -1;">
<c:if test="${not empty msg}">
              <div class="alert alert--success" role="status" aria-live="polite">${msg}</div>
</c:if>
<c:if test="${not empty emg}">
              <div class="alert alert--error" role="alert" aria-live="assertive">${emg}</div>
</c:if>
          </div>
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
                        <label for="cccd_number" class="form-card__label">CCCD ID:<span class="required-star">*</span></label>
                        <input style="width: 30%" pattern=".{12}" title="Vui lòng nhập đúng 12 ký tự" id="cccd_number" name="cccd_number" type="text" class="form-card__input" required>
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
                          <label for="front" class="form-card__label">Chọn ảnh mặt trước CCCD:<span class="required-star">*</span></label>
                         
                      </td>
                      <td>
                          <label for="back" class="form-card__label">Chọn ảnh mặt sau CCCD:<span class="required-star">*</span></label>
                          
                      </td>
                  <tr>
                      <td>
                         <div class="form-card__field image-field">
                            <input id="front" name="front" type="file" accept="image/*" class="form-control file-input" required>
                            <img id="preview-front" class="image-preview" alt="Xem trước ảnh mặt trước">
                          </div> 
                      </td>
                      <td>
                          <div class="form-card__field image-field">
                            <input id="back" name="back" type="file" accept="image/*" class="form-control file-input" required>
                            <img id="preview-back" class="image-preview" alt="Xem trước ảnh mặt trước">
                          </div>
                      </td>
                      
                  </tr>
                  </tr>
                  <tr>
                      <td>
                          <label for="selfie" class="form-card__label">Chọn ảnh cá nhân:<span class="required-star">*</span></label>
                           <div class="form-card__field image-field">
                            <input id="selfie" name="selfie" type="file" accept="image/*" class="form-control file-input" required>
                            <img id="preview-selfie" class="image-preview" alt="Xem trước ảnh mặt trước">
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
            </form>
</section>

                <script>
                    // Khóa form khi đã có ảnh KYC
                    document.addEventListener("DOMContentLoaded", function() {
                        const form = document.getElementById("kycForm");
                        if (form && "${frontUrl}" !== "") {
                            form.querySelectorAll("input, button").forEach(el => el.disabled = true);
                        }
                    });
                </script>
                
                <script>
                function previewImage(inputId, imgId) {
                  const input = document.getElementById(inputId);
                  const img = document.getElementById(imgId);

                  input.addEventListener("change", function () {
                    const file = this.files[0];
                    if (file) {
                      const reader = new FileReader();
                      reader.onload = e => {
                        img.src = e.target.result;
                        img.classList.add("image-preview--visible");
                      };
                      reader.readAsDataURL(file);
                    } else {
                      img.src = "";
                      img.classList.remove("image-preview--visible");
                    }
                  });
                }

                document.addEventListener("DOMContentLoaded", () => {
                  previewImage("front", "preview-front");
                  previewImage("back", "preview-back");
                  previewImage("selfie", "preview-selfie");
                });
              </script>
            </section>


</section>

    </section>
   
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
