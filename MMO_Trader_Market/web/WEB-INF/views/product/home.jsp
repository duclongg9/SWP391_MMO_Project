<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content landing">

    <section class="panel landing__filters">
        <div class="panel__header">
            <div class="panel__header-text">
                <h3 class="panel__title">Khám phá sản phẩm</h3>
                <p class="panel__subtitle">Tìm kiếm, lọc theo shop và mua ngay những sản phẩm bạn cần.</p>
            </div>
        </div>
        <div class="panel__body">
            <c:set var="filterIncludeSize" value="${false}" />
            <c:set var="filterQuery" value="${query}" />
            <c:set var="filterType" value="${selectedType}" />
            <c:set var="filterSubtype" value="${selectedSubtype}" />
            <%@ include file="/WEB-INF/views/product/fragments/filter-form.jspf" %>
        </div>
    </section>

    <section class="panel landing__hero">
        <div class="landing__hero-main">
            <h2>Chợ tài khoản MMO dành cho seller và buyer chuyên nghiệp</h2>
            <p class="landing__lead">
                Thống kê trên trang được đồng bộ trực tiếp từ cơ sở dữ liệu MySQL của hệ thống.
            </p>
            <c:set var="summary" value="${summary}" />
            <ul class="landing__metrics">
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.totalCompletedOrders}" type="number" />
                    </strong> đơn đã hoàn tất
                </li>
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.activeShopCount}" type="number" />
                    </strong> shop đang hoạt động
                </li>
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.activeBuyerCount}" type="number" />
                    </strong> người mua đã xác minh
                </li>
            </ul>
            <div class="landing__cta">
                <a class="button button--primary" href="${pageContext.request.contextPath}/login.jsp">Đăng nhập</a>
                <a class="button button--ghost" href="${pageContext.request.contextPath}/products">Quản trị sản phẩm</a>
            </div>
        </div>

        <aside class="landing__categories" id="product-types">
            <h3 class="landing__aside-title">Danh mục chính</h3>
            <c:choose>
                <c:when test="${empty productCategories}">
                    <p>Đang cập nhật dữ liệu.</p>
                </c:when>
                <c:otherwise>
                    <ul class="category-menu">
                        <c:forEach var="category" items="${productCategories}">
                            <c:url var="categoryUrl" value="/products">
                                <c:param name="type" value="${category.typeCode}" />
                            </c:url>
                            <li class="category-menu__item">
                                <span class="category-menu__icon">🏷️</span>
                                <div>
                                    <strong><a href="${categoryUrl}"><c:out value="${category.typeLabel}" /></a></strong>
                                    <p><fmt:formatNumber value="${category.availableProducts}" type="number" /> sản phẩm khả dụng</p>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </aside>
    </section>


    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Shop nổi bật</h3>
        </div>
        <c:choose>
            <c:when test="${empty shops}">
                <p>Chưa có dữ liệu.</p>
            </c:when>
            <c:otherwise>
                <ul class="category-menu category-menu--grid">
                    <c:forEach var="shop" items="${shops}">
                        <li class="category-menu__item">
                            <span class="category-menu__icon">
                                <c:out value="${shopIcons[shop.status]}" />
                            </span>
                            <div>
                                <strong><c:out value="${shop.name}" /></strong>
                                <p><c:out value="${shop.description}" /></p>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm nổi bật</h3>
            <span class="panel__tag">Dữ liệu trực tiếp</span>
        </div>

        <div class="landing__products">
            <c:choose>
                <c:when test="${empty featuredProducts}">
                    <p>Chưa có dữ liệu.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="product" items="${featuredProducts}">
                        <article class="product-card product-card--featured">
                            <div class="product-card__image">
                                <c:choose>
                                    <c:when test="${not empty product.primaryImageUrl}">
                                        <img src="${product.primaryImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(product.name)}" loading="lazy" />
                                    </c:when>
                                    <c:otherwise>
                                        <div class="product-card__placeholder">Không có ảnh</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-card__body">
                                <header class="product-card__header">
                                    <h4><c:out value="${product.name}" /></h4>
                                </header>
                                <p class="product-card__meta">
                                    <span><c:out value="${product.productTypeLabel}" /> • <c:out value="${product.productSubtypeLabel}" /></span>
                                    <span>Shop: <strong><c:out value="${product.shopName}" /></strong></span>
                                </p>
                                <p class="product-card__description"><c:out value="${product.shortDescription}" /></p>
                                <p class="product-card__price">
                                    <c:choose>
                                        <c:when test="${product.minPrice eq product.maxPrice}">
                                            Giá
                                            <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:when>
                                        <c:otherwise>
                                            Giá từ
                                            <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                            –
                                            <fmt:formatNumber value="${product.maxPrice}" type="currency" currencySymbol="đ" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <ul class="product-card__meta product-card__meta--stats">
                                    <li>Tồn kho: <strong><c:out value="${product.inventoryCount}" /></strong></li>
                                    <li>Đã bán: <strong><c:out value="${product.soldCount}" /></strong></li>
                                </ul>
                                <footer class="product-card__footer">
                                    <c:url var="detailUrl" value="/product/detail">
                                        <c:param name="id" value="${product.id}" />
                                    </c:url>
                                    <a class="button button--primary" href="${detailUrl}">Xem chi tiết</a>
                                </footer>
                            </div>
                        </article>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="panel landing__section landing__grid">
        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Khách hàng tiêu biểu</h3>
            </div>
            <c:choose>
                <c:when test="${empty customerProfile}">
                    <p>Chưa có dữ liệu.</p>
                </c:when>
                <c:otherwise>
                    <div class="profile-card">
                        <h4><c:out value="${customerProfile.displayName}" /></h4>
                        <p class="profile-card__subtitle"><c:out value="${customerProfile.email}" /></p>
                        <dl class="profile-card__stats">
                            <div>
                                <dt>Ngày tham gia</dt>
                                <dd><c:out value="${customerProfile.joinDate}" /></dd>
                            </div>
                            <div>
                                <dt>Tổng số đơn</dt>
                                <dd><c:out value="${customerProfile.totalOrders}" /></dd>
                            </div>
                            <div>
                                <dt>Đơn hoàn thành</dt>
                                <dd><c:out value="${customerProfile.completedOrders}" /></dd>
                            </div>
                            <div>
                                <dt>Đơn khiếu nại</dt>
                                <dd><c:out value="${customerProfile.disputedOrders}" /></dd>
                            </div>
                            <div>
                                <dt>Độ hài lòng</dt>
                                <dd>
                                    <fmt:formatNumber value="${customerProfile.satisfactionScore}" type="number" minFractionDigits="1" maxFractionDigits="1" /> / 5.0
                                </dd>
                            </div>
                        </dl>
                        <p class="profile-card__note">Thông tin hiển thị dựa trên dữ liệu người mua thực tế.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="landing__column">
            <div class="panel__header">
                <h3 class="panel__title">Trao đổi gần đây</h3>
            </div>
            <c:choose>
                <c:when test="${empty recentMessages}">
                    <p>Chưa có dữ liệu.</p>
                </c:when>
                <c:otherwise>
                    <div class="reviews">
                        <c:forEach var="message" items="${recentMessages}">
                            <article class="review-card">
                                <header>
                                    <strong><c:out value="${message.senderName}" /></strong>
                                    <span class="review-card__rating">
                                        <c:out value="${message.createdAt}" />
                                    </span>
                                </header>
                                <p class="review-card__comment"><c:out value="${message.content}" /></p>
                                <footer>Sản phẩm: <em><c:out value="${message.productName}" /></em></footer>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="panel landing__section" id="faq">
        <div class="panel__header">
            <h3 class="panel__title">Câu hỏi thường gặp</h3>
        </div>
        <div class="panel__body faq-list">
            <c:choose>
                <c:when test="${empty faqs}">
                    <p>Đang cập nhật câu hỏi.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="faq" items="${faqs}">
                        <details class="faq-item">
                            <summary class="faq-item__question">
                                <span><c:out value="${faq.title}" /></span>
                            </summary>
                            <p class="faq-item__answer"><c:out value="${faq.description}" /></p>
                        </details>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Cấu hình hệ thống</h3>
        </div>
        <c:choose>
            <c:when test="${empty systemNotes}">
                <p>Chưa có dữ liệu.</p>
            </c:when>
            <c:otherwise>
                <ol class="tips-list">
                    <c:forEach var="config" items="${systemNotes}">
                        <li>
                            <strong><c:out value="${config.configKey}" />:</strong>
                            <c:out value="${config.configValue}" />
                        </li>
                    </c:forEach>
                </ol>
            </c:otherwise>
        </c:choose>
    </section>

</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
