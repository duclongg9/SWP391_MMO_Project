<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page">
    <section class="panel">
        <div class="panel__header panel__header--stack">
            <div>
                <h2 class="panel__title">Quản lý cửa hàng</h2>
                <p class="panel__subtitle">Theo dõi hiệu suất từng shop và thao tác nhanh với sản phẩm.</p>
            </div>
            <div class="seller-shop__toolbar">
                <form class="seller-shop__filters" method="get" action="${cPath}/seller/shops">
                    <input class="seller-shop__search" type="search" name="q" placeholder="Tìm theo tên shop"
                           value="${fn:escapeXml(filterKeyword)}" />
                    <label class="seller-shop__date">
                        <span>Từ</span>
                        <input type="date" name="from" value="${filterFrom}" max="${today}" />
                    </label>
                    <label class="seller-shop__date">
                        <span>Đến</span>
                        <input type="date" name="to" value="${filterTo}" max="${today}" />
                    </label>
                    <button class="button button--primary" type="submit">Tìm kiếm</button>
                    <a class="button button--ghost" href="${cPath}/seller/shops">Làm mới</a>
                </form>
                <a class="button button--secondary" href="${cPath}/seller/shops/create">Tạo shop mới</a>
            </div>
        </div>
        <div class="panel__body">
            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="alert alert--${sessionScope.flashType}"><c:out value="${sessionScope.flashMessage}"/></div>
                <c:remove var="flashMessage" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>
            <c:if test="${not empty filterError}">
                <div class="alert alert--error"><c:out value="${filterError}" /></div>
            </c:if>
            <c:choose>
                <c:when test="${not empty shops}">
                    <div class="table-responsive">
                        <table class="table table--interactive seller-shop__table">
                            <thead>
                                <tr>
                                    <th>Tên shop</th>
                                    <th>Mô tả</th>
                                    <th># Sản phẩm</th>
                                    <th>Đã bán</th>
                                    <th>Tồn kho</th>
                                    <th>Created At</th>
                                    <th>Updated At</th>
                                    <th>Trạng thái</th>
                                    <th class="table__actions">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="shop" items="${shops}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${shop.name}"/></strong>
                                        </td>
                                        <td>
                                            <div class="seller-shop__desc" title="${fn:escapeXml(shop.description)}">
                                                <c:choose>
                                                    <c:when test="${not empty shop.description}">
                                                        <c:out value="${shop.description}" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td class="text-center">${shop.productCount}</td>
                                        <td class="text-center">${shop.totalSold}</td>
                                        <td class="text-center">${shop.totalStock}</td>
                                        <td>
                                            <fmt:formatDate value="${shop.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${shop.updatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${shop.status == 'Active'}">
                                                    <span class="badge badge--success">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${shop.status == 'Pending'}">
                                                    <span class="badge badge--info">Đang duyệt</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--warning">Tạm ẩn</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__actions">
                                            <div class="actions">
                                                <a class="button button--secondary"
                                                   href="${cPath}/seller/products/create?shopId=${shop.id}">Tạo sản phẩm</a>
                                                <a class="button button--ghost" href="${cPath}/seller/shops/edit?id=${shop.id}">Sửa</a>
                                                <c:choose>
                                                    <c:when test="${shop.status == 'Active'}">
                                                        <form method="post" action="${cPath}/seller/shops/status" class="seller-shop__status-form">
                                                            <input type="hidden" name="id" value="${shop.id}" />
                                                            <input type="hidden" name="action" value="HIDE" />
                                                            <button type="submit" class="button button--ghost seller-shop__hide"
                                                                    data-shop-name="${fn:escapeXml(shop.name)}">Ẩn shop</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${cPath}/seller/shops/status">
                                                            <input type="hidden" name="id" value="${shop.id}" />
                                                            <input type="hidden" name="action" value="UNHIDE" />
                                                            <button type="submit" class="button button--ghost">Hiện shop</button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="seller-shop__empty">Chưa có shop nào. <a href="${cPath}/seller/shops/create">Tạo shop ngay</a></p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<style>
.seller-shop__toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
    align-items: center;
    justify-content: space-between;
}
.seller-shop__filters {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    align-items: center;
}
.seller-shop__search {
    min-width: 220px;
    padding: 0.5rem 0.75rem;
    border-radius: 6px;
    border: 1px solid #dcdcdc;
}
.seller-shop__date {
    display: flex;
    flex-direction: column;
    font-size: 0.85rem;
    color: #555;
}
.seller-shop__date input {
    margin-top: 0.25rem;
    padding: 0.45rem 0.6rem;
    border-radius: 6px;
    border: 1px solid #dcdcdc;
}
.seller-shop__table th,
.seller-shop__table td {
    vertical-align: middle;
}
.seller-shop__desc {
    max-width: 280px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.seller-shop__empty {
    text-align: center;
    padding: 2rem 0;
    color: #666;
}
.seller-shop__status-form {
    display: inline;
}
.seller-shop__hide {
    color: #d35400;
}
.table-responsive {
    width: 100%;
    overflow-x: auto;
}
.text-center {
    text-align: center;
}
@media (max-width: 768px) {
    .seller-shop__filters {
        width: 100%;
        flex-direction: column;
        align-items: stretch;
    }
    .seller-shop__toolbar {
        align-items: stretch;
    }
    .seller-shop__search,
    .seller-shop__filters input[type="date"],
    .seller-shop__filters button,
    .seller-shop__filters .button {
        width: 100%;
    }
}
</style>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.seller-shop__hide').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                const name = btn.getAttribute('data-shop-name') || '';
                if (!confirm('Ẩn shop "' + name + '"? Tất cả sản phẩm sẽ chuyển sang trạng thái UNLISTED.')) {
                    e.preventDefault();
                }
            });
        });
    });
</script>
