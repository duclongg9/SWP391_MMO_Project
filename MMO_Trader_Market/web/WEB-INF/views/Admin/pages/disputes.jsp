<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (disputes != null ? fn:length(disputes) : 0)}" />
<c:set var="pages"    value="${pg_pages != null ? pg_pages : ((total + pageSize - 1) / pageSize)}" />

<div class="container-fluid">
    <h4 class="mb-4">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>Quản lý khiếu nại
    </h4>

    <c:if test="${not empty error}">
        <div class="alert alert-danger shadow-sm mb-3">
                ${fn:escapeXml(error)}
        </div>
    </c:if>

    <!-- FILTER -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="dpFilter" class="row g-2 align-items-end"
                  action="${base}/admin/disputes" method="get">

                <input type="hidden" name="page" id="pageInput" value="${pageNow}">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" id="sort"
                       value="${empty param.sort ? 'date_desc' : param.sort}"/>

                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="search" class="form-control"
                               placeholder="Mã đơn, email người báo cáo..."
                               value="${fn:escapeXml(param.q)}">
                    </div>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="status">Trạng thái</label>
                    <select id="status" name="status" class="form-select">
                        <c:set var="st" value="${empty param.status ? 'all' : param.status}" />
                        <option value="all"                   ${st=='all'?'selected':''}>Tất cả</option>
                        <option value="Open"                  ${st=='Open'?'selected':''}>Open</option>
                        <option value="InReview"              ${st=='InReview'?'selected':''}>In review</option>
                        <option value="ResolvedWithRefund"    ${st=='ResolvedWithRefund'?'selected':''}>Resolved + Refund</option>
                        <option value="ResolvedWithoutRefund" ${st=='ResolvedWithoutRefund'?'selected':''}>Resolved no Refund</option>
                        <option value="Closed"                ${st=='Closed'?'selected':''}>Closed</option>
                        <option value="Cancelled"             ${st=='Cancelled'?'selected':''}>Cancelled</option>
                    </select>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="issueType">Loại vấn đề</label>
                    <select id="issueType" name="issueType" class="form-select">
                        <c:set var="it" value="${empty param.issueType ? 'all' : param.issueType}" />
                        <option value="all"                    ${it=='all'?'selected':''}>Tất cả</option>
                        <option value="ACCOUNT_NOT_WORKING"    ${it=='ACCOUNT_NOT_WORKING'?'selected':''}>Account không hoạt động</option>
                        <option value="ACCOUNT_DUPLICATED"     ${it=='ACCOUNT_DUPLICATED'?'selected':''}>Account trùng</option>
                        <option value="ACCOUNT_EXPIRED"        ${it=='ACCOUNT_EXPIRED'?'selected':''}>Account hết hạn</option>
                        <option value="ACCOUNT_MISSING"        ${it=='ACCOUNT_MISSING'?'selected':''}>Không nhận được account</option>
                        <option value="OTHER"                  ${it=='OTHER'?'selected':''}>Khác</option>
                    </select>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <input id="from" name="from" type="date" class="form-control"
                           value="${fn:escapeXml(param.from)}">
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <input id="to" name="to" type="date" class="form-control"
                           value="${fn:escapeXml(param.to)}">
                </div>

                <div class="col-12 col-md-1 d-grid">
                    <a href="${base}/admin/disputes" class="btn btn-outline-secondary">
                        Xóa lọc
                    </a>
                </div>
            </form>

            <div id="toastBox"></div>
        </div>
    </div>

    <!-- LIST -->
    <div class="card shadow-sm">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Mã đơn</th>
                        <th>Người báo cáo</th>
                        <th>Loại vấn đề</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th class="text-center">Chi tiết</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty disputes}">
                            <c:forEach var="d" items="${disputes}" varStatus="st">
                                <tr>
                                    <td>${(pageNow-1)*pageSize + st.index + 1}</td>

                                    <td>${fn:escapeXml(d.orderReferenceCode)}</td>

                                    <td>
                                            ${fn:escapeXml(d.reporterName)}<br>
                                        <small class="text-muted">
                                                ${fn:escapeXml(d.reporterEmail)}
                                        </small>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty d.customIssueTitle}">
                                                ${fn:escapeXml(d.customIssueTitle)}
                                            </c:when>
                                            <c:otherwise>
                                                ${fn:escapeXml(d.issueType)}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <span class="badge
                                            <c:choose>
                                                <c:when test="${d.status == 'Open'}"> bg-warning text-dark</c:when>
                                                <c:when test="${d.status == 'InReview'}"> bg-info text-dark</c:when>
                                                <c:when test="${d.status == 'ResolvedWithRefund' || d.status == 'ResolvedWithoutRefund'}"> bg-success</c:when>
                                                <c:when test="${d.status == 'Closed'}"> bg-secondary</c:when>
                                                <c:when test="${d.status == 'Cancelled'}"> bg-dark</c:when>
                                                <c:otherwise> bg-light text-dark</c:otherwise>
                                            </c:choose>">
                                                ${fn:escapeXml(d.status)}
                                        </span>
                                    </td>

                                    <td>
                                        <fmt:formatDate value="${d.createdAt}" pattern="dd-MM-yyyy HH:mm"/>
                                    </td>

                                    <td class="text-center">
                                        <button class="btn btn-sm btn-outline-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#dpModal_${d.id}">
                                            Xem
                                        </button>
                                    </td>
                                </tr>

                                <!-- MODAL -->
                                <div class="modal fade" id="dpModal_${d.id}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog modal-lg modal-dialog-centered">
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h5 class="modal-title">
                                                    Khiếu nại #${d.id} - Đơn: ${fn:escapeXml(d.orderReferenceCode)}
                                                </h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                            </div>

                                            <div class="modal-body">
                                                <div class="row g-3 mb-3">
                                                    <div class="col-md-6">
                                                        <div class="small text-muted mb-1">Người báo cáo</div>
                                                        <div class="fw-semibold">${fn:escapeXml(d.reporterName)}</div>
                                                        <div class="text-muted small">${fn:escapeXml(d.reporterEmail)}</div>
                                                    </div>

                                                    <div class="col-md-3">
                                                        <div class="small text-muted mb-1">Thời gian tạo</div>
                                                        <div class="fw-semibold">
                                                            <fmt:formatDate value="${d.createdAt}" pattern="dd-MM-yyyy HH:mm"/>
                                                        </div>
                                                    </div>

                                                    <div class="col-md-3">
                                                        <div class="small text-muted mb-1">Trạng thái hiện tại</div>
                                                        <span class="badge
                                                            <c:choose>
                                                                <c:when test="${d.status == 'Open'}"> bg-warning text-dark</c:when>
                                                                <c:when test="${d.status == 'InReview'}"> bg-info text-dark</c:when>
                                                                <c:when test="${d.status == 'ResolvedWithRefund' || d.status == 'ResolvedWithoutRefund'}"> bg-success</c:when>
                                                                <c:when test="${d.status == 'Closed'}"> bg-secondary</c:when>
                                                                <c:when test="${d.status == 'Cancelled'}"> bg-dark</c:when>
                                                                <c:otherwise> bg-light text-dark</c:otherwise>
                                                            </c:choose>">
                                                                ${fn:escapeXml(d.status)}
                                                        </span>
                                                    </div>
                                                </div>

                                                <div class="mb-3">
                                                    <div class="small text-muted mb-1">Loại vấn đề</div>
                                                    <div class="fw-semibold">
                                                        <c:choose>
                                                            <c:when test="${not empty d.customIssueTitle}">
                                                                ${fn:escapeXml(d.customIssueTitle)}
                                                            </c:when>
                                                            <c:otherwise>
                                                                ${fn:escapeXml(d.issueType)}
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>

                                                <div class="mb-3">
                                                    <div class="small text-muted mb-1">Lý do chi tiết</div>
                                                    <div class="border rounded p-3 bg-light"
                                                         style="min-height:80px; white-space:pre-wrap;">
                                                        <c:out value="${d.reason}"/>
                                                    </div>
                                                </div>

                                                <c:if test="${not empty d.attachments}">
                                                    <div class="mb-3">
                                                        <div class="small text-muted mb-1">Ảnh đính kèm</div>
                                                        <div class="d-flex flex-wrap gap-2">
                                                            <c:forEach var="att" items="${d.attachments}">
                                                                <c:set var="attWebPath" value="${att.webPath}" />
                                                                <c:if test="${not empty attWebPath}">
                                                                    <c:url var="attUrl" value="${attWebPath}" />
                                                                    <img src="${fn:escapeXml(attUrl)}"
                                                                         alt="Attachment"
                                                                         class="img-thumbnail js-zoomable"
                                                                         style="width:90px;height:90px;object-fit:cover;cursor:zoom-in;">
                                                                </c:if>
                                                            </c:forEach>
                                                        </div>
                                                    </div>
                                                </c:if>

                                                <c:if test="${not empty d.orderSnapshotJson}">
                                                    <div class="mb-3">
                                                        <div class="small text-muted mb-1">Snapshot đơn hàng</div>
                                                        <textarea class="form-control form-control-sm"
                                                                  rows="3" readonly>${fn:escapeXml(d.orderSnapshotJson)}</textarea>
                                                    </div>
                                                </c:if>

                                                <!-- FORM XỬ LÝ -->
                                                <form id="dpForm_${d.id}"
                                                      action="${base}/admin/disputes/status"
                                                      method="post"
                                                      class="mt-3">
                                                    <input type="hidden" name="id" value="${d.id}">
                                                    <input type="hidden" name="action" id="dpAction_${d.id}" value="">

                                                    <label for="dpNote_${d.id}" class="small text-muted mb-1">
                                                        Ghi chú xử lý (resolution_note)
                                                    </label>
                                                    <textarea id="dpNote_${d.id}"
                                                              name="resolution_note"
                                                              class="form-control"
                                                              rows="3"
                                                              placeholder="Mô tả cách xử lý, lý do reject/refund...">${fn:escapeXml(d.resolutionNote)}</textarea>

                                                    <div id="dpNoteErr_${d.id}"
                                                         class="text-danger small mt-1 d-none">
                                                        ⚠️ Vui lòng nhập ghi chú khi chọn Reject.
                                                    </div>

                                                    <div class="d-flex align-items-center gap-2 mt-3">
                                                        <button type="button"
                                                                class="btn btn-outline-info"
                                                                onclick="onDisputeAction(${d.id}, 'inreview');">
                                                            In review
                                                        </button>
                                                        <button type="button"
                                                                class="btn btn-success"
                                                                onclick="onDisputeAction(${d.id}, 'accept');">
                                                            Accept
                                                        </button>
                                                        <button type="button"
                                                                class="btn btn-danger"
                                                                onclick="onDisputeAction(${d.id}, 'reject');">
                                                            Reject
                                                        </button>
                                                        <button type="button"
                                                                class="btn btn-outline-secondary ms-auto"
                                                                data-bs-dismiss="modal">
                                                            Đóng
                                                        </button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <!-- /MODAL -->
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">
                                    Không có khiếu nại nào.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- PAGINATION -->
    <c:url var="dpPath" value="/admin/disputes"/>
    <nav aria-label="Pagination">
        <ul class="pagination justify-content-center mt-3">
            <li class="page-item ${pageNow <= 1 ? 'disabled' : ''}">
                <c:url var="uPrev" value="${dpPath}">
                    <c:param name="page" value="${pageNow-1}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="status" value="${param.status}" />
                    <c:param name="issueType" value="${param.issueType}" />
                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="sort" value="${param.sort}" />
                </c:url>
                <a class="page-link" href="${pageNow <= 1 ? '#' : uPrev}">&laquo;</a>
            </li>

            <c:forEach var="i" begin="1" end="${pages}">
                <c:url var="uI" value="${dpPath}">
                    <c:param name="page" value="${i}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="status" value="${param.status}" />
                    <c:param name="issueType" value="${param.issueType}" />
                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="sort" value="${param.sort}" />
                </c:url>
                <li class="page-item ${i == pageNow ? 'active' : ''}">
                    <a class="page-link" href="${uI}">${i}</a>
                </li>
            </c:forEach>

            <li class="page-item ${pageNow >= pages ? 'disabled' : ''}">
                <c:url var="uNext" value="${dpPath}">
                    <c:param name="page" value="${pageNow+1}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="status" value="${param.status}" />
                    <c:param name="issueType" value="${param.issueType}" />
                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="sort" value="${param.sort}" />
                </c:url>
                <a class="page-link" href="${pageNow >= pages ? '#' : uNext}">&raquo;</a>
            </li>
        </ul>
    </nav>
</div>

<style>
    #toastBox{
        position: fixed;
        bottom: 30px;
        right: 30px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        z-index: 9999;
    }
    .mm-toast{
        position: relative;
        width: 380px;
        min-height: 60px;
        background: #fff;
        margin-top: 10px;
        padding: 14px 16px;
        box-shadow: 0 4px 16px rgba(0,0,0,.18);
        display: flex;
        align-items: center;
        font-weight: 500;
        border-left: 4px solid #28a745;
        transform: translateX(100%);
        animation: toast-in .35s ease-out forwards;
    }
    .mm-toast.error{
        color:#dc3545;
        border-left-color:#dc3545;
    }
    .mm-toast::after{
        content:'';
        position:absolute;
        left:0;
        bottom:0;
        width:100%;
        height:3px;
        background:#28a745;
        animation: toast-bar 4s linear forwards;
    }
    .mm-toast.error::after{
        background:#dc3545;
    }
    @keyframes toast-in{ to{ transform:translateX(0);} }
    @keyframes toast-bar{ to{ width:0;} }

    .modal-header{
        align-items:center;
    }
    .modal-header .modal-title{
        font-weight:600;
        font-size:18px;
        margin-right:auto;
    }
</style>

<script>
    function showToast(msg, type){
        const box = document.getElementById('toastBox');
        if(!box){ alert(msg); return; }
        const t = document.createElement('div');
        t.className = 'mm-toast' + (type === 'error' ? ' error' : '');
        t.innerHTML = msg;
        box.appendChild(t);
        setTimeout(()=> t.remove(), 4000);
    }

    function onDisputeAction(id, action){
        const form   = document.getElementById('dpForm_' + id);
        const note   = document.getElementById('dpNote_' + id);
        const errBox = document.getElementById('dpNoteErr_' + id);
        const hidden = document.getElementById('dpAction_' + id);
        if(!form || !hidden) return false;

        if(errBox) errBox.classList.add('d-none');
        if(note) note.classList.remove('is-invalid');

        if(action === 'reject'){
            if(!note || !note.value.trim()){
                if(errBox) errBox.classList.remove('d-none');
                if(note){
                    note.classList.add('is-invalid');
                    note.focus();
                }
                return false;
            }
            if(!confirm('Bạn chắc chắn muốn REJECT khiếu nại này?')) return false;
        }else if(action === 'accept'){
            if(!confirm('Xác nhận ACCEPT / giải quyết khiếu nại này?')) return false;
        }else if(action === 'inreview'){
            if(!confirm('Đánh dấu khiếu nại này là IN REVIEW?')) return false;
        }else{
            return false;
        }

        hidden.value = action;
        form.submit();
        return true;
    }

    document.addEventListener('input', function(e){
        const m = e.target.id && e.target.id.match(/^dpNote_(\d+)$/);
        if(!m) return;
        const id = m[1];
        const err = document.getElementById('dpNoteErr_' + id);
        if(err) err.classList.add('d-none');
        e.target.classList.remove('is-invalid');
    });

    (function(){
        const form      = document.getElementById('dpFilter');
        if(!form) return;
        const pageInput = document.getElementById('pageInput');
        const status    = document.getElementById('status');
        const issueType = document.getElementById('issueType');
        const q         = document.getElementById('q');

        function resetPage(){ if(pageInput) pageInput.value = '1'; }

        if(status){
            status.addEventListener('change', function(){
                resetPage();
                form.submit();
            });
        }
        if(issueType){
            issueType.addEventListener('change', function(){
                resetPage();
                form.submit();
            });
        }

        form.addEventListener('keydown', function(e){
            if(e.key === 'Enter' && e.target !== q){
                e.preventDefault();
            }
        });
    })();
</script>

<c:if test="${not empty sessionScope.flash}">
    <script>
        (function(){
            const msg = "${fn:escapeXml(sessionScope.flash)}";
            const lower = msg.toLowerCase();
            const isErr = lower.includes("lỗi") || lower.includes("error");
            const icon = isErr
                ? '<i class="fa fa-times-circle me-2"></i>'
                : '<i class="fa fa-check-circle me-2"></i>';
            showToast(icon + msg, isErr ? 'error' : 'success');
        })();
    </script>
    <c:remove var="flash" scope="session"/>
</c:if>
