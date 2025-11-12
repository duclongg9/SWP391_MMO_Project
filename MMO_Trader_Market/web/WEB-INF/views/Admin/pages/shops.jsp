<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.time.LocalDate" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<%
    String today = LocalDate.now().toString();
    request.setAttribute("today", today);
%>

<!-- Phân trang & sort -->
<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (shopList != null ? fn:length(shopList) : 0)}" />
<c:set var="pages"    value="${(total + pageSize - 1) / pageSize}" />
<c:set var="sort"     value="${empty requestScope.sort ? 'date_desc' : requestScope.sort}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shop me-2"></i>Quản lý cửa hàng</h4>

    <c:if test="${not empty error}">
        <div class="alert alert-danger shadow-sm mb-3">${fn:escapeXml(error)}</div>
    </c:if>

    <!-- ===== Bộ lọc ===== -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="shopFilter"
                  class="row g-2 align-items-end"
                  action="${base}/admin/shops"
                  method="get">

                <input type="hidden" name="page" id="pageInput" value="${pageNow}">
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="sort" id="sort" value="${sort}"/>

                <!-- Từ khóa -->
                <div class="col-12 col-md-4">
                    <label for="q" class="form-label mb-1">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="search" class="form-control"
                               placeholder="Tên cửa hàng…"
                               value="${fn:escapeXml(q)}">
                    </div>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <input id="from" name="from" type="date"
                           class="form-control"
                           value="${fn:escapeXml(from)}"
                           max="${today}">
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <input id="to" name="to" type="date"
                           class="form-control"
                           value="${fn:escapeXml(to)}"
                           max="${today}">
                </div>

                <!-- Trạng thái -->
                <div class="col-12 col-md-2">
                    <label for="status" class="form-label mb-1">Trạng thái</label>
                    <select id="status" name="status" class="form-select">
                        <option value="all"        ${status == 'all'        ? 'selected' : ''}>Tất cả</option>
                        <option value="Pending"    ${status == 'Pending'    ? 'selected' : ''}>Pending</option>
                        <option value="Active"     ${status == 'Active'     ? 'selected' : ''}>Active</option>
                        <option value="Suspended"  ${status == 'Suspended'  ? 'selected' : ''}>Suspended</option>
                    </select>
                </div>

                <!-- Xóa lọc -->
                <div class="col-12 col-md-2 d-grid">
                    <a class="btn btn-outline-secondary" href="${base}/admin/shops">Xóa lọc</a>
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
                        <th>Chủ sở hữu</th>
                        <th>Tên cửa hàng</th>
                        <th>Nội dung</th>

                        <!-- Sort trạng thái -->
                        <th>
                            <c:url var="uStatusSort" value="/admin/shops">
                                <c:param name="q"      value="${q}" />
                                <c:param name="from"   value="${from}" />
                                <c:param name="to"     value="${to}" />
                                <c:param name="status" value="${status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort == 'status_asc' ? 'status_desc' : 'status_asc'}" />
                            </c:url>
                            <a href="${uStatusSort}" class="text-decoration-none">Trạng thái</a>
                        </th>

                        <!-- Sort ngày tạo -->
                        <th>
                            <c:url var="uDateSort" value="/admin/shops">
                                <c:param name="q"      value="${q}" />
                                <c:param name="from"   value="${from}" />
                                <c:param name="to"     value="${to}" />
                                <c:param name="status" value="${status}" />
                                <c:param name="size"   value="${pageSize}" />
                                <c:param name="page"   value="1" />
                                <c:param name="sort"   value="${sort == 'date_asc' ? 'date_desc' : 'date_asc'}" />
                            </c:url>
                            <a href="${uDateSort}" class="text-decoration-none">Ngày tạo</a>
                        </th>

                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${not empty shopList}">
                            <c:forEach var="s" items="${shopList}" varStatus="st">
                                <tr>
                                    <td>${(pageNow-1)*pageSize + st.index + 1}</td>
                                    <td>${fn:escapeXml(s.ownerName)}</td>
                                    <td class="fw-semibold">${fn:escapeXml(s.name)}</td>
                                    <td class="text-muted">
                                        <c:choose>
                                            <c:when test="${empty s.description}">
                                                <span class="fst-italic">—</span>
                                            </c:when>
                                            <c:otherwise>${fn:escapeXml(s.description)}</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <!-- Badge trạng thái -->
                                    <td>
                                        <span class="badge
                                            <c:choose>
                                                <c:when test='${s.status eq "Active"}'>bg-success</c:when>
                                                <c:when test='${s.status eq "Pending"}'>bg-secondary</c:when>
                                                <c:when test='${s.status eq "Suspended"}'>bg-danger</c:when>
                                                <c:otherwise>bg-secondary</c:otherwise>
                                            </c:choose>
                                        ">
                                                ${fn:escapeXml(s.status)}
                                        </span>
                                    </td>

                                    <!-- Ngày tạo -->
                                    <td><fmt:formatDate value="${s.createdAt}" pattern="dd-MM-yyyy"/></td>

                                    <!-- View detail -->
                                    <td class="text-center">
                                        <button class="btn btn-sm btn-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#shopModal_${s.id}">
                                            <i class="bi bi-eye"></i> Xem chi tiết
                                        </button>
                                    </td>
                                </tr>

                                <!-- Modal chi tiết -->
                                <div class="modal fade" id="shopModal_${s.id}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog modal-lg modal-dialog-centered">
                                        <div class="modal-content">
                                            <div class="modal-header bg-dark text-white">
                                                <h5 class="modal-title">
                                                    Cửa hàng #${s.id} – ${fn:escapeXml(s.name)}
                                                </h5>
                                                <button type="button" class="btn-close btn-close-white"
                                                        data-bs-dismiss="modal"></button>
                                            </div>

                                            <div class="modal-body">
                                                <div class="row g-3">
                                                    <div class="col-md-6">
                                                        <div class="small text-muted mb-1">Chủ cửa hàng</div>
                                                        <div class="fw-semibold">${fn:escapeXml(s.ownerName)}</div>
                                                    </div>
                                                    <div class="col-md-6">
                                                        <div class="small text-muted mb-1">Ngày tạo</div>
                                                        <div class="fw-semibold">
                                                            <fmt:formatDate value="${s.createdAt}" pattern="dd-MM-yyyy"/>
                                                        </div>
                                                    </div>

                                                    <div class="col-12">
                                                        <div class="small text-muted mb-1">Tên cửa hàng</div>
                                                        <div class="fw-semibold">${fn:escapeXml(s.name)}</div>
                                                    </div>

                                                    <div class="col-12">
                                                        <div class="small text-muted mb-1">Nội dung cửa hàng muốn bán</div>
                                                        <div class="border rounded p-2 bg-light">
                                                            <c:out value="${s.description}" />
                                                        </div>
                                                    </div>

                                                    <div class="col-12">
                                                        <div class="small text-muted mb-1">Trạng thái hiện tại</div>
                                                        <span class="badge
                                                            <c:choose>
                                                                <c:when test='${s.status eq "Active"}'>bg-success</c:when>
                                                                <c:when test='${s.status eq "Pending"}'>bg-secondary</c:when>
                                                                <c:when test='${s.status eq "Suspended"}'>bg-danger</c:when>
                                                                <c:otherwise>bg-secondary</c:otherwise>
                                                            </c:choose>
                                                        ">
                                                                ${fn:escapeXml(s.status)}
                                                        </span>
                                                    </div>

                                                    <div class="col-12">
                                                        <form id="shopForm_${s.id}"
                                                              action="${base}/admin/shops/status"
                                                              method="post"
                                                              class="d-flex flex-column gap-2">

                                                            <input type="hidden" name="id" value="${s.id}"/>
                                                            <input type="hidden" name="action" id="shopAction_${s.id}" value=""/>

                                                            <textarea id="shopNote_${s.id}"
                                                                      name="admin_note"
                                                                      class="form-control"
                                                                      rows="3">${fn:escapeXml(s.adminNote)}</textarea>
                                                            <div id="shopNoteErr_${s.id}" class="text-danger small mt-1 d-none">
                                                                ⚠️ Vui lòng nhập Admin note trước khi ban cửa hàng.
                                                            </div>

                                                            <div class="d-flex align-items-center gap-2 mt-3">
                                                                <c:choose>
                                                                    <c:when test='${s.status eq "Active"}'>
                                                                        <button type="button"
                                                                                class="btn btn-danger px-4"
                                                                                onclick="onBanShop(${s.id});">
                                                                            <i class="bi bi-slash-circle me-1"></i>Ban
                                                                        </button>
                                                                    </c:when>
                                                                    <c:when test='${s.status eq "Suspended"}'>
                                                                        <button type="button"
                                                                                class="btn btn-success px-4"
                                                                                onclick="onUnbanShop(${s.id});">
                                                                            <i class="bi bi-unlock me-1"></i>Unban
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                <span class="text-muted">
                    Cửa hàng chưa Active, không thể Ban/Unban.
                </span>
                                                                    </c:otherwise>
                                                                </c:choose>

                                                                <button type="button"
                                                                        class="btn btn-outline-secondary ms-auto px-4"
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
                                <td colspan="7" class="text-center text-muted py-4">
                                    Không có cửa hàng nào
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <c:url var="shopsPath" value="/admin/shops"/>
    <nav aria-label="Pagination">
        <ul class="pagination justify-content-center mt-3">

            <li class="page-item ${pageNow <= 1 ? 'disabled' : ''}">
                <c:url var="uPrev" value="${shopsPath}">
                    <c:param name="q"      value="${q}" />
                    <c:param name="from"   value="${from}" />
                    <c:param name="to"     value="${to}" />
                    <c:param name="status" value="${status}" />
                    <c:param name="size"   value="${pageSize}" />
                    <c:param name="page"   value="${pageNow-1}" />
                    <c:param name="sort"   value="${sort}" />
                </c:url>
                <a class="page-link" href="${pageNow <= 1 ? '#' : uPrev}" aria-label="Previous">&laquo;</a>
            </li>

            <!-- Pages -->
            <c:forEach var="i" begin="1" end="${pages}">
                <c:url var="uI" value="${shopsPath}">
                    <c:param name="q"      value="${q}" />
                    <c:param name="from"   value="${from}" />
                    <c:param name="to"     value="${to}" />
                    <c:param name="status" value="${status}" />
                    <c:param name="size"   value="${pageSize}" />
                    <c:param name="page"   value="${i}" />
                    <c:param name="sort"   value="${sort}" />
                </c:url>
                <li class="page-item ${i == pageNow ? 'active' : ''}">
                    <a class="page-link" href="${uI}">${i}</a>
                </li>
            </c:forEach>

            <li class="page-item ${pageNow >= pages ? 'disabled' : ''}">
                <c:url var="uNext" value="${shopsPath}">
                    <c:param name="q"      value="${q}" />
                    <c:param name="from"   value="${from}" />
                    <c:param name="to"     value="${to}" />
                    <c:param name="status" value="${status}" />
                    <c:param name="size"   value="${pageSize}" />
                    <c:param name="page"   value="${pageNow+1}" />
                    <c:param name="sort"   value="${sort}" />
                </c:url>
                <a class="page-link" href="${pageNow >= pages ? '#' : uNext}" aria-label="Next">&raquo;</a>
            </li>
        </ul>
    </nav>
</div>

<style>
    #toastBox {
        position: fixed;
        bottom: 30px;
        right: 30px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        padding: 20px;
        z-index: 9999;
    }
    .mm-toast {
        width: 400px;
        min-height: 60px;
        background: #fff;
        font-weight: 500;
        margin: 10px 0;
        box-shadow: 0 0 20px rgba(0,0,0,0.25);
        display: flex;
        align-items: center;
        padding: 16px 18px;
        position: relative;
        transform: translateX(100%);
        animation: moveleft 0.4s linear forwards;
        border-left: 4px solid #28a745;
    }
    .mm-toast.error {
        color: #dc3545;
        border-left-color: #dc3545;
    }
    .mm-toast::after {
        content:'';
        position:absolute;
        left:0;
        bottom:0;
        width:100%;
        height:4px;
        background:#28a745;
        animation: anim 5s linear forwards;
    }
    .mm-toast.error::after {
        background:#dc3545;
    }
    @keyframes anim { 100% { width:0; } }
    @keyframes moveleft { 100% { transform: translateX(0); } }
</style>

<script>
    function showToast(msg, type = 'success') {
        const box = document.getElementById('toastBox');
        if (!box) { alert(msg); return; }
        const t = document.createElement('div');
        t.className = 'mm-toast' + (type === 'error' ? ' error' : '');
        t.innerHTML = msg;
        box.appendChild(t);
        setTimeout(() => t.remove(), 5000);
    }


    function onBanShop(id) {
        const form   = document.getElementById('shopForm_' + id);
        const note   = document.getElementById('shopNote_' + id);
        const errBox = document.getElementById('shopNoteErr_' + id);
        const action = document.getElementById('shopAction_' + id);

        if (!form || !note || !action) return false;

        if (errBox) errBox.classList.add('d-none');
        note.classList.remove('is-invalid');

        if (!note.value.trim()) {
            if (errBox) errBox.classList.remove('d-none');
            note.classList.add('is-invalid');
            note.focus();
            return false;
        }

        if (!confirm('Bạn có chắc chắn muốn BAN cửa hàng này không?')) {
            return false;
        }

        action.value = 'ban';
        form.submit();
        return true;
    }

    // Clear lỗi khi gõ lại admin_note
    document.addEventListener('input', function (e) {
        const m = e.target.id && e.target.id.match(/^shopNote_(\d+)$/);
        if (!m) return;
        const id = m[1];
        const err = document.getElementById('shopNoteErr_' + id);
        if (err) err.classList.add('d-none');
        e.target.classList.remove('is-invalid');
    });

    function onUnbanShop(id) {
        const form   = document.getElementById('shopForm_' + id);
        const action = document.getElementById('shopAction_' + id);

        if (!form || !action) return false;

        if (!confirm('Bạn có chắc chắn muốn mở khóa (UNBAN) cửa hàng này không?')) {
            return false;
        }

        action.value = 'unban';
        form.submit();
        return true;
    }


    // Filter logic + validate date + auto submit giống các trang khác
    window.addEventListener('DOMContentLoaded', () => {
        const form      = document.getElementById('shopFilter');
        const ipQ       = document.getElementById('q');
        const fromEl    = document.getElementById('from');
        const toEl      = document.getElementById('to');
        const ipStatus  = document.getElementById('status');
        const pageInput = document.getElementById('pageInput');

        const today = new Date(); today.setHours(0,0,0,0);

        if (!form) return;

        form.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target !== ipQ) e.preventDefault();
        });

        if (ipQ) {
            ipQ.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    if (pageInput) pageInput.value = '1';
                    form.submit();
                }
            });
        }

        if (ipStatus) {
            ipStatus.addEventListener('change', () => {
                if (pageInput) pageInput.value = '1';
                form.submit();
            });
        }

        if (fromEl) {
            fromEl.addEventListener('change', function () {
                const d = new Date(this.value);
                if (d > today) {
                    showToast('<i class="fa fa-times-circle"></i> Không được chọn ngày trong tương lai!', 'error');
                    this.value = '';
                } else if (pageInput) {
                    pageInput.value = '1';
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
                    if (pageInput) pageInput.value = '1';
                    form.submit();
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
