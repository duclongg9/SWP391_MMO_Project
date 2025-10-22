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

    </section>


    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm nổi bật</h3>
            <span class="panel__tag">Dữ liệu trực tiếp</span>
        </div>

        <c:choose>
            <c:when test="${empty featured}">
                <p>Chưa có dữ liệu.</p>
            </c:when>
            <c:otherwise>
                <div class="featured-carousel" data-featured-carousel>
                    <button class="featured-carousel__control featured-carousel__control--prev" type="button" aria-label="Cuộn về trước">‹</button>
                    <div class="featured-carousel__viewport">
                        <ul class="featured-carousel__list">
                            <c:forEach var="product" items="${featured}">
                                <li class="featured-carousel__item">
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
                                                <h4 class="product-card__title" title="${product.name}"><c:out value="${product.name}" /></h4>
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
                                                        <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        Từ
                                                        <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                                        –
                                                        <fmt:formatNumber value="${product.maxPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </p>
                                            <ul class="product-card__meta product-card__meta--stats">
                                                <li>Còn: <strong><c:out value="${product.inventoryCount}" default="0" /></strong></li>
                                                <li>Đã bán: <strong><c:out value="${product.soldCount}" default="0" /></strong></li>
                                            </ul>
                                            <footer class="product-card__footer">
                                                <c:url var="detailUrl" value="/product/detail">
                                                    <c:param name="id" value="${product.id}" />
                                                </c:url>
                                                <a class="button button--primary" href="${detailUrl}">Xem chi tiết</a>
                                            </footer>
                                        </div>
                                    </article>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                    <button class="featured-carousel__control featured-carousel__control--next" type="button" aria-label="Cuộn về sau">›</button>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Bán chạy theo subtype</h3>
            <span class="panel__tag">Mỗi subtype 1 sản phẩm</span>
        </div>
        <c:choose>
            <c:when test="${empty topBySubtype}">
                <p>Đang cập nhật dữ liệu.</p>
            </c:when>
            <c:otherwise>
                <div class="product-grid product-grid--subtype">
                    <c:forEach var="entry" items="${topBySubtype}">
                        <c:set var="product" value="${entry.value}" />
                        <article class="product-card product-card--subtype">
                            <div class="product-card__image">
                                <c:choose>
                                    <c:when test="${not empty product.primaryImageUrl}">
                                        <img src="${product.primaryImageUrl}" alt="Ảnh sản phẩm ${fn:escapeXml(product.name)}" loading="lazy" />
                                    </c:when>
                                    <c:otherwise>
                                        <div class="product-card__placeholder">Không có ảnh</div>
                                    </c:otherwise>
                                </c:choose>
                                <span class="product-card__badge"><c:out value="${product.productSubtypeLabel}" /></span>
                            </div>
                            <div class="product-card__body">
                                <h4 class="product-card__title"><c:out value="${product.name}" /></h4>
                                <p class="product-card__description"><c:out value="${product.shortDescription}" /></p>
                                <p class="product-card__meta">Shop: <strong><c:out value="${product.shopName}" /></strong></p>
                                <p class="product-card__price">
                                    <c:choose>
                                        <c:when test="${product.minPrice eq product.maxPrice}">
                                            Giá
                                            <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:when>
                                        <c:otherwise>
                                            Từ
                                            <fmt:formatNumber value="${product.minPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                            –
                                            <fmt:formatNumber value="${product.maxPrice}" type="currency" currencySymbol="₫" minFractionDigits="0" maxFractionDigits="0" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <ul class="product-card__meta product-card__meta--stats">
                                    <li>Còn: <strong><c:out value="${product.inventoryCount}" default="0" /></strong></li>
                                    <li>Đã bán: <strong><c:out value="${product.soldCount}" default="0" /></strong></li>
                                </ul>
                                <footer class="product-card__footer">
                                    <c:url var="detailUrl" value="/product/detail">
                                        <c:param name="id" value="${product.id}" />
                                    </c:url>
                                    <a class="button button--ghost" href="${detailUrl}">Xem chi tiết</a>
                                </footer>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
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

    <!-- Giới thiệu / About -->
    <section class="panel landing__section">
        <div class="panel__body">
            <div class="landing__about">
                <h3 class="landing__about-title">MMO Trader Market</h3>
                <p class="landing__about-lead">
                    Một sản phẩm ra đời với mục đích thuận tiện và an toàn hơn trong các giao dịch mua bán sản phẩm số.
                </p>
                <p class="landing__about-text">
                    Như các bạn đã biết, tình trạng lừ.a đảo trên mạng xã hội kéo dài bao nhiêu năm nay, mặc dù đã có rất nhiều giải pháp từ cộng đồng như là trung gian hay bảo hiểm, nhưng vẫn rất nhiều người dùng lựa chọn mua bán nhanh gọn mà bỏ qua các bước kiểm tra, hay trung gian, từ đó tạo cơ hội cho s.c.a.m.m.e.r hoạt động. Ở MMO Trader Market, bạn sẽ có 1 trải nghiệm mua hàng yên tâm hơn rất nhiều, chúng tôi sẽ giữ tiền người bán 3 ngày, kiểm tra toàn bộ sản phẩm bán ra có trùng với người khác hay không, nhắm mục đích tạo ra một nơi giao dịch mà người dùng có thể tin tưởng, một trang mà người bán có thể yên tâm đặt kho hàng, và cạnh tranh sòng phẳng.
                </p>
            </div>
        </div>
    </section>


</main>

<script>
    (function() {
        const carousels = document.querySelectorAll('[data-featured-carousel]');
        carousels.forEach(function(carousel) {
            const viewport = carousel.querySelector('.featured-carousel__viewport');
            const next = carousel.querySelector('.featured-carousel__control--next');
            const prev = carousel.querySelector('.featured-carousel__control--prev');
            const item = carousel.querySelector('.featured-carousel__item');
            const list = carousel.querySelector('.featured-carousel__list');
            if (!viewport || !item || !list) {
                return;
            }
            const itemWidth = item.getBoundingClientRect().width;
            const styles = getComputedStyle(list);
            const columnGap = styles.columnGap || styles.gap || '16';
            const parsedGap = parseFloat(columnGap);
            const gap = Number.isNaN(parsedGap) ? 16 : parsedGap;
            const scrollAmount = itemWidth + gap;

            function scroll(direction) {
                viewport.scrollBy({ left: scrollAmount * direction, behavior: 'smooth' });
            }

            if (next) {
                next.addEventListener('click', function() { scroll(1); });
            }
            if (prev) {
                prev.addEventListener('click', function() { scroll(-1); });
            }
        });
    })();
</script>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
