<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<style>
        .options-container {
            max-width: 900px;
            margin: 2rem auto;
            padding: 0 1rem;
        }
        
        .page-header {
            text-align: center;
            margin-bottom: 3rem;
            padding: 2rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 12px;
        }
        
        .page-header h1 {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        
        .kyc-intro-card {
            background: linear-gradient(135deg, #f0f9ff 0%, #f0fff4 100%);
            border-radius: 16px;
            padding: 2.5rem;
            margin-bottom: 3rem;
            border: 2px solid #10b981;
            box-shadow: 0 8px 32px rgba(16, 185, 129, 0.1);
        }
        
        .intro-content {
            display: flex;
            align-items: center;
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .intro-icon {
            font-size: 4rem;
            line-height: 1;
        }
        
        .intro-text h3 {
            font-size: 1.75rem;
            color: #065f46;
            margin: 0 0 0.5rem 0;
        }
        
        .intro-text p {
            color: #047857;
            font-size: 1.1rem;
            margin: 0;
        }
        
        .requirements-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .requirement-item {
            background: white;
            padding: 1.5rem;
            border-radius: 12px;
            display: flex;
            align-items: center;
            gap: 1rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
            border: 1px solid #d1fae5;
        }
        
        .req-icon {
            font-size: 2.5rem;
            line-height: 1;
        }
        
        .req-content h4 {
            margin: 0 0 0.25rem 0;
            color: #065f46;
            font-size: 1.1rem;
        }
        
        .req-content p {
            margin: 0;
            color: #6b7280;
            font-size: 0.9rem;
        }
        
        .cta-section {
            text-align: center;
            padding: 1.5rem 0;
        }
        
        .main-cta-button {
            display: inline-block;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            padding: 1rem 3rem;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 600;
            font-size: 1.2rem;
            transition: all 0.3s;
            box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
        }
        
        .main-cta-button:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
        }
        
        .cta-note {
            margin-top: 0.75rem;
            color: #065f46;
            font-size: 0.9rem;
        }
        
        .benefits-section {
            background: white;
            border-radius: 16px;
            padding: 2rem;
            margin-bottom: 2rem;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
        }
        
        .benefits-title {
            text-align: center;
            font-size: 1.5rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 2rem;
        }
        
        .benefits-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
        }
        
        .benefit-card {
            text-align: center;
            padding: 1.5rem;
            background: #f8fafc;
            border-radius: 12px;
            border: 1px solid #e5e7eb;
            transition: transform 0.2s;
        }
        
        .benefit-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .benefit-icon {
            font-size: 2.5rem;
            margin-bottom: 1rem;
        }
        
        .benefit-card h4 {
            margin: 0 0 0.5rem 0;
            color: #374151;
        }
        
        .benefit-card p {
            margin: 0;
            color: #6b7280;
            font-size: 0.9rem;
        }
        
        .option-card {
            background: white;
            border-radius: 12px;
            padding: 2rem;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            border: 3px solid #e5e7eb;
            text-align: center;
            transition: all 0.3s;
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }
        
        .option-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
            border-color: #6366f1;
        }
        
        .option-card.recommended {
            border-color: #10b981;
            background: linear-gradient(135deg, #f0fff4 0%, #f0fff4 100%);
        }
        
        .option-card.recommended::before {
            content: "KHUYẾN NGHỊ";
            position: absolute;
            top: 1rem;
            right: -2rem;
            background: #10b981;
            color: white;
            padding: 0.25rem 3rem;
            font-size: 0.75rem;
            font-weight: 600;
            transform: rotate(45deg);
            letter-spacing: 0.05em;
        }
        
        .option-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
        }
        
        .option-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 1rem;
        }
        
        .option-description {
            color: #6b7280;
            line-height: 1.6;
            margin-bottom: 1.5rem;
        }
        
        .option-features {
            list-style: none;
            padding: 0;
            margin: 1rem 0;
            text-align: left;
        }
        
        .option-features li {
            padding: 0.5rem 0;
            color: #374151;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .option-features li::before {
            content: "✅";
            font-size: 1rem;
        }
        
        .option-button {
            background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
            color: white;
            padding: 0.75rem 2rem;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s;
            border: none;
            cursor: pointer;
            font-size: 1rem;
        }
        
        .option-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
        }
        
        .option-card.recommended .option-button {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }
        
        .comparison-section {
            background: #f8fafc;
            border-radius: 12px;
            padding: 2rem;
            margin: 2rem 0;
        }
        
        .comparison-title {
            text-align: center;
            font-size: 1.5rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 1.5rem;
        }
        
        .comparison-table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .comparison-table th,
        .comparison-table td {
            padding: 1rem;
            text-align: center;
            border-bottom: 1px solid #e5e7eb;
        }
        
        .comparison-table th {
            background: #f9fafb;
            font-weight: 600;
            color: #374151;
        }
        
        .comparison-table tr:last-child td {
            border-bottom: none;
        }
        
        .pro {
            color: #10b981;
            font-weight: 500;
        }
        
        .con {
            color: #ef4444;
            font-weight: 500;
        }
        
        .note-section {
            background: #fffbeb;
            border: 1px solid #f59e0b;
            border-radius: 8px;
            padding: 1.5rem;
            margin: 2rem 0;
        }
        
        .note-title {
            color: #92400e;
            font-weight: 600;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .note-content {
            color: #78350f;
            line-height: 1.6;
        }
        
        @media (max-width: 768px) {
            .kyc-intro-card {
                padding: 1.5rem;
            }
            
            .intro-content {
                flex-direction: column;
                text-align: center;
            }
            
            .requirements-grid {
                grid-template-columns: 1fr;
            }
            
            .requirement-item {
                flex-direction: column;
                text-align: center;
            }
            
            .benefits-grid {
                grid-template-columns: 1fr;
            }
            
            .main-cta-button {
                padding: 1rem 2rem;
                font-size: 1.1rem;
            }
        }
</style>

<main class="layout__content">
    <div class="options-container">
        <div class="page-header">
            <h1>🛡️ Xác minh KYC - Trở thành Seller</h1>
            <p>Hoàn thành xác minh danh tính để bắt đầu bán hàng</p>
        </div>
        
        <div class="kyc-intro-card">
            <div class="intro-content">
                <div class="intro-icon">🎯</div>
                <div class="intro-text">
                    <h3>Yêu cầu để trở thành Seller</h3>
                    <p>Để đảm bảo an toàn và uy tín cho cộng đồng, bạn cần hoàn thành các bước sau:</p>
                </div>
            </div>
            
            <div class="requirements-grid">
                <div class="requirement-item">
                    <div class="req-icon">🆔</div>
                    <div class="req-content">
                        <h4>Xác minh danh tính</h4>
                        <p>Upload CCCD mặt trước/sau + ảnh selfie</p>
                    </div>
                </div>
                
                <div class="requirement-item">
                    <div class="req-icon">🏪</div>
                    <div class="req-content">
                        <h4>Thông tin cửa hàng</h4>
                        <p>Tên cửa hàng, mô tả kinh doanh</p>
                    </div>
                </div>
                
                <div class="requirement-item">
                    <div class="req-icon">📞</div>
                    <div class="req-content">
                        <h4>Thông tin liên hệ</h4>
                        <p>SĐT, Facebook, Zalo để khách hàng liên hệ</p>
                    </div>
                </div>
            </div>
            
            <div class="cta-section">
                <a href="${pageContext.request.contextPath}/user/kyc-request" class="main-cta-button">
                    🚀 Bắt đầu xác minh KYC
                </a>
                <p class="cta-note">Quá trình xác minh chỉ mất 5-10 phút</p>
            </div>
        </div>
        
 
        
        <div class="note-section">
            <div class="note-title">
                ⚠️ Lưu ý quan trọng
            </div>
            <div class="note-content">
                <p><strong>Bảo mật thông tin:</strong> Thông tin CCCD được mã hóa và lưu trữ an toàn, chỉ admin mới có thể xem để xác minh danh tính.</p>
                <p><strong>Thời gian xử lý:</strong> Admin sẽ xem xét yêu cầu của bạn trong vòng 1-2 ngày làm việc.</p>
                <p><strong>Yêu cầu ảnh:</strong> Ảnh CCCD và selfie phải rõ nét, không bị mờ hoặc che khuất.</p>
                <p><strong>Thông tin chính xác:</strong> Vui lòng đảm bảo thông tin nhập vào khớp với CCCD để tránh bị từ chối.</p>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
