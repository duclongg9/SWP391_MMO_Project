<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shop me-2"></i>Quản lý cửa hàng</h4>
    <%
        // đọc lại params để set mặc định vào input
        String q      = request.getParameter("q")      != null ? request.getParameter("q")      : "";
        String role   = request.getParameter("role")   != null ? request.getParameter("role")   : "";
        String fromD  = request.getParameter("from")   != null ? request.getParameter("from")   : "";
        String toD    = request.getParameter("to")     != null ? request.getParameter("to")     : "";
    %>
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
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>#</th>
                    <th>Chủ sở hữu</th>
                    <th>Tên cửa hàng</th>
                    <th>Nội dung</th>
                    <th>Trạng thái</th>
                    <th>Đăng ký ngày</th>
                    <th class="text-center">Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${shopList}" varStatus="st">
                    <tr>
                        <td>${st.index + 1}</td>
                        <td>${s.ownerName}</td>
                        <td>${s.name}</td>
                        <td>${s.description}</td>
                        <td>${s.status}</td>
                        <td><fmt:formatDate value="${s.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${s.status eq 'Active'}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/shops/ban?id=${s.id}" style="display:inline;">
                                        <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-ban"></i> Ban</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/shops/unban?id=${s.id}" style="display:inline;">
                                        <button type="submit" class="btn btn-sm btn-outline-success"><i class="bi bi-arrow-clockwise"></i> Unban</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty shopList}">
                    <tr><td colspan="6" class="text-center text-muted py-4">Không có cửa hàng nào</td></tr>
                </c:if>
                </tbody>
            </table>        </div>
    </div>
</div>

<style>
    .card {
        border-radius: 10px;
    }
    th {
        white-space: nowrap;
    }
    td {
        vertical-align: middle;
    }
    table {
        font-size: 15px;
    }
</style>
