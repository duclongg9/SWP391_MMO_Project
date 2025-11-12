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
                <form method="get" action="${base}/seller/disputes" class="form" style="display:flex;flex-wrap:wrap;gap:12px;">
                    <input type="hidden" name="size" value="${pg_size != null ? pg_size : 10}">
                    <div style="flex:1 1 220px;">
                        <label class="form__label" for="q">Từ khóa</label>
                        <input id="q" name="q" class="form__control" type="search" placeholder="Mã đơn, email người mua..."
                               value="${fn:escapeXml(query)}">
                    </div>
                    <div style="flex:1 1 160px;">
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
                    <div style="flex:1 1 160px;">
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
                    <div style="flex:1 1 200px;">
                        <label class="form__label" for="productId">Sản phẩm</label>
                        <select id="productId" name="productId" class="form__control">
                            <option value="" ${empty selectedProductId ? 'selected' : ''}>Tất cả sản phẩm</option>
                            <c:forEach var="p" items="${products}">
                                <option value="${p.id}" ${selectedProductId == p.id ? 'selected' : ''}>${fn:escapeXml(p.name)}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div style="align-self:flex-end;display:flex;gap:8px;">
                        <button type="submit" class="button button--primary">Lọc</button>
                        <a href="${base}/seller/disputes" class="button">Xóa lọc</a>
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
                                        <td>${fn:escapeXml(d.status)}</td>
                                        <td><fmt:formatDate value="${d.createdAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                                        <td><fmt:formatDate value="${d.resolvedAt}" pattern="dd-MM-yyyy HH:mm"/></td>
                                        <td>
                                            <details>
                                                <summary>Xem</summary>
                                                <div style="margin-top:8px;">
                                                    <div><strong>Loại vấn đề:</strong>
                                                        <c:choose>
                                                            <c:when test="${not empty d.customIssueTitle}">${fn:escapeXml(d.customIssueTitle)}</c:when>
                                                            <c:otherwise>${fn:escapeXml(d.issueType)}</c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div style="margin-top:6px;"><strong>Lý do:</strong>
                                                        <div class="text-muted" style="white-space:pre-wrap;">${fn:escapeXml(d.reason)}</div>
                                                    </div>
                                                    <c:if test="${not empty d.resolutionNote}">
                                                        <div style="margin-top:6px;"><strong>Ghi chú xử lý:</strong>
                                                            <div class="text-muted" style="white-space:pre-wrap;">${fn:escapeXml(d.resolutionNote)}</div>
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${not empty d.attachments}">
                                                        <div style="margin-top:6px;"><strong>Ảnh đính kèm:</strong>
                                                            <div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:4px;">
                                                                <c:forEach var="att" items="${d.attachments}">
                                                                    <c:set var="attWebPath" value="${att.webPath}" />
                                                                    <c:if test="${not empty attWebPath}">
                                                                        <c:url var="attUrl" value="${attWebPath}" />
                                                                        <a href="${fn:escapeXml(attUrl)}" target="_blank" rel="noopener"
                                                                           style="display:inline-block;border:1px solid #ddd;border-radius:6px;overflow:hidden;">
                                                                            <img src="${fn:escapeXml(attUrl)}" alt="Attachment"
                                                                                 style="width:80px;height:80px;object-fit:cover;display:block;">
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
