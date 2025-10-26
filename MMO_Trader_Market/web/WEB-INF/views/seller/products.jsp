<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Products" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    List<Products> products = (List<Products>) request.getAttribute("products");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int totalPages = (Integer) request.getAttribute("totalPages");
    long totalProducts = (Long) request.getAttribute("totalProducts");
    String keyword = (String) request.getAttribute("keyword");
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    request.setAttribute("bodyClass", "layout seller-page");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <div class="container">
        <div class="page-header">
            <div>
                <h1>Quản Lý Sản Phẩm</h1>
                <p>Tổng: <%= totalProducts %> sản phẩm</p>
            </div>
            <a href="<%= request.getContextPath() %>/seller/products/create" class="button button--primary">
                ➕ Thêm sản phẩm mới
            </a>
        </div>

        <%-- Search and Filter --%>
        <div class="search-box">
            <form method="get" action="<%= request.getContextPath() %>/seller/products" class="search-form">
                <input type="text" name="keyword" class="search-input" placeholder="Tìm kiếm sản phẩm..." value="<%= keyword != null ? keyword : "" %>">
                <button type="submit" class="button button--primary">🔍 Tìm</button>
            </form>
        </div>

        <div class="products-table">
            <% if (products == null || products.isEmpty()) { %>
                <div class="empty-state">
                    <p>Chưa có sản phẩm nào. Hãy thêm sản phẩm đầu tiên của bạn!</p>
                </div>
            <% } else { %>
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên sản phẩm</th>
                            <th>Giá</th>
                            <th>Tồn kho</th>
                            <th>Đã bán</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Products product : products) { %>
                        <tr>
                            <td><%= product.getId() %></td>
                            <td><%= product.getName() %></td>
                            <td><%= String.format("%,.0f", product.getPrice()) %> đ</td>
                            <td><%= product.getInventoryCount() %></td>
                            <td><%= product.getSoldCount() %></td>
                            <td>
                                <% if ("Available".equals(product.getStatus())) { %>
                                    <span class="badge badge--success">Đang bán</span>
                                <% } else { %>
                                    <span class="badge badge--warning">Ngừng bán</span>
                                <% } %>
                            </td>
                            <td>
                                <a href="<%= request.getContextPath() %>/seller/products/edit?id=<%= product.getId() %>">Sửa</a> |
                                <a href="<%= request.getContextPath() %>/seller/products/delete?id=<%= product.getId() %>" onclick="return confirm('Bạn có chắc muốn ngừng bán sản phẩm này?')" class="action-link action-link--danger">Ngừng bán</a>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>

        <%-- Pagination --%>
        <div class="pagination">
            <% for (int i = 1; i <= totalPages; i++) { %>
                <a href="?page=<%= i %>" class="<%= i == currentPage ? "active" : "" %>"><%= i %></a>
            <% } %>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
<style>
.container {max-width: 1200px; margin: 0 auto; padding: 20px;}
.products-table {background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}
.table {width: 100%; border-collapse: collapse;}
.table th, .table td {padding: 12px; text-align: left; border-bottom: 1px solid #eee;}
</style>
