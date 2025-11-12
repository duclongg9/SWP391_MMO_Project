<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free/css/all.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/components/notification.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components/kyc.css">
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
    <!-- Alerts -->
    <div style="grid-column: 1 / -1;">
      <c:if test="${not empty msg}">
        <div class="alert alert--success" role="status" aria-live="polite">${msg}</div>
      </c:if>
      <c:if test="${not empty emg}">
        <div class="alert alert--error" role="alert" aria-live="assertive">${emg}</div>
      </c:if>
    </div>

    <!-- FORM CHUYỂN KHOẢN -->
    <section class="panel">
      <form method="post"
            action="${pageContext.request.contextPath}/withdraw"
            class="form-card"
            id="bankTransferForm"
            autocomplete="off">
        <div class="panel__header">
          <div class="panel__header-text">
            <h2 class="panel__title">Thông tin chuyển khoản</h2>
            <p class="panel__subtitle">Điền đầy đủ thông tin để tạo yêu cầu chuyển khoản.</p>
          </div>
          <span class="panel__tag"><i class="fa-solid fa-building-columns"></i>&nbsp;Bank Transfer</span>
        </div>

        <div class="panel__body">
          <div class="form-card__grid form-card__grid--two-cols">
            <!-- Họ tên chủ tài khoản -->
            <div class="form-card__field">
              <label for="fullName" class="form-card__label">Tên chủ tài khoản<span class="required-star" data-tooltip="Mục này là bắt buộc">*</span></label>
              <input id="accountName" name="accountName" type="text" class="form-card__input"
                     placeholder="VD: Nguyen Van A" required maxlength="100">
              <p class="form-note">Tên hiển thị trên tài khoản ngân hàng.</p>
            </div>

            <!-- Số tài khoản -->
            <div class="form-card__field">
              <label for="accountNumber" class="form-card__label">Số tài khoản<span class="required-star" data-tooltip="Mục này là bắt buộc">*</span></label>
              <input id="accountNumber" name="accountNumber" inputmode="numeric" pattern="\d{6,20}"
                     title="Chỉ nhập số, từ 6 đến 20 ký tự"
                     class="form-card__input" placeholder="VD: 0123456789" required>
              <p class="form-note">Chỉ gồm chữ số (6–20 ký tự).</p>
            </div>
            </div>
            <!-- Ngân hàng (dropdown) -->
            <div class="form-card__field">
            <label for="bankCode" class="form-card__label">Ngân hàng<span class="required-star" data-tooltip="Mục này là bắt buộc">*</span></label>
            <select id="bankCode" name="bankCode" class="form-card__input" required>
              <option value="" disabled ${empty param.bankCode ? 'selected' : ''}>-- Chọn ngân hàng --</option>
              <c:forEach items="${banks}" var="b">
                <option value="${b.code}" ${param.bankCode == b.code ? 'selected' : ''}>
                  ${b.shortName} - ${b.name}
                </option>
              </c:forEach>
            </select>
            <p class="form-note">Chọn đúng ngân hàng nhận tiền.</p>
            </div>
              
            <!-- Số tiền -->
            <div class="form-card__field">
              <label for="amount" class="form-card__label">Số tiền cần rút (VND)<span class="required-star" data-tooltip="Mục này là bắt buộc">*</span></label>
              <input id="amount" name="amount" type="number" min="100000" step="1000"
                     class="form-card__input" placeholder="VD: 100000" required>
              <p class="form-note">Tối thiểu 100.000 VND.</p>
            </div>

            <!-- Nội dung chuyển khoản -->
            <div class="form-card__field" style="grid-column: 1 / -1;">
              <label for="transferNote" class="form-card__label">Nội dung chuyển khoản</label>
              <input id="transferNote" name="transferNote" type="text" class="form-card__input"
                     placeholder="VD: NAP VNPAY - UserID 12345 - Đơn #A1B2C3" maxlength="160" required>
              <p class="form-note">Ghi rõ nội dung để hệ thống đối soát nhanh.</p>
            </div>
          
        </div>

        <div class="panel__footer">
          <button type="submit" class="button button--primary">
            <i class="fa-solid fa-paper-plane"></i>&nbsp;Tạo yêu cầu
          </button>
          <button type="reset" class="button button--ghost">
            <i class="fa-solid fa-rotate-left"></i>&nbsp;Làm mới
          </button>
        </div>
      </form>
    </section>

 <div id="toastBox"></div> 
  </section>
</main>

              
              
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
     document.addEventListener('DOMContentLoaded', () => {
            const toastBox = document.getElementById('toastBox');

            function showToast(msg, type = 'success') {
                if (!toastBox) return;
                const toast = document.createElement('div');
                toast.classList.add('mm-toast');
                if (type === 'error') toast.classList.add('error');
                toast.innerHTML = msg;
                toastBox.appendChild(toast);
                setTimeout(() => toast.remove(), 5000);
            }

            // Lấy message từ request
            const okMsg = "${fn:escapeXml(msg)}";
            const errMsg = "${fn:escapeXml(emg)}";

            if(errMsg) showToast('<i class="fa fa-times-circle"></i> ' + errMsg, 'error');
            if(okMsg) showToast('<i class="fa fa-check-circle"></i> ' + okMsg, 'success');
        });
        </script>
        
<c:if test="${not empty sessionScope.flash}">
    <script>
        const msg = "${fn:escapeXml(sessionScope.flash)}";

        const icon = msg.toLowerCase().includes("lỗi")
            ? '<i class="fa fa-times-circle"></i>'
            : '<i class="fa fa-check-circle"></i>';

        showToast(icon + " " + msg, msg.toLowerCase().includes("lỗi") ? "error" : "success");
    </script>
    <c:remove var="flash" scope="session"/>
</c:if>