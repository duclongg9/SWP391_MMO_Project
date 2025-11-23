<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.time.LocalDate" %>

<%
    String today = LocalDate.now().toString();
    request.setAttribute("today", today);
%>

<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (txList != null ? fn:length(txList) : 0)}" />
<c:set var="pages"    value="${(total + pageSize - 1) / pageSize}" />
<c:set var="sort"     value="${empty requestScope.sort ? 'date_desc' : requestScope.sort}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-cash-coin me-2"></i>Nạp / Rút</h4>
    <div class="card shadow-sm">
        <div class="card-body">

            <!-- FILTER -->
            <form id="cashFilter"
                  action="${pageContext.request.contextPath}/admin/cashs"
                  method="get"
                  class="row g-2 align-items-end mb-3">

                <input type="hidden" name="page" id="pageInput" value="${pageNow}">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" id="sort" value="${sort}"/>

                <div class="col-sm-2">
                    <label class="form-label mb-1" for="typeSelect">Loại</label>
                    <select class="form-select" name="type" id="typeSelect"
                            onchange="onFilterChange()">
                        <option value="all"        ${f_type=='all'?'selected':''}>Tất cả</option>
                        <option value="Deposit"    ${f_type=='Deposit'?'selected':''}>Nạp tiền</option>
                        <option value="Withdrawal" ${f_type=='Withdrawal'?'selected':''}>Rút tiền</option>
                    </select>
                </div>

                <div class="col-sm-2">
                    <label class="form-label mb-1" for="q">Tên người dùng</label>
                    <div class="input-group">
                        <input id="q" type="search" class="form-control" name="q" value="${f_q}" placeholder="Nhập tên…">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-search"></i></button>
                    </div>
                </div>

                <div class="col-sm-2">
                    <label class="form-label mb-1" for="statusSelect">Trạng thái</label>
                    <select class="form-select" name="status" id="statusSelect"
                            onchange="onFilterChange()">
                        <option value="all"       ${f_status=='all'?'selected':''}>Tất cả</option>
                        <option value="Pending"   ${f_status=='Pending'?'selected':''}>Pending</option>
                        <option value="Completed" ${f_status=='Completed'?'selected':''}>Completed</option>
                        <option value="Rejected"  ${f_status=='Rejected'?'selected':''}>Rejected</option>
                    </select>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <input id="from" name="from"
                           type="date"
                           class="form-control"
                           value="${fn:escapeXml(from)}"
                           max="${today}">
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <input id="to" name="to"
                           type="date"
                           class="form-control"
                           value="${fn:escapeXml(to)}"
                           max="${today}">
                </div>

                <div class="col-6 col-md-1 d-flex gap-2">
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/cashs">Xóa lọc</a>
                </div>
            </form>

            <div id="toastBox"></div>

            <!-- TABLE -->
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Loại</th>
                        <th>User</th>
                        <th>Số tiền</th>
                        <th>
                            <c:url var="uStatusSort" value="/admin/cashs">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort == 'status_asc' ? 'status_desc' : 'status_asc'}" />
                            </c:url>
                            <a href="${uStatusSort}" class="text-decoration-none">Trạng thái</a>
                        </th>
                        <th>
                            <c:url var="uDateSort" value="/admin/cashs">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort == 'date_asc' ? 'date_desc' : 'date_asc'}" />
                            </c:url>
                            <a href="${uDateSort}" class="text-decoration-none">Tạo lúc</a>
                        </th>
                        <th>Xử lý lúc</th>
                        <th>Admin note</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach var="t" items="${txList}" varStatus="st">
                        <tr>
                            <td>${(pageNow-1)*pageSize + st.index + 1}</td>

                            <td>
                                <c:choose>
                                    <c:when test="${t.type eq 'Deposit'}">
                                        <span class="badge bg-primary">Nạp tiền</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-warning text-dark">Rút tiền</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td class="fw-semibold">${fn:escapeXml(t.userName)}</td>
                            <td><fmt:formatNumber value="${t.amount}" type="number"/></td>

                            <td>
                                <c:set var="statusClass"
                                       value="${t.status eq 'Completed'
                                                ? 'bg-success'
                                                : (t.status eq 'Pending'
                                                    ? 'bg-secondary'
                                                    : 'bg-danger')}"/>
                                <span class="badge ${statusClass}">${fn:escapeXml(t.status)}</span>
                            </td>

                            <td><fmt:formatDate value="${t.createdAt}" pattern="dd-MM-yyyy HH:mm"/></td>

                            <td>
                                <c:choose>
                                    <c:when test="${t.processedAt != null}">
                                        <fmt:formatDate value="${t.processedAt}" pattern="dd-MM-yyyy HH:mm"/>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${not empty t.adminNote}">
                                        ${fn:escapeXml(t.adminNote)}
                                    </c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>

                            <td class="text-center">
                                <button type="button"
                                        class="btn btn-sm btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#txModal_${t.type}_${t.id}">
                                    <i class="bi bi-eye"></i> Xem chi tiết
                                </button>
                            </td>
                        </tr>

                        <!-- MODAL -->
                        <div class="modal fade" id="txModal_${t.type}_${t.id}" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-dark text-white">
                                        <h5 class="modal-title">
                                            Chi tiết
                                            <c:choose>
                                                <c:when test="${t.type eq 'Deposit'}"> nạp tiền</c:when>
                                                <c:otherwise> rút tiền</c:otherwise>
                                            </c:choose>
                                            #${t.id}
                                        </h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                    </div>

                                    <div class="modal-body">

                                        <!-- Info chung -->
                                        <div class="row g-3 mb-3">
                                            <div class="col-md-4">
                                                <div class="text-muted small">Loại</div>
                                                <div class="fw-semibold">
                                                    <c:choose>
                                                        <c:when test="${t.type eq 'Deposit'}">Nạp tiền</c:when>
                                                        <c:otherwise>Rút tiền</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">User</div>
                                                <div class="fw-semibold">
                                                        ${fn:escapeXml(t.userName)}
                                                    <span class="text-muted">(#${t.userId})</span>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">Số tiền</div>
                                                <div class="fw-semibold">
                                                    <fmt:formatNumber value="${t.amount}" type="number"/>
                                                </div>
                                            </div>

                                            <div class="col-md-4">
                                                <div class="text-muted small">Trạng thái</div>
                                                <c:set var="mStatusClass"
                                                       value="${t.status eq 'Completed'
                                                                ? 'bg-success'
                                                                : (t.status eq 'Pending'
                                                                    ? 'bg-warning text-dark'
                                                                    : 'bg-danger')}"/>
                                                <span class="badge ${mStatusClass}">
                                                        ${fn:escapeXml(t.status)}
                                                </span>
                                            </div>

                                            <div class="col-md-4">
                                                <div class="text-muted small">Tạo lúc</div>
                                                <div class="fw-semibold">
                                                    <fmt:formatDate value="${t.createdAt}" pattern="dd-MM-yyyy HH:mm"/>
                                                </div>
                                            </div>

                                            <div class="col-md-4">
                                                <div class="text-muted small">Xử lý lúc</div>
                                                <div class="fw-semibold">
                                                    <c:choose>
                                                        <c:when test="${t.processedAt != null}">
                                                            <fmt:formatDate value="${t.processedAt}" pattern="dd-MM-yyyy HH:mm"/>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- ========= WITHDRAWAL ========= -->
                                        <c:if test="${t.type eq 'Withdrawal'}">
                                            <c:choose>
                                                <!-- Pending -->
                                                <c:when test="${t.status eq 'Pending'}">
                                                    <form id="cashForm_${t.id}"
                                                          action="${pageContext.request.contextPath}/admin/cashs/status"
                                                          method="post"
                                                          enctype="multipart/form-data"
                                                          class="d-flex flex-column gap-3 mt-2">

                                                        <input type="hidden" name="id" value="${t.id}"/>
                                                        <input type="hidden" name="txType" value="Withdrawal"/>
                                                        <input type="hidden" name="action" id="cashAction_${t.id}" value=""/>

                                                        <div class="text-danger small mt-1 id-error d-none">
                                                            Lỗi: ID giao dịch không hợp lệ.
                                                        </div>

                                                        <!-- QR / thông tin khách -->
                                                        <div class="border rounded-3 p-3">
                                                            <div class="fw-semibold mb-2">QR / thông tin của khách (nếu có)</div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.bankAccountInfo}">
                                                                        <c:set var="infoNorm"
                                                                               value="${fn:replace(t.bankAccountInfo, '\\\\', '/')}" />
                                                                        <div class="js-proof-images"
                                                                             data-ctx="${pageContext.request.contextPath}"
                                                                             data-urls="${fn:escapeXml(infoNorm)}"></div>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="text-muted fst-italic">Không có ảnh hợp lệ.</div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>

                                                        <!-- Ảnh chuyển khoản admin: BẮT BUỘC khi Accept -->
                                                        <div class="border rounded-3 p-3 bg-light">
                                                            <div class="fw-semibold mb-1">Ảnh chuyển khoản của admin (bắt buộc khi DUYỆT)</div>
                                                            <div class="text-end mb-2">
                                                                <span id="adminStatus_${t.id}"
                                                                      class="badge ${not empty t.adminProofUrl ? 'bg-success' : 'bg-secondary'}">
                                                                        ${not empty t.adminProofUrl ? 'Đã upload' : 'Chưa có'}
                                                                </span>
                                                            </div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.adminProofUrl}">
                                                                        <c:set var="adminNorm"
                                                                               value="${fn:replace(t.adminProofUrl, '\\\\', '/')}" />
                                                                        <img id="adminPreview_${t.id}"
                                                                             src="${pageContext.request.contextPath}/assets/images/QRcode/${fn:escapeXml(adminNorm)}"
                                                                             alt="Admin proof"
                                                                             class="rounded border js-zoomable"
                                                                             style="max-width:280px;max-height:420px;object-fit:cover;">
                                                                        <div id="adminEmpty_${t.id}" class="d-none"></div>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <img id="adminPreview_${t.id}"
                                                                             class="rounded border js-zoomable d-none"
                                                                             style="max-width:280px;max-height:420px;object-fit:cover;"
                                                                             alt="Admin proof preview">
                                                                        <div id="adminEmpty_${t.id}" class="text-muted fst-italic">
                                                                            Chưa có ảnh. Chọn file bên dưới để xem trước.
                                                                        </div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>

                                                            <div class="mt-2">
                                                                <input type="file"
                                                                       id="adminFile_${t.id}"
                                                                       name="proof_file"
                                                                       class="form-control"
                                                                       accept="image/*"
                                                                       onchange="previewAdminProofById(this,
                                                                               'adminPreview_${t.id}',
                                                                               'adminEmpty_${t.id}',
                                                                               'adminStatus_${t.id}',
                                                                               'adminProofError_${t.id}')">
                                                            </div>

                                                            <div id="adminProofError_${t.id}"
                                                                 class="text-danger small mt-1 d-none">
                                                                ⚠️ Vui lòng upload ảnh chuyển khoản của admin trước khi duyệt.
                                                            </div>
                                                        </div>

                                                        <!-- Admin note -->
                                                        <div>
                                                            <label class="form-label mb-1 fw-semibold">Admin note (lý do)</label>
                                                            <textarea id="cashNote_${t.id}"
                                                                      name="note"
                                                                      class="form-control"
                                                                      rows="3"
                                                                      placeholder="Bắt buộc nhập khi Reject…">${fn:escapeXml(t.adminNote)}</textarea>
                                                            <div id="cashError_${t.id}"
                                                                 class="text-danger small mt-1 d-none">
                                                                ⚠️ Vui lòng nhập lý do khi từ chối giao dịch.
                                                            </div>
                                                        </div>

                                                        <!-- Buttons -->
                                                        <div class="d-flex align-items- mt-3 gap-2">
                                                            <button type="button"
                                                                    class="btn btn-success"
                                                                    onclick="onAcceptCash(${t.id}, 'Withdrawal')">
                                                                <i class="bi bi-check-circle me-1"></i>Accept
                                                            </button>

                                                            <button type="button"
                                                                    class="btn btn-danger px-4"
                                                                    onclick="onRejectCash(${t.id});">
                                                                <i class="bi bi-x-circle me-1"></i>Reject
                                                            </button>

                                                            <button type="button"
                                                                    class="btn btn-outline-secondary ms-auto px-4"
                                                                    data-bs-dismiss="modal">
                                                                Đóng
                                                            </button>
                                                        </div>
                                                    </form>
                                                </c:when>

                                                <!-- Đã xử lý -->
                                                <c:otherwise>
                                                    <div class="mt-3">
                                                        <div class="border rounded-3 p-3 mb-3">
                                                            <div class="fw-semibold mb-2">QR / thông tin của khách (nếu có)</div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.bankAccountInfo}">
                                                                        <c:set var="infoNorm"
                                                                               value="${fn:replace(t.bankAccountInfo, '\\\\', '/')}" />
                                                                        <div class="js-proof-images"
                                                                             data-ctx="${pageContext.request.contextPath}"
                                                                             data-urls="${fn:escapeXml(infoNorm)}"></div>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="text-muted fst-italic">Không có QR / ảnh từ khách.</div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="border rounded-3 p-3 mb-3 bg-light">
                                                            <div class="fw-semibold mb-2">Ảnh chuyển khoản của admin</div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.adminProofUrl}">
                                                                        <c:set var="adminNorm"
                                                                               value="${fn:replace(t.adminProofUrl, '\\\\', '/')}" />
                                                                        <img src="${pageContext.request.contextPath}/assets/images/QRcode/${fn:escapeXml(adminNorm)}"
                                                                             alt="Admin proof"
                                                                             class="rounded border js-zoomable"
                                                                             style="max-width:280px;max-height:420px;object-fit:cover;">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="text-muted fst-italic">Không có ảnh chuyển khoản của admin.</div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="mb-3">
                                                            <div class="text-muted small mb-1">Admin note</div>
                                                            <div class="fw-semibold">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.adminNote}">${fn:escapeXml(t.adminNote)}</c:when>
                                                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="d-flex justify-content-end">
                                                            <button type="button"
                                                                    class="btn btn-outline-secondary px-4"
                                                                    data-bs-dismiss="modal">
                                                                Đóng
                                                            </button>
                                                        </div>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>

                                        <!-- ========= DEPOSIT ========= -->
                                        <c:if test="${t.type eq 'Deposit'}">
                                            <c:choose>
                                                <!-- Pending -->
                                                <c:when test="${t.status eq 'Pending'}">
                                                    <form id="cashForm_${t.id}"
                                                          action="${pageContext.request.contextPath}/admin/cashs/status"
                                                          method="post"
                                                          class="d-flex flex-column gap-3 mt-2">

                                                        <input type="hidden" name="id" value="${t.id}"/>
                                                        <input type="hidden" name="txType" value="Deposit"/>
                                                        <input type="hidden" name="action" id="cashAction_${t.id}" value=""/>

                                                        <div class="text-danger small mt-1 id-error d-none">
                                                            Lỗi: ID giao dịch không hợp lệ.
                                                        </div>

                                                        <div class="border rounded-3 p-3 bg-light">
                                                            <div class="d-flex justify-content-between align-items-center mb-2">
                                                                <div class="fw-semibold">Chứng từ khách gửi</div>
                                                                <span class="badge bg-info text-dark">QR / Bill</span>
                                                            </div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.qrContent}">
                                                                        <c:set var="qrNorm"
                                                                               value="${fn:replace(t.qrContent, '\\\\', '/')}" />
                                                                        <div class="js-proof-images"
                                                                             data-ctx="${pageContext.request.contextPath}"
                                                                             data-urls="${fn:escapeXml(qrNorm)}"></div>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="text-muted fst-italic">Khách chưa gửi ảnh chứng từ.</div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>

                                                        <div>
                                                            <label class="form-label mb-1 fw-semibold">Admin note (lý do)</label>
                                                            <textarea id="cashNote_${t.id}"
                                                                      name="note"
                                                                      class="form-control"
                                                                      rows="3"
                                                                      placeholder="Bắt buộc nhập khi Reject…">${fn:escapeXml(t.adminNote)}</textarea>
                                                            <div id="cashError_${t.id}"
                                                                 class="text-danger small mt-1 d-none">
                                                                ⚠️ Vui lòng nhập lý do khi từ chối giao dịch.
                                                            </div>
                                                        </div>

                                                        <div class="d-flex align-items-center gap-2 mt-1">
                                                            <button type="submit"
                                                                    class="btn btn-success px-4"
                                                                    onclick="document.getElementById('cashAction_${t.id}').value='accept'; return onSubmitCash(${t.id}, 'accept');">
                                                                <i class="bi bi-check-circle me-1"></i>Accept
                                                            </button>
                                                            <button type="button"
                                                                    class="btn btn-danger px-4"
                                                                    onclick="onRejectCash(${t.id});">
                                                                <i class="bi bi-x-circle me-1"></i>Reject
                                                            </button>
                                                            <button type="button"
                                                                    class="btn btn-outline-secondary ms-auto px-4"
                                                                    data-bs-dismiss="modal">
                                                                Đóng
                                                            </button>
                                                        </div>
                                                    </form>
                                                </c:when>

                                                <!-- Đã xử lý -->
                                                <c:otherwise>
                                                    <div class="mt-3">
                                                        <div class="border rounded-3 p-3 mb-3 bg-light">
                                                            <div class="d-flex justify-content-between align-items-center mb-2">
                                                                <div class="fw-semibold">Chứng từ khách gửi</div>
                                                                <span class="badge bg-info text-dark">QR / Bill</span>
                                                            </div>
                                                            <div class="proof-image-wrapper">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.qrContent}">
                                                                        <c:set var="qrNorm"
                                                                               value="${fn:replace(t.qrContent, '\\\\', '/')}" />
                                                                        <div class="js-proof-images"
                                                                             data-ctx="${pageContext.request.contextPath}"
                                                                             data-urls="${fn:escapeXml(qrNorm)}"></div>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="text-muted fst-italic">Không có ảnh chứng từ.</div>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="mb-3">
                                                            <div class="text-muted small mb-1">Admin note</div>
                                                            <div class="fw-semibold">
                                                                <c:choose>
                                                                    <c:when test="${not empty t.adminNote}">${fn:escapeXml(t.adminNote)}</c:when>
                                                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </div>
                                                        <div class="d-flex justify-content-end">
                                                            <button type="button"
                                                                    class="btn btn-outline-secondary px-4"
                                                                    data-bs-dismiss="modal">
                                                                Đóng
                                                            </button>
                                                        </div>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>

                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- END MODAL -->

                    </c:forEach>

                    <c:if test="${empty txList}">
                        <tr>
                            <td colspan="10" class="text-center text-muted py-4">Không có giao dịch</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <!-- Modal zoom ảnh dùng chung -->
            <div class="modal fade" id="imgZoomModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-xl">
                    <div class="modal-content bg-dark border-0">
                        <div class="modal-body d-flex justify-content-center p-2">
                            <img id="imgZoomTarget" src="" alt="Preview" class="img-fluid rounded">
                        </div>
                    </div>
                </div>
            </div>

            <!-- PAGINATION -->
            <c:url var="cashsPath" value="/admin/cashs"/>
            <nav aria-label="Pagination">
                <ul class="pagination justify-content-center mt-3">

                    <!-- Prev -->
                    <li class="page-item ${pageNow <= 1 ? 'disabled' : ''}">
                        <c:url var="uPrev" value="${cashsPath}">
                            <c:param name="type"   value="${f_type}" />
                            <c:param name="q"      value="${f_q}" />
                            <c:param name="status" value="${f_status}" />
                            <c:param name="from"   value="${from}" />
                            <c:param name="to"     value="${to}" />
                            <c:param name="size"   value="${pageSize}" />
                            <c:param name="page"   value="${pageNow-1}" />
                            <c:param name="sort"   value="${sort}" />
                        </c:url>
                        <a class="page-link" href="${pageNow <= 1 ? '#' : uPrev}" aria-label="Previous">&laquo;</a>
                    </li>

                    <!-- Pages -->
                    <c:forEach var="i" begin="1" end="${pages}">
                        <c:url var="uI" value="${cashsPath}">
                            <c:param name="type"   value="${f_type}" />
                            <c:param name="q"      value="${f_q}" />
                            <c:param name="status" value="${f_status}" />
                            <c:param name="from"   value="${from}" />
                            <c:param name="to"     value="${to}" />
                            <c:param name="size"   value="${pageSize}" />
                            <c:param name="page"   value="${i}" />
                            <c:param name="sort"   value="${sort}" />
                        </c:url>
                        <li class="page-item ${i == pageNow ? 'active' : ''}">
                            <a class="page-link" href="${uI}">${i}</a>
                        </li>
                    </c:forEach>

                    <!-- Next -->
                    <li class="page-item ${pageNow >= pages ? 'disabled' : ''}">
                        <c:url var="uNext" value="${cashsPath}">
                            <c:param name="type"   value="${f_type}" />
                            <c:param name="q"      value="${f_q}" />
                            <c:param name="status" value="${f_status}" />
                            <c:param name="from"   value="${from}" />
                            <c:param name="to"     value="${to}" />
                            <c:param name="size"   value="${pageSize}" />
                            <c:param name="page"   value="${pageNow+1}" />
                            <c:param name="sort"   value="${sort}" />
                        </c:url>
                        <a class="page-link" href="${pageNow >= pages ? '#' : uNext}" aria-label="Next">&raquo;</a>
                    </li>

                </ul>
            </nav>

        </div>
    </div>
</div>

<style>
    #toastBox {
        position: fixed; bottom: 30px; right: 30px;
        display: flex; flex-direction: column; align-items: flex-end;
        padding: 20px; z-index: 9999;
    }
    .mm-toast {
        width: 400px; height: 80px; background: #fff;
        font-weight: 500; margin: 15px 0;
        box-shadow: 0 0 20px rgba(0,0,0,0.3);
        display: flex; align-items: center;
        padding: 20px; position: relative;
        transform: translateX(100%);
        animation: moveleft 0.5s linear forwards;
    }
    .mm-toast.error { color: red; }
    .mm-toast::after {
        content:''; position:absolute; left:0; bottom:0; width:100%; height:5px;
        background: green; animation: anim 5s linear forwards;
    }
    .mm-toast.error::after { background:red; }
    @keyframes anim { 100% { width:0; } }
    @keyframes moveleft { 100% { transform: translateX(0); } }

    .proof-image-wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
    }
    .proof-image-wrapper img {
        max-width: 280px;
        max-height: 420px;
        border-radius: 8px;
        object-fit: cover;
        transition: transform 0.2s ease-in-out;
    }
    .proof-image-wrapper img:hover {
        transform: scale(1.03);
        cursor: zoom-in;
    }
    #imgZoomTarget {
        max-width: 95vw;
        max-height: 95vh;
        object-fit: contain;
    }
</style>

<script>
    /* ===== Toast ===== */
    function showToast(msg, type = 'success') {
        const box = document.getElementById('toastBox');
        if (!box) { alert(msg); return; }
        const t = document.createElement('div');
        t.className = 'mm-toast' + (type === 'error' ? ' error' : '');
        t.innerHTML = msg;
        box.appendChild(t);
        setTimeout(() => t.remove(), 5000);
    }

    /* ===== Auto submit filter (type/status/date) ===== */
    function onFilterChange() {
        const form = document.getElementById('cashFilter');
        if (!form) return;
        let pageInput = document.getElementById('pageInput');
        if (!pageInput) {
            pageInput = document.createElement('input');
            pageInput.type = 'hidden';
            pageInput.name = 'page';
            pageInput.id = 'pageInput';
            form.appendChild(pageInput);
        }
        pageInput.value = '1';

        if (typeof form.requestSubmit === 'function') form.requestSubmit();
        else form.submit();
    }

    /* ===== Accept/Reject ===== */
    function onAcceptCash(id, txType) {
        const form = document.getElementById('cashForm_' + id);
        if (!form) return false;

        const action = document.getElementById('cashAction_' + id);
        const note   = document.getElementById('cashNote_' + id);
        const proofInput = document.getElementById('adminFile_' + id);
        const preview    = document.getElementById('adminPreview_' + id);
        const errNote    = document.getElementById('cashError_' + id);
        const errProof   = document.getElementById('adminProofError_' + id);

        if (errNote)  errNote.classList.add('d-none');
        if (errProof) errProof.classList.add('d-none');
        if (note)        note.classList.remove('is-invalid');
        if (proofInput)  proofInput.classList.remove('is-invalid');

        const noteVal = note ? note.value.trim() : "";

        if (txType === 'Deposit' && !noteVal) {
            if (errNote) {
                errNote.textContent = "Vui lòng nhập ghi chú khi duyệt nạp tiền.";
                errNote.classList.remove('d-none');
            }
            if (note) note.classList.add('is-invalid');
            if (note) note.focus();
            return false;
        }

        if (txType === 'Withdrawal') {
            const hasExisting = preview && !preview.classList.contains('d-none') && preview.src ? true : false;
            const hasFile     = proofInput && proofInput.files && proofInput.files.length > 0;
            const hasImage = hasExisting || hasFile;
            const hasNote = !!noteVal;

            if (!hasImage || !hasNote) {
                if (!hasImage) {
                    if (errProof) errProof.classList.remove('d-none');
                    if (proofInput) proofInput.classList.add('is-invalid');
                }
                if (!hasNote) {
                    if (errNote) errNote.classList.remove('d-none');
                    if (note) note.classList.add('is-invalid');
                }
                if (!hasNote && note) note.focus();
                else if (!hasImage && proofInput) proofInput.focus();
                return false;
            }
        }

        const msg = `Bạn có chắc chắn muốn DUYỆT giao dịch #${id} (${txType}) không?`;
        if (!confirm(msg)) return false;

        if (action) action.value = 'accept';
        form.submit();
        return true;
    }

    function onRejectCash(id) {
        const form   = document.getElementById('cashForm_' + id);
        const note   = document.getElementById('cashNote_' + id);
        const errBox = document.getElementById('cashError_' + id);
        const action = document.getElementById('cashAction_' + id);

        if (!form || !note || !action) return false;

        if (errBox) errBox.classList.add('d-none');
        note.classList.remove('is-invalid');

        if (!note.value.trim()) {
            if (errBox) errBox.classList.remove('d-none');
            note.classList.add('is-invalid');
            note.focus();
            return false;
        }

        if (!confirm(`Bạn có chắc chắn muốn TỪ CHỐI giao dịch #${id} không?`)) {
            return false;
        }

        action.value = 'reject';
        form.submit();
        return true;
    }

    document.addEventListener('input', function (e) {
        if (!e.target.id) return;
        const m = e.target.id.match(/^cashNote_(\d+)$/);
        if (!m) return;
        const id = m[1];
        const err = document.getElementById('cashError_' + id);
        if (err) err.classList.add('d-none');
        e.target.classList.remove('is-invalid');
    });

    /* ===== Preview ảnh admin ===== */
    function previewAdminProofById(input, imgId, emptyId, statusId, errorId) {
        const file = input.files && input.files[0];
        if (!file) return;

        if (!file.type || !file.type.startsWith('image/')) {
            showToast('File không phải ảnh, vui lòng chọn file ảnh.', 'error');
            input.value = '';
            return;
        }

        const img    = document.getElementById(imgId);
        const empty  = document.getElementById(emptyId);
        const status = document.getElementById(statusId);
        const err    = errorId ? document.getElementById(errorId) : null;

        const url = URL.createObjectURL(file);
        if (img) {
            img.src = url;
            img.classList.remove('d-none');
        }
        if (empty)  empty.classList.add('d-none');
        if (status) {
            status.textContent = 'Đã chọn (chưa lưu)';
            status.className = 'badge bg-warning text-dark';
        }
        if (err) err.classList.add('d-none');
        input.classList.remove('is-invalid');
    }

    /* ===== Render ảnh chứng từ ===== */
    function htmlUnescape(s) {
        if (!s) return "";
        return s.replace(/&quot;|&#34;/g, '"')
            .replace(/&#39;|&apos;/g, "'")
            .replace(/&lt;/g, '<')
            .replace(/&gt;/g, '>')
            .replace(/&amp;/g, '&');
    }

    function renderProofImages(container) {
        if (!container) return;

        const raw = htmlUnescape(container.getAttribute("data-urls") || "").trim();
        if (!raw) {
            container.innerHTML = '<span class="text-muted">Không có ảnh.</span>';
            return;
        }

        const ctx = (container.getAttribute("data-ctx") || "").replace(/\/+$/, "");

        let urls = [];
        try {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) urls = parsed;
            else if (typeof parsed === "string") urls = [parsed];
        } catch (e) {
            urls = raw.split(/[\s,]+/).filter(Boolean);
        }

        if (!urls.length) {
            container.innerHTML = '<span class="text-muted">Không có ảnh hợp lệ.</span>';
            return;
        }

        const frag = document.createDocumentFragment();

        urls.forEach(function (u) {
            let url = (u || "").trim();
            if (!url) return;

            if (!/^https?:\/\//i.test(url) && url.charAt(0) !== '/') {
                url = ctx + "/assets/images/QRcode/" + url;
            }

            const img = document.createElement("img");
            img.src = url;
            img.alt = "Proof";
            img.className = "rounded border js-zoomable";
            img.style.maxWidth = "280px";
            img.style.maxHeight = "420px";
            img.style.objectFit = "cover";
            frag.appendChild(img);
        });

        container.innerHTML = "";
        container.appendChild(frag);
    }

    document.addEventListener('DOMContentLoaded', function () {
        const form      = document.getElementById('cashFilter');
        const qInput    = document.getElementById('q');
        const selType   = document.getElementById('typeSelect');
        const selStatus = document.getElementById('statusSelect');
        const fromEl    = document.getElementById('from');
        const toEl      = document.getElementById('to');

        const today = new Date(); today.setHours(0,0,0,0);

        if (form) {
            form.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' && e.target !== qInput) e.preventDefault();
            });
        }

        if (qInput) {
            qInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    const page = document.getElementById('pageInput');
                    if (page) page.value = '1';
                }
            });
        }

        if (selType)   selType.addEventListener('change', onFilterChange);
        if (selStatus) selStatus.addEventListener('change', onFilterChange);

        function notFuture(input) {
            if (!input.value) return true;
            const d = new Date(input.value);
            if (d > today) {
                showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!', 'error');
                input.value = '';
                return false;
            }
            return true;
        }
        if (fromEl) fromEl.addEventListener('change', function () {
            if (notFuture(this)) onFilterChange();
        });
        if (toEl) toEl.addEventListener('change', function () {
            if (notFuture(this)) onFilterChange();
        });

        document.querySelectorAll('.js-proof-images').forEach(renderProofImages);
    });

    /* ===== Zoom ảnh dùng modal chung ===== */
    document.addEventListener('click', function (e) {
        const img = e.target.closest('.js-zoomable');
        if (!img) return;
        const modalEl = document.getElementById('imgZoomModal');
        const target  = document.getElementById('imgZoomTarget');
        if (!modalEl || !target) {
            window.open(img.src, '_blank');
            return;
        }
        target.src = img.src;
        if (window.bootstrap && bootstrap.Modal) {
            new bootstrap.Modal(modalEl).show();
        } else {
            window.open(img.src, '_blank');
        }
    });
</script>

<c:if test="${not empty sessionScope.flash}">
    <script>
        (function(){
            const msg = "${fn:escapeXml(sessionScope.flash)}";
            const lower = msg.toLowerCase();
            const icon = lower.includes("lỗi")
                ? '<i class="fa fa-times-circle"></i>'
                : '<i class="fa fa-check-circle"></i>';
            if (typeof showToast === 'function') {
                showToast(icon + " " + msg, lower.includes("lỗi") ? "error" : "success");
            } else {
                alert(msg);
            }
        })();
    </script>
    <c:remove var="flash" scope="session"/>
</c:if>
