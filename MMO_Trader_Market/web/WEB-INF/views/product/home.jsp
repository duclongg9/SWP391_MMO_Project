<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Product" %>
<%@ page import="model.ProductCategory" %>
<%@ page import="model.ProductStatus" %>
<%@ page import="model.CustomerProfile" %>
<%@ page import="model.Review" %>
<%
    List<ProductCategory> categories = (List<ProductCategory>) request.getAttribute("categories");
    List<Product> featuredProducts = (List<Product>) request.getAttribute("featuredProducts");
    CustomerProfile profile = (CustomerProfile) request.getAttribute("customerProfile");
    List<Review> reviews = (List<Review>) request.getAttribute("reviews");
    List<String> buyerTips = (List<String>) request.getAttribute("buyerTips");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content landing">
    <%-- /** Hero section: highlights the marketplace value proposition */ --%>
    <section class="panel landing__hero">
        <div class="landing__hero-main">
            <h2>Chợ tài khoản MMO dành cho seller và buyer chuyên nghiệp</h2>
            <p class="landing__lead">
                Nơi bạn có thể tìm thấy tài khoản game, mail doanh nghiệp, phần mềm bản quyền và nhiều hơn thế nữa.
                Toàn bộ dữ liệu trên trang chỉ nhằm mục đích demo basecode phục vụ học tập.
            </p>
            <ul class="landing__metrics">
                <li><strong>250+</strong> giao dịch hoàn tất mỗi tháng</li>
                <li><strong>50+</strong> đối tác uy tín đang hoạt động</li>
                <li><strong>24/7</strong> hỗ trợ đa kênh qua chat, ticket</li>
            </ul>
            <div class="landing__cta">
                <a class="button button--primary" href="<%= request.getContextPath() %>/login.jsp">Đăng nhập ngay</a>
                <a class="button button--ghost" href="<%= request.getContextPath() %>/products">Xem quản trị</a>
            </div>
        </div>
        <aside class="landing__categories">
            <h3 class="landing__aside-title">Danh mục nổi bật</h3>
            <ul class="category-menu">
                <% if (categories != null) { %>
                    <% for (ProductCategory category : categories) { %>
                        <li class="category-menu__item">
                            <span class="category-menu__icon"><%= category.getIcon() %></span>
                            <div>
                                <strong><%= category.getName() %></strong>
                                <p><%= category.getDescription() %></p>
                            </div>
                        </li>
                    <% } %>
                <% } %>
            </ul>
        </aside>
    </section>

    <%-- /** Featured product cards */ --%>
    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm gợi ý</h3>
            <span class="panel__tag">Demo dữ liệu</span>
        </div>
        <div class="landing__products">
            <% if (featuredProducts != null) { %>
                <% for (Product product : featuredProducts) { %>
                    <article class="product-card">
                        <header class="product-card__header">
                            <h4><%= product.getName() %></h4>
                            <span class="badge">Tài khoản</span>
                        </header>
                        <p class="product-card__description"><%= product.getDescription() %></p>
                        <ul class="product-card__meta">
                            <li>Mã sản phẩm: <strong>#<%= product.getId() %></strong></li>
                            <li>Giá đề xuất: <strong><%= product.getPrice() %> đ</strong></li>
                            <li>Trạng thái duyệt: <strong><%= product.getStatus() %></strong></li>
                        </ul>
                        <footer class="product-card__footer">
                            <a class="button button--ghost" href="#">Xem chi tiết demo</a>
                            <% if (product.getStatus() == ProductStatus.APPROVED) { %>
                            <a class="button button--primary" href="<%= request.getContextPath() %>/orders/buy?productId=<%= product.getId() %>">Mua ngay</a>
                            <% } else { %>
                            <span class="badge badge--warning">Đang chờ duyệt</span>
                            <% } %>
                        </footer>
                    </article>
                <% } %>
            <% } %>
        </div>
    </section>

    <%-- /** Customer profile with quick facts */ --%>
    <section class="panel landing__section landing__grid">
        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Khách hàng tiêu biểu</h3>
            </div>
            <% if (profile != null) { %>
            <div class="profile-card">
                <h4><%= profile.getDisplayName() %></h4>
                <p class="profile-card__subtitle"><%= profile.getMembershipLevel() %></p>
                <dl class="profile-card__stats">
                    <div>
                        <dt>Ngày tham gia</dt>
                        <dd><%= profile.getJoinDate().toString() %></dd>
                    </div>
                    <div>
                        <dt>Đơn hàng thành công</dt>
                        <dd><%= profile.getSuccessfulOrders() %> đơn</dd>
                    </div>
                    <div>
                        <dt>Độ hài lòng</dt>
                        <dd><%= profile.getSatisfactionScore() %>/5.0</dd>
                    </div>
                </dl>
                <p class="profile-card__note">Khách hàng đã được xác minh danh tính KYC. Thông tin chỉ dùng minh họa.</p>
            </div>
            <% } %>
        </div>
        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Đánh giá mới nhất</h3>
            </div>
            <div class="reviews">
                <% if (reviews != null) { %>
                    <% for (Review review : reviews) { %>
                        <article class="review-card">
                            <header>
                                <strong><%= review.getReviewerName() %></strong>
                                <span class="review-card__rating">★ <%= review.getRating() %>/5</span>
                            </header>
                            <p class="review-card__comment"><%= review.getComment() %></p>
                            <footer>Về sản phẩm: <em><%= review.getProductName() %></em></footer>
                        </article>
                    <% } %>
                <% } %>
            </div>
        </div>
    </section>

    <%-- /** Buyer safety tips */ --%>
    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Ghi chú an toàn cho người mua</h3>
        </div>
        <ol class="tips-list">
            <% if (buyerTips != null) { %>
                <% for (String tip : buyerTips) { %>
                    <li><%= tip %></li>
                <% } %>
            <% } %>
        </ol>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
