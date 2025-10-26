<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (txList != null ? fn:length(txList) : 0)}" />
<c:set var="pages"    value="${(total + pageSize - 1) / pageSize}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-cash-coin me-2"></i>Nạp / Rút</h4>

    <div class="card shadow-sm">
        <div class="card-body">

            <!-- FILTER -->
            <form id="cashFilter"
                  action="${pageContext.request.contextPath}/admin/cashs"
                  method="get"
                  class="row g-2 align-items-end mb-3">

                <!-- hidden để giữ page/size khi phân trang & reset về 1 khi đổi filter -->
                <input type="hidden" name="page" id="pageInput" value="${pageNow}">
                <input type="hidden" name="size" value="${pageSize}">

                <div class="col-sm-2">
                    <label class="form-label mb-1">Loại</label>
                    <select class="form-select" name="type" id="typeSelect">
                        <option value="all"        ${f_type=='all'?'selected':''}>Tất cả</option>
                        <option value="Deposit"    ${f_type=='Deposit'?'selected':''}>Nạp tiền</option>
                        <option value="Withdrawal" ${f_type=='Withdrawal'?'selected':''}>Rút tiền</option>
                    </select>
                </div>

                <div class="col-sm-3">
                    <label class="form-label mb-1">Tên người dùng</label>
                    <div class="input-group">
                        <input type="text" class="form-control" name="q" value="${f_q}" placeholder="Nhập tên…">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-search"></i></button>
                    </div>
                </div>

                <div class="col-sm-2">
                    <label class="form-label mb-1">Trạng thái</label>
                    <select class="form-select" name="status" id="statusSelect">
                        <option value="all"       ${f_status=='all'?'selected':''}>Tất cả</option>
                        <option value="Pending"   ${f_status=='Pending'?'selected':''}>Pending</option>
                        <option value="Completed" ${f_status=='Completed'?'selected':''}>Completed</option>
                        <option value="Rejected"  ${f_status=='Rejected'?'selected':''}>Rejected</option>
                    </select>
                </div>

                <div class="col-sm-2">
                    <label class="form-label mb-1">Sắp xếp theo ngày</label>
                    <select class="form-select" name="order" id="orderSelect">
                        <option value="newest" ${f_order=='newest'?'selected':''}>Mới nhất → cũ</option>
                        <option value="oldest" ${f_order=='oldest'?'selected':''}>Cũ nhất → mới</option>
                    </select>
                </div>

                <div class="col-12 col-md-3 d-flex gap-2">
                    <button class="btn btn-dark flex-fill" type="submit">
                        <i class="bi bi-funnel"></i> Lọc / Tìm
                    </button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/cashs">Xóa lọc</a>
                </div>
            </form>

            <!-- TABLE -->
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Loại</th>
                        <th>User</th>
                        <th>Số tiền</th>
                        <th>Trạng thái</th>
                        <th>Tạo lúc</th>
                        <th>Xử lý lúc</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach var="t" items="${txList}" varStatus="st">
                        <tr>
                            <td>${(pageNow-1)*pageSize + st.index + 1}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.type eq 'Deposit'}"><span class="badge bg-primary">Nạp tiền</span></c:when>
                                    <c:otherwise><span class="badge bg-warning text-dark">Rút tiền</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="fw-semibold">${fn:escapeXml(t.userName)}</td>
                            <td><fmt:formatNumber value="${t.amount}" type="number"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.status eq 'Completed'}"><span class="badge bg-success">Completed</span></c:when>
                                    <c:when test="${t.status eq 'Pending'}"><span class="badge bg-secondary">Pending</span></c:when>
                                    <c:otherwise><span class="badge bg-danger">${fn:escapeXml(t.status)}</span></c:otherwise>
                                </c:choose>
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
                            <td class="text-center">
                                <button type="button" class="btn btn-sm btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#txModal_${t.type}_${t.id}">
                                    <i class="bi bi-eye"></i> Xem chi tiết
                                </button>
                            </td>
                        </tr>

                        <!-- ===== Modal cho từng giao dịch ===== -->
                        <div class="modal fade" id="txModal_${t.type}_${t.id}" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-dark text-white">
                                        <h5 class="modal-title">
                                            Chi tiết <c:choose><c:when test="${t.type eq 'Deposit'}">nạp tiền</c:when><c:otherwise>rút tiền</c:otherwise></c:choose> #${t.id}
                                        </h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                    </div>

                                    <div class="modal-body">
                                        <!-- info chung -->
                                        <div class="row g-3 mb-3">
                                            <div class="col-md-4">
                                                <div class="text-muted small">Loại</div>
                                                <div class="fw-semibold"><c:choose><c:when test="${t.type eq 'Deposit'}">Nạp tiền</c:when><c:otherwise>Rút tiền</c:otherwise></c:choose></div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">User</div>
                                                <div class="fw-semibold">${fn:escapeXml(t.userName)} <span class="text-muted">(#${t.userId})</span></div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">Số tiền</div>
                                                <div class="fw-semibold"><fmt:formatNumber value="${t.amount}" type="number"/></div>
                                            </div>

                                            <div class="col-md-4">
                                                <div class="text-muted small">Trạng thái</div>
                                                <span class="badge
                          <c:choose>
                            <c:when test="${t.status eq 'Completed'}">bg-success</c:when>
                            <c:when test="${t.status eq 'Pending'}">bg-warning text-dark</c:when>
                            <c:otherwise>bg-danger</c:otherwise>
                          </c:choose>">${t.status}</span>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">Tạo lúc</div>
                                                <div class="fw-semibold"><fmt:formatDate value="${t.createdAt}" pattern="dd-MM-yyyy HH:mm"/></div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="text-muted small">Xử lý lúc</div>
                                                <div class="fw-semibold">
                                                    <c:choose>
                                                        <c:when test="${t.processedAt != null}"><fmt:formatDate value="${t.processedAt}" pattern="dd-MM-yyyy HH:mm"/></c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- phần riêng theo loại -->
                                        <c:if test="${t.type eq 'Withdrawal'}">
                                            <div class="mb-3">
                                                <div class="text-muted small mb-1">Bank Account Info</div>
                                                <div class="border rounded p-3 bg-light">
                                                    <dl class="row mb-0 js-bankinfo" data-json='${fn:escapeXml(t.bankAccountInfo)}'></dl>
                                                </div>
                                            </div>

                                            <div class="mb-3">
                                                <div class="text-muted small mb-1">Admin Proof URL</div>
                                                <div class="fw-semibold">
                                                    <c:choose>
                                                        <c:when test="${empty t.adminProofUrl}">—</c:when>
                                                        <c:otherwise><a href="${fn:escapeXml(t.adminProofUrl)}" target="_blank" rel="noopener">${fn:escapeXml(t.adminProofUrl)}</a></c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>

                                            <c:choose>
                                                <c:when test="${t.status eq 'Pending'}">
                                                    <form action="${pageContext.request.contextPath}/admin/cashs/withdraw/status" method="post" class="d-flex flex-column gap-2">
                                                        <input type="hidden" name="id" value="${t.id}"/>
                                                        <div>
                                                            <label class="form-label mb-1">Admin note (lý do)</label>
                                                            <textarea name="note" class="form-control" rows="3" placeholder="Nhập lý do xử lý…"></textarea>
                                                        </div>
                                                        <div>
                                                            <label class="form-label mb-1">Admin proof URL (tuỳ chọn)</label>
                                                            <input type="url" name="proof_url" class="form-control" placeholder="https://… (ảnh hoá đơn/biên nhận)">
                                                        </div>
                                                        <div class="mt-2 d-flex gap-2">
                                                            <button class="btn btn-success" name="action" value="accept"><i class="bi bi-check-circle"></i> Accept</button>
                                                            <button class="btn btn-danger"  name="action" value="reject" onclick="return confirm('Từ chối yêu cầu rút tiền này?');"><i class="bi bi-x-circle"></i> Reject</button>
                                                            <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">Đóng</button>
                                                        </div>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-flex justify-content-end"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button></div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>

                                        <c:if test="${t.type eq 'Deposit'}">
                                            <c:choose>
                                                <c:when test="${t.status eq 'Pending'}">
                                                    <form action="${pageContext.request.contextPath}/admin/cashs/deposit/status" method="post" class="d-flex flex-column gap-2">
                                                        <input type="hidden" name="id" value="${t.id}"/>
                                                        <div>
                                                            <label class="form-label mb-1">Admin note (lý do)</label>
                                                            <textarea name="note" class="form-control" rows="3" placeholder="Nhập lý do xử lý…"></textarea>
                                                        </div>
                                                        <div class="mt-2 d-flex gap-2">
                                                            <button class="btn btn-success" name="action" value="accept"><i class="bi bi-check-circle"></i> Accept</button>
                                                            <button class="btn btn-danger"  name="action" value="reject" onclick="return confirm('Từ chối nạp tiền này?');"><i class="bi bi-x-circle"></i> Reject</button>
                                                            <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">Đóng</button>
                                                        </div>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-flex justify-content-end"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button></div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- ===== /Modal ===== -->
                    </c:forEach>

                    <c:if test="${empty txList}">
                        <tr><td colspan="8" class="text-center text-muted py-4">Không có giao dịch</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <!-- PAGINATION -->
            <c:url var="cashsPath" value="/admin/cashs"/>

            <c:if test="${pages > 1}">
                <nav aria-label="Pagination">
                    <ul class="pagination justify-content-center mt-3">

                        <!-- Prev -->
                        <li class="page-item ${pageNow<=1?'disabled':''}">
                            <c:url var="uPrev" value="${cashsPath}">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="order"  value="${f_order}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="${pageNow-1}" />
                            </c:url>
                            <a class="page-link" href="${uPrev}" aria-label="Previous">&laquo;</a>
                        </li>

                        <!-- window page -->
                        <c:set var="start" value="${pageNow-2 < 1 ? 1 : pageNow-2}" />
                        <c:set var="end"   value="${pageNow+2 > pages ? pages : pageNow+2}" />

                        <c:if test="${start > 1}">
                            <c:url var="u1" value="${cashsPath}">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="order"  value="${f_order}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                            </c:url>
                            <li class="page-item"><a class="page-link" href="${u1}">1</a></li>
                            <li class="page-item disabled"><span class="page-link">…</span></li>
                        </c:if>

                        <c:forEach var="i" begin="${start}" end="${end}">
                            <c:url var="ui" value="${cashsPath}">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="order"  value="${f_order}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="${i}" />
                            </c:url>
                            <li class="page-item ${i==pageNow?'active':''}">
                                <a class="page-link" href="${ui}">${i}</a>
                            </li>
                        </c:forEach>

                        <c:if test="${end < pages}">
                            <li class="page-item disabled"><span class="page-link">…</span></li>
                            <c:url var="uLast" value="${cashsPath}">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="order"  value="${f_order}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="${pages}" />
                            </c:url>
                            <li class="page-item"><a class="page-link" href="${uLast}">${pages}</a></li>
                        </c:if>

                        <!-- Next -->
                        <li class="page-item ${pageNow>=pages?'disabled':''}">
                            <c:url var="uNext" value="${cashsPath}">
                                <c:param name="type"   value="${f_type}" />
                                <c:param name="q"      value="${f_q}" />
                                <c:param name="status" value="${f_status}" />
                                <c:param name="order"  value="${f_order}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="${pageNow+1}" />
                            </c:url>
                            <a class="page-link" href="${uNext}" aria-label="Next">&raquo;</a>
                        </li>

                    </ul>
                </nav>
            </c:if>

        </div>
    </div>
</div>

<!-- JS: render bank info + auto-submit filter -->
<script>
    (function () {
        function htmlUnescape(s) {
            return s.replace(/&quot;|&#34;/g, '"')
                .replace(/&apos;|&#39;/g, "'")
                .replace(/&lt;/g, '<')
                .replace(/&gt;/g, '>')
                .replace(/&amp;/g, '&');
        }
        function renderBankInfo(el) {
            if (!el) return;
            var rawAttr = el.getAttribute('data-json') || '';
            var raw = htmlUnescape(rawAttr).trim();
            if (!raw) {
                el.innerHTML = '<dt class="col-sm-4">Bank</dt><dd class="col-sm-8 text-muted">—</dd>';
                return;
            }
            try {
                var obj = JSON.parse(raw), html = '';
                Object.keys(obj).forEach(function (k) {
                    var key = String(k).replace(/_/g, ' ');
                    var val = (obj[k] == null ? '' : obj[k]);
                    html += '<dt class="col-sm-4 text-capitalize">' + key + '</dt>' +
                        '<dd class="col-sm-8 fw-semibold">' + val + '</dd>';
                });
                el.innerHTML = html;
            } catch (e) {
                el.innerHTML =
                    '<dt class="col-sm-4">Raw</dt>' +
                    '<dd class="col-sm-8"><code class="text-break">' + rawAttr + '</code></dd>';
            }
        }
        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('.js-bankinfo').forEach(renderBankInfo);
        });
        document.addEventListener('shown.bs.modal', function (ev) {
            ev.target.querySelectorAll('.js-bankinfo').forEach(renderBankInfo);
        });
    })();

    // Auto-submit + reset page về 1 khi đổi filter
    (function () {
        const form = document.getElementById('cashFilter');
        const pageInput = document.getElementById('pageInput');
        ['typeSelect', 'statusSelect', 'orderSelect'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('change', () => { pageInput.value = 1; form.submit(); });
        });
        const q = form.querySelector('input[name="q"]');
        if (q) q.addEventListener('keydown', e => { if (e.key === 'Enter') { pageInput.value = 1; form.submit(); }});
    })();
</script>
