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
            <div class="panel__header-actions">
                <form method="get" action="${cPath}/seller/shops" style="display: inline-flex; align-items: center; gap: 8px;">
                    <label for="sortBy" style="font-weight: 500;">Sắp xếp:</label>
                    <select class="form-input" id="sortBy" name="sortBy" onchange="this.form.submit()" style="width: auto;">
                        <option value="sales_desc" ${sortBy == 'sales_desc' ? 'selected' : ''}>Lượng bán (cao → thấp)</option>
                        <option value="created_desc" ${sortBy == 'created_desc' ? 'selected' : ''}>Mới nhất</option>
                    </select>
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
                    <table class="table table--interactive">
                        <colgroup>
                            <col>                       <!-- Tên shop -->
                            <col>                       <!-- Mô tả -->
                            <col class="col--sm">       <!-- Số sản phẩm -->
                            <col class="col--sm">       <!-- Lượng bán -->
                            <col class="col--sm">       <!-- Tồn kho -->
                            <col class="col--md">       <!-- Trạng thái -->
                            <col class="col--act">      <!-- Thao tác -->
                        </colgroup>

                        <thead>
                            <tr>
                                <th>Tên shop</th>
                                <th>Mô tả</th>
                                <th>Số sản phẩm</th>
                                <th>Lượng bán</th>
                                <th>Tồn kho</th>
                                <th>Trạng thái</th>
                                <th class="table__actions">Thao tác</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="shop" items="${shops}">
                                <tr>
                                    <td><strong><c:out value="${shop.name}"/></strong></td>

                                    <!-- Mô tả: kẹp 2 dòng nhờ .desc trong CSS -->
                                    <td class="desc">
                                        <c:choose>
                                            <c:when test="${not empty shop.description}">
                                                <c:out value="${shop.description}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color:#999;">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td><c:out value="${shop.productCount}"/></td>
                                    <td><c:out value="${shop.totalSold}"/></td>
                                    <td><c:out value="${shop.totalInventory}"/></td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${shop.status == 'Active'}">
                                                <span class="badge">Hoạt Động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge--warning">Ngừng Hoạt Động</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- Thao tác: luôn cùng một hàng nhờ .table__actions + .actions -->
                                    <td class="table__actions">
                                        <div class="actions">
                                            <a class="button button--ghost" href="${cPath}/seller/shops/edit?id=${shop.id}">Sửa</a>
                                            <c:choose>
                                                <c:when test="${shop.status == 'Active'}">
                                                    <form method="post" action="${cPath}/seller/shops/hide">
                                                        <input type="hidden" name="id" value="${shop.id}" />
                                                        <button type="submit"
                                                                class="button button--ghost js-hide-btn"
                                                                data-shop-name="${fn:escapeXml(shop.name)}">Ẩn</button>
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
                                    </td>

                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                </c:when>
                <c:otherwise>
                    <p style="text-align: center; padding: 2rem; color: #666;">Chưa có shop nào. <a href="${cPath}/seller/shops/create">Tạo shop ngay</a></p>
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


