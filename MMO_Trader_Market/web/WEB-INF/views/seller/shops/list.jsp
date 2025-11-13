<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page">
    <section class="panel">
        <div class="panel__header panel__header--stack">
            <div class="panel__header-text">
                <h2 class="panel__title">Danh sách shop của tôi</h2>
                <p class="panel__subtitle">Theo dõi hiệu suất bán hàng và quản lý trạng thái từng shop.</p>
            </div>
            <div class="shop-toolbar">
                <form class="shop-toolbar__filters" method="get" action="${cPath}/seller/shops">
                    <div class="shop-toolbar__search">
                        <input class="form-input shop-toolbar__search-input" type="search" name="q"
                               value="${fn:escapeXml(keyword)}"
                               placeholder="Tìm kiếm theo tên shop" />
                        <button class="button button--ghost shop-toolbar__search-btn" type="submit">Tìm kiếm</button>
                        <c:if test="${not empty keyword}">
                            <a class="button button--ghost button--subtle shop-toolbar__reset" href="${cPath}/seller/shops">Xóa lọc</a>
                        </c:if>
                    </div>
                    <div class="shop-toolbar__sort">
                        <label class="shop-toolbar__label" for="sortBy">Sắp xếp</label>
                        <select class="form-input shop-toolbar__select" id="sortBy" name="sortBy" onchange="this.form.submit()">
                            <option value="sales_desc" ${sortBy == 'sales_desc' ? 'selected' : ''}>Lượt bán (cao → thấp)</option>
                            <option value="created_desc" ${sortBy == 'created_desc' ? 'selected' : ''}>Mới cập nhật</option>
                            <option value="name_asc" ${sortBy == 'name_asc' ? 'selected' : ''}>Tên (A → Z)</option>
                        </select>
                    </div>
                </form>
                <a class="button button--primary shop-toolbar__create" href="${cPath}/seller/shops/create">Tạo shop mới</a>
            </div>
        </div>
        <div class="panel__body">
            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert alert--${sessionScope.flashType}"><c:out value="${sessionScope.flashMessage}"/></div>
                <c:remove var="flashMessage" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <c:choose>
                <c:when test="${not empty shops}">
                    <div class="shop-grid">
                        <c:forEach var="shop" items="${shops}">
                            <article class="shop-card ${shop.status == 'Active' ? '' : 'shop-card--inactive'}">
                                <!-- Tái cấu trúc header nhằm cân chỉnh lại trạng thái và tiêu đề thẻ shop -->
                                <header class="shop-card__header">
                                    <c:choose>
                                        <c:when test="${shop.status == 'Active'}">
                                            <span class="shop-card__status">
                                                <i class="bi bi-lightning-charge-fill"></i>
                                                Hoạt động
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="shop-card__status">
                                                <i class="bi bi-pause-circle-fill"></i>
                                                Ngừng hoạt động
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                    <h3 class="shop-card__title"><c:out value="${shop.name}"/></h3>
                                </header>
                                <p class="shop-card__description">
                                    <c:choose>
                                        <c:when test="${not empty shop.description}">
                                            <c:out value="${shop.description}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="shop-card__description--empty">Chưa có mô tả</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <div class="shop-card__stats">
                                    <div class="shop-card__stat">
                                        <span class="shop-card__stat-label">Số sản phẩm</span>
                                        <span class="shop-card__stat-value"><c:out value="${shop.productCount}"/></span>
                                    </div>
                                    <div class="shop-card__stat">
                                        <span class="shop-card__stat-label">Lượt bán</span>
                                        <span class="shop-card__stat-value"><c:out value="${shop.totalSold}"/></span>
                                    </div>
                                </div>
                                <div class="shop-card__divider" aria-hidden="true"></div>
                                <div class="shop-card__dates">
                                    <div class="shop-card__date">
                                        <span class="shop-card__date-label">Ngày tạo</span>
                                        <span class="shop-card__date-value">
                                            <fmt:formatDate value="${shop.createdAt}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </div>
                                    <div class="shop-card__date">
                                        <span class="shop-card__date-label">Cập nhật gần nhất</span>
                                        <span class="shop-card__date-value">
                                            <fmt:formatDate value="${shop.updatedAt}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </div>
                                </div>
                                <div class="shop-card__actions">
                                    <a class="button button--ghost" href="${cPath}/seller/inventory">Quản lý sản phẩm</a>
                                    <a class="button button--ghost" href="${cPath}/seller/shops/edit?id=${shop.id}">Sửa shop</a>
                                    <a class="button button--ghost" href="${cPath}/seller/products/create?shopId=${shop.id}">Tạo sản phẩm</a>
                                    <c:choose>
                                        <c:when test="${shop.status == 'Active'}">
                                            <form method="post" action="${cPath}/seller/shops/hide">
                                                <input type="hidden" name="id" value="${shop.id}" />
                                                <button type="submit" class="button button--ghost button--danger js-hide-btn"
                                                        data-shop-name="${fn:escapeXml(shop.name)}">
                                                    Ẩn shop
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form method="post" action="${cPath}/seller/shops/restore">
                                                <input type="hidden" name="id" value="${shop.id}" />
                                                <button type="submit" class="button button--ghost">Khôi phục</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="shop-empty-state">
                        <c:choose>
                            <c:when test="${not empty keyword}">
                                Không tìm thấy shop nào khớp với "<strong><c:out value="${keyword}"/></strong>".
                                <a href="${cPath}/seller/shops">Xóa bộ lọc</a>
                            </c:when>
                            <c:otherwise>
                                Chưa có shop nào. <a href="${cPath}/seller/shops/create">Tạo shop ngay</a>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        // Xác nhận trước khi ẩn shop, đảm bảo seller hiểu rằng sản phẩm cũng bị ngừng bán.
        var buttons = document.querySelectorAll('.js-hide-btn');
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                var name = btn.getAttribute('data-shop-name') || '';
                var ok = confirm('Bạn có chắc chắn muốn ngừng hoạt động shop "' + name + '"?\nTất cả sản phẩm thuộc shop sẽ chuyển sang trạng thái Ngừng hoạt động.');
                if (!ok) {
                    e.preventDefault();
                }
            });
        });
    });
</script>
