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
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="kycFilterForm" class="row g-2 align-items-end" action="<c:url value='/admin/kycs'/>" method="get">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" value="${sort}"/>

                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Tìm kiếm</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="search" class="form-control"
                               placeholder="Tên người dùng…" value="${fn:escapeXml(q)}" />
                    </div>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <input id="from" name="from" type="date" lang="vi" class="form-control" placeholder="DD-MM-YYYY" value="${fn:escapeXml(from)}">
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <input id="to" name="to" type="date" lang="vi" class="form-control" placeholder="DD-MM-YYYY" value="${fn:escapeXml(to)}">
                </div>

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
                                        <button class="btn btn-sm btn-primary" data-bs-toggle="modal" data-bs-target="#kycModal_${k.id}">
                                            <i class="bi bi-eye"></i> Xem chi tiết
                                        </button>
                                    </td>
                                </tr>

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

                                                    <!-- Ảnh: chỉ 1 modal zoom dùng chung, mỗi ảnh có data-full -->
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Mặt trước</div>
                                                        <a href="#" class="zoom-link" data-full="${k.frontImageUrl}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                                            <img src="${k.frontImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="front"
                                                                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}${k.frontImageUrl}'">
                                                        </a>
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Mặt sau</div>
                                                        <a href="#" class="zoom-link" data-full="${k.backImageUrl}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                                            <img src="${k.backImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="back"
                                                                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}${k.backImageUrl}'">
                                                        </a>
                                                    </div>
                                                    <div class="col-md-4 text-center">
                                                        <div class="small text-muted mb-2">Selfie</div>
                                                        <a href="#" class="zoom-link" data-full="${k.selfieImageUrl}" data-bs-toggle="modal" data-bs-target="#imgZoomModal">
                                                            <img src="${k.selfieImageUrl}" class="img-fluid rounded shadow-sm kyc-thumb" alt="selfie"
                                                                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}${k.selfieImageUrl}'">
                                                        </a>
                                                    </div>

                                                    <div class="col-12 mt-3">
                                                        <div class="small text-muted mb-1">Ghi chú/Phản hồi quản trị</div>
                                                        <form action="${base}/admin/kycs/status" method="post" onsubmit="return handleApproveSubmit(event, ${k.id});">
                                                            <input type="hidden" name="id" value="${k.id}"/>
                                                            <textarea name="feedback" class="form-control" rows="3"
                                                                      placeholder="Ghi chú cho người dùng (bắt buộc khi từ chối)"
                                                                      <c:if test="${k.statusId != 1}">disabled</c:if>>${k.adminFeedback}</textarea>

                                                            <div class="d-flex gap-2 mt-3">
                                                                <c:choose>
                                                                    <c:when test="${k.statusId == 1}">
                                                                        <!-- Nút Accept -->
                                                                        <button type="submit" class="btn btn-success" name="action" value="approve">
                                                                            <i class="bi bi-check-circle"></i> Accept
                                                                        </button>
                                                                        <!-- Nút Reject -->
                                                                        <button class="btn btn-danger" name="action" value="reject"
                                                                                onclick="return confirm('Từ chối KYC này?');">
                                                                            <i class="bi bi-x-circle"></i> Reject
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="text-muted align-self-center">Hồ sơ đã xử lý.</span>
                                                                    </c:otherwise>
                                                                </c:choose>

                                                                <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">Đóng</button>
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



    <div style="position:fixed;bottom:6px;left:6px;font:12px/1 monospace;background:#f6f8fa;border:1px solid #ddd;padding:6px 8px;border-radius:6px;z-index:9999">
        pageNow=${pageNow}, pages=${pages}, total=${total}, size=${pageSize},
        isFirst=${isFirst}, isLast=${isLast}, singlePage=${singlePage}
    </div>

    <div class="modal fade" id="imgZoomModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark text-center">
                <img id="zoomImg" src="" alt="Preview" class="img-fluid">
            </div>
        </div>
    </div>


</div>

<style>
    .table td,.table th{ vertical-align:middle }

    .kyc-thumb {
        cursor: zoom-in;
        transition: transform 0.2s ease;
    }
    .kyc-thumb:hover {
        transform: scale(1.05);
    }

    #imgZoomModal img {
        width: 100%;
        height: auto;
        border-radius: 8px;
    }

    #toastBox{
        position: fixed;
        bottom: 30px; right: 30px;
        display: flex; align-items: flex-end; flex-direction: column;
        overflow: hidden; padding: 20px; z-index: 9999;
    }

    .mm-toast{
        width: 400px; height: 80px; background: #fff;
        font-weight: 500; margin: 15px 0; box-shadow: 0 0 20px rgba(0,0,0,0.3);
        display: flex; align-items: center; padding: 20px; position: relative;
        transform: translateX(100%);
        animation: moveleft 0.5s linear forwards;
    }

    .mm-toast i{
        margin: 0 20px;
        font-size: 35px;
        color: green;
    }
    .mm-toast.error i{
        color: red;
    }
    .mm-toast.error{
        color: red;
    }
    .mm-toast.error::after{
        background: red;
    }
    .mm-toast::after{
        content: '';
        position: absolute;
        left: 0;
        bottom: 0;
        width: 100%;
        height: 5px;
        background: green;
        animation: anim 5s linear forwards;
    }

    @keyframes anim {
        100%{ width: 0; }
    }
    @keyframes moveleft {
        100%{ transform: translateX(0); }
    }



</style>

<script>
    const toastBox = document.getElementById('toastBox');

    function showToast(msg, type = 'success') {
        const toast = document.createElement('div');
        toast.classList.add('mm-toast');

        if (type === 'error') {
            toast.classList.add('error');
        } else if (type === 'warning') {
            toast.classList.add('warning');
        }

        toast.innerHTML = msg;
        toastBox.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 5000);
    }
    document.addEventListener('DOMContentLoaded', function () {
        // =============================
        // ✅ PHẦN 1: AUTO FILTER (trừ ô q)
        // =============================
        const form   = document.getElementById('kycFilterForm');
        if (!form) return; // an toàn

        const qInput = document.getElementById('q');
        const selStt = document.getElementById('status');
        const fromEl = document.getElementById('from');
        const toEl   = document.getElementById('to');

        const today = new Date();
        today.setHours(0,0,0,0);

        // Reset về trang 1 trước khi lọc
        function resetPageToFirst() {
            let pageHidden = form.querySelector('input[name="page"]');
            if (!pageHidden) {
                pageHidden = document.createElement('input');
                pageHidden.type = 'hidden';
                pageHidden.name = 'page';
                form.appendChild(pageHidden);
            }
            pageHidden.value = '1';
        }




        // Chỉ cho phép Enter submit ở ô q; các ô khác bị chặn
        form.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target !== qInput) {
                e.preventDefault();
            }
        });


        // Nếu muốn chắc chắn reset page khi Enter ở q:
        if (qInput) {
            qInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') resetPageToFirst();
            });
        }

        // ✅ Đổi trạng thái -> submit ngay
        if (selStt) {
            selStt.addEventListener('change', () => {
                resetPageToFirst();
                form.submit();
            });
        }

        if(fromEl){
            fromEl.addEventListener("change",function (){
                const selected = new Date(this.value);
                if(selected > today){
                    showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!','error');
                    this.value = "";
                }
            });
        }


        // ✅ End date -> submit ngay
        if (toEl) {
            toEl.addEventListener('change', function (){
                const selected = new Date(this.value);
                    if(selected > today){
                        showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!', 'error');
                        this.value = "";
                    }else{
                        resetPageToFirst();
                        form.submit();
                    }
            });
        }


        // ============================= Room Image
        const zoomModal = document.getElementById('imgZoomModal');
        if (zoomModal) {
            let lastDetailModal = null;

            document.addEventListener('click', function(e) {
                const link = e.target.closest('.zoom-link');
                if (!link) return;
                e.preventDefault();

                lastDetailModal = e.target.closest('.modal');
                const full = link.getAttribute('data-full') || link.querySelector('img')?.src;
                const img = document.getElementById('zoomImg');
                if (img) img.src = full;

                new bootstrap.Modal(zoomModal).show();
            });

            zoomModal.addEventListener('hidden.bs.modal', function() {
                if (lastDetailModal) {
                    new bootstrap.Modal(lastDetailModal).show();
                    lastDetailModal = null;
                }
            });
        }
    });
</script>
<c:if test="${not empty sessionScope.flash}">
    <script>
        const msg = "${fn:escapeXml(sessionScope.flash)}";

        const icon = msg.toLowerCase().includes("lỗi")
            ? '<i class="fa fa-times-circle"></i>'
            : '<i class="fa fa-check-circle"></i>';

        showToast(icon + " " + msg, msg.toLowerCase().includes("lỗi") ? "error" : "success");
    </script>
    <c:remove var="flash" scope="session"/>
</c:if>