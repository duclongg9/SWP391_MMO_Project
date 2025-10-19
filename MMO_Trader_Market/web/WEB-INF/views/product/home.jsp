<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content landing">

    <!-- Hero -->
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
                <a class="button button--primary" href="${pageContext.request.contextPath}/login.jsp">Đăng nhập ngay</a>
                <a class="button button--ghost" href="${pageContext.request.contextPath}/products">Xem quản trị</a>
            </div>
        </div>

        <!-- Danh mục nổi bật -->
        <aside class="landing__categories">
            <h3 class="landing__aside-title">Danh mục nổi bật</h3>
            <ul class="category-menu">
                <c:forEach var="cat" items="${categories}">
                    <li class="category-menu__item">
                        <span class="category-menu__icon">${cat.icon}</span>
                        <div>
                            <strong>${cat.name}</strong>
                            <p>${cat.description}</p>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </aside>
    </section>

    <!-- Sản phẩm gợi ý -->
    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm gợi ý</h3>
            <span class="panel__tag">Demo dữ liệu</span>
        </div>

        <div class="landing__products">
            <c:forEach var="p" items="${featuredProducts}">
                <article class="product-card">
                    <header class="product-card__header">
                        <h4>${p.name}</h4>
                        <span class="badge">Tài khoản</span>
                    </header>
                    <p class="product-card__description">${p.description}</p>
                    <ul class="product-card__meta">
                        <li>Mã sản phẩm: <strong>#${p.id}</strong></li>
                        <li>Giá đề xuất: <strong>${p.price} đ</strong></li>
                        <li>Trạng thái duyệt: <strong>${p.status}</strong></li>
                    </ul>
                    <footer class="product-card__footer">
                        <button class="button button--ghost" type="button">Xem chi tiết demo</button>
                        <button class="button button--primary" type="button">Thêm vào giỏ</button>
                    </footer>
                </article>
            </c:forEach>
        </div>
    </section>

    <!-- Khách hàng tiêu biểu & review -->
    <section class="panel landing__section landing__grid">
        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Khách hàng tiêu biểu</h3>
            </div>
            <c:if test="${not empty customerProfile}">
                <div class="profile-card">
                    <h4>${customerProfile.displayName}</h4>
                    <p class="profile-card__subtitle">${customerProfile.membershipLevel}</p>
                    <dl class="profile-card__stats">
                        <div>
                            <dt>Ngày tham gia</dt>
                            <dd>${customerProfile.joinDate}</dd>
                        </div>
                        <div>
                            <dt>Đơn hàng thành công</dt>
                            <dd>${customerProfile.successfulOrders} đơn</dd>
                        </div>
                        <div>
                            <dt>Độ hài lòng</dt>
                            <dd>${customerProfile.satisfactionScore}/5.0</dd>
                        </div>
                    </dl>
                    <p class="profile-card__note">Khách hàng đã được xác minh danh tính KYC. Thông tin chỉ dùng minh họa.</p>
                </div>
            </c:if>
        </div>

        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Đánh giá mới nhất</h3>
            </div>
            <div class="reviews">
                <c:forEach var="rv" items="${reviews}">
                    <article class="review-card">
                        <header>
                            <strong>${rv.reviewerName}</strong>
                            <span class="review-card__rating">★ ${rv.rating}/5</span>
                        </header>
                        <p class="review-card__comment">${rv.comment}</p>
                        <footer>Về sản phẩm: <em>${rv.productName}</em></footer>
                    </article>
                </c:forEach>
            </div>
        </div>
    </section>

    <!-- Tips an toàn -->
    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Ghi chú an toàn cho người mua</h3>
        </div>
        <ol class="tips-list">
            <c:forEach var="tip" items="${buyerTips}">
                <li>${tip}</li>
            </c:forEach>
        </ol>
    </section>

</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
