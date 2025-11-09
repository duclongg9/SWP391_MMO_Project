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
        <div class="panel__header">
            <div class="panel__header-text">
                <h2 class="panel__title">Danh sách shop của tôi</h2>
                <p class="panel__subtitle">Quản lý các shop và xem thống kê bán hàng</p>
            </div>
            <div class="panel__header-actions shop-toolbar">
                <form method="get" action="${cPath}/seller/shops" class="shop-toolbar__form">
                    <div class="shop-toolbar__field">
                        <label class="shop-toolbar__label" for="shopSearch">Tìm kiếm</label>
                        <input class="form-input shop-toolbar__input" type="search" id="shopSearch" name="search"
                               value="${fn:escapeXml(search)}" placeholder="Nhập tên shop" />
                    </div>
                    <div class="shop-toolbar__field">
                        <label class="shop-toolbar__label" for="sortBy">Sắp xếp</label>
                        <select class="form-input shop-toolbar__input" id="sortBy" name="sortBy" onchange="this.form.submit()">
                            <option value="sales_desc" ${sortBy == 'sales_desc' ? 'selected' : ''}>Lượng bán (cao → thấp)</option>
                            <option value="created_desc" ${sortBy == 'created_desc' ? 'selected' : ''}>Mới nhất</option>
                        </select>
                    </div>
                    <button type="submit" class="button button--ghost shop-toolbar__submit">Lọc</button>
                    <c:if test="${not empty search}">
                        <a class="button button--ghost" href="${cPath}/seller/shops">Bỏ lọc</a>
                    </c:if>
                </form>
                <a class="button button--primary" href="${cPath}/seller/shops/create">Tạo shop</a>
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
                    <div class="shop-cards">
                        <c:forEach var="shop" items="${shops}">
                            <article class="shop-card ${shop.status == 'Active' ? '' : 'shop-card--inactive'}">
                                <div class="shop-card__header">
                                    <h3 class="shop-card__title"><c:out value="${shop.name}"/></h3>
                                    <c:choose>
                                        <c:when test="${shop.status == 'Active'}">
                                            <span class="badge badge--success">Hoạt động</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge--warning">Ngừng hoạt động</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <p class="shop-card__description">
                                    <c:choose>
                                        <c:when test="${not empty shop.description}">
                                            <c:out value="${shop.description}"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="shop-card__description--placeholder">Chưa có mô tả</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <div class="shop-card__stats">
                                    <div class="shop-card__stat">
                                        <span class="shop-card__stat-label">Số sản phẩm</span>
                                        <span class="shop-card__stat-value"><c:out value="${shop.productCount}"/></span>
                                    </div>
                                    <div class="shop-card__stat">
                                        <span class="shop-card__stat-label">Lượng bán</span>
                                        <span class="shop-card__stat-value"><c:out value="${shop.totalSold}"/></span>
                                    </div>
                                </div>
                                <div class="shop-card__meta">
                                    <div class="shop-card__meta-item">
                                        <span class="shop-card__meta-label">Ngày tạo</span>
                                        <span class="shop-card__meta-value">
                                            <fmt:formatDate value="${shop.createdAt}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </div>
                                    <div class="shop-card__meta-item">
                                        <span class="shop-card__meta-label">Cập nhật gần nhất</span>
                                        <span class="shop-card__meta-value">
                                            <c:choose>
                                                <c:when test="${not empty shop.updatedAt}">
                                                    <fmt:formatDate value="${shop.updatedAt}" pattern="dd/MM/yyyy"/>
                                                </c:when>
                                                <c:otherwise>
                                                    —
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>
                                <div class="shop-card__actions">
                                    <a class="button button--ghost" href="${cPath}/seller/shops/edit?id=${shop.id}">Sửa</a>
                                    <a class="button button--ghost" href="${cPath}/seller/products/create?shopId=${shop.id}">Tạo sản phẩm</a>
                                    <c:choose>
                                        <c:when test="${shop.status == 'Active'}">
                                            <form method="post" action="${cPath}/seller/shops/hide" class="shop-card__action-form">
                                                <input type="hidden" name="id" value="${shop.id}" />
                                                <button type="submit" class="button button--ghost js-hide-btn"
                                                        data-shop-name="${fn:escapeXml(shop.name)}">Ẩn</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form method="post" action="${cPath}/seller/shops/restore" class="shop-card__action-form">
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
                    <c:choose>
                        <c:when test="${not empty search}">
                            <p class="shop-empty">Không tìm thấy shop nào trùng với từ khóa "<strong><c:out value="${search}"/></strong>".</p>
                        </c:when>
                        <c:otherwise>
                            <p class="shop-empty">Chưa có shop nào. <a href="${cPath}/seller/shops/create">Tạo shop ngay</a></p>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var buttons = document.querySelectorAll('.js-hide-btn');
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                var name = btn.getAttribute('data-shop-name') || '';
                var ok = confirm('Bạn có chắc chắn ngừng hoạt động shop "' + name + '" không? Nếu bạn ngừng hoạt động thì tất cả sản phẩm trong shop của bạn sẽ bị ẩn!');
                if (!ok) {
                    e.preventDefault();
                }
            });
        });
    });
</script>
