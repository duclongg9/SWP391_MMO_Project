<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .kyc-container {
            max-width: 900px;
            margin: 2rem auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        
        .status-header {
            padding: 2rem;
            text-align: center;
        }
        
        .status-pending {
            background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
            color: white;
        }
        
        .status-approved {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
        }
        
        .status-rejected {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            color: white;
        }
        
        .status-icon {
            font-size: 3rem;
            margin-bottom: 0.5rem;
        }
        
        .status-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }
        
        .status-message {
            font-size: 1.1rem;
            opacity: 0.9;
        }
        
        .kyc-content {
            padding: 2rem;
        }
        
        .info-section {
            margin-bottom: 2rem;
            border-bottom: 1px solid #e5e7eb;
            padding-bottom: 1.5rem;
        }
        
        .info-section:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }
        
        .section-title {
            font-size: 1.2rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
        }
        
        .info-item {
            padding: 1rem;
            background: #f9fafb;
            border-radius: 8px;
            border-left: 4px solid #6366f1;
        }
        
        .info-label {
            font-weight: 500;
            color: #6b7280;
            font-size: 0.9rem;
            margin-bottom: 0.25rem;
        }
        
        .info-value {
            color: #111827;
            font-weight: 500;
        }
        
        .image-gallery {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-top: 1rem;
        }
        
        .image-card {
            background: #f9fafb;
            border-radius: 8px;
            padding: 1rem;
            text-align: center;
            border: 2px solid #e5e7eb;
            transition: transform 0.2s;
        }
        
        .image-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .image-label {
            font-weight: 500;
            color: #374151;
            margin-bottom: 0.5rem;
        }
        
        .image-preview {
            width: 100%;
            height: 150px;
            object-fit: cover;
            border-radius: 6px;
            border: 1px solid #d1d5db;
        }
        
        .no-image {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 150px;
            background: #f3f4f6;
            color: #9ca3af;
            border: 2px dashed #d1d5db;
            border-radius: 6px;
        }
        
        .rejection-reason {
            background: #fee2e2;
            border: 1px solid #fca5a5;
            border-radius: 8px;
            padding: 1rem;
            margin-top: 1rem;
        }
        
        .rejection-title {
            font-weight: 600;
            color: #dc2626;
            margin-bottom: 0.5rem;
        }
        
        .rejection-text {
            color: #7f1d1d;
            line-height: 1.5;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            justify-content: center;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s;
            border: none;
            font-size: 1rem;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
            color: white;
        }
        
        .btn-primary:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }
        
        .btn-secondary {
            background: #f3f4f6;
            color: #374151;
            border: 1px solid #d1d5db;
        }
        
        .btn-secondary:hover {
            background: #e5e7eb;
        }
        
        .timeline {
            position: relative;
            margin: 1rem 0;
        }
        
        .timeline-item {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-bottom: 0.5rem;
        }
        
        .timeline-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            flex-shrink: 0;
        }
        
        .timeline-dot.completed {
            background: #10b981;
        }
        
        .timeline-dot.current {
            background: #f59e0b;
        }
        
        .timeline-dot.pending {
            background: #d1d5db;
        }
        
        .timeline-text {
            color: #374151;
        }
        
        @media (max-width: 768px) {
            .kyc-container {
                margin: 1rem;
            }
            
            .image-gallery {
                grid-template-columns: 1fr;
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .info-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <%@include file="../_layout.jsp"%>
    
    <div class="main-content">
        <div class="kyc-container">
            <!-- Status Header -->
            <div class="status-header 
                <c:choose>
                    <c:when test='${kycRequest.status == "Pending"}'>status-pending</c:when>
                    <c:when test='${kycRequest.status == "Approved"}'>status-approved</c:when>
                    <c:when test='${kycRequest.status == "Rejected"}'>status-rejected</c:when>
                </c:choose>
            ">
                <div class="status-icon">
                    <c:choose>
                        <c:when test='${kycRequest.status == "Pending"}'>⏳</c:when>
                        <c:when test='${kycRequest.status == "Approved"}'>✅</c:when>
                        <c:when test='${kycRequest.status == "Rejected"}'>❌</c:when>
                    </c:choose>
                </div>
                <div class="status-title">
                    <c:choose>
                        <c:when test='${kycRequest.status == "Pending"}'>Đang chờ xét duyệt</c:when>
                        <c:when test='${kycRequest.status == "Approved"}'>Đã được duyệt</c:when>
                        <c:when test='${kycRequest.status == "Rejected"}'>Bị từ chối</c:when>
                    </c:choose>
                </div>
                <div class="status-message">
                    <c:choose>
                        <c:when test='${kycRequest.status == "Pending"}'>
                            Yêu cầu KYC của bạn đang được admin xem xét. Vui lòng chờ trong vòng 1-2 ngày làm việc.
                        </c:when>
                        <c:when test='${kycRequest.status == "Approved"}'>
                            Chúc mừng! Bạn đã trở thành seller. Bây giờ bạn có thể tạo gian hàng và bán sản phẩm.
                        </c:when>
                        <c:when test='${kycRequest.status == "Rejected"}'>
                            Yêu cầu KYC của bạn không được chấp nhận. Vui lòng xem lý do bên dưới.
                        </c:when>
                    </c:choose>
                </div>
            </div>
            
            <div class="kyc-content">
                <!-- Progress Timeline -->
                <div class="info-section">
                    <div class="section-title">
                        📋 Tiến trình xử lý
                    </div>
                    <div class="timeline">
                        <div class="timeline-item">
                            <div class="timeline-dot completed"></div>
                            <div class="timeline-text">Gửi yêu cầu KYC - 
                                <fmt:formatDate value="${kycRequest.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </div>
                        </div>
                        <div class="timeline-item">
                            <div class="timeline-dot 
                                <c:choose>
                                    <c:when test='${kycRequest.status == "Pending"}'>current</c:when>
                                    <c:otherwise>completed</c:otherwise>
                                </c:choose>
                            "></div>
                            <div class="timeline-text">Admin xem xét
                                <c:if test='${kycRequest.status != "Pending"}'>
                                    - <fmt:formatDate value="${kycRequest.reviewedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:if>
                            </div>
                        </div>
                        <div class="timeline-item">
                            <div class="timeline-dot 
                                <c:choose>
                                    <c:when test='${kycRequest.status == "Approved"}'>completed</c:when>
                                    <c:otherwise>pending</c:otherwise>
                                </c:choose>
                            "></div>
                            <div class="timeline-text">Trở thành seller</div>
                        </div>
                    </div>
                </div>
                
                <!-- Personal Information -->
                <div class="info-section">
                    <div class="section-title">
                        🆔 Thông tin cá nhân
                    </div>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="info-label">Họ và tên</div>
                            <div class="info-value">${kycRequest.fullName}</div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">Ngày sinh</div>
                            <div class="info-value">
                                <fmt:formatDate value="${kycRequest.dateOfBirth}" pattern="dd/MM/yyyy"/>
                            </div>
                        </div>
                        <div class="info-item">
                            <div class="info-label">Số CCCD/CMND</div>
                            <div class="info-value">${kycRequest.idNumber}</div>
                        </div>
                    </div>
                </div>
                
                <!-- KYC Images -->
                <div class="info-section">
                    <div class="section-title">
                        📷 Ảnh xác minh
                    </div>
                    <div class="image-gallery">
                        <div class="image-card">
                            <div class="image-label">CCCD mặt trước</div>
                            <c:choose>
                                <c:when test="${not empty frontIdImageUrl}">
                                    <img src="${frontIdImageUrl}" alt="CCCD mặt trước" class="image-preview" 
                                         onclick="openImageModal(this.src)">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-image">Không có ảnh</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="image-card">
                            <div class="image-label">CCCD mặt sau</div>
                            <c:choose>
                                <c:when test="${not empty backIdImageUrl}">
                                    <img src="${backIdImageUrl}" alt="CCCD mặt sau" class="image-preview" 
                                         onclick="openImageModal(this.src)">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-image">Không có ảnh</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <div class="image-card">
                            <div class="image-label">Ảnh selfie</div>
                            <c:choose>
                                <c:when test="${not empty selfieImageUrl}">
                                    <img src="${selfieImageUrl}" alt="Ảnh selfie" class="image-preview" 
                                         onclick="openImageModal(this.src)">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-image">Không có ảnh</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
                
                <!-- Business Information -->
                <div class="info-section">
                    <div class="section-title">
                        🏪 Thông tin kinh doanh
                    </div>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="info-label">Tên gian hàng</div>
                            <div class="info-value">${kycRequest.businessName}</div>
                        </div>
                    </div>
                    
                    <div style="margin-top: 1rem;">
                        <div class="info-item">
                            <div class="info-label">Mô tả kinh doanh</div>
                            <div class="info-value">${kycRequest.businessDescription}</div>
                        </div>
                    </div>
                    
                    <div style="margin-top: 1rem;">
                        <div class="info-item">
                            <div class="info-label">Kinh nghiệm</div>
                            <div class="info-value">${kycRequest.experience}</div>
                        </div>
                    </div>
                    
                    <div style="margin-top: 1rem;">
                        <div class="info-item">
                            <div class="info-label">Thông tin liên hệ</div>
                            <div class="info-value">${kycRequest.contactInfo}</div>
                        </div>
                    </div>
                </div>
                
                <!-- Rejection Reason (if rejected) -->
                <c:if test='${kycRequest.status == "Rejected" && not empty kycRequest.rejectionReason}'>
                    <div class="info-section">
                        <div class="rejection-reason">
                            <div class="rejection-title">📝 Lý do từ chối:</div>
                            <div class="rejection-text">${kycRequest.rejectionReason}</div>
                        </div>
                    </div>
                </c:if>
                
                <!-- Action Buttons -->
                <div class="action-buttons">
                    <c:choose>
                        <c:when test='${kycRequest.status == "Approved"}'>
                            <a href="${pageContext.request.contextPath}/seller/shop" class="btn btn-primary">
                                🏪 Tạo gian hàng
                            </a>
                        </c:when>
                        <c:when test='${kycRequest.status == "Rejected"}'>
                            <a href="${pageContext.request.contextPath}/user/kyc-request-new" class="btn btn-primary">
                                🔄 Gửi yêu cầu mới
                            </a>
                        </c:when>
                    </c:choose>
                    
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">
                        🏠 Về trang chủ
                    </a>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Image Modal -->
    <div id="imageModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
         background: rgba(0,0,0,0.8); z-index: 1000; justify-content: center; align-items: center;">
        <div style="position: relative; max-width: 90%; max-height: 90%;">
            <img id="modalImage" style="max-width: 100%; max-height: 100%; object-fit: contain;">
            <button onclick="closeImageModal()" 
                    style="position: absolute; top: -10px; right: -10px; background: white; 
                           border: none; border-radius: 50%; width: 30px; height: 30px; 
                           cursor: pointer; font-size: 18px;">×</button>
        </div>
    </div>
    
    <script>
        function openImageModal(src) {
            document.getElementById('modalImage').src = src;
            document.getElementById('imageModal').style.display = 'flex';
        }
        
        function closeImageModal() {
            document.getElementById('imageModal').style.display = 'none';
        }
        
        // Close modal when clicking outside image
        document.getElementById('imageModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeImageModal();
            }
        });
        
        // Close modal with Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeImageModal();
            }
        });
    </script>
</body>
</html>
