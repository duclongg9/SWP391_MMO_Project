<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    // đọc lại params để set mặc định vào input
    String q      = request.getParameter("q")      != null ? request.getParameter("q")      : "";
    String role   = request.getParameter("role")   != null ? request.getParameter("role")   : "";
    String fromD  = request.getParameter("from")   != null ? request.getParameter("from")   : "";
    String toD    = request.getParameter("to")     != null ? request.getParameter("to")     : "";
%>

<div class="container-fluid">
    <!-- Bộ lọc -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <div class="row g-2 align-items-end">
                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="keyword" type="text" class="form-control" placeholder="Tên hoặc email..."
                               value="<%= q %>">
                    </div>
                </div>

                <!-- Vai trò -->
                <div class="col-12 col-md-2">
                    <label class="form-label mb-1">Vai trò</label>
                    <select id="role" class="form-select">
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
                        <input id="fromDate" type="text" class="form-control" placeholder="DD-MM-YYYY"
                               value="<%= fromD %>">
                    </div>
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="toDate" type="text" class="form-control" placeholder="DD-MM-YYYY"
                               value="<%= toD %>">
                    </div>
                </div>

                <!-- Nút -->
                <div class="col-12 col-md-3 d-flex gap-2">
                    <button id="btnSearch" class="btn btn-dark flex-fill">
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
                                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                            aria-label="Đóng"></button>
                                </div>

                                <!-- Form POST thuần đến /admin/users -->
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


                                        <!-- (tuỳ chọn) thêm hidden input CSRF nếu bạn có filter CSRF -->
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
                </div>
            </div>
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
                        <td class="text-center">${st.index + 1}</td>
                        <td>${u.name}</td>
                        <td>${u.email}</td>
                        <td><fmt:formatDate value="${u.createdAt}" pattern="dd-MM-yyyy"/></td>
                        <td><fmt:formatDate value="${u.updatedAt}" pattern="dd-MM-yyyy"/></td>
                        <td>${u.roleName}</td>
                        <td>
                            <c:choose>
                                <c:when test="${u.status == true}">
                                    <span class="badge bg-success">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Không hoạt động</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td class="text-center">
                            <!-- Ban -->
                            <form action="${pageContext.request.contextPath}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="ban">
                                <button class="btn btn-sm btn-outline-danger"
                                        <c:if test="${u.status != true}">disabled</c:if>>
                                    <i class="bi bi-flag"></i> Ban
                                </button>
                            </form>

                            <!-- Unban -->
                            <form action="${pageContext.request.contextPath}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="unban">
                                <button class="btn btn-sm btn-outline-secondary"
                                        <c:if test="${u.status == true}">disabled</c:if>>
                                    <i class="bi bi-flag"></i> Unban
                                </button>
                            </form>
                        </td>

                    </tr>
                </c:if>
            </c:forEach>
            </tbody>


        </table>
    </div>
</div>

<script>
    // context path, ví dụ "/mmo"
    const base = '<c:out value="${pageContext.request.contextPath}"/>';

    // Nút tìm kiếm
    document.getElementById('btnSearch').addEventListener('click', function (e) {
        e.preventDefault();
        const q    = document.getElementById('keyword').value.trim();
        const from = document.getElementById('fromDate').value.trim();
        const to   = document.getElementById('toDate').value.trim();
        const role = document.getElementById('role').value;

        const params = new URLSearchParams();
        if (q)    params.set('q', q);
        if (from) params.set('from', from);
        if (to)   params.set('to', to);
        if (role) params.set('role', role);

        window.location.href = base + '/admin/users' + (params.toString() ? ('?' + params.toString()) : '');
    });

    // Đổi vai trò lọc ngay
    document.getElementById('role').addEventListener('change', function () {
        const role = this.value;
        const q    = document.getElementById('keyword').value.trim();
        const from = document.getElementById('fromDate').value.trim();
        const to   = document.getElementById('toDate').value.trim();

        const params = new URLSearchParams();
        if (q)    params.set('q', q);
        if (from) params.set('from', from);
        if (to)   params.set('to', to);
        if (role) params.set('role', role);

        window.location.href = base + '/admin/users' + (params.toString() ? ('?' + params.toString()) : '');
    });

    // (Giữ nếu bạn còn dùng nút xóa)
    async function delUser(id) {
        if (!Number.isInteger(id)) { alert('ID không hợp lệ'); return; }
        if (!confirm('Xóa người dùng này?')) return;

        const url = base + '/admin/users/' + id;
        const res  = await fetch(url, { method: 'DELETE' });
        const text = await res.text();
        if (res.ok) location.reload();
        else alert(`Xóa thất bại (HTTP ${res.status}): ${text}`);
    }

    // ====== BAN / UNBAN ======
    (function () {
        // Có context path thì base = "/mmo"; nếu deploy ở root thì base = ""
        const base = '<c:out value="${pageContext.request.contextPath}"/>';

        function getId(btn) {
            const id = Number(btn?.dataset?.id);
            if (!Number.isInteger(id)) {
                console.error('Không lấy được id từ button:', btn);
                alert('ID không hợp lệ');
                throw new Error('missing id');
            }
            return id;
        }

        window.banUser = async function (btn) {
            const id = getId(btn);
            if (!confirm('Ban tài khoản này?')) return;

            const url = `${base}/admin/users/${id}/ban`;
            console.log('POST', url); // debug

            try {
                const res  = await fetch(url, { method: 'POST' });
                const text = await res.text();
                if (res.ok) location.reload();
                else alert(`Ban thất bại (HTTP ${res.status}): ${text}`);
            } catch (e) {
                console.error(e);
                alert('Lỗi kết nối khi Ban.');
            }
        };

        window.unbanUser = async function (btn) {
            const id = getId(btn);
            if (!confirm('Mở ban tài khoản này?')) return;

            const url = `${base}/admin/users/${id}/unban`;
            console.log('POST', url); // debug

            try {
                const res  = await fetch(url, { method: 'POST' });
                const text = await res.text();
                if (res.ok) location.reload();
                else alert(`Unban thất bại (HTTP ${res.status}): ${text}`);
            } catch (e) {
                console.error(e);
                alert('Lỗi kết nối khi Unban.');
            }
        };
    })();

</script>

