<jsp:useBean id="pageTitle" scope="request" type="java.lang.String"/>
<jsp:useBean id="active" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle != null ? pageTitle : "MMO Admin"}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script>
        window.APP_BASE = '${pageContext.request.contextPath}';
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Nạp admin.js -->
    <script src="${pageContext.request.contextPath}/assets/Script/admin.js" defer></script>
</head>

<body>
<!-- Sidebar -->
<div class="sidebar">
    <div>
        <div class="top-section">
            <h4>
                <span class="short">MMO</span>
                <span class="full">MMO Admin</span>
            </h4>
        </div>

        </a>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="${active=='dashboard'?'active':''}">
                    <i class="bi bi-speedometer2"></i><span>Tổng quan</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/users" class="${active=='users'?'active':''}">
                    <i class="bi bi-people"></i><span>Người dùng</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/shops" class="${active=='shops'?'active':''}">
                    <i class="bi bi-shop"></i><span>Cửa hàng</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/kycs" class="${active=='kycs'?'active':''}">
                    <i class="bi bi-shield-check"></i><span>Duyệt KYC</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/cashs" class="${active=='cashs'?'active':''}">
                    <i class="bi bi-cash-stack"></i><span>Nạp / Rút</span>
                </a>
        <a href="${pageContext.request.contextPath}/admin/disputes" class="${active=='disputes'?'active':''}">
            <i class="bi bi-arrow-up-right-circle-fill"></i><span>Khiếu Nại</span>
        </a>
                <a href="${pageContext.request.contextPath}/admin/systems" class="${active=='systems'?'active':''}">
                    <i class="bi bi-gear"></i><span>Cấu hình hệ thống</span>
                </a>
            </div>

    <!-- Logout -->
    <div class="logout-section">
        <a href="${pageContext.request.contextPath}/index.html" style="margin: 0;">
            <i class="bi bi-box-arrow-right me-2"></i><span>Đăng xuất</span>
        </a>
    </div>
</div>

<div class="main">
            <div class="header">
                <h5 class="m-0">${pageTitle != null ? pageTitle : "Bảng điều khiển"}</h5>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/home">
                    <i class="bi bi-box-arrow-up-right me-2"></i>Về trang Market
                </a>
            </div>

            <div class="page-content">
                <jsp:useBean id="content" scope="request" type="java.lang.String"/>
                <jsp:include page="${content}" />
            </div>
        </div>



</body>

</html>


