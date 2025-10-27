<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shop me-2"></i>Quản lý cửa hàng</h4>

    <c:if test="${not empty flash}">
        <div class="alert alert-success shadow-sm">${flash}</div>
        <%
            // Chỉ hiển thị 1 lần
            session.removeAttribute("flash");
        %>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger shadow-sm">${error}</div>
    </c:if>

    <!-- Filter -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="shopFilter" class="row g-2 align-items-end" action="${base}/admin/shops" method="get">
                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label for="q" class="form-label mb-1">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="text" class="form-control"
                               placeholder="Tên cửa hàng..."
                               value="${fn:escapeXml(q)}">
                    </div>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label for="from" class="form-label mb-1">Từ ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                        <!-- LƯU Ý: value phải là yyyy-MM-dd (đã chuẩn hóa ở servlet) -->
                        <input id="from" name="from" type="date" class="form-control"
                               value="${from}">
                    </div>
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label for="to" class="form-label mb-1">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="to" name="to" type="date" class="form-control"
                               value="${to}">
                    </div>
                </div>

                <!-- Trạng thái (String) -->
                <div class="col-6 col-md-2">
                    <label for="status" class="form-label mb-1">Trạng thái</label>
                    <select id="status" name="status" class="form-select">
                        <option value="all"      ${status == 'all'      ? 'selected' : ''}>Tất cả</option>
                        <option value="Active"   ${status == 'Active'   ? 'selected' : ''}>Active</option>
                        <option value="Pending"   ${status == 'Pending'   ? 'selected' : ''}>Pending</option>
                        <option value="Rejected" ${status == 'Rejected' ? 'selected' : ''}>Rejected</option>
                    </select>
                </div>

                <!-- Nút -->
                <div class="col-12 col-md-3 d-flex gap-2">
                    <button type="submit" class="btn btn-dark flex-fill">
                        <i class="bi bi-funnel"></i> Lọc
                    </button>
                    <a class="btn btn-outline-secondary" href="${base}/admin/shops">Xóa lọc</a>
                </div>
            </form>
        </div>
    </div>

    <!-- Table -->
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
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty shopList}">
                            <c:forEach var="s" items="${shopList}" varStatus="st">
                                <tr>
                                    <td>${st.index + 1}</td>
                                    <td>${fn:escapeXml(s.ownerName)}</td>
                                    <td class="fw-semibold">${fn:escapeXml(s.name)}</td>
                                    <td class="text-muted">
                                        <c:choose>
                                            <c:when test="${empty s.description}"><span class="fst-italic">—</span></c:when>
                                            <c:otherwise>${fn:escapeXml(s.description)}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge
                                          <c:choose>
                                            <c:when test='${s.status eq "Active"}'>bg-success</c:when>
                                            <c:when test='${s.status eq "Banned"}'>bg-warning text-dark</c:when>
                                            <c:when test='${s.status eq "Rejected"}'>bg-danger</c:when>
                                            <c:otherwise>bg-secondary</c:otherwise>
                                          </c:choose>">
                                            ${fn:escapeXml(s.status)}
                                        </span>
                                    </td>
                                    <td><fmt:formatDate value="${s.createdAt}" pattern="dd-MM-yyyy"/></td>
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
                                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
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
                                                        <div class="border rounded p-2 bg-light">${fn:escapeXml(s.description)}</div>
                                                    </div>

                                                    <div class="col-12">
                                                        <div class="small text-muted mb-1">Trạng thái hiện tại</div>
                                                        <span class="badge
                                                          <c:choose>
                                                            <c:when test='${s.status eq "Active"}'>bg-success</c:when>
                                                            <c:when test='${s.status eq "Banned"}'>bg-warning text-dark</c:when>
                                                            <c:when test='${s.status eq "Rejected"}'>bg-danger</c:when>
                                                            <c:otherwise>bg-secondary</c:otherwise>
                                                          </c:choose> ">
                                                            ${fn:escapeXml(s.status)}
                                                        </span>
                                                    </div>

                                                    <div class="col-12">
                                                        <!-- Ẩn Accept/Reject nếu đã xử lý -->
                                                        <c:choose>
                                                            <c:when test='${s.status ne "Active" && s.status ne "Rejected"}'>
                                                                <form action="${base}/admin/shops/status" method="post" class="d-flex gap-2">
                                                                    <input type="hidden" name="id" value="${s.id}"/>
                                                                    <button class="btn btn-success" name="action" value="accept">
                                                                        <i class="bi bi-check-circle"></i> Accept
                                                                    </button>
                                                                    <button class="btn btn-danger" name="action" value="reject"
                                                                            onclick="return confirm('Từ chối cửa hàng này?');">
                                                                        <i class="bi bi-x-circle"></i> Reject
                                                                    </button>
                                                                    <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">Đóng</button>
                                                                </form>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="d-flex w-100 align-items-center mt-2">
                                                                    <span class="text-muted">Cửa hàng đã được xử lý.</span>
                                                                    <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">Đóng</button>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
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
                            <tr><td colspan="7" class="text-center text-muted py-4">Không có cửa hàng nào</td></tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<style>
    .card{border-radius:12px}
    .table td,.table th{vertical-align:middle}
    .table thead th{white-space:nowrap}
</style>

<script>
    window.addEventListener('DOMContentLoaded', () => {
        // Tự ẩn flash
        document.querySelectorAll('.alert').forEach(a => {
            setTimeout(() => { a.classList.add('fade'); a.style.opacity = 0; }, 1800);
            setTimeout(() => a.remove(), 2600);
        });

        const form     = document.getElementById('shopFilter');
        const ipQ      = document.getElementById('q');
        const ipFrom   = document.getElementById('from');
        const ipTo     = document.getElementById('to');
        const ipStatus = document.getElementById('status');

        if (!form) return;

        // Không submit khi nhấn Enter trong các ô filter (chỉ submit khi bấm Lọc)
        [ipQ, ipFrom, ipTo].forEach(el => {
            if (!el) return;
            el.addEventListener('keydown', e => { if (e.key === 'Enter') e.preventDefault(); });
        });

        // Đổi trạng thái -> tự submit
        if (ipStatus) {
            ipStatus.addEventListener('change', () => form.submit());
        }
    });
</script>
