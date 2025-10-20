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
        .kyc-detail-container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 1rem;
        }
        
        .detail-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            border-radius: 12px 12px 0 0;
            text-align: center;
        }
        
        .detail-content {
            background: white;
            border-radius: 0 0 12px 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            padding: 2rem;
        }
        
        .info-section {
            margin-bottom: 2rem;
            padding-bottom: 1.5rem;
            border-bottom: 1px solid #e5e7eb;
        }
        
        .info-section:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }
        
        .section-title {
            font-size: 1.25rem;
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
        
        .info-card {
            background: #f9fafb;
            padding: 1rem;
            border-radius: 8px;
            border-left: 4px solid #6366f1;
        }
        
        .info-label {
            font-size: 0.85rem;
            font-weight: 500;
            color: #6b7280;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.25rem;
        }
        
        .info-value {
            font-size: 1rem;
            color: #111827;
            font-weight: 500;
        }
        
        .images-section {
            margin: 2rem 0;
        }
        
        .images-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-top: 1rem;
        }
        
        .image-card {
            background: white;
            border: 2px solid #e5e7eb;
            border-radius: 12px;
            padding: 1rem;
            text-align: center;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .image-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            border-color: #6366f1;
        }
        
        .image-title {
            font-weight: 600;
            color: #374151;
            margin-bottom: 0.75rem;
            font-size: 1rem;
        }
        
        .image-preview {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #d1d5db;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .image-preview:hover {
            transform: scale(1.02);
        }
        
        .no-image {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 200px;
            background: #f3f4f6;
            color: #9ca3af;
            border: 2px dashed #d1d5db;
            border-radius: 8px;
            font-size: 0.9rem;
        }
        
        .action-section {
            background: #f8fafc;
            padding: 2rem;
            border-radius: 12px;
            margin-top: 2rem;
            text-align: center;
        }
        
        .action-buttons {
            display: flex;
            justify-content: center;
            gap: 1rem;
            margin-top: 1rem;
        }
        
        .btn {
            padding: 0.75rem 2rem;
            border-radius: 8px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.2s;
            border: none;
            font-size: 1rem;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .btn-approve {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
        }
        
        .btn-approve:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
        }
        
        .btn-reject {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            color: white;
        }
        
        .btn-reject:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3);
        }
        
        .btn-back {
            background: #f3f4f6;
            color: #374151;
            border: 1px solid #d1d5db;
        }
        
        .btn-back:hover {
            background: #e5e7eb;
            transform: translateY(-1px);
        }
        
        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-size: 0.9rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        
        .status-pending {
            background: #fef3c7;
            color: #92400e;
            border: 1px solid #fbbf24;
        }
        
        .alert {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            border-left: 4px solid;
        }
        
        .alert-error {
            background: #fef2f2;
            border-color: #ef4444;
            color: #991b1b;
        }
        
        .verification-note {
            background: #fffbeb;
            border: 1px solid #f59e0b;
            border-radius: 8px;
            padding: 1.5rem;
            margin: 1rem 0;
        }
        
        .verification-title {
            font-weight: 600;
            color: #92400e;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .verification-checklist {
            color: #78350f;
            margin: 0;
            padding-left: 1.5rem;
        }
        
        .verification-checklist li {
            margin-bottom: 0.25rem;
        }
        
        /* Modal styles */
        .image-modal {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.9);
            z-index: 1000;
            display: none;
            justify-content: center;
            align-items: center;
            cursor: zoom-out;
        }
        
        .modal-content {
            position: relative;
            max-width: 90%;
            max-height: 90%;
        }
        
        .modal-image {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 8px;
        }
        
        .modal-close {
            position: absolute;
            top: -40px;
            right: -40px;
            background: white;
            border: none;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            cursor: pointer;
            font-size: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        @media (max-width: 768px) {
            .kyc-detail-container {
                padding: 0;
                margin: 1rem;
            }
            
            .images-grid {
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
    <%@include file="../Admin/_layout.jsp"%>
    
    <div class="kyc-detail-container">
        <div class="detail-header">
            <h1>🛡️ Chi tiết yêu cầu KYC</h1>
            <p>Xem xét thông tin xác minh danh tính của người dùng</p>
        </div>
        
        <div class="detail-content">
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <strong>Lỗi:</strong> ${error}
                </div>
            </c:if>
            
            <!-- Status -->
            <div class="info-section">
                <div class="section-title">
                    📊 Trạng thái yêu cầu
                </div>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <span class="status-badge status-pending">
                        ⏳ ${kycRequest.status}
                    </span>
                    <span style="color: #6b7280;">
                        Gửi lúc: <fmt:formatDate value="${kycRequest.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                    </span>
                </div>
            </div>
            
            <!-- Personal Information -->
            <div class="info-section">
                <div class="section-title">
                    🆔 Thông tin cá nhân (từ CCCD)
                </div>
                <div class="info-grid">
                    <div class="info-card">
                        <div class="info-label">Họ và tên đầy đủ</div>
                        <div class="info-value">${kycRequest.fullName}</div>
                    </div>
                    <div class="info-card">
                        <div class="info-label">Ngày sinh</div>
                        <div class="info-value">
                            <fmt:formatDate value="${kycRequest.dateOfBirth}" pattern="dd/MM/yyyy"/>
                        </div>
                    </div>
                    <div class="info-card">
                        <div class="info-label">Số CCCD/CMND</div>
                        <div class="info-value">${kycRequest.idNumber}</div>
                    </div>
                </div>
            </div>
            
            <!-- KYC Images -->
            <div class="images-section">
                <div class="section-title">
                    📷 Ảnh xác minh danh tính
                </div>
                
                <div class="verification-note">
                    <div class="verification-title">
                        ⚠️ Lưu ý khi kiểm tra
                    </div>
                    <ul class="verification-checklist">
                        <li>Kiểm tra thông tin trên CCCD có khớp với thông tin đã nhập</li>
                        <li>Ảnh CCCD phải rõ nét, đầy đủ 4 góc, không bị che khuất</li>
                        <li>Ảnh selfie phải thấy rõ mặt người, không được đeo khẩu trang</li>
                        <li>So sánh ảnh trên CCCD với ảnh selfie để xác nhận danh tính</li>
                    </ul>
                </div>
                
                <div class="images-grid">
                    <div class="image-card">
                        <div class="image-title">📄 CCCD mặt trước</div>
                        <c:choose>
                            <c:when test="${not empty frontIdImageUrl}">
                                <img src="${frontIdImageUrl}" alt="CCCD mặt trước" class="image-preview" 
                                     onclick="openImageModal(this.src, 'CCCD mặt trước')">
                            </c:when>
                            <c:otherwise>
                                <div class="no-image">❌ Không có ảnh</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div class="image-card">
                        <div class="image-title">📄 CCCD mặt sau</div>
                        <c:choose>
                            <c:when test="${not empty backIdImageUrl}">
                                <img src="${backIdImageUrl}" alt="CCCD mặt sau" class="image-preview" 
                                     onclick="openImageModal(this.src, 'CCCD mặt sau')">
                            </c:when>
                            <c:otherwise>
                                <div class="no-image">❌ Không có ảnh</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div class="image-card">
                        <div class="image-title">🤳 Ảnh selfie (chân dung)</div>
                        <c:choose>
                            <c:when test="${not empty selfieImageUrl}">
                                <img src="${selfieImageUrl}" alt="Ảnh selfie" class="image-preview" 
                                     onclick="openImageModal(this.src, 'Ảnh selfie')">
                            </c:when>
                            <c:otherwise>
                                <div class="no-image">❌ Không có ảnh</div>
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
                    <div class="info-card">
                        <div class="info-label">Tên gian hàng/doanh nghiệp</div>
                        <div class="info-value">${kycRequest.businessName}</div>
                    </div>
                </div>
                
                <div style="margin-top: 1rem;">
                    <div class="info-card">
                        <div class="info-label">Mô tả kinh doanh</div>
                        <div class="info-value">${kycRequest.businessDescription}</div>
                    </div>
                </div>
                
                <div style="margin-top: 1rem;">
                    <div class="info-card">
                        <div class="info-label">Kinh nghiệm bán hàng</div>
                        <div class="info-value">${kycRequest.experience}</div>
                    </div>
                </div>
                
                <div style="margin-top: 1rem;">
                    <div class="info-card">
                        <div class="info-label">Thông tin liên hệ</div>
                        <div class="info-value">${kycRequest.contactInfo}</div>
                    </div>
                </div>
            </div>
            
            <!-- Actions -->
            <c:if test='${kycRequest.status == "Pending"}'>
                <div class="action-section">
                    <h3>Quyết định xét duyệt</h3>
                    <p>Hãy xem xét kỹ lưỡng các thông tin và ảnh xác minh trước khi đưa ra quyết định.</p>
                    
                    <div class="action-buttons">
                        <form action="${pageContext.request.contextPath}/admin/kyc-requests" method="post" style="display: inline;">
                            <input type="hidden" name="action" value="approve">
                            <input type="hidden" name="id" value="${kycRequest.id}">
                            <input type="hidden" name="adminNotes" value="Đã xác minh thành công">
                            <button type="submit" class="btn btn-approve" 
                                    onclick="return confirm('Bạn có chắc chắn muốn DUYỆT yêu cầu KYC này? Người dùng sẽ trở thành seller.')">
                                ✅ Duyệt yêu cầu
                            </button>
                        </form>
                        
                        <a href="${pageContext.request.contextPath}/admin/kyc-requests?action=reject-form&id=${kycRequest.id}" 
                           class="btn btn-reject">
                            ❌ Từ chối
                        </a>
                        
                        <a href="${pageContext.request.contextPath}/admin/kyc-requests" class="btn btn-back">
                            ← Quay lại danh sách
                        </a>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    
    <!-- Image Modal -->
    <div id="imageModal" class="image-modal">
        <div class="modal-content">
            <button class="modal-close" onclick="closeImageModal()">×</button>
            <img id="modalImage" class="modal-image" alt="">
        </div>
    </div>
    
    <script>
        function openImageModal(src, title) {
            document.getElementById('modalImage').src = src;
            document.getElementById('modalImage').alt = title;
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
