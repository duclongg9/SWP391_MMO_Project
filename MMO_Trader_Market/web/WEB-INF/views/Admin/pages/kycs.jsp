<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.time.LocalDate" %>
<%
    String today = LocalDate.now().toString();
    request.setAttribute("today", today);
%>
<c:set var="base" value="${pageContext.request.contextPath}" />

<c:set var="pageNow"     value="${requestScope.pg_page}" />
<c:set var="pageSize"    value="${requestScope.pg_size}" />
<c:set var="total"       value="${requestScope.pg_total}" />
<c:set var="pages"       value="${requestScope.pg_pages}" />
<c:set var="isFirst"     value="${requestScope.pg_isFirst}" />
<c:set var="isLast"      value="${requestScope.pg_isLast}" />
<c:set var="singlePage"  value="${requestScope.pg_single}" />

<div class="container-fluid">
    <h4 class="mb-4">
        <i class="bi bi-shield-check me-2"></i>Danh sách KYC cần duyệt
    </h4>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <!-- ========== BỘ LỌC ========== -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="kycFilterForm"
                  class="row g-2 align-items-end"
                  action="<c:url value='/admin/kycs'/>"
                  method="get">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" value="${sort}"/>

                <!-- Tìm kiếm -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Tìm kiếm</label>
                    <div class="input-group">
                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>
                        <input id="q" name="q" type="search" class="form-control"
                               placeholder="Tên người dùng…"
                               value="${fn:escapeXml(q)}" />
                    </div>
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

                <div class="col-12 col-md-2 d-grid">
                    <a class="btn btn-outline-secondary" href="<c:url value='/admin/kycs'/>">Xóa lọc</a>
                </div>
            </form>

            <div id="toastBox"></div>
        </div>
    </div>

    <!-- ========== BẢNG DANH SÁCH ========== -->
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
                        <th>Admin Feedback</th>
                        <th>Ngày gửi</th>
                        <th>Trạng thái</th>
                        <th class="text-center" style="width:140px">Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty kycList}">
                            <c:forEach var="k" items="${kycList}" varStatus="st">

                                <!-- Chuẩn hóa src -->
                                <c:set var="frontSrc"  value="${k.frontImageUrl}" />
                                <c:set var="backSrc"   value="${k.backImageUrl}" />
                                <c:set var="selfieSrc" value="${k.selfieImageUrl}" />

                                <tr>
                                    <td>${(pageNow-1)*pageSize + st.index + 1}</td>
                                    <td>
                                        <div class="fw-semibold">${k.userName}</div>
                                        <div class="text-muted small">${k.userEmail}</div>
                                    </td>

                                    <td class="text-center">
                                        <a href="#" class="zoom-link" data-full="${frontSrc}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                            <img src="${frontSrc}" class="img-thumbnail kyc-thumb" style="width:80px;height:56px;object-fit:cover;">
                                        </a>
                                    </td>
                                    <td class="text-center">
                                        <a href="#" class="zoom-link" data-full="${backSrc}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                            <img src="${backSrc}" class="img-thumbnail kyc-thumb" style="width:80px;height:56px;object-fit:cover;">
                                        </a>
                                    </td>
                                    <td class="text-center">
                                        <a href="#" class="zoom-link" data-full="${selfieSrc}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                            <img src="${selfieSrc}" class="img-thumbnail kyc-thumb" style="width:80px;height:56px;object-fit:cover;">
                                        </a>
                                    </td>

                                    <td>${k.idNumber}</td>
                                    <td>${k.adminFeedback}</td>
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
                                        <button class="btn btn-sm btn-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#kycModal_${k.id}">
                                            <i class="bi bi-eye"></i> Xem chi tiết
                                        </button>
                                    </td>
                                </tr>
                                <!-- ========== MODAL CHI TIẾT ========== -->
                                <div class="modal fade" id="kycModal_${k.id}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog modal-lg modal-dialog-centered">
                                        <div class="modal-content">
                                            <div class="modal-header bg-dark text-white">
                                                <h5 class="modal-title">KYC #${k.id} – ${k.userName}</h5>
                                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                            </div>
                                            <div class="modal-body">
                                                <div class="row g-3">
                                                    <div class="col-md-6">
                                                        <div class="small text-muted mb-1">Email</div>
                                                        <div class="fw-semibold">${k.userEmail}</div>
                                                    </div>
                                                    <div class="col-md-6">
                                                        <div class="small text-muted mb-1">Số giấy tờ</div>
                                                        <div class="fw-semibold">${k.idNumber}</div>
                                                    </div>

                                                    <div class="col-12"><hr/></div>

                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Mặt trước</div>
                                                        <a href="#" class="zoom-link" data-full="${frontSrc}">
                                                            <img src="${frontSrc}" class="img-fluid rounded shadow-sm kyc-thumb">
                                                        </a>
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Mặt sau</div>
                                                        <a href="#" class="zoom-link" data-full="${backSrc}">
                                                            <img src="${backSrc}" class="img-fluid rounded shadow-sm kyc-thumb">
                                                        </a>
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Selfie</div>
                                                        <a href="#" class="zoom-link" data-full="${selfieSrc}">
                                                            <img src="${selfieSrc}" class="img-fluid rounded shadow-sm kyc-thumb">
                                                        </a>
                                                    </div>

                                                    <!-- Form duyệt -->
                                                    <div class="col-12 mt-3">
                                                        <div class="small text-muted mb-1">Ghi chú/Phản hồi quản trị</div>

                                                        <form id="kycForm_${k.id}"
                                                              action="${base}/admin/kycs/status"
                                                              method="post">
                                                            <input type="hidden" name="id" value="${k.id}">
                                                            <!-- action sẽ set bằng JS: approve / reject -->
                                                            <input type="hidden" name="action" id="kycAction_${k.id}" value="">

                                                            <textarea id="kycFeedback_${k.id}"
                                                                      name="feedback"
                                                                      rows="3"
                                                                      class="form-control"
                                                                      placeholder="Ghi chú cho người dùng (bắt buộc khi từ chối)">${k.adminFeedback}</textarea>

                                                            <div id="kycError_${k.id}"
                                                                 class="text-danger small mt-1 d-none">
                                                                ⚠️ Vui lòng nhập lý do từ chối KYC.
                                                            </div>

                                                            <div class="d-flex gap-2 mt-3">
                                                                <c:choose>
                                                                    <c:when test="${k.statusId == 1}">
                                                                        <!-- Accept: submit thẳng -->
                                                                        <button type="submit"
                                                                                class="btn btn-success"
                                                                                onclick="document.getElementById('kycAction_${k.id}').value='approve';">
                                                                            <i class="bi bi-check-circle"></i> Accept
                                                                        </button>

                                                                        <!-- Reject: JS kiểm tra rồi submit -->
                                                                        <button type="button"
                                                                                class="btn btn-danger"
                                                                                onclick="onRejectKyc(${k.id});">
                                                                            <i class="bi bi-x-circle"></i> Reject
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="text-muted align-self-center">
                                                                            Hồ sơ đã xử lý.
                                                                        </span>
                                                                    </c:otherwise>
                                                                </c:choose>

                                                                <button type="button"
                                                                        class="btn btn-secondary ms-auto"
                                                                        data-bs-dismiss="modal">
                                                                    Đóng
                                                                </button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="10" class="text-center text-muted py-4">
                                    Không có hồ sơ KYC.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <nav aria-label="Pagination">
        <ul class="pagination justify-content-center mt-3">

            <!-- Prev -->
            <li class="page-item ${pageNow <= 1 ? 'disabled' : ''}">
                <c:url var="uPrev" value="${usersPath}">
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="role" value="${param.role}" />

                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="page" value="${pageNow-1}" />
                </c:url>
                <a class="page-link" href="${pageNow <= 1 ? '#' : uPrev}" aria-label="Previous">&laquo;</a>
            </li>

            <!-- Page numbers: 1..pages -->
            <c:forEach var="i" begin="1" end="${pages}">
                <c:url var="uI" value="${usersPath}">
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="role" value="${param.role}" />
                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="page" value="${i}" />
                </c:url>
                <li class="page-item ${i == pageNow ? 'active' : ''}">
                    <a class="page-link" href="${uI}">${i}</a>
                </li>
            </c:forEach>

            <!-- Next -->
            <li class="page-item ${pageNow >= pages ? 'disabled' : ''}">
                <c:url var="uNext" value="${usersPath}">
                    <c:param name="q"    value="${param.q}" />
                    <c:param name="role" value="${param.role}" />
                    <c:param name="from" value="${param.from}" />
                    <c:param name="to"   value="${param.to}" />
                    <c:param name="size" value="${pageSize}" />
                    <c:param name="page" value="${pageNow+1}" />
                </c:url>
                <a class="page-link" href="${pageNow >= pages ? '#' : uNext}" aria-label="Next">&raquo;</a>
            </li>

        </ul>
    </nav>

</div>

<!-- Modal zoom ảnh -->
<div class="modal fade" id="imgZoomModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content bg-dark text-center">
            <img id="zoomImg" src="" alt="Preview" class="img-fluid">
        </div>
    </div>
</div>

<style>
    .table td, .table th { vertical-align: middle; }
    .kyc-thumb { cursor: zoom-in; transition: transform 0.2s ease; }
    .kyc-thumb:hover { transform: scale(1.05); }
    #imgZoomModal img { width: 50%; max-width: 50%; border-radius: 8px; margin: 0 auto; }
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
</style>

<script>
    function showToast(msg, type = 'success') {
        const box = document.getElementById('toastBox');
        if (!box) return;
        const toast = document.createElement('div');
        toast.classList.add('mm-toast');
        if (type === 'error') toast.classList.add('error');
        toast.innerHTML = msg;
        box.appendChild(toast);
        setTimeout(() => toast.remove(), 5000);
    }

    // Reject KYC: dùng id -> không phụ thuộc closest()
    function onRejectKyc(id) {
        console.log('onRejectKyc clicked', id);

        const form   = document.getElementById('kycForm_' + id);
        const fb     = document.getElementById('kycFeedback_' + id);
        const errBox = document.getElementById('kycError_' + id);
        const action = document.getElementById('kycAction_' + id);

        if (!form || !fb || !action) {
            console.error('Thiếu phần tử form/feedback/action cho KYC', id);
            return false;
        }

        // clear lỗi cũ
        if (errBox) errBox.classList.add('d-none');
        fb.classList.remove('is-invalid');

        if (!fb.value.trim()) {
            // thiếu feedback
            if (errBox) errBox.classList.remove('d-none');
            fb.classList.add('is-invalid');
            fb.focus();
            console.log('Reject: thiếu feedback');
            return false;
        }

        // confirm
        if (!confirm('Bạn có chắc chắn muốn TỪ CHỐI hồ sơ KYC này không?')) {
            console.log('Reject: user cancel');
            return false;
        }

        action.value = 'reject';
        console.log('Reject: submit form', id);
        form.submit();
        return true;
    }

    // Ẩn lỗi khi gõ lại feedback
    document.addEventListener('input', function (e) {
        const idMatch = e.target.id && e.target.id.match(/^kycFeedback_(\d+)$/);
        if (!idMatch) return;
        const id = idMatch[1];
        const err = document.getElementById('kycError_' + id);
        if (err) err.classList.add('d-none');
        e.target.classList.remove('is-invalid');
    });

    // Filter + ngày + zoom (rút gọn, giữ logic như trước)
    document.addEventListener('DOMContentLoaded', function () {
        const form   = document.getElementById('kycFilterForm');
        const qInput = document.getElementById('q');
        const selStt = document.getElementById('status');
        const fromEl = document.getElementById('from');
        const toEl   = document.getElementById('to');
        const today  = new Date(); today.setHours(0,0,0,0);

        function resetPageToFirst() {
            if (!form) return;
            let p = form.querySelector('input[name="page"]');
            if (!p) {
                p = document.createElement('input');
                p.type = 'hidden';
                p.name = 'page';
                form.appendChild(p);
            }
            p.value = '1';
        }

        if (form) {
            form.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' && e.target !== qInput) e.preventDefault();
            });
        }
        if (qInput) {
            qInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') resetPageToFirst();
            });
        }
        if (selStt) {
            selStt.addEventListener('change', function () {
                resetPageToFirst();
                form.submit();
            });
        }
        if (fromEl) {
            fromEl.addEventListener('change', function () {
                const d = new Date(this.value);
                if (d > today) {
                    showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!', 'error');
                    this.value = '';
                }
            });
        }
        if (toEl) {
            toEl.addEventListener('change', function () {
                const d = new Date(this.value);
                if (d > today) {
                    showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!', 'error');
                    this.value = '';
                } else {
                    resetPageToFirst();
                    form.submit();
                }
            });
        }

        // zoom ảnh
        const zoomModal = document.getElementById('imgZoomModal');
        const zoomImg   = document.getElementById('zoomImg');
        if (zoomModal && zoomImg && window.bootstrap) {
            let lastModal = null;
            document.addEventListener('click', function (e) {
                const link = e.target.closest('.zoom-link');
                if (!link) return;
                e.preventDefault();
                lastModal = e.target.closest('.modal');
                const full = link.getAttribute('data-full') || link.querySelector('img')?.src;
                zoomImg.src = full || '';
                new bootstrap.Modal(zoomModal).show();
            });
            zoomModal.addEventListener('hidden.bs.modal', function () {
                if (lastModal) {
                    new bootstrap.Modal(lastModal).show();
                    lastModal = null;
                }
            });
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
            showToast(icon + " " + msg, lower.includes("lỗi") ? "error" : "success");
        })();
    </script>
    <c:remove var="flash" scope="session"/>
</c:if>
