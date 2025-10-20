<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - MMO Trader Market Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/Admin.css">
    <style>
        .kyc-management-container {
            padding: 2rem;
            max-width: 1400px;
            margin: 0 auto;
        }
        
        .page-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            border-radius: 12px;
            margin-bottom: 2rem;
            text-align: center;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        
        .stat-card {
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            text-align: center;
            border-left: 4px solid #667eea;
        }
        
        .stat-number {
            font-size: 2rem;
            font-weight: 600;
            color: #667eea;
        }
        
        .stat-label {
            color: #6b7280;
            font-size: 0.9rem;
            margin-top: 0.25rem;
        }
        
        .requests-table-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        
        .table-header {
            background: #f8fafc;
            padding: 1.5rem;
            border-bottom: 1px solid #e5e7eb;
        }
        
        .table-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #374151;
            margin: 0;
        }
        
        .requests-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .requests-table th,
        .requests-table td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }
        
        .requests-table th {
            background: #f9fafb;
            font-weight: 600;
            color: #374151;
            font-size: 0.9rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        
        .requests-table tbody tr:hover {
            background: #f9fafb;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        
        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
        }
        
        .user-details h4 {
            margin: 0;
            color: #374151;
            font-size: 0.95rem;
        }
        
        .user-details p {
            margin: 0;
            color: #6b7280;
            font-size: 0.85rem;
        }
        
        .status-badge {
            padding: 0.375rem 0.75rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        
        .status-pending {
            background: #fef3c7;
            color: #92400e;
            border: 1px solid #fbbf24;
        }
        
        .business-info {
            max-width: 200px;
        }
        
        .business-name {
            font-weight: 500;
            color: #374151;
            margin-bottom: 0.25rem;
        }
        
        .business-desc {
            color: #6b7280;
            font-size: 0.85rem;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        
        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }
        
        .btn {
            padding: 0.5rem 1rem;
            border-radius: 6px;
            text-decoration: none;
            font-size: 0.85rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
        }
        
        .btn-primary {
            background: #3b82f6;
            color: white;
        }
        
        .btn-primary:hover {
            background: #2563eb;
            transform: translateY(-1px);
        }
        
        .btn-danger {
            background: #ef4444;
            color: white;
        }
        
        .btn-danger:hover {
            background: #dc2626;
            transform: translateY(-1px);
        }
        
        .btn-secondary {
            background: #f3f4f6;
            color: #374151;
            border: 1px solid #d1d5db;
        }
        
        .btn-secondary:hover {
            background: #e5e7eb;
        }
        
        .empty-state {
            text-align: center;
            padding: 4rem 2rem;
            color: #6b7280;
        }
        
        .empty-icon {
            font-size: 4rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }
        
        .alert {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border-left: 4px solid;
        }
        
        .alert-success {
            background: #f0f9ff;
            border-color: #0ea5e9;
            color: #0c4a6e;
        }
        
        .alert-error {
            background: #fef2f2;
            border-color: #ef4444;
            color: #991b1b;
        }
        
        @media (max-width: 768px) {
            .kyc-management-container {
                padding: 1rem;
            }
            
            .requests-table {
                font-size: 0.85rem;
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .stats-grid {
                grid-template-columns: 1fr 1fr;
            }
        }
    </style>
</head>
<body>
    <%@include file="../Admin/_layout.jsp"%>
    
    <div class="kyc-management-container">
        <div class="page-header">
            <h1>🛡️ Quản lý yêu cầu KYC</h1>
            <p>Xem xét và duyệt các yêu cầu xác minh danh tính để trở thành seller</p>
        </div>
        
        <!-- Statistics -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number">${pendingRequests.size()}</div>
                <div class="stat-label">Yêu cầu chờ duyệt</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">0</div>
                <div class="stat-label">Đã duyệt hôm nay</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">0</div>
                <div class="stat-label">Từ chối hôm nay</div>
            </div>
            <div class="stat-card">
                <div class="stat-number">0</div>
                <div class="stat-label">Tổng seller active</div>
            </div>
        </div>
        
        <!-- Alerts -->
        <c:if test="${not empty success}">
            <div class="alert alert-success">
                <strong>Thành công:</strong> ${success}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <strong>Lỗi:</strong> ${error}
            </div>
        </c:if>
        
        <!-- Requests Table -->
        <div class="requests-table-container">
            <div class="table-header">
                <h2 class="table-title">Danh sách yêu cầu KYC đang chờ</h2>
            </div>
            
            <c:choose>
                <c:when test="${empty pendingRequests}">
                    <div class="empty-state">
                        <div class="empty-icon">📭</div>
                        <h3>Không có yêu cầu KYC nào</h3>
                        <p>Hiện tại không có yêu cầu xác minh danh tính nào cần xử lý.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="requests-table">
                        <thead>
                            <tr>
                                <th>Người dùng</th>
                                <th>Thông tin CCCD</th>
                                <th>Thông tin kinh doanh</th>
                                <th>Ngày gửi</th>
                                <th>Trạng thái</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${pendingRequests}" var="request">
                                <tr>
                                    <td>
                                        <div class="user-info">
                                            <div class="user-avatar">
                                                ${request.user.name.substring(0, 1).toUpperCase()}
                                            </div>
                                            <div class="user-details">
                                                <h4>${request.user.name}</h4>
                                                <p>${request.user.email}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div>
                                            <strong>${request.request.fullName}</strong><br>
                                            <small>CCCD: ${request.request.idNumber}</small><br>
                                            <small>Sinh: <fmt:formatDate value="${request.request.dateOfBirth}" pattern="dd/MM/yyyy"/></small>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="business-info">
                                            <div class="business-name">${request.request.businessName}</div>
                                            <div class="business-desc">${request.request.businessDescription}</div>
                                        </div>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${request.request.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </td>
                                    <td>
                                        <span class="status-badge status-pending">
                                            ⏳ Chờ duyệt
                                        </span>
                                    </td>
                                    <td>
                                        <div class="action-buttons">
                                            <a href="${pageContext.request.contextPath}/admin/kyc-requests?action=detail&id=${request.request.id}" 
                                               class="btn btn-primary">
                                                👀 Xem chi tiết
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
