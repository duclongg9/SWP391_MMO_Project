<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle != null ? pageTitle : "MMO Admin"}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            font-family: "Poppins", sans-serif;
            background: #f4f6f9;
            margin: 0;
            overflow-x: hidden;
        }

        /* Sidebar */
        .sidebar {
            position: fixed;
            top: 0;
            left: 0;
            height: 100vh;
            width: 70px;
            background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
            color: #fff;
            transition: width 0.3s ease;
            overflow-x: hidden;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
        }

        .sidebar:hover { width: 220px; }

        /* Logo trên cùng */
        .sidebar .top-section {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 60px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
        }

        .sidebar h4 {
            font-size: 18px;
            font-weight: 600;
            color: #fff;
            margin: 0;
            letter-spacing: 0.5px;
            white-space: nowrap;
            overflow: hidden;
            transition: opacity 0.3s ease, transform 0.3s ease;
        }

        /* Khi thu gọn chỉ hiển thị MMO */
        .sidebar:not(:hover) h4 span.full {
            display: none;
        }
        .sidebar:not(:hover) h4 span.short {
            display: inline;
        }
        .sidebar:hover h4 span.full {
            display: inline;
        }
        .sidebar:hover h4 span.short {
            display: none;
        }

        /* Menu item */
        .sidebar a {
            color: #cbd5e1;
            text-decoration: none;
            display: flex;
            padding: 12px 18px;
            border-radius: 8px;
            white-space: nowrap;
            transition: all 0.25s ease;
            margin: 15px 7px;
        }

        .sidebar a:hover {
            background: linear-gradient(90deg, #2563eb 0%, #38bdf8 100%);
            color: #fff;
            box-shadow: 0 0 10px rgba(56, 189, 248, 0.4);
            transform: translateX(4px);
        }

        .sidebar a.active {
            background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
            color: #fff;
            margin-right: 5px;
            font-weight: 600;
            box-shadow: inset 2px 0 0 #38bdf8;
        }

        .sidebar i {
            font-size: 20px;

            text-align: center;
            transition: transform 0.2s ease;
        }

        .sidebar a:hover i { transform: scale(1.15); }

        .sidebar span {
            opacity: 0;
            margin-left: 10px;
            transition: opacity 0.3s ease;
        }

        .sidebar:hover span { opacity: 1; }

        /* Logout */
        .logout-section {
            padding: 15px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        .logout-btn {
            border: none;
            background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
            color: #fff;
            border-radius: 6px;
            padding: 10px 15px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
        }

        .logout-btn:hover {
            background: linear-gradient(90deg, #f87171 0%, #dc2626 100%);
            transform: scale(1.03);
            box-shadow: 0 0 10px rgba(239, 68, 68, 0.4);
        }

        /* Main */
        .main {
            margin-left: 70px;
            transition: margin-left 0.3s ease;
            padding: 20px;
        }

        .sidebar:hover ~ .main { margin-left: 220px; }

        /* Header */
        .header {
            background: #fff;
            padding: 10px 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 10;
        }

        .page-content {
            margin-top: 20px;
            background: #fff;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
        }
    </style>
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

        <a href="${pageContext.request.contextPath}/admin" class="${active=='dashboard'?'active':''}">
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
        <a href="${pageContext.request.contextPath}/admin/systems" class="${active=='systems'?'active':''}">
            <i class="bi bi-gear"></i><span>Cấu hình hệ thống</span>
        </a>
    </div>

    <!-- Logout -->
    <div class="logout-section">
        <a href="${pageContext.request.contextPath}/index.html" methods="get" style="margin: 0;">
            <i class="bi bi-box-arrow-right me-2"></i><span>Đăng xuất</span>
        </a>
    </div>
</div>

<!-- Main -->
<div class="main">
    <div class="header">
        <h5 class="m-0">${pageTitle != null ? pageTitle : "Bảng điều khiển"}</h5>
    </div>

    <div class="page-content">
        <jsp:include page="${content}" />
    </div>
</div>

</body>
</html>
