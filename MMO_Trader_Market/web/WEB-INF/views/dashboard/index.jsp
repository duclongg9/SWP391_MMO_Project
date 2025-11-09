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
    </section>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sản phẩm nổi bật</h2>
            <form class="search-bar" method="get" action="<%= request.getContextPath() %>/products">
                <label class="search-bar__icon" for="keyword">🔍</label>
                <input class="search-bar__input" type="text" id="keyword" name="keyword" placeholder="Tìm sản phẩm...">
                <button class="button button--primary" type="submit">Tìm kiếm</button>
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
                <span class="badge"><%= product.getStatus() != null ? product.getStatus() : "" %></span>
            </li>
            <%
                    }
                }
            %>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
