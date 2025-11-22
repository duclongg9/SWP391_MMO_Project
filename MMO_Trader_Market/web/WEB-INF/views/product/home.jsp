<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<c:set var="fragmentBase" value="${cPath}/fragment/home" /> <!--base URL để gọi các mảnh.-->
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content landing">


<!--placeholder-->
    <section class="panel landing__hero" id="home-summary" data-fragment-url="${fragmentBase}/summary">
        <div class="fragment fragment--loading">
            <p>Đang tải dữ liệu tổng quan...</p>
        </div>
    </section>

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

    <section class="panel landing__section" id="home-featured" data-fragment-url="${fragmentBase}/featured">
        <div class="fragment fragment--loading">
            <p>Đang tải sản phẩm nổi bật...</p>
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

    <section class="panel landing__section" id="system-notes" data-fragment-url="${fragmentBase}/system-notes">
        <div class="fragment fragment--loading">
            <p>Đang tải cấu hình hệ thống...</p>
        </div>
    </section>

</main>

<script src="${cPath}/assets/Script/home.js" defer></script>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
