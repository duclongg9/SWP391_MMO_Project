<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    // đọc lại params để set mặc định vào input (fallback nếu servlet chưa set EL)
    String q      = request.getParameter("q")    != null ? request.getParameter("q")    : "";
    String role   = request.getParameter("role") != null ? request.getParameter("role") : "";
    String fromD  = request.getParameter("from") != null ? request.getParameter("from") : "";
    String toD    = request.getParameter("to")   != null ? request.getParameter("to")   : "";
%>

<!-- Biến phân trang (ưu tiên pg_* từ servlet; fallback chiều dài list) -->
<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (userList != null ? fn:length(userList) : 0)}" />
<c:set var="pages"    value="${(total + pageSize - 1) / pageSize}" />

<div class="container-fluid">
    <!-- Bộ lọc -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="userFilter" class="row g-2 align-items-end" action="${pageContext.request.contextPath}/admin/users" method="get">
                <!-- giữ page/size + reset page=1 khi đổi filter -->
                <input type="hidden" name="page" id="pageInput" value="${pageNow}">
                <input type="hidden" name="size" value="${pageSize}">

                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="keyword" name="q" type="text" class="form-control" placeholder="Tên hoặc email..." value="<%= q %>">
                    </div>
                </div>

                <!-- Vai trò -->
                <div class="col-12 col-md-2">
                    <label class="form-label mb-1">Vai trò</label>
                    <select id="role" name="role" class="form-select">
                        <option value=""        <%= role.equals("")? "selected":"" %>>Tất cả</option>
                        <option value="buyer"   <%= role.equals("buyer")? "selected":"" %>>Buyer</option>
                        <option value="seller"  <%= role.equals("seller")? "selected":"" %>>Seller</option>
                        <option value="admin"   <%= role.equals("admin")? "selected":"" %>>Admin</option>
                    </select>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1">Từ ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                        <input id="fromDate" name="from" type="text" class="form-control" placeholder="DD-MM-YYYY" value="<%= fromD %>">
                    </div>
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="toDate" name="to" type="text" class="form-control" placeholder="DD-MM-YYYY" value="<%= toD %>">
                    </div>
                </div>

                <!-- Nút -->
                <div class="col-12 col-md-3 d-flex gap-2">
                    <button id="btnSearch" class="btn btn-dark flex-fill" type="submit">
                        <i class="bi bi-funnel"></i> Tìm kiếm
                    </button>
                    <button
                            class="btn btn-primary flex-fill"
                            type="button"
                            data-bs-toggle="modal"
                            data-bs-target="#userCreateModal">
                        <i class="bi bi-plus-circle"></i> Thêm người dùng
                    </button>

                    <!-- POPUP tạo người dùng (form POST thuần) -->
                    <div class="modal fade" id="userCreateModal" tabindex="-1" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered">
                            <div class="modal-content">
                                <div class="modal-header bg-dark text-white">
                                    <h5 class="modal-title">Thêm người dùng mới</h5>
                                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Đóng"></button>
                                </div>

                                <form action="${pageContext.request.contextPath}/admin/users" method="post" novalidate>
                                    <div class="modal-body">
                                        <div class="mb-3">
                                            <label class="form-label">Họ tên</label>
                                            <input name="name" type="text" class="form-control" required minlength="2" maxlength="120">
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Email</label>
                                            <input name="email" type="email" class="form-control" required maxlength="160">
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Mật khẩu</label>
                                            <input name="password" type="password" class="form-control" required minlength="6" maxlength="100">
                                        </div>
                                        <%-- <input type="hidden" name="_csrf" value="${_csrfToken}"> --%>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="bi bi-check-circle"></i> Lưu
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div> <!-- /col buttons -->
            </form>
        </div>
    </div>

    <!-- Bảng -->
    <div class="table-responsive shadow-sm rounded-3">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
            <tr>
                <th class="text-center" style="width:72px">#</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Ngày tạo</th>
                <th>Ngày cập nhập</th>
                <th style="width:140px">Vai trò</th>
                <th style="width:140px">Trạng thái</th>
                <th class="text-center" style="width:170px">Hành động</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${userList}" varStatus="st">
                <c:if test="${u.roleName ne 'ADMIN'}">
                    <tr>
                        <td class="text-center">${(pageNow-1)*pageSize + st.index + 1}</td>
                        <td>${u.name}</td>
                        <td>${u.email}</td>
                        <td><fmt:formatDate value="${u.createdAt}" pattern="dd-MM-yyyy"/></td>
                        <td><fmt:formatDate value="${u.updatedAt}" pattern="dd-MM-yyyy"/></td>
                        <td>${u.roleName}</td>
                        <td>
                            <c:choose>
                                <c:when test="${u.status eq 1}">
                                    <span class="badge bg-success">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Không hoạt động</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-center">
                            <form action="${pageContext.request.contextPath}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="ban">
                                <button class="btn btn-sm btn-outline-danger" <c:if test="${u.status ne 1}">disabled</c:if>>
                                    <i class="bi bi-flag"></i> Ban
                                </button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="unban">
                                <button class="btn btn-sm btn-outline-secondary" <c:if test="${u.status eq 1}">disabled</c:if>>
                                    <i class="bi bi-flag"></i> Unban
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:if>
            </c:forEach>

            <c:if test="${empty userList}">
                <tr><td colspan="8" class="text-center text-muted py-4">Không có người dùng</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- Pagination -->
    <c:url var="usersPath" value="/admin/users"/>
    <c:if test="${pages > 1}">
        <nav aria-label="Pagination">
            <ul class="pagination justify-content-center mt-3">

                <!-- Prev -->
                <li class="page-item ${pageNow<=1?'disabled':''}">
                    <c:url var="uPrev" value="${usersPath}">
                        <c:param name="q"    value="${param.q}" />
                        <c:param name="role" value="${param.role}" />
                        <c:param name="from" value="${param.from}" />
                        <c:param name="to"   value="${param.to}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="page" value="${pageNow-1}" />
                    </c:url>
                    <a class="page-link" href="${uPrev}" aria-label="Previous">&laquo;</a>
                </li>

                <!-- window -->
                <c:set var="start" value="${pageNow-2 < 1 ? 1 : pageNow-2}" />
                <c:set var="end"   value="${pageNow+2 > pages ? pages : pageNow+2}" />

                <c:if test="${start > 1}">
                    <c:url var="u1" value="${usersPath}">
                        <c:param name="q"    value="${param.q}" />
                        <c:param name="role" value="${param.role}" />
                        <c:param name="from" value="${param.from}" />
                        <c:param name="to"   value="${param.to}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="page" value="1" />
                    </c:url>
                    <li class="page-item"><a class="page-link" href="${u1}">1</a></li>
                    <li class="page-item disabled"><span class="page-link">…</span></li>
                </c:if>

                <c:forEach var="i" begin="${start}" end="${end}">
                    <c:url var="ui" value="${usersPath}">
                        <c:param name="q"    value="${param.q}" />
                        <c:param name="role" value="${param.role}" />
                        <c:param name="from" value="${param.from}" />
                        <c:param name="to"   value="${param.to}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="page" value="${i}" />
                    </c:url>
                    <li class="page-item ${i==pageNow?'active':''}">
                        <a class="page-link" href="${ui}">${i}</a>
                    </li>
                </c:forEach>

                <c:if test="${end < pages}">
                    <li class="page-item disabled"><span class="page-link">…</span></li>
                    <c:url var="uLast" value="${usersPath}">
                        <c:param name="q"    value="${param.q}" />
                        <c:param name="role" value="${param.role}" />
                        <c:param name="from" value="${param.from}" />
                        <c:param name="to"   value="${param.to}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="page" value="${pages}" />
                    </c:url>
                    <li class="page-item"><a class="page-link" href="${uLast}">${pages}</a></li>
                </c:if>

                <!-- Next -->
                <li class="page-item ${pageNow>=pages?'disabled':''}">
                    <c:url var="uNext" value="${usersPath}">
                        <c:param name="q"    value="${param.q}" />
                        <c:param name="role" value="${param.role}" />
                        <c:param name="from" value="${param.from}" />
                        <c:param name="to"   value="${param.to}" />
                        <c:param name="size" value="${pageSize}" />
                        <c:param name="page" value="${pageNow+1}" />
                    </c:url>
                    <a class="page-link" href="${uNext}" aria-label="Next">&raquo;</a>
                </li>
            </ul>
        </nav>
    </c:if>
</div>

<script>
    // Auto-submit + reset page về 1 khi đổi filter
    (function () {
        const form = document.getElementById('userFilter');
        const pageInput = document.getElementById('pageInput');

        ['role'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('change', () => { pageInput.value = 1; form.submit(); });
        });

        const q = document.getElementById('keyword');
        if (q) q.addEventListener('keydown', e => {
            if (e.key === 'Enter') { pageInput.value = 1; /* form.submit(); */ }
        });

        const from = document.getElementById('fromDate');
        const to   = document.getElementById('toDate');
        [from, to].forEach(el => {
            if (el) el.addEventListener('change', () => { pageInput.value = 1; form.submit(); });
        });
    })();

    // ====== BAN / UNBAN (giữ nguyên nếu bạn đang dùng) ======
    const base = '<c:out value="${pageContext.request.contextPath}"/>';
    async function delUser(id) {
        if (!Number.isInteger(id)) { alert('ID không hợp lệ'); return; }
        if (!confirm('Xóa người dùng này?')) return;
        const url = base + '/admin/users/' + id;
        const res  = await fetch(url, { method: 'DELETE' });
        const text = await res.text();
        if (res.ok) location.reload();
        else alert(`Xóa thất bại (HTTP ${res.status}): ${text}`);
    }
</script>
