<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");

    java.util.List<String> extraStylesheets = (java.util.List<String>) request.getAttribute("extraStylesheets");
    if (extraStylesheets == null) {
        extraStylesheets = new java.util.ArrayList<String>();
    } else {
        extraStylesheets = new java.util.ArrayList<String>(extraStylesheets);
    }
    extraStylesheets.add(request.getContextPath() + "/assets/css/components/filterbar.css");
    extraStylesheets.add(request.getContextPath() + "/assets/css/components/seller-disputes.css");
    request.setAttribute("extraStylesheets", extraStylesheets);
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<%-- Theo yêu cầu: tái sử dụng filterbar và tách CSS sang file chung để dễ bảo trì. --%>

<main class="layout__content seller-page">
    <c:if test="${not empty errorMessage}">
        <div class="alert alert--danger" role="alert">${errorMessage}</div>
    </c:if>

    <c:if test="${not empty shop}">
        <section class="panel">
            <div class="panel__header">
                <div>
                    <h2 class="panel__title">Khiếu nại từ người mua</h2>
                    <p class="panel__subtitle">Theo dõi trạng thái tranh chấp của đơn hàng thuộc cửa hàng ${fn:escapeXml(shop.name)}.</p>
                </div>
            </div>
            <div class="panel__body">
                <form method="get" action="${base}/seller/disputes" class="filterbar">
                    <input type="hidden" name="size" value="${pg_size != null ? pg_size : 10}">
                    <div class="filterbar__row">
                        <div class="filterbar__field">
                            <label class="filterbar__label" for="q">Từ khóa</label>
                            <input id="q" name="q" class="form-control" type="search" placeholder="Mã đơn, email người mua..."
                                   value="${fn:escapeXml(query)}">
                        </div>
                        <div class="filterbar__field">
                            <label class="filterbar__label" for="status">Trạng thái</label>
                            <select id="status" name="status" class="select">
                                <c:set var="currentStatus" value="${status}" />
                                <option value="all" ${currentStatus=='all'?'selected':''}>Tất cả</option>
                                <option value="Open" ${currentStatus=='Open'?'selected':''}>Open</option>
                                <option value="InReview" ${currentStatus=='InReview'?'selected':''}>In review</option>
                                <option value="ResolvedWithRefund" ${currentStatus=='ResolvedWithRefund'?'selected':''}>Resolved + Refund</option>
                                <option value="ResolvedWithoutRefund" ${currentStatus=='ResolvedWithoutRefund'?'selected':''}>Resolved (Không hoàn)</option>
                                <option value="Closed" ${currentStatus=='Closed'?'selected':''}>Closed</option>
                                <option value="Cancelled" ${currentStatus=='Cancelled'?'selected':''}>Cancelled</option>
                            </select>
                        </div>
                        <div class="filterbar__field">
                            <label class="filterbar__label" for="issueType">Loại vấn đề</label>
                            <select id="issueType" name="issueType" class="select">
                                <c:set var="currentIssue" value="${issueType}" />
                                <option value="all" ${currentIssue=='all'?'selected':''}>Tất cả</option>
                                <option value="ACCOUNT_NOT_WORKING" ${currentIssue=='ACCOUNT_NOT_WORKING'?'selected':''}>Account không hoạt động</option>
                                <option value="ACCOUNT_DUPLICATED" ${currentIssue=='ACCOUNT_DUPLICATED'?'selected':''}>Account trùng</option>
                                <option value="ACCOUNT_EXPIRED" ${currentIssue=='ACCOUNT_EXPIRED'?'selected':''}>Account hết hạn</option>
                                <option value="ACCOUNT_MISSING" ${currentIssue=='ACCOUNT_MISSING'?'selected':''}>Thiếu credential</option>
                                <option value="OTHER" ${currentIssue=='OTHER'?'selected':''}>Khác</option>
                            </select>
                        </div>
                        <div class="filterbar__field">
                            <label class="filterbar__label" for="productId">Sản phẩm</label>
                            <%-- Bổ sung ô tìm kiếm cho bộ lọc sản phẩm để thao tác nhanh theo hướng dẫn người dùng. --%>
                            <div class="filterbar__field-group">
                                <input id="productSearch" type="search" class="form-control filterbar__search-input"
                                       placeholder="Tìm nhanh theo tên sản phẩm">
                                <select id="productId" name="productId" class="select">
                                    <option value="" ${empty selectedProductId ? 'selected' : ''}>Tất cả sản phẩm</option>
                                    <c:forEach var="p" items="${products}">
                                        <option value="${p.id}" ${selectedProductId == p.id ? 'selected' : ''}>${fn:escapeXml(p.name)}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="filterbar__actions">
                            <button type="submit" class="button button--primary">Lọc</button>
                            <a href="${base}/seller/disputes" class="button button--ghost">Xóa lọc</a>
                        </div>
                    </div>
                </form>
            </div>
        </section>

        <section class="panel">
            <div class="panel__body">
                <c:choose>
                    <c:when test="${empty disputes}">
                        <p class="disputes-empty">Chưa có khiếu nại nào.</p>
                    </c:when>
                    <c:otherwise>
                        <c:set var="totalDisputes" value="${fn:length(disputes)}" />
                        <c:set var="openCount" value="0" />
                        <c:set var="inReviewCount" value="0" />
                        <c:set var="resolvedCount" value="0" />
                        <c:set var="closedCount" value="0" />
                        <c:forEach var="disputeSummary" items="${disputes}">
                            <c:choose>
                                <c:when test="${disputeSummary.status == 'Open'}">
                                    <c:set var="openCount" value="${openCount + 1}" />
                                </c:when>
                                <c:when test="${disputeSummary.status == 'InReview'}">
                                    <c:set var="inReviewCount" value="${inReviewCount + 1}" />
                                </c:when>
                                <c:when test="${disputeSummary.status == 'ResolvedWithRefund' || disputeSummary.status == 'ResolvedWithoutRefund'}">
                                    <c:set var="resolvedCount" value="${resolvedCount + 1}" />
                                </c:when>
                                <c:when test="${disputeSummary.status == 'Closed' || disputeSummary.status == 'Cancelled'}">
                                    <c:set var="closedCount" value="${closedCount + 1}" />
                                </c:when>
                            </c:choose>
                        </c:forEach>

                        <div class="dispute-summary" aria-label="Tổng quan khiếu nại">
                            <div class="dispute-summary__item">
                                <span class="dispute-summary__label">Tổng khiếu nại</span>
                                <span class="dispute-summary__value">${totalDisputes}</span>
                            </div>
                            <div class="dispute-summary__item">
                                <span class="dispute-summary__label">Đang mở</span>
                                <span class="dispute-summary__value">${openCount}</span>
                            </div>
                            <div class="dispute-summary__item">
                                <span class="dispute-summary__label">Đang rà soát</span>
                                <span class="dispute-summary__value">${inReviewCount}</span>
                            </div>
                            <div class="dispute-summary__item">
                                <span class="dispute-summary__label">Đã xử lý</span>
                                <span class="dispute-summary__value">${resolvedCount}</span>
                            </div>
                            <div class="dispute-summary__item">
                                <span class="dispute-summary__label">Đã đóng</span>
                                <span class="dispute-summary__value">${closedCount}</span>
                            </div>
                        </div>

                        <div class="disputes-table-wrapper">
                            <table class="table table--interactive">
                                <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Mã đơn</th>
                                    <th>Sản phẩm</th>
                                    <th>Cửa hàng</th>
                                    <th>Trạng thái</th>
                                    <th>Tạo lúc</th>
                                    <th>Hoàn tất</th>
                                    <th>Chi tiết</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="d" items="${disputes}" varStatus="st">
                                    <tr>
                                        <td>${(pg_page-1)*pg_size + st.index + 1}</td>
                                        <td>${fn:escapeXml(d.orderReferenceCode)}</td>
                                        <td>${fn:escapeXml(d.productName)}</td>
                                        <td>${fn:escapeXml(d.shopName)}</td>
                                        <td>
                                            <c:set var="statusBadgeClass" value="status-badge--closed" />
                                            <c:choose>
                                                <c:when test="${d.status == 'Open'}">
                                                    <c:set var="statusBadgeClass" value="status-badge--open" />
                                                </c:when>
                                                <c:when test="${d.status == 'InReview'}">
                                                    <c:set var="statusBadgeClass" value="status-badge--review" />
                                                </c:when>
                                                <c:when test="${d.status == 'ResolvedWithRefund' || d.status == 'ResolvedWithoutRefund'}">
                                                    <c:set var="statusBadgeClass" value="status-badge--resolved" />
                                                </c:when>
                                                <c:when test="${d.status == 'Closed' || d.status == 'Cancelled'}">
                                                    <c:set var="statusBadgeClass" value="status-badge--closed" />
                                                </c:when>
                                            </c:choose>
                                            <span class="status-badge ${statusBadgeClass}">
                                                ${fn:escapeXml(d.status)}
                                            </span>
                                        </td>
                                        <td><fmt:formatDate value="${d.createdAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty d.resolvedAt}">
                                                    <fmt:formatDate value="${d.resolvedAt}" pattern="dd-MM-yyyy HH:mm"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">Đang xử lý</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <details>
                                                <summary class="details-toggle">Xem chi tiết</summary>
                                                <div class="dispute-details">
                                                    <div class="dispute-details__group">
                                                        <strong>Loại vấn đề:</strong>
                                                        <c:choose>
                                                            <c:when test="${not empty d.customIssueTitle}">${fn:escapeXml(d.customIssueTitle)}</c:when>
                                                            <c:otherwise>${fn:escapeXml(d.issueType)}</c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div class="dispute-details__group">
                                                        <strong>Lý do khiếu nại:</strong>
                                                        <div class="dispute-text-block">${fn:escapeXml(d.reason)}</div>
                                                    </div>
                                                    <c:if test="${not empty d.resolutionNote}">
                                                        <div class="dispute-details__group">
                                                            <strong>Ghi chú xử lý:</strong>
                                                            <div class="dispute-text-block">${fn:escapeXml(d.resolutionNote)}</div>
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${not empty d.attachments}">
                                                        <div class="dispute-details__group">
                                                            <strong>Tệp đính kèm:</strong>
                                                            <div class="dispute-attachments">
                                                                <c:forEach var="att" items="${d.attachments}">
                                                                    <c:set var="attWebPath" value="${att.webPath}" />
                                                                    <c:if test="${not empty attWebPath}">
                                                                        <c:url var="attUrl" value="${attWebPath}" />
                                                                        <a class="dispute-attachments__item" href="${fn:escapeXml(attUrl)}" target="_blank" rel="noopener">
                                                                            <img class="dispute-attachments__image" src="${fn:escapeXml(attUrl)}" alt="Bằng chứng tranh chấp">
                                                                        </a>
                                                                    </c:if>
                                                                </c:forEach>
                                                            </div>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </details>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <c:if test="${pg_pages > 1}">
                            <div class="disputes-pagination">
                                <c:forEach var="i" begin="1" end="${pg_pages}">
                                    <c:url var="pageUrl" value="/seller/disputes">
                                        <c:param name="page" value="${i}" />
                                        <c:param name="size" value="${pg_size}" />
                                        <c:param name="status" value="${status}" />
                                        <c:param name="issueType" value="${issueType}" />
                                        <c:param name="q" value="${query}" />
                                        <c:if test="${not empty selectedProductId}">
                                            <c:param name="productId" value="${selectedProductId}" />
                                        </c:if>
                                    </c:url>
                                    <a href="${base}${pageUrl}" class="button ${i == pg_page ? 'button--primary' : ''}">${i}</a>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </c:if>
</main>
<script>
    // Script thuần để hỗ trợ tìm kiếm trong bộ lọc sản phẩm theo yêu cầu người dùng.
    document.addEventListener('DOMContentLoaded', function () {
        var searchInput = document.getElementById('productSearch');
        var select = document.getElementById('productId');

        if (!searchInput || !select) {
            return;
        }

        var optionsData = Array.from(select.options).map(function (option, index) {
            return {
                value: option.value,
                text: option.text,
                textLower: option.text.toLowerCase(),
                alwaysVisible: index === 0 || option.value === ''
            };
        });

        select.dataset.selectedValue = select.value;

        select.addEventListener('change', function () {
            select.dataset.selectedValue = select.value;
        });

        function rebuildOptions(keyword) {
            var term = keyword.trim().toLowerCase();
            var currentSelected = select.dataset.selectedValue || '';

            select.innerHTML = '';

            optionsData.forEach(function (data) {
                if (!term || data.textLower.indexOf(term) !== -1 || data.alwaysVisible) {
                    var option = document.createElement('option');
                    option.value = data.value;
                    option.textContent = data.text;
                    if (data.value === currentSelected) {
                        option.selected = true;
                    }
                    select.appendChild(option);
                }
            });

            if (!Array.from(select.options).some(function (option) { return option.selected; }) && select.options.length > 0) {
                select.options[0].selected = true;
                select.dataset.selectedValue = select.options[0].value;
            }
        }

        searchInput.addEventListener('input', function () {
            rebuildOptions(searchInput.value);
        });
    });
</script>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
