<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<!-- Nhận biến từ Servlet (đã tính sẵn) -->
<c:set var="pageNow"     value="${requestScope.pg_page}" />
<c:set var="pageSize"    value="${requestScope.pg_size}" />
<c:set var="total"       value="${requestScope.pg_total}" />
<c:set var="pages"       value="${requestScope.pg_pages}" />
<c:set var="isFirst"     value="${requestScope.pg_isFirst}" />
<c:set var="isLast"      value="${requestScope.pg_isLast}" />
<c:set var="singlePage"  value="${requestScope.pg_single}" />


<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shield-check me-2"></i>Danh sách KYC cần duyệt</h4>

    <c:if test="${not empty flash}">
        <div class="alert alert-success shadow-sm">${flash}</div>
        <% session.removeAttribute("flash"); %>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form class="row g-2 align-items-end" action="<c:url value='/admin/kycs'/>" method="get">
                <!-- Khi lọc → luôn quay về trang 1 -->
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" value="${sort}"/>

                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="search" class="form-control"
                               placeholder="Tên người dùng… (nhấn Enter để lọc)"
                               value="${fn:escapeXml(q)}" />
                    </div>
                    <small class="text-muted">Không có nút Lọc; nhấn Enter để tìm.</small>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <input id="from" name="from" type="date" class="form-control" value="${fn:escapeXml(from)}">
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <input id="to" name="to" type="date" class="form-control" value="${fn:escapeXml(to)}">
                </div>

                <!-- Trạng thái -->
                <div class="col-12 col-md-2">
                    <label class="form-label mb-1" for="status">Trạng thái</label>
                    <select id="status" name="status" class="form-select">
                        <option value="all" ${empty status || status == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="1" ${status == '1' ? 'selected' : ''}>Pending</option>
                        <option value="2" ${status == '2' ? 'selected' : ''}>Approved</option>
                        <option value="3" ${status == '3' ? 'selected' : ''}>Rejected</option>
                    </select>
                </div>

                <!-- Xóa lọc -->
                <div class="col-12 col-md-2 d-grid">
                    <a class="btn btn-outline-secondary" href="<c:url value='/admin/kycs'/>">Xóa lọc</a>
                </div>
            </form>
        </div>
    </div>

    <!-- ===== Bảng danh sách ===== -->
    <div class="card shadow-sm">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Người dùng</th>
                        <th class="text-center">Mặt trước</th>
                        <th class="text-center">Mặt sau</th>
                        <th class="text-center">Selfie</th>
                        <th>Số giấy tờ</th>

                        <!-- Cột Ngày gửi (đảo sort không dùng JS) -->
                        <th>
                            <c:url var="uDate" value="/admin/kycs">
                                <c:param name="q"      value="${q}" />
                                <c:param name="from"   value="${from}" />
                                <c:param name="to"     value="${to}" />
                                <c:param name="status" value="${status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort eq 'date_desc' ? 'date_asc' : 'date_desc'}" />
                            </c:url>
                            <a href="${uDate}" class="text-decoration-none">Ngày gửi</a>
                        </th>

                        <!-- Cột Trạng thái (đảo sort không dùng JS) -->
                        <th>
                            <c:url var="uStatus" value="/admin/kycs">
                                <c:param name="q"      value="${q}" />
                                <c:param name="from"   value="${from}" />
                                <c:param name="to"     value="${to}" />
                                <c:param name="status" value="${status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort eq 'status_asc' ? 'status_desc' : 'status_asc'}" />
                            </c:url>
                            <a href="${uStatus}" class="text-decoration-none">Trạng thái</a>
                        </th>

                        <th class="text-center" style="width:140px">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty kycList}">
                            <c:forEach var="k" items="${kycList}" varStatus="st">

                                <!-- Nếu ảnh đã chuẩn hoá ở Servlet thì có thể dùng trực tiếp k.frontImageUrl ...
                                Nếu chưa, fallback thêm base vào URL tương đối: -->
                                <c:set var="frontSrc"  value="${k.frontImageUrl}" />
                                <c:set var="backSrc"   value="${k.backImageUrl}" />
                                <c:set var="selfieSrc" value="${k.selfieImageUrl}" />

                                <c:if test="${not empty frontSrc and not fn:startsWith(frontSrc,'http')}">
                                    <c:if test="${not fn:startsWith(frontSrc,'/')}">
                                        <c:set var="frontSrc" value="/${frontSrc}" />
                                    </c:if>
                                    <c:set var="frontSrc" value="${base}${frontSrc}" />
                                </c:if>
                                <c:if test="${not empty backSrc and not fn:startsWith(backSrc,'http')}">
                                    <c:if test="${not fn:startsWith(backSrc,'/')}">
                                        <c:set var="backSrc" value="/${backSrc}" />
                                    </c:if>
                                    <c:set var="backSrc" value="${base}${backSrc}" />
                                </c:if>
                                <c:if test="${not empty selfieSrc and not fn:startsWith(selfieSrc,'http')}">
                                    <c:if test="${not fn:startsWith(selfieSrc,'/')}">
                                        <c:set var="selfieSrc" value="/${selfieSrc}" />
                                    </c:if>
                                    <c:set var="selfieSrc" value="${base}${selfieSrc}" />
                                </c:if>

                                <tr>
                                    <td>${(pageNow-1)*pageSize + st.index + 1}</td>

                                    <td>
                                        <div class="fw-semibold">${k.userName}</div>
                                        <div class="text-muted small">${k.userEmail}</div>
                                    </td>

                                    <td class="text-center">
                                        <img src="${frontSrc}" alt="front" class="img-thumbnail"
                                             style="width:80px;height:56px;object-fit:cover;">
                                    </td>
                                    <td class="text-center">
                                        <img src="${backSrc}" alt="back" class="img-thumbnail"
                                             style="width:80px;height:56px;object-fit:cover;">
                                    </td>
                                    <td class="text-center">
                                        <img src="${selfieSrc}" alt="selfie" class="img-thumbnail"
                                             style="width:80px;height:56px;object-fit:cover;">
                                    </td>

                                    <td>${k.idNumber}</td>
                                    <td><fmt:formatDate value="${k.createdAt}" pattern="dd-MM-yyyy"/></td>

                                    <td>
                                        <c:set var="statusText">
                                            <c:choose>
                                                <c:when test="${not empty k.statusName}">${k.statusName}</c:when>
                                                <c:when test="${k.statusId == 1}">Pending</c:when>
                                                <c:when test="${k.statusId == 2}">Approved</c:when>
                                                <c:when test="${k.statusId == 3}">Rejected</c:when>
                                                <c:otherwise>Unknown</c:otherwise>
                                            </c:choose>
                                        </c:set>
                                        <span class="badge
                      <c:choose>
                        <c:when test='${k.statusId == 1}'>bg-warning text-dark</c:when>
                        <c:when test='${k.statusId == 2}'>bg-success</c:when>
                        <c:when test='${k.statusId == 3}'>bg-danger</c:when>
                        <c:otherwise>bg-secondary</c:otherwise>
                      </c:choose>">
                                                ${statusText}
                                        </span>
                                    </td>

                                    <td class="text-center">
                                        <c:url var="detailUrl" value="/admin/kycs/detail">
                                            <c:param name="id" value="${k.id}" />
                                        </c:url>
                                        <a class="btn btn-sm btn-primary" href="${detailUrl}">
                                            <i class="bi bi-eye"></i> Xem chi tiết
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="9" class="text-center text-muted py-4">Không có hồ sơ KYC.</td></tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- ===== Phân trang ===== -->
    <nav aria-label="Pagination">
        <ul class="pagination justify-content-center mt-3">

            <!-- Prev -->
            <c:choose>
                <c:when test="${pageNow <= 1 || pages <= 1}">
                    <li class="page-item disabled">
                        <span class="page-link" aria-disabled="true">&laquo;</span>
                    </li>
                </c:when>
                <c:otherwise>
                    <c:url var="uPrev" value="/admin/kycs">
                        <c:param name="page" value="${pageNow-1}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="sort" value="${sort}" />
                        <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                        <c:if test="${not empty from}"><c:param name="from" value="${from}"/></c:if>
                        <c:if test="${not empty to}"><c:param name="to" value="${to}"/></c:if>
                        <c:if test="${not empty status && status ne 'all'}"><c:param name="status" value="${status}"/></c:if>
                    </c:url>
                    <li class="page-item">
                        <a class="page-link" href="${uPrev}" aria-label="Previous">&laquo;</a>
                    </li>
                </c:otherwise>
            </c:choose>

            <!-- Current page -->
            <li class="page-item active"><span class="page-link">${pageNow}</span></li>

            <!-- Next -->
            <c:choose>
                <c:when test="${pageNow >= pages || pages <= 1}">
                    <li class="page-item disabled">
                        <span class="page-link" aria-disabled="true">&raquo;</span>
                    </li>
                </c:when>
                <c:otherwise>
                    <c:url var="uNext" value="/admin/kycs">
                        <c:param name="page" value="${pageNow+1}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="sort" value="${sort}" />
                        <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                        <c:if test="${not empty from}"><c:param name="from" value="${from}"/></c:if>
                        <c:if test="${not empty to}"><c:param name="to" value="${to}"/></c:if>
                        <c:if test="${not empty status && status ne 'all'}"><c:param name="status" value="${status}"/></c:if>
                    </c:url>
                    <li class="page-item">
                        <a class="page-link" href="${uNext}" aria-label="Next">&raquo;</a>
                    </li>
                </c:otherwise>
            </c:choose>

        </ul>
    </nav>
    <div style="position:fixed;bottom:6px;left:6px;font:12px/1 monospace;background:#f6f8fa;border:1px solid #ddd;padding:6px 8px;border-radius:6px;z-index:9999">
        pageNow=${pageNow}, pages=${pages}, total=${total}, size=${pageSize},
        isFirst=${isFirst}, isLast=${isLast}, singlePage=${singlePage}
    </div>
</div>

<style>
    .table td,.table th{ vertical-align:middle }
</style>
