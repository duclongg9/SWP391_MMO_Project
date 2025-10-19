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
    <input type="hidden" name="action" value="updateProfile">
  <div class="panel__header">
    <h2 class="panel__title">Ví của bạn</h2>
  </div>

  <table class="table table--interactive" role="presentation">
    <tbody>
      <tr>
        <th scope="row">Số dư còn lại</th>
        <td style="
        font-size: 2.8rem;
        font-weight: 800;
        line-height: 1.05;
        padding: 0.9rem 1rem;
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 4.8rem; /* đảm bảo chiều cao giống ~3 hàng */
        color: #005b96; /* màu xanh dương (hoặc đổi) */
        background: rgba(0,91,150,0.04);
        border-radius: 8px;
      "
      aria-label="Số dư hiện có">
          <fmt:formatNumber value="${wallet.balance}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
        </td>
      </tr>
    </tbody>
  </table>

          
           <!--Lịch sử ví -->
<form id="profileForm" class="" method="get"
      action="${pageContext.request.contextPath}/wallet">
    <input type="hidden" name="action" value="updateProfile">
  <div class="panel__header">
    <h2 class="panel__title">Lịch sử giao dịch</h2>
  </div>

    <table class="table table--interactive" role="presentation">
        <thead>
            <th scope="col">Mã</th>
            <th scope="col">Loại giao dịch</th>
            <th scope="col">Số tiền</th>
            <th scope="col">Số tiền trước giao dịch</th>
            <th scope="col">Số tiền sau giao dịch</th>
            <th scope="col">ghi chú</th>
            <th scope="col">Thời gian tạo</th>
        </thead>
      <tbody>
          <c:forEach var="p" items="${listTransaction}">
        <tr>
          <td scope="row"><label>${p.id}</label></th>
          <td scope="row"><label>${p.transactionType}</label></th>
          <td><fmt:formatNumber value="${p.amount}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
          <td><fmt:formatNumber value="${p.balanceBefore}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
          <td><fmt:formatNumber value="${p.balanceAfter}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
          <td scope="row"><label>${p.note}</label></th>
          <td>
              <c:choose>
                <%-- ưu tiên getter trả java.util.Date --%>
                <c:when test="${not empty p.createdAtDate}">
                  <fmt:formatDate value="${p.createdAtDate}"
                                  pattern="dd/MM/yyyy HH:mm"
                                  timeZone="Asia/Ho_Chi_Minh" />
                </c:when>
                <c:when test="${not empty p.createdAt}">
                  <c:out value="${p.createdAt}" />
                </c:when>
                <c:otherwise>-</c:otherwise>
              </c:choose>
          </td>
        </tr>
        </c:forEach>
      </tbody>
    </table>
           <!-- Pagination: hiển thị khi nhiều hơn 1 trang -->
            <c:if test="${totalPages > 1}">
              <nav class="pagination" aria-label="Phân trang">
                <!-- Prev -->
                <c:choose>
                  <c:when test="${currentPage > 1}">
                    <c:url var="prevUrl" value="${pageContext.request.requestURI}">
                      <c:param name="page" value="${currentPage - 1}" />
                      <c:param name="pageSize" value="${pageSize}" />
                      <!-- nếu có filter: <c:param name="search" value="${param.search}" /> -->
                    </c:url>
                    <a class="pagination__item" href="${prevUrl}" aria-label="Previous">‹</a>
                  </c:when>
                  <c:otherwise>
                    <span class="pagination__item pagination__item--disabled" aria-disabled="true">‹</span>
                  </c:otherwise>
                </c:choose>

                <!-- Numbers (window = 5) -->
                <c:set var="window" value="5" />
                <c:set var="half" value="${window / 2}" />
                <c:set var="start" value="${currentPage - half}" />
                <c:if test="${start < 1}"><c:set var="start" value="1" /></c:if>
                <c:set var="end" value="${start + window - 1}" />
                <c:if test="${end > totalPages}">
                  <c:set var="end" value="${totalPages}" />
                  <c:set var="start" value="${end - window + 1}" />
                  <c:if test="${start < 1}"><c:set var="start" value="1" /></c:if>
                </c:if>

                <!-- first + ellipsis -->
                <c:if test="${start > 1}">
                  <c:url var="firstUrl" value="${pageContext.request.requestURI}">
                    <c:param name="page" value="1" />
                    <c:param name="pageSize" value="${pageSize}" />
                  </c:url>
                  <a class="pagination__item" href="${firstUrl}">1</a>
                  <span class="pagination__item">…</span>
                </c:if>

                <!-- in các số trong window -->
                <c:forEach var="i" begin="${start}" end="${end}">
                  <c:choose>
                    <c:when test="${i == currentPage}">
                      <span class="pagination__item pagination__item--active" aria-current="page">${i}</span>
                    </c:when>
                    <c:otherwise>
                      <c:url var="nUrl" value="${pageContext.request.requestURI}">
                        <c:param name="page" value="${i}" />
                        <c:param name="pageSize" value="${pageSize}" />
                      </c:url>
                      <a class="pagination__item" href="${nUrl}">${i}</a>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>

                <!-- ellipsis + last -->
                <c:if test="${end < totalPages}">
                  <span class="pagination__item">…</span>
                  <c:url var="lastUrl" value="${pageContext.request.requestURI}">
                    <c:param name="page" value="${totalPages}" />
                    <c:param name="pageSize" value="${pageSize}" />
                  </c:url>
                  <a class="pagination__item" href="${lastUrl}">${totalPages}</a>
                </c:if>

                <!-- Next -->
                <c:choose>
                  <c:when test="${currentPage < totalPages}">
                    <c:url var="nextUrl" value="${pageContext.request.requestURI}">
                      <c:param name="page" value="${currentPage + 1}" />
                      <c:param name="pageSize" value="${pageSize}" />
                    </c:url>
                    <a class="pagination__item" href="${nextUrl}" aria-label="Next">›</a>
                  </c:when>
                  <c:otherwise>
                    <span class="pagination__item pagination__item--disabled" aria-disabled="true">›</span>
                  </c:otherwise>
                </c:choose>
              </nav>
            </c:if>

</div>


</form>


        
    </section>
   
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
