<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<!-- ===== Biến phân trang nhận từ servlet ===== -->
<c:set var="pageNow"  value="${pg_page  != null ? pg_page  : 1}" />
<c:set var="pageSize" value="${pg_size  != null ? pg_size  : 8}" />
<c:set var="total"    value="${pg_total != null ? pg_total : (userList != null ? fn:length(userList) : 0)}" />
<c:set var="pages"    value="${pg_pages != null ? pg_pages : ((total + pageSize - 1) / pageSize)}" />


<c:set var="openCreate" value="${openCreateModal or param.openCreate == '1'}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-people me-2"></i>Quản lý người dùng</h4>

    <!-- ===== Bộ lọc (GET, không auto-submit) ===== -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <form id="userFilterForm" class="row g-2 align-items-end"
                  action="${base}/admin/users" method="get" novalidate>
                <input type="hidden" name="size" value="${pageSize}">
                <input type="hidden" name="page" value="1">

                <div class="col-12 col-md-3">
                    <label class="form-label mb-1" for="q">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="q" name="q" type="text" class="form-control"
                               placeholder="Tên hoặc email..." value="${param.q}">
                    </div>
                </div>

                <div class="col-12 col-md-2">
                    <label class="form-label mb-1" for="role">Vai trò</label>
                    <select id="role" name="role" class="form-select">
                        <c:set var="r" value="${empty param.role ? '' : param.role}"/>
                        <option value=""        ${r==''? 'selected' : ''}>Tất cả</option>
                        <option value="buyer"   ${r=='buyer'? 'selected' : ''}>Buyer</option>
                        <option value="seller"  ${r=='seller'? 'selected' : ''}>Seller</option>
                    </select>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="from">Từ ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                        <input id="from" name="from" type="date" lang="vi" class="form-control"
                               placeholder="DD-MM-YYYY" value="${param.from}">
                    </div>
                </div>

                <div class="col-6 col-md-2">
                    <label class="form-label mb-1" for="to">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="to" name="to" type="date" lang="vi" class="form-control"
                               placeholder="DD-MM-YYYY" value="${param.to}">
                    </div>
                </div>

                <div class="col-12 col-md-3 d-flex gap-2">
                    <a class="btn btn-primary flex-fill" href="${base}/admin/users?openCreate=1">
                        <i class="bi bi-plus-circle"></i> Thêm người dùng
                    </a>
                    <a class="btn btn-outline-secondary" href="${base}/admin/users">Xóa lọc</a>
                </div>
            </form>

        </div>
    </div>

    <!-- ===== Bảng danh sách ===== -->
    <div class="table-responsive shadow-sm rounded-3">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
            <tr>
                <th class="text-center" style="width:72px">#</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Ngày tạo</th>
                <th>Ngày cập nhật</th>
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
                                <c:when test="${u.status == true}">
                                    <span class="badge bg-success">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Không hoạt động</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-center">
                            <form action="${base}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="ban">
                                <button class="btn btn-sm btn-outline-danger" <c:if test="${u.status != true}">disabled</c:if>>
                                    <i class="bi bi-flag"></i> Ban
                                </button>
                            </form>
                            <form action="${base}/admin/users/status" method="post" class="d-inline">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="action" value="unban">
                                <button class="btn btn-sm btn-outline-secondary" <c:if test="${u.status == true}">disabled</c:if>>
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


    <!-- Debug nhỏ (tuỳ thích) -->
    <div style="position:fixed;bottom:6px;left:6px;font:12px/1 monospace;background:#f6f8fa;border:1px solid #ddd;padding:6px 8px;border-radius:6px;z-index:9999">
        pageNow=${pageNow}, pages=${pages}, total=${total}, size=${pageSize}
    </div>
</div>

<div class="modal fade ${openCreate ? 'show' : ''}"
     id="userCreateModal"
     tabindex="-1"
     aria-hidden="${!openCreate}"
     style="${openCreate ? 'display:block' : ''}">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-dark text-white">
                <h5 class="modal-title">Thêm người dùng mới</h5>
                <a  class="btn-close btn-close-white" href="${base}/admin/users" aria-label="Đóng"></a>
            </div>

            <form action="${base}/admin/users" method="post" novalidate>
                <div class="modal-body">
                    <c:if test="${not empty form_errs['form']}">
                        <div class="alert alert-danger">${form_errs['form']}</div>
                    </c:if>

                    <div class="mb-3">
                        <label class="form-label">Họ tên</label>
                        <input name="name" type="text"
                               class="form-control ${not empty form_errs['name'] ? 'is-invalid' : ''}"
                               required minlength="2" maxlength="120"
                               value="${fn:escapeXml(form_name)}">
                        <div class="invalid-feedback">${form_errs['name']}</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input name="email" type="email"
                               class="form-control ${not empty form_errs['email'] ? 'is-invalid' : ''}"
                               required maxlength="160"
                               value="${fn:escapeXml(form_email)}">
                        <div class="invalid-feedback">${form_errs['email']}</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>
                        <input name="password" type="password"
                               class="form-control ${not empty form_errs['password'] ? 'is-invalid' : ''}"
                               required minlength="6" maxlength="100">
                        <div class="invalid-feedback">${form_errs['password']}</div>
                    </div>
                </div>

                <div class="modal-footer">
                    <!-- Đóng modal không cần JS: quay về trang users -->
                    <a class="btn btn-secondary" href="${base}/admin/users">Đóng</a>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-circle"></i> Lưu
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>


<c:if test="${openCreate}">
    <style>body{overflow:hidden}</style>
    <div class="modal-backdrop fade show"></div>
</c:if>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const form   = document.getElementById('userFilterForm');
        if (!form) return;

        const qInput = document.getElementById('q');
        const roleEl = document.getElementById('role');
        const fromEl = document.getElementById('from');
        const toEl   = document.getElementById('to');

        // reset về trang 1 khi filter
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

        // Chặn Enter nếu bạn không muốn reload khi người ta ấn Enter ở role/to
        form.addEventListener('keydown', (e) => {
            const el = e.target;
            // chỉ cho phép Enter ở ô q
            if (e.key === 'Enter' && el.id !== 'q') {
                e.preventDefault();
            }
        });

        // ✅ Role thay đổi → submit ngay
        if (roleEl) {
            roleEl.addEventListener('change', () => {
                resetPageToFirst();
                form.submit();
            });
        }

        // ✅ To thay đổi → submit ngay
        if (toEl) {
            toEl.addEventListener('change', () => {
                resetPageToFirst();
                form.submit();
            });
        }
    });
</script>