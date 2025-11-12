<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<c:set var="base" value="${pageContext.request.contextPath}" />

<!-- Page specific styles for seller disputes UI refresh -->
<style>
    .dispute-filters {
        display: grid;
        gap: 16px;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        margin: 0;
        padding: 0;
        list-style: none;
    }

    .dispute-filters__actions {
        display: flex;
        gap: 8px;
        align-items: flex-end;
        justify-content: flex-start;
    }

    .dispute-summary {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        gap: 12px;
        margin-bottom: 1.5rem;
    }

    .dispute-summary__item {
        background: #f8f9fb;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 1rem;
        display: flex;
        flex-direction: column;
        gap: 6px;
        box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05);
    }

    .dispute-summary__label {
        color: #475569;
        font-size: 0.85rem;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .dispute-summary__value {
        font-size: 1.6rem;
        font-weight: 600;
        color: #0f172a;
    }

    .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 0.35rem 0.65rem;
        border-radius: 999px;
        font-size: 0.85rem;
        font-weight: 600;
        text-transform: capitalize;
    }

    .status-badge--open {
        background: rgba(59, 130, 246, 0.14);
        color: #1d4ed8;
    }

    .status-badge--review {
        background: rgba(250, 204, 21, 0.18);
        color: #92400e;
    }

    .status-badge--resolved {
        background: rgba(16, 185, 129, 0.18);
        color: #047857;
    }

    .status-badge--closed {
        background: rgba(148, 163, 184, 0.2);
        color: #334155;
    }

    .dispute-details {
        background: #f8fafc;
        border-radius: 8px;
        padding: 0.75rem;
        border: 1px dashed #cbd5f5;
        display: grid;
        gap: 0.75rem;
    }

    .dispute-details__group {
        display: grid;
        gap: 4px;
    }

    .dispute-attachments {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
    }

    .dispute-attachments__item {
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        overflow: hidden;
        display: inline-flex;
        background: #fff;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .dispute-attachments__item:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(15, 23, 42, 0.15);
    }

    .table-responsive {
        border-radius: 10px;
        border: 1px solid #e2e8f0;
        overflow: hidden;
    }

    .table thead th {
        background: #f1f5f9;
    }

    details > summary {
        cursor: pointer;
        color: #2563eb;
        font-weight: 600;
    }

    details[open] > summary::after {
        content: "";
    }

    @media (max-width: 768px) {
        .dispute-filters__actions {
            width: 100%;
            justify-content: flex-start;
        }

        .dispute-filters__actions .button {
            flex: 1 1 auto;
        }
    }
</style>

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
                <form method="get" action="${base}/seller/disputes" class="form">
                    <input type="hidden" name="size" value="${pg_size != null ? pg_size : 10}">
                    <div class="dispute-filters">
                        <div>
                            <label class="form__label" for="q">Từ khóa</label>
                            <input id="q" name="q" class="form__control" type="search" placeholder="Mã đơn, email người mua..."
                                   value="${fn:escapeXml(query)}">
                        </div>
                        <div>
                            <label class="form__label" for="status">Trạng thái</label>
                            <select id="status" name="status" class="form__control">
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
                        <div>
                            <label class="form__label" for="issueType">Loại vấn đề</label>
                            <select id="issueType" name="issueType" class="form__control">
                                <c:set var="currentIssue" value="${issueType}" />
                                <option value="all" ${currentIssue=='all'?'selected':''}>Tất cả</option>
                                <option value="ACCOUNT_NOT_WORKING" ${currentIssue=='ACCOUNT_NOT_WORKING'?'selected':''}>Account không hoạt động</option>
                                <option value="ACCOUNT_DUPLICATED" ${currentIssue=='ACCOUNT_DUPLICATED'?'selected':''}>Account trùng</option>
                                <option value="ACCOUNT_EXPIRED" ${currentIssue=='ACCOUNT_EXPIRED'?'selected':''}>Account hết hạn</option>
                                <option value="ACCOUNT_MISSING" ${currentIssue=='ACCOUNT_MISSING'?'selected':''}>Thiếu credential</option>
                                <option value="OTHER" ${currentIssue=='OTHER'?'selected':''}>Khác</option>
                            </select>
                        </div>
                        <div>
                            <label class="form__label" for="productId">Sản phẩm</label>
                            <select id="productId" name="productId" class="form__control">
                                <option value="" ${empty selectedProductId ? 'selected' : ''}>Tất cả sản phẩm</option>
                                <c:forEach var="p" items="${products}">
                                    <option value="${p.id}" ${selectedProductId == p.id ? 'selected' : ''}>${fn:escapeXml(p.name)}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="dispute-filters__actions">
                            <button type="submit" class="button button--primary">🔎 Lọc</button>
                            <a href="${base}/seller/disputes" class="button">Xóa lọc</a>
                        </div>
                    </div>
                </form>
            </div>
        </section>

        <section class="panel">
            <div class="panel__body">
                <c:choose>
                    <c:when test="${empty disputes}">
                        <p class="text-muted" style="text-align:center;padding:2rem;">Chưa có khiếu nại nào.</p>
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

                        <div class="table-responsive">
                            <table class="table table--interactive" style="width:100%;">
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
                                                <summary>Xem chi tiết</summary>
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
                                                        <div class="text-muted" style="white-space:pre-wrap;">${fn:escapeXml(d.reason)}</div>
                                                    </div>
                                                    <c:if test="${not empty d.resolutionNote}">
                                                        <div class="dispute-details__group">
                                                            <strong>Ghi chú xử lý:</strong>
                                                            <div class="text-muted" style="white-space:pre-wrap;">${fn:escapeXml(d.resolutionNote)}</div>
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
                                                                            <img src="${fn:escapeXml(attUrl)}" alt="Bằng chứng tranh chấp" style="width:90px;height:90px;object-fit:cover;display:block;">
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
                            <div style="display:flex;justify-content:center;gap:8px;margin-top:16px;flex-wrap:wrap;">
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
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
