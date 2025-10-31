<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<div id="homepage-config"
     data-context-path="${cPath}"
     data-products-url="${cPath}/products"
     data-product-detail-base="${cPath}/product/detail/"></div>
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
        <div class="landing__hero-main"
             data-fragment-type="summary"
             data-fragment-endpoint="${cPath}/api/home/summary">
            <h2>Chợ tài khoản MMO chuyên nghiệp, UY TÍN </h2>
            <p class="landing__lead">
                Điều mà chúng tôi đã đạt được:
            </p>
            <ul class="landing__metrics">
                <li>
                    <strong class="metric-value" data-field="totalCompletedOrders">--</strong> đơn đã hoàn tất
                </li>
                <li>
                    <strong class="metric-value" data-field="activeShopCount">--</strong> shop đang hoạt động
                </li>
                <li>
                    <strong class="metric-value" data-field="activeBuyerCount">--</strong> người mua đã xác minh
                </li>
            </ul>
            <p class="fragment__status" data-fragment-status>Đang tải thống kê...</p>
        </div>

        <aside class="landing__categories" id="product-types"
               data-fragment-type="categories"
               data-fragment-endpoint="${cPath}/api/home/categories">
            <h3 class="landing__aside-title">Danh mục chính</h3>
            <ul class="category-menu" data-fragment-list></ul>
            <p class="fragment__status" data-fragment-status>Đang tải danh mục...</p>
        </aside>
    </section>


    <section class="panel landing__section">
        <div class="panel__header">
            <h3 class="panel__title">Sản phẩm nổi bật</h3>
            <span class="panel__tag">Dữ liệu trực tiếp</span>
        </div>

        <div class="landing__products"
             data-fragment-type="highlights"
             data-fragment-endpoint="${cPath}/api/home/highlights?limit=6">
            <p class="fragment__status" data-fragment-status>Đang tải sản phẩm nổi bật...</p>
        </div>
    </section>


    <section class="panel landing__section" id="faq">
        <div class="panel__header">
            <h3 class="panel__title">Câu hỏi thường gặp</h3>
        </div>
        <div class="panel__body faq-list">
            <c:if test="${empty faqs}">
                <p>Đang cập nhật câu hỏi.</p>
            </c:if>
            <c:if test="${not empty faqs}">
                <c:forEach var="faq" items="${faqs}">
                    <details class="faq-item">
                        <summary class="faq-item__question">
                            <span><c:out value="${faq.title}" /></span>
                        </summary>
                        <p class="faq-item__answer"><c:out value="${faq.description}" /></p>
                    </details>
                </c:forEach>
            </c:if>
        </div>
    </section>

    <section class="panel landing__section"
             data-fragment-type="systemNotes"
             data-fragment-endpoint="${cPath}/api/home/system-notes">
        <div class="panel__header">
            <h3 class="panel__title">Cấu hình hệ thống</h3>
        </div>
        <ol class="tips-list" data-fragment-list></ol>
        <p class="fragment__status" data-fragment-status>Đang tải ghi chú hệ thống...</p>
    </section>

</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<script src="${cPath}/assets/Script/homepage.js" defer></script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
