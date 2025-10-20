<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<style>
        .kyc-container {
            max-width: 800px;
            margin: 2rem auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        
        .kyc-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            text-align: center;
        }
        
        .kyc-form {
            padding: 2rem;
        }
        
        .form-section {
            margin-bottom: 2rem;
            border-bottom: 1px solid #eee;
            padding-bottom: 1.5rem;
        }
        
        .form-section:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }
        
        .section-title {
            font-size: 1.2rem;
            font-weight: 600;
            color: #333;
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .form-group {
            margin-bottom: 1rem;
        }
        
        .form-label {
            display: block;
            font-weight: 500;
            color: #555;
            margin-bottom: 0.5rem;
        }
        
        .form-label.required::after {
            content: " *";
            color: #e74c3c;
        }
        
        .form-input,
        .form-textarea,
        .form-file {
            width: 100%;
            padding: 0.75rem;
            border: 2px solid #e1e5e9;
            border-radius: 8px;
            font-size: 1rem;
            transition: border-color 0.3s;
        }
        
        .form-input:focus,
        .form-textarea:focus,
        .form-file:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .form-textarea {
            resize: vertical;
            min-height: 100px;
        }
        
        .file-upload-area {
            border: 2px dashed #d1d5db;
            border-radius: 8px;
            padding: 1.5rem;
            text-align: center;
            background: #f9fafb;
            transition: all 0.3s;
        }
        
        .file-upload-area:hover {
            border-color: #667eea;
            background: #f0f4ff;
        }
        
        .file-upload-icon {
            font-size: 2rem;
            margin-bottom: 0.5rem;
            color: #9ca3af;
        }
        
        .file-upload-text {
            color: #6b7280;
            margin-bottom: 0.5rem;
        }
        
        .file-requirements {
            font-size: 0.85rem;
            color: #9ca3af;
        }
        
        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }
        
        .alert {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
        }
        
        .alert-error {
            background: #fee2e2;
            border: 1px solid #fca5a5;
            color: #dc2626;
        }
        
        .alert-info {
            background: #dbeafe;
            border: 1px solid #93c5fd;
            color: #1d4ed8;
        }
        
        .submit-button {
            width: 100%;
            padding: 1rem 2rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .submit-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
        }
        
        .submit-button:disabled {
            background: #9ca3af;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }
        
        @media (max-width: 768px) {
            .form-grid {
                grid-template-columns: 1fr;
            }
            
            .kyc-container {
                margin: 1rem;
                border-radius: 8px;
            }
            
            .kyc-header,
            .kyc-form {
                padding: 1.5rem;
            }
        }
</style>

<main class="layout__content">
    <div class="kyc-container">
        <div class="kyc-header">
            <h1>🛡️ Xác minh danh tính (KYC)</h1>
            <p>Để trở thành seller, bạn cần hoàn thành xác minh danh tính</p>
        </div>
        
        <div class="kyc-form">
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <strong>Lỗi:</strong> ${error}
                </div>
            </c:if>
            
            <div class="alert alert-info">
                <strong>Lưu ý quan trọng:</strong>
                <ul style="margin: 0.5rem 0 0 1rem;">
                    <li>Vui lòng chuẩn bị CCCD và chụp ảnh selfie rõ nét</li>
                    <li>Thông tin phải trùng khớp với CCCD</li>
                    <li>Admin sẽ xem xét trong vòng 1-2 ngày làm việc</li>
                    <li>File ảnh tối đa 5MB, định dạng JPG/PNG</li>
                </ul>
            </div>
                
                <form action="${pageContext.request.contextPath}/user/kyc-request" method="post" enctype="multipart/form-data" id="kycForm">
                    <!-- Thông tin cá nhân từ CCCD -->
                    <div class="form-section">
                        <div class="section-title">
                            🆔 Thông tin cá nhân (theo CCCD)
                        </div>
                        
                        <div class="form-group">
                            <label for="fullName" class="form-label required">Họ và tên đầy đủ</label>
                            <input type="text" id="fullName" name="fullName" class="form-input" 
                                   value="${fullName}" placeholder="Nguyễn Văn A" required maxlength="50">
                        </div>
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="dateOfBirth" class="form-label required">Ngày sinh</label>
                                <input type="date" id="dateOfBirth" name="dateOfBirth" class="form-input" 
                                       value="${dateOfBirth}" required>
                            </div>
                            
                            <div class="form-group">
                                <label for="idNumber" class="form-label required">Số CCCD/CMND</label>
                                <input type="text" id="idNumber" name="idNumber" class="form-input" 
                                       value="${idNumber}" placeholder="123456789012" required 
                                       pattern="[0-9]{9,12}" title="Số CCCD/CMND phải từ 9-12 chữ số">
                            </div>
                        </div>
                    </div>
                    
                    <!-- Upload ảnh CCCD và selfie -->
                    <div class="form-section">
                        <div class="section-title">
                            📷 Upload ảnh xác minh
                        </div>
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="frontIdImage" class="form-label required">Ảnh CCCD mặt trước</label>
                                <div class="file-upload-area">
                                    <div class="file-upload-icon">🪪</div>
                                    <input type="file" id="frontIdImage" name="frontIdImage" class="form-file" 
                                           accept="image/jpeg,image/jpg,image/png" required>
                                    <div class="file-requirements">JPG, PNG tối đa 5MB</div>
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="backIdImage" class="form-label required">Ảnh CCCD mặt sau</label>
                                <div class="file-upload-area">
                                    <div class="file-upload-icon">🪪</div>
                                    <input type="file" id="backIdImage" name="backIdImage" class="form-file" 
                                           accept="image/jpeg,image/jpg,image/png" required>
                                    <div class="file-requirements">JPG, PNG tối đa 5MB</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="selfieImage" class="form-label required">Ảnh selfie (chụp mặt)</label>
                            <div class="file-upload-area">
                                <div class="file-upload-icon">🤳</div>
                                <input type="file" id="selfieImage" name="selfieImage" class="form-file" 
                                       accept="image/jpeg,image/jpg,image/png" required>
                                <div class="file-requirements">Ảnh chân dung rõ mặt, JPG/PNG tối đa 5MB</div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Thông tin cửa hàng -->
                    <div class="form-section">
                        <div class="section-title">
                            🏪 Thông tin cửa hàng
                        </div>
                        
                        <div class="form-group">
                            <label for="businessType" class="form-label required">Loại hình kinh doanh</label>
                            <select id="businessType" name="businessType" class="form-input" required>
                                <option value="">Chọn loại hình kinh doanh</option>
                                <option value="individual" ${businessType == 'individual' ? 'selected' : ''}>Cá nhân</option>
                                <option value="company" ${businessType == 'company' ? 'selected' : ''}>Doanh nghiệp/Công ty</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="businessName" class="form-label required">Tên cửa hàng/Doanh nghiệp</label>
                            <input type="text" id="businessName" name="businessName" class="form-input" 
                                   value="${businessName}" placeholder="VD: Game Store Pro, Công ty TNHH ABC..." required 
                                   maxlength="100">
                            <small class="form-help">Tên này sẽ hiển thị trên cửa hàng của bạn</small>
                        </div>
                        
                        <div class="form-group">
                            <label for="businessDescription" class="form-label required">Mô tả kinh doanh</label>
                            <textarea id="businessDescription" name="businessDescription" class="form-textarea" 
                                      placeholder="Mô tả chi tiết về các sản phẩm/dịch vụ bạn định bán, thị trường mục tiêu..." 
                                      required maxlength="1000">${businessDescription}</textarea>
                        </div>
                        
                        <div class="form-group">
                            <label for="experience" class="form-label required">Kinh nghiệm bán hàng</label>
                            <textarea id="experience" name="experience" class="form-textarea" 
                                      placeholder="Chia sẻ kinh nghiệm bán hàng, các kênh đã từng bán, thành tích..." 
                                      required maxlength="500">${experience}</textarea>
                        </div>
                    </div>
                    
                    <!-- Thông tin liên hệ -->
                    <div class="form-section">
                        <div class="section-title">
                            📞 Thông tin liên hệ
                        </div>
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="phoneNumber" class="form-label required">Số điện thoại</label>
                                <input type="tel" id="phoneNumber" name="phoneNumber" class="form-input" 
                                       value="${phoneNumber}" placeholder="0987654321" required 
                                       pattern="[0-9]{10,11}" title="Số điện thoại phải có 10-11 chữ số">
                            </div>
                            
                            <div class="form-group">
                                <label for="email" class="form-label">Email liên hệ</label>
                                <input type="email" id="email" name="email" class="form-input" 
                                       value="${email}" placeholder="shop@example.com" maxlength="100">
                            </div>
                        </div>
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="facebookLink" class="form-label">Link Facebook</label>
                                <input type="url" id="facebookLink" name="facebookLink" class="form-input" 
                                       value="${facebookLink}" placeholder="https://facebook.com/yourshop" maxlength="200">
                            </div>
                            
                            <div class="form-group">
                                <label for="zaloNumber" class="form-label">Số Zalo</label>
                                <input type="tel" id="zaloNumber" name="zaloNumber" class="form-input" 
                                       value="${zaloNumber}" placeholder="0987654321" 
                                       pattern="[0-9]{10,11}" title="Số Zalo phải có 10-11 chữ số">
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="otherContacts" class="form-label">Kênh liên hệ khác (nếu có)</label>
                            <textarea id="otherContacts" name="otherContacts" class="form-textarea" 
                                      placeholder="Discord, Telegram, Website, địa chỉ cửa hàng..." 
                                      maxlength="300">${otherContacts}</textarea>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-button" id="submitBtn">
                        🚀 Gửi yêu cầu xác minh
                    </button>
                </form>
            </div>
        </div>
    </div>
    
    <script>
        // Validate file sizes
        function validateFileSize(input, maxSize = 5 * 1024 * 1024) {
            if (input.files && input.files[0]) {
                if (input.files[0].size > maxSize) {
                    alert('File quá lớn! Vui lòng chọn file nhỏ hơn 5MB.');
                    input.value = '';
                    return false;
                }
            }
            return true;
        }
        
        // Add file validation
        document.querySelectorAll('input[type="file"]').forEach(input => {
            input.addEventListener('change', function() {
                validateFileSize(this);
            });
        });
        
        // Form submission
        document.getElementById('kycForm').addEventListener('submit', function(e) {
            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.textContent = '🔄 Đang xử lý...';
        });
        
        // Character count for textareas
        document.querySelectorAll('textarea').forEach(textarea => {
            const maxLength = textarea.getAttribute('maxlength');
            if (maxLength) {
                const counter = document.createElement('div');
                counter.style.fontSize = '0.85rem';
                counter.style.color = '#6b7280';
                counter.style.textAlign = 'right';
                counter.style.marginTop = '0.25rem';
                
                function updateCounter() {
                    const remaining = maxLength - textarea.value.length;
                    counter.textContent = `${textarea.value.length}/${maxLength} ký tự`;
                    counter.style.color = remaining < 50 ? '#dc2626' : '#6b7280';
                }
                
                textarea.addEventListener('input', updateCounter);
                textarea.parentNode.appendChild(counter);
                updateCounter();
            }
        });
    </script>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
