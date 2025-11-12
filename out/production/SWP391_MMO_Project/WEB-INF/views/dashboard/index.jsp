<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.DecimalFormatSymbols" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Products" %>
<%
    request.setAttribute("pageTitle", "Bảng điều khiển - MMO Trader Market");
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
    
    // Lấy dữ liệu từ request
    Integer totalProducts = (Integer) request.getAttribute("totalProducts");
    if (totalProducts == null) totalProducts = 0;
    
    BigDecimal monthlyRevenue = (BigDecimal) request.getAttribute("monthlyRevenue");
    if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
    
    Integer completedOrders = (Integer) request.getAttribute("completedOrders");
    if (completedOrders == null) completedOrders = 0;
    
    // Format số tiền với dấu phẩy
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
    symbols.setGroupingSeparator(',');
    DecimalFormat currencyFormat = new DecimalFormat("#,###", symbols);
    
    // Format doanh thu
    String formattedRevenue = "0";
    if (monthlyRevenue != null && monthlyRevenue.compareTo(BigDecimal.ZERO) >= 0) {
        // Kiểm tra nếu là số nguyên
        if (monthlyRevenue.scale() == 0 || monthlyRevenue.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            formattedRevenue = currencyFormat.format(monthlyRevenue.longValue());
        } else {
            DecimalFormat revenueFormat = new DecimalFormat("#,###.##", symbols);
            formattedRevenue = revenueFormat.format(monthlyRevenue.doubleValue());
        }
    }
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content dashboard">
    <section class="dashboard__row">
        <article class="stat-card">
            <div class="icon icon--primary">📦</div>
            <div>
                <p class="stat-card__label">Tổng sản phẩm</p>
                <p class="stat-card__value"><%= totalProducts %></p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--accent">💰</div>
            <div>
                <p class="stat-card__label">Doanh thu tháng</p>
                <p class="stat-card__value"><%= formattedRevenue %> ₫</p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--muted">✅</div>
            <div>
                <p class="stat-card__label">Đơn đã bán</p>
                <p class="stat-card__value"><%= completedOrders %></p>
            </div>
        </article>
        <article class="stat-card">
            <div class="icon icon--primary">📊</div>
            <div>
                <p class="stat-card__label">Tổng tồn kho</p>
                <p class="stat-card__value"><%= request.getAttribute("totalInventory") != null ? request.getAttribute("totalInventory") : 0 %></p>
            </div>
        </article>
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sản phẩm của shop</h2>
            <form class="search-bar" method="get" action="<%= request.getContextPath() %>/dashboard">
                <label class="search-bar__icon" for="keyword">🔍</label>
                <input class="search-bar__input" type="text" id="keyword" name="keyword" 
                       placeholder="Tìm sản phẩm..." 
                       value="<%= request.getAttribute("keyword") != null ? request.getAttribute("keyword") : "" %>">
                <button class="button button--primary" type="submit">Tìm kiếm</button>
                <% if (request.getAttribute("keyword") != null && !((String) request.getAttribute("keyword")).isEmpty()) { %>
                    <a href="<%= request.getContextPath() %>/dashboard" class="button" style="margin-left: 0.5rem;">Xóa</a>
                <% } %>
            </form>
        </div>
        <ul class="product-grid">
            <%
                List<Products> products = (List<Products>) request.getAttribute("products");
                if (products == null || products.isEmpty()) {
            %>
            <li class="product-card product-card--empty">
                <p>Chưa có sản phẩm nào được duyệt.</p>
            </li>
            <%
                } else {
                    for (Products product : products) {
                        // Format giá sản phẩm
                        BigDecimal price = product.getPrice();
                        String formattedPrice = "";
                        if (price != null) {
                            // Kiểm tra nếu là số nguyên (không có phần thập phân)
                            if (price.scale() == 0 || price.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                                formattedPrice = currencyFormat.format(price.longValue());
                            } else {
                                // Nếu có phần thập phân, format với phần thập phân
                                DecimalFormat priceFormat = new DecimalFormat("#,###.##", symbols);
                                formattedPrice = priceFormat.format(price.doubleValue());
                            }
                        }
            %>
            <li class="product-card">
                <h3><%= product.getName() %></h3>
                <p><%= product.getShortDescription() != null ? product.getShortDescription() : (product.getDescription() != null && product.getDescription().length() > 100 ? product.getDescription().substring(0, 100) + "..." : product.getDescription()) %></p>
                <span class="product-card__price"><%= formattedPrice %> ₫</span>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem; gap: 0.5rem; flex-wrap: wrap;">
                    <span style="color: #666; font-size: 0.875rem;">
                        📦 Tồn kho: <strong><%= product.getInventoryCount() != null ? product.getInventoryCount() : 0 %></strong>
                    </span>
                    <span class="badge"><%= product.getStatus() != null ? product.getStatus() : "" %></span>
                </div>
            </li>
            <%
                    }
                }
            %>
        </ul>
        <% 
            Integer currentPage = (Integer) request.getAttribute("page");
            Integer totalPages = (Integer) request.getAttribute("totalPages");
            String currentKeyword = (String) request.getAttribute("keyword");
            if (currentPage == null) currentPage = 1;
            if (totalPages == null) totalPages = 1;
            if (currentKeyword == null) currentKeyword = "";
        %>
        <% if (totalPages > 1) { %>
            <div style="display: flex; justify-content: center; align-items: center; gap: 0.5rem; margin-top: 2rem; padding: 1rem;">
                <% if (currentPage > 1) { %>
                    <a href="<%= request.getContextPath() %>/dashboard?page=<%= currentPage - 1 %><%= currentKeyword.isEmpty() ? "" : "&keyword=" + java.net.URLEncoder.encode(currentKeyword, "UTF-8") %>" 
                       class="button">‹ Trước</a>
                <% } %>
                <% 
                    int startPage = Math.max(1, currentPage - 2);
                    int endPage = Math.min(totalPages, currentPage + 2);
                    for (int i = startPage; i <= endPage; i++) {
                %>
                    <% if (i == currentPage) { %>
                        <span class="button button--primary" style="pointer-events: none;"><%= i %></span>
                    <% } else { %>
                        <a href="<%= request.getContextPath() %>/dashboard?page=<%= i %><%= currentKeyword.isEmpty() ? "" : "&keyword=" + java.net.URLEncoder.encode(currentKeyword, "UTF-8") %>" 
                           class="button"><%= i %></a>
                    <% } %>
                <% } %>
                <% if (currentPage < totalPages) { %>
                    <a href="<%= request.getContextPath() %>/dashboard?page=<%= currentPage + 1 %><%= currentKeyword.isEmpty() ? "" : "&keyword=" + java.net.URLEncoder.encode(currentKeyword, "UTF-8") %>" 
                       class="button">Sau ›</a>
                <% } %>
            </div>
        <% } %>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
