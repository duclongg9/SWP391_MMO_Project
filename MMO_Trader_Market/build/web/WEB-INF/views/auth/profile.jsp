<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <section class="panel" style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:24px;align-items:start">
       <li class="product-card">
               <div class="panel__header">
            <h2 class="panel__title">Thông tin người dùng</h2>
            
               </div>
       </li>
       <li class="product-card">
               <div class="panel__header">
            <h2 class="panel__title">Thông tin người dùng</h2>
            
               </div>
       </li>
        
    </section>
   
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
