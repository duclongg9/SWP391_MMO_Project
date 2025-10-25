<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shield-check me-2"></i>Danh sách KYC cần duyệt</h4>

    <c:if test="${not empty flash}">
        <div class="alert alert-success">${flash}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="kycFilter" class="row g-2 align-items-end" action="${base}/admin/kycs" method="get">
                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="text" class="form-control"
                               placeholder="Tên, email, số giấy tờ..."
                               value="${fn:escapeXml(q)}">
                    </div>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                        <input id="from" name="from" type="date" class="form-control"
                               value="${fn:escapeXml(from)}">
                    </div>
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="to" name="to" type="date" class="form-control"
                               value="${fn:escapeXml(to)}">
                    </div>
                </div>

                <!-- Trạng thái -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="status">Trạng thái</label>
                    <select id="status" name="status" class="form-select">
                        <option value="all" ${status == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="1"   ${status == '1'   ? 'selected' : ''}>Pending</option>
                        <option value="2"   ${status == '2'   ? 'selected' : ''}>Approved</option>
                        <option value="3"   ${status == '3'   ? 'selected' : ''}>Rejected</option>
                    </select>
                </div>

                <!-- Sắp xếp -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="sort">Sắp xếp</label>
                    <select id="sort" name="sort" class="form-select">
                        <option value="date_desc"   ${sort == 'date_desc'   ? 'selected' : ''}>Ngày gửi: mới → cũ</option>
                        <option value="date_asc"    ${sort == 'date_asc'    ? 'selected' : ''}>Ngày gửi: cũ → mới</option>
                        <option value="status_asc"  ${sort == 'status_asc'  ? 'selected' : ''}>Trạng thái: A → Z</option>
                        <option value="status_desc" ${sort == 'status_desc' ? 'selected' : ''}>Trạng thái: Z → A</option>
                    </select>
                </div>

                <!-- Nút -->
                <div class="col-12 col-md-1 d-grid">
                    <button class="btn btn-dark" type="submit">
                        <i class="bi bi-funnel"></i> Lọc
                    </button>
                </div>
                <div class="col-12 col-md-1 d-grid">
                    <a class="btn btn-outline-secondary" href="${base}/admin/kycs">Xóa lọc</a>
                </div>
            </form>
        </div>
    </div>
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
                        <th>Ngày gửi</th>

                        <!-- Bấm để toggle status_asc/status_desc -->
                        <th id="thStatus" class="cursor-pointer" style="user-select:none">
                            Trạng thái
                            <c:if test="${sort == 'status_asc'}">
                                <i class="bi bi-caret-up-fill ms-1"></i>
                            </c:if>
                            <c:if test="${sort == 'status_desc'}">
                                <i class="bi bi-caret-down-fill ms-1"></i>
                            </c:if>
                        </th>

                        <th class="text-center" style="width:140px">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty kycList}">
                            <c:forEach var="k" items="${kycList}" varStatus="st">
                                <tr>
                                    <td>${st.index + 1}</td>

                                    <td>
                                        <div class="fw-semibold">${k.userName}</div>
                                        <div class="text-muted small">${k.userEmail}</div>
                                    </td>

                                    <td class="text-center">
                                        <img src="${k.frontImageUrl}" alt="front"
                                             class="img-thumbnail kyc-thumb"
                                             style="width:80px;height:56px;object-fit:cover;"
                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.frontImageUrl}'">
                                    </td>
                                    <td class="text-center">
                                        <img src="${k.backImageUrl}" alt="back"
                                             class="img-thumbnail kyc-thumb"
                                             style="width:80px;height:56px;object-fit:cover;"
                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.backImageUrl}'">
                                    </td>
                                    <td class="text-center">
                                        <img src="${k.selfieImageUrl}" alt="selfie"
                                             class="img-thumbnail kyc-thumb"
                                             style="width:80px;height:56px;object-fit:cover;"
                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.selfieImageUrl}'">
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
                        <c:when test="${k.statusId == 1}">bg-warning text-dark</c:when>
                        <c:when test="${k.statusId == 2}">bg-success</c:when>
                        <c:when test="${k.statusId == 3}">bg-danger</c:when>
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

                                <!-- Modal chi tiết -->
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
                                                        <img src="${k.frontImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="front"
                                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.frontImageUrl}'">
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Mặt sau</div>
                                                        <img src="${k.backImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="back"
                                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.backImageUrl}'">
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Selfie</div>
                                                        <img src="${k.selfieImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="selfie"
                                                             onerror="this.onerror=null;this.src='${base}/assets/img/${k.selfieImageUrl}'">
                                                    </div>

                                                    <div class="col-12 mt-3">
                                                        <div class="small text-muted mb-1">Ghi chú/Phản hồi quản trị</div>
                                                        <form action="${base}/admin/kycs/status" method="post">
                                                            <input type="hidden" name="id" value="${k.id}"/>
                                                            <textarea name="feedback" class="form-control" rows="3"
                                                                      placeholder="Ghi chú cho người dùng (bắt buộc khi từ chối)"
                                                                      <c:if test="${k.statusId != 1}">disabled</c:if>>${k.adminFeedback}</textarea>

                                                            <div class="d-flex gap-2 mt-3">
                                                                    <%-- Chỉ thao tác khi còn Pending --%>
                                                                <c:choose>
                                                                    <c:when test="${k.statusId == 1}">
                                                                        <button class="btn btn-success" name="action" value="approve">
                                                                            <i class="bi bi-check-circle"></i> Accept
                                                                        </button>
                                                                        <button class="btn btn-danger" name="action" value="reject"
                                                                                onclick="return confirm('Từ chối KYC này?');">
                                                                            <i class="bi bi-x-circle"></i> Reject
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="text-muted align-self-center">Hồ sơ đã xử lý.</span>
                                                                    </c:otherwise>
                                                                </c:choose>

                                                                <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">
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
                                <!-- /Modal -->
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="9" class="text-center text-muted py-4">Không có hồ sơ KYC.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal xem ảnh lớn dùng chung -->
<div class="modal fade" id="imgPreviewModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content bg-dark">
            <div class="modal-body p-0">
                <img id="previewImg" class="w-100" alt="preview"
                     style="max-height:90vh;object-fit:contain;">
            </div>
        </div>
    </div>
</div>

<style>
    .table td,.table th{vertical-align:middle}
    .kyc-thumb{cursor:zoom-in}
    .cursor-pointer{cursor:pointer}
</style>

<script>
    // ===== TỰ ẨN THÔNG BÁO =====
    window.addEventListener('DOMContentLoaded', () => {
        // Ẩn alert
        document.querySelectorAll('.alert').forEach(a => {
            setTimeout(() => { a.classList.add('fade'); a.style.opacity = 0; }, 1800);
            setTimeout(() => a.remove(), 2600);
        });

        const form     = document.getElementById('kycFilter');
        const ipQ      = document.getElementById('q');
        const ipStatus = document.getElementById('status');
        const ipSort   = document.getElementById('sort');

        // Debounce search theo từ khóa
        let t;
        ipQ.addEventListener('input', function() {
            clearTimeout(t);
            t = setTimeout(() => form.submit(), 300);
        });

        // Đổi trạng thái -> tự sắp xếp theo trạng thái & submit
        ipStatus.addEventListener('change', () => {
            const v = (ipStatus.value || '').toLowerCase();
            if (v === 'all') {
                ipSort.value = 'date_desc';
            } else {
                // nếu đang không phải sort theo status thì set mặc định A→Z
                if (!/^status_/.test(ipSort.value)) ipSort.value = 'status_asc';
            }
            form.submit();
        });

        // Bấm header "Trạng thái" để toggle sort
        document.getElementById('thStatus').addEventListener('click', () => {
            const cur = (ipSort.value || '').toLowerCase();
            ipSort.value = (cur === 'status_asc') ? 'status_desc' : 'status_asc';
            form.submit();
        });

        // Preview ảnh chung
        document.addEventListener('click', function (e) {
            const img = e.target.closest('.kyc-thumb');
            if (!img) return;
            const src = img.getAttribute('src');
            const modalEl  = document.getElementById('imgPreviewModal');
            const modalImg = document.getElementById('previewImg');
            modalImg.src   = src;
            const bsModal  = bootstrap.Modal.getOrCreateInstance(modalEl);
            bsModal.show();
        });
    });

    const base = '<c:out value="${pageContext.request.contextPath}"/>';
    const form = document.getElementById('kycFilter');
    const ipQ  = document.getElementById('q');
    const ipStatus = document.getElementById('status');
    const ipSort   = document.getElementById('sort');

    // ===== AUTO SEARCH (debounce) =====
    let t;
    ipQ.addEventListener('input', function() {
        clearTimeout(t);
        t = setTimeout(() => form.submit(), 300); // gõ dừng 300ms sẽ lọc
    });

    // ===== đổi trạng thái -> lọc ngay =====
    ipStatus.addEventListener('change', () => form.submit());

    // ===== bấm header "Trạng thái" để toggle sort =====
    document.getElementById('thStatus').addEventListener('click', () => {
        const cur = (ipSort.value || '').toLowerCase();
        ipSort.value = (cur === 'status_asc') ? 'status_desc' : 'status_asc';
        form.submit();
    });

    // ===== Preview ảnh chung (giữ nguyên) =====
    document.addEventListener('click', function (e) {
        const img = e.target.closest('.kyc-thumb');
        if (!img) return;
        const src = img.getAttribute('src');
        const modalEl  = document.getElementById('imgPreviewModal');
        const modalImg = document.getElementById('previewImg');
        modalImg.src   = src;
        const bsModal  = bootstrap.Modal.getOrCreateInstance(modalEl);
        bsModal.show();
    });
    ['from','to'].forEach(id=>{
        const el = document.getElementById(id);
        el.addEventListener('focus', ()=> el.showPicker && el.showPicker());
    });
</script>

