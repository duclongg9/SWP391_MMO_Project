<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content seller-page shop-detail">
    <section class="panel">
        <div class="panel__header shop-detail__header">
            <div class="shop-detail__title-group">
                <a class="button button--ghost shop-detail__back" href="${cPath}/seller/shops">← Danh sách shop</a>
                <h1 class="panel__title shop-detail__title">${shop.name}</h1>
                <span class="shop-detail__status ${shop.status == 'Active' ? 'is-active' : 'is-suspended'}">
                    <c:choose>
                        <c:when test="${shop.status == 'Active'}">Đang hoạt động</c:when>
                        <c:when test="${shop.status == 'Suspended'}">Tạm dừng</c:when>
                        <c:otherwise>${shop.status}</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="shop-detail__meta">
                <p class="shop-detail__description">
                    <c:choose>
                        <c:when test="${not empty shop.description}">${shop.description}</c:when>
                        <c:otherwise><span class="shop-detail__description--empty">Chưa có mô tả cho shop này.</span></c:otherwise>
                    </c:choose>
                </p>
                <div class="shop-detail__meta-grid">
                    <div>
                        <span class="shop-detail__meta-label">Tạo ngày</span>
                        <span class="shop-detail__meta-value">
                            <fmt:formatDate value="${shop.createdAt}" pattern="dd/MM/yyyy" />
                        </span>
                    </div>
                    <div>
                        <span class="shop-detail__meta-label">Cập nhật</span>
                        <span class="shop-detail__meta-value">
                            <fmt:formatDate value="${shop.updatedAt}" pattern="dd/MM/yyyy" />
                        </span>
                    </div>
                    <div>
                        <span class="shop-detail__meta-label">Tổng sản phẩm</span>
                        <span class="shop-detail__meta-value">${shop.productCount}</span>
                    </div>
                    <div>
                        <span class="shop-detail__meta-label">Tồn kho mô phỏng</span>
                        <span class="shop-detail__meta-value">${shop.totalInventory}</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="panel__body">
            <div class="shop-detail__metrics">
                <article class="stat-card shop-detail__metric-card">
                    <div>
                        <p class="stat-card__label">Đơn hàng hoàn thành</p>
                        <p class="stat-card__value">${completedOrdersCount}</p>
                    </div>
                </article>
                <article class="stat-card shop-detail__metric-card">
                    <div>
                        <p class="stat-card__label">Tổng lượng bán</p>
                        <p class="stat-card__value">${shop.totalSold}</p>
                    </div>
                </article>
                <article class="stat-card shop-detail__metric-card">
                    <div>
                        <p class="stat-card__label">Tồn kho hiện có</p>
                        <p class="stat-card__value">${shop.totalInventory}</p>
                    </div>
                </article>
                <article class="stat-card shop-detail__metric-card">
                    <div>
                        <p class="stat-card__label">Doanh thu theo quý</p>
                        <p class="stat-card__value"><fmt:formatNumber value="${quarterRevenueTotal}" type="currency" /></p>
                    </div>
                </article>
            </div>

            <c:if test="${not empty bestSeller}">
                <article class="shop-detail__best-seller">
                    <div class="shop-detail__best-seller-thumb">
                        <c:choose>
                            <c:when test="${not empty bestSeller.productImageUrl}">
                                <img src="${bestSeller.productImageUrl}" alt="${bestSeller.productName}" loading="lazy" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="initial" value="${not empty bestSeller.productName ? fn:substring(bestSeller.productName, 0, 1) : '?'}" />
                                <div class="shop-detail__best-seller-placeholder">${initial}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="shop-detail__best-seller-info">
                        <h2>Sản phẩm bán chạy</h2>
                        <p class="shop-detail__best-seller-name">${bestSeller.productName}</p>
                        <ul>
                            <li><strong>${bestSeller.totalSold}</strong> lượt bán</li>
                            <li>Doanh thu: <strong><fmt:formatNumber value="${bestSeller.totalRevenue}" type="currency" /></strong></li>
                        </ul>
                        <a class="button button--ghost" href="${cPath}/seller/products/edit?id=${bestSeller.productId}">Quản lý sản phẩm</a>
                    </div>
                </article>
            </c:if>

            <div class="shop-detail__actions">
                <a class="button button--ghost" href="${cPath}/seller/shops/edit?id=${shop.id}">Chỉnh sửa shop</a>
                <a class="button button--ghost" href="${cPath}/seller/products/create?shopId=${shop.id}">Thêm sản phẩm mới</a>
                <form method="post" action="${cPath}/seller/credentials/generate">
                    <button type="submit" class="button button--primary">Cập nhật kho từ tồn hiện tại</button>
                </form>
                <a class="button" href="${cPath}/seller/inventory">Quản lý tồn kho</a>
            </div>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header shop-detail__section-header">
            <div>
                <h2 class="panel__title">Doanh thu theo quý</h2>
                <p class="panel__subtitle">So sánh các quý gần đây để nắm bắt đà tăng trưởng.</p>
            </div>
            <div class="shop-detail__range">
                <c:url var="range3Url" value="${cPath}/seller/shops/detail">
                    <c:param name="id" value="${shop.id}" />
                    <c:param name="range" value="3" />
                </c:url>
                <c:url var="range6Url" value="${cPath}/seller/shops/detail">
                    <c:param name="id" value="${shop.id}" />
                    <c:param name="range" value="6" />
                </c:url>
                <c:url var="range12Url" value="${cPath}/seller/shops/detail">
                    <c:param name="id" value="${shop.id}" />
                    <c:param name="range" value="12" />
                </c:url>
                <a class="shop-detail__range-button ${selectedRange == 3 ? 'is-active' : ''}" href="${range3Url}">3 tháng</a>
                <a class="shop-detail__range-button ${selectedRange == 6 ? 'is-active' : ''}" href="${range6Url}">6 tháng</a>
                <a class="shop-detail__range-button ${selectedRange == 12 ? 'is-active' : ''}" href="${range12Url}">12 tháng</a>
            </div>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty quarterRevenue}">
                    <div class="shop-detail__chart">
                        <canvas id="quarterRevenueChart" height="180"></canvas>
                    </div>
                    <table class="shop-detail__revenue-table">
                        <thead>
                            <tr>
                                <th>Quý</th>
                                <th>Doanh thu</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${quarterRevenue}">
                                <tr>
                                    <td>${item.label}</td>
                                    <td><fmt:formatNumber value="${item.revenue}" type="currency" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p class="shop-detail__empty">Chưa có dữ liệu doanh thu cho khoảng thời gian này.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header shop-detail__section-header">
            <div>
                <h2 class="panel__title">Đơn hàng đã hoàn thành</h2>
                <p class="panel__subtitle">Tổng quan các đơn đã bàn giao cho người mua.</p>
            </div>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not empty orders}">
                    <div class="table-responsive">
                        <table class="table table--interactive shop-detail__orders">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Sản phẩm</th>
                                    <th>Doanh thu</th>
                                    <th>Ngày tạo</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${orders}">
                                    <tr>
                                        <td>#${order.id}</td>
                                        <td>${order.productName}</td>
                                        <td><fmt:formatNumber value="${order.totalAmount}" type="currency" /></td>
                                        <td>
                                            <c:if test="${not empty order.createdAt}">
                                                <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" timeZone="Asia/Ho_Chi_Minh" />
                                            </c:if>
                                        </td>
                                        <td><span class="shop-detail__badge">${order.status}</span></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="shop-detail__empty">Chưa có đơn hàng hoàn thành nào.</p>
                </c:otherwise>
            </c:choose>

            <div class="shop-detail__pagination">
                <div class="shop-detail__pagination-info">
                    <span>Tổng ${totalOrders} đơn</span>
                    <span>Trang ${page} / ${totalPages}</span>
                </div>
                <div class="shop-detail__pagination-actions">
                    <c:url var="prevUrl" value="${cPath}/seller/shops/detail">
                        <c:param name="id" value="${shop.id}" />
                        <c:param name="range" value="${selectedRange}" />
                        <c:param name="page" value="${page - 1}" />
                        <c:param name="size" value="${pageSize}" />
                    </c:url>
                    <c:url var="nextUrl" value="${cPath}/seller/shops/detail">
                        <c:param name="id" value="${shop.id}" />
                        <c:param name="range" value="${selectedRange}" />
                        <c:param name="page" value="${page + 1}" />
                        <c:param name="size" value="${pageSize}" />
                    </c:url>
                    <button class="button button--ghost" type="button" ${page <= 1 ? 'disabled' : ''} onclick="location.href='${prevUrl}'">Trước</button>
                    <button class="button button--ghost" type="button" ${page >= totalPages ? 'disabled' : ''} onclick="location.href='${nextUrl}'">Sau</button>
                </div>
            </div>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    (function () {
        const ctx = document.getElementById('quarterRevenueChart');
        if (!ctx) {
            return;
        }
        const labels = [
            <c:forEach var="item" items="${quarterRevenue}" varStatus="loop">
                '${item.label}'<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        ];
        const data = [
            <c:forEach var="item" items="${quarterRevenue}" varStatus="loop">
                ${item.revenue}<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        ];
        const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Doanh thu',
                    data,
                    backgroundColor: 'rgba(0, 102, 255, 0.2)',
                    borderColor: 'rgba(0, 102, 255, 0.75)',
                    borderWidth: 2,
                    borderRadius: 6,
                    maxBarThickness: 48
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        ticks: {
                            callback: (value) => formatter.format(value)
                        },
                        beginAtZero: true
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: (context) => formatter.format(context.parsed.y)
                        }
                    }
                }
            }
        });
    })();
</script>
