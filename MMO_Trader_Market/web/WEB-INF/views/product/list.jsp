<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Products" %>
<%
    request.setAttribute("pageTitle", "Danh sách sản phẩm - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerTitle", "Danh sách sản phẩm");
    request.setAttribute("headerSubtitle", "Quản lý sản phẩm theo mô hình MVC");
    request.setAttribute("headerModifier", "layout__header--split");

    List<Map<String, String>> navItems = new ArrayList<>();
    String contextPath = request.getContextPath();

    Map<String, String> dashboardLink = new HashMap<>();
    dashboardLink.put("href", contextPath + "/dashboard");
    dashboardLink.put("label", "Bảng điều khiển");
    navItems.add(dashboardLink);

    Map<String, String> guideLink = new HashMap<>();
    guideLink.put("href", contextPath + "/styleguide");
    guideLink.put("label", "Thư viện giao diện");
    navItems.add(guideLink);

    Map<String, String> orderLink = new HashMap<>();
    orderLink.put("href", contextPath + "/orders");
    orderLink.put("label", "Đơn đã mua");
    navItems.add(orderLink);

    request.setAttribute("navItems", navItems);
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bộ lọc nhanh</h2>
            <form class="search-bar" method="get" action="<%= request.getContextPath() %>/products">
                <label class="search-bar__icon" for="keyword">🔎</label>
                <input class="search-bar__input" type="search" id="keyword" name="keyword" placeholder="Nhập từ khóa...">
                <button class="button button--ghost" type="submit">Lọc</button>
            </form>
        </div>
        <table class="table table--interactive">
            <thead>
            <tr>
                <th>Mã</th>
                <th>Tên sản phẩm</th>
                <th>Mô tả</th>
                <th>Giá</th>
                <th>Trạng thái</th>
                <th class="table__actions">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <%
                List<Products> products = (List<Products>) request.getAttribute("products");
                if (products != null) {
                    for (Products product : products) {
            %>
            <tr>
                <td><%= product.getId() %></td>
                <td><%= product.getName() %></td>
                <td><%= product.getDescription() %></td>
                <td><%= product.getPrice() %> đ</td>
                <td><span class="badge"><%= product.getStatus() %></span></td>
                <td class="table__actions">
                    <button class="button button--ghost" type="button">Sửa</button>
                    <button class="button button--danger" type="button">Xóa</button>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
        <nav class="pagination" aria-label="Phân trang sản phẩm">
            <a class="pagination__item pagination__item--disabled" href="#" aria-disabled="true">«</a>
            <a class="pagination__item pagination__item--active" href="#">1</a>
            <a class="pagination__item" href="#">2</a>
            <a class="pagination__item" href="#">3</a>
            <a class="pagination__item" href="#">»</a>
        </nav>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
