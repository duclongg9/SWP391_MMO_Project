<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("bodyClass", "layout");
    request.setAttribute("headerModifier", "layout__header--split");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content seller-page">
    <c:if test="${not empty errorMessage}">
        <div style="background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
            ${errorMessage}
        </div>
    </c:if>
    
    <c:if test="${not empty errors}">
        <div style="background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
            <ul style="margin: 0; padding-left: 1.5rem;">
                <c:forEach var="error" items="${errors}">
                    <li>${error}</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/seller/products/create" method="post" enctype="multipart/form-data">
        <c:if test="${not empty selectedShopId}">
            <input type="hidden" name="shopId" value="${selectedShopId}" />
        </c:if>
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Thông tin sản phẩm</h2>
            <p class="panel__subtitle">Bổ sung mô tả chi tiết, giá bán và ảnh minh hoạ trước khi đăng bán.</p>
        </div>
            <c:if test="${not empty shop}">
                <div style="padding: 1rem 1.5rem 0; font-weight: 600; color: #005b96;">
                    Đăng bán tại shop: <span><c:out value="${shop.name}"/></span>
                </div>
            </c:if>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; padding: 1.5rem;">
                <!-- Cột trái -->
                <div>
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-name">
                            Tên sản phẩm <span style="color: red;">*</span>
                        </label>
                        <input class="form-input" type="text" id="product-name" name="productName" 
                               placeholder="Ví dụ: Gmail Doanh nghiệp 100GB" 
                               value="${productName}" required maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-type">
                            Loại sản phẩm <span style="color: red;">*</span>
                        </label>
                        <select class="form-input" id="product-type" name="productType" required
                                style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                            <option value="">-- Chọn loại --</option>
                            <option value="EMAIL" ${productType == 'EMAIL' ? 'selected' : ''}>Email</option>
                            <option value="SOCIAL" ${productType == 'SOCIAL' ? 'selected' : ''}>Mạng xã hội</option>
                            <option value="GAME" ${productType == 'GAME' ? 'selected' : ''}>Game</option>
                            <option value="SOFTWARE" ${productType == 'SOFTWARE' ? 'selected' : ''}>Phần mềm</option>
                            <option value="OTHER" ${productType == 'OTHER' ? 'selected' : ''}>Khác</option>
                        </select>
                    </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="product-subtype">
                            Phân loại chi tiết <span style="color: red;">*</span>
                        </label>
                        <select class="form-input" id="product-subtype" name="productSubtype" required
                                style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;"
                                data-initial="${productSubtype}">
                            <option value="">-- Chọn phân loại --</option>
                        </select>
                        <small style="color: #666; font-size: 0.875rem;">Vui lòng chọn loại sản phẩm trước</small>
                    </div>
                </div>
                
                <!-- Cột phải -->
                <div>
                    <!-- Giá mặc định (nếu không dùng variants) - ẩn đi vì sẽ dùng variants -->
                    <input type="hidden" id="product-price" name="price" value="0">
            </div>
                
                <!-- Phần Biến thể sản phẩm - full width -->
                <div style="grid-column: 1 / -1; margin-bottom: 1.5rem;">
                    <div style="margin-bottom: 1rem;">
                        <h3 style="font-size: 1.125rem; font-weight: 600; margin-bottom: 0.5rem;">Biến thể sản phẩm <span style="color: red;">*</span></h3>
                        <p style="color: #666; font-size: 0.875rem; margin-bottom: 1rem;">
                            Thêm các biến thể sản phẩm với giá và ảnh riêng. Mỗi biến thể có thể có 1-3 ảnh.
                        </p>
                        <button type="button" id="add-variant-btn" 
                                style="padding: 0.5rem 1rem; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem; font-weight: 500;">
                            + Thêm biến thể
                        </button>
                    </div>
                    
                    <!-- Container cho các variants -->
                    <div id="variants-container" style="display: flex; flex-direction: column; gap: 1.5rem;">
                        <!-- Variants will be added here by JavaScript -->
                    </div>
                </div>
                
                <!-- Phần mô tả - full width -->
                <div style="grid-column: 1 / -1;">
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="short-description">
                            Mô tả ngắn
                        </label>
                        <textarea class="form-input" id="short-description" name="shortDescription" 
                                  rows="2" placeholder="Mô tả ngắn về sản phẩm"
                                  style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; resize: vertical;">${shortDescription}</textarea>
            </div>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="description">
                            Mô tả chi tiết
                        </label>
                        <textarea class="form-input" id="description" name="description" 
                                  rows="5" placeholder="Mô tả chi tiết, chính sách bảo hành..."
                                  style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; resize: vertical;">${description}</textarea>
            </div>
            </div>
        </div>
        <div class="panel__footer" style="display: flex; gap: 1rem;">
                <button class="button button--primary" type="submit">
                    Đăng sản phẩm ngay
            </button>
                <a href="${pageContext.request.contextPath}/seller/inventory" 
                   class="button button--ghost" style="text-decoration: none;">
                    Hủy
                </a>
        </div>
    </section>
    </form>
    
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Mẹo đăng sản phẩm hiệu quả</h2>
        </div>
        <ul class="guide-list">
            <li>Chuẩn bị ít nhất 3 hình ảnh minh hoạ chất lượng cao.</li>
            <li>Nhập mô tả rõ ràng về quyền lợi bàn giao và chính sách bảo hành.</li>
            <li>Cập nhật tồn kho thực tế để hệ thống tự động khóa đơn khi bán hết.</li>
        </ul>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<script>
(function() {
    var typeSelect = document.getElementById('product-type');
    var subtypeSelect = document.getElementById('product-subtype');
    var initialSubtype = subtypeSelect.dataset.initial || '';
    
    if (!typeSelect || !subtypeSelect) {
        return;
    }
    
    // Định nghĩa các subtype theo từng loại sản phẩm
    var SUBTYPE_OPTIONS = {
        EMAIL: [
            {code: 'GMAIL', label: 'Gmail'},
            {code: 'YAHOO', label: 'Yahoo'},
            {code: 'OUTLOOK', label: 'Outlook'},
            {code: 'OTHER', label: 'Khác'}
        ],
        SOCIAL: [
            {code: 'FACEBOOK', label: 'Facebook'},
            {code: 'TIKTOK', label: 'TikTok'},
            {code: 'X', label: 'X (Twitter)'},
            {code: 'OTHER', label: 'Khác'}
        ],
        SOFTWARE: [
            {code: 'CANVA', label: 'Canva'},
            {code: 'OFFICE', label: 'Office'},
            {code: 'WINDOWS', label: 'Windows'},
            {code: 'CHATGPT', label: 'ChatGPT'},
            {code: 'OTHER', label: 'Khác'}
        ],
        GAME: [
            {code: 'VALORANT', label: 'Valorant'},
            {code: 'LEAGUE_OF_LEGENDS', label: 'League of Legends'},
            {code: 'CS2', label: 'CS2'},
            {code: 'OTHER', label: 'Khác'}
        ],
        OTHER: [
            {code: 'OTHER', label: 'Khác'}
        ]
    };
    
    function renderSubtypeOptions(typeValue, selectedValue) {
        // Xóa tất cả options hiện tại
        while (subtypeSelect.firstChild) {
            subtypeSelect.removeChild(subtypeSelect.firstChild);
        }
        
        // Thêm option mặc định
        var defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = '-- Chọn phân loại --';
        subtypeSelect.appendChild(defaultOpt);
        
        // Nếu chưa chọn type, hiển thị thông báo
        if (!typeValue || !SUBTYPE_OPTIONS[typeValue]) {
            var disabledOpt = document.createElement('option');
            disabledOpt.value = '';
            disabledOpt.textContent = 'Vui lòng chọn loại sản phẩm trước';
            disabledOpt.disabled = true;
            disabledOpt.selected = true;
            subtypeSelect.appendChild(disabledOpt);
            subtypeSelect.disabled = true;
            return;
        }
        
        // Enable select
        subtypeSelect.disabled = false;
        
        // Thêm các options tương ứng với type đã chọn
        var options = SUBTYPE_OPTIONS[typeValue] || [];
        options.forEach(function(option) {
            var opt = document.createElement('option');
            opt.value = option.code;
            opt.textContent = option.label;
            if (option.code === selectedValue) {
                opt.selected = true;
            }
            subtypeSelect.appendChild(opt);
        });
    }
    
    // Render ban đầu dựa trên type đã chọn (nếu có)
    var initialType = typeSelect.value;
    if (initialType) {
        renderSubtypeOptions(initialType, initialSubtype);
    } else {
        renderSubtypeOptions('', '');
    }
    
    // Lắng nghe sự kiện thay đổi type
    typeSelect.addEventListener('change', function() {
        renderSubtypeOptions(typeSelect.value, '');
    });
})();

// Variants management functionality
(function() {
    var variantCounter = 0;
    var variantsContainer = document.getElementById('variants-container');
    var addVariantBtn = document.getElementById('add-variant-btn');
    
    if (!variantsContainer || !addVariantBtn) {
        return;
    }
    
    // Function để tạo variant form mới
    function createVariantForm() {
        variantCounter++;
        var variantId = 'variant-' + variantCounter;
        
        var variantDiv = document.createElement('div');
        variantDiv.className = 'variant-item';
        variantDiv.id = variantId;
        variantDiv.style.border = '1px solid #ddd';
        variantDiv.style.borderRadius = '8px';
        variantDiv.style.padding = '1.5rem';
        variantDiv.style.backgroundColor = '#f9f9f9';
        
        variantDiv.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h4 style="margin: 0; font-size: 1rem; font-weight: 600;">Biến thể #${variantCounter}</h4>
                <button type="button" class="remove-variant-btn" 
                        style="padding: 0.25rem 0.75rem; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                    Xóa biến thể
                </button>
            </div>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;">
                        Tên biến thể <span style="color: red;">*</span>
                    </label>
                    <input type="text" class="variant-name" 
                           placeholder="Ví dụ: Email 3 tháng" 
                           required
                           style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div>
                    <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;">
                        Giá (VNĐ) <span style="color: red;">*</span>
                    </label>
                    <input type="number" class="variant-price" 
                           placeholder="30000" 
                           required min="0" step="1000"
                           style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
            </div>
            
            <div>
                <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;">
                    Ảnh biến thể (1-3 ảnh) <span style="color: red;">*</span>
                </label>
                <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; margin-bottom: 0.5rem;">
                    <input type="file" class="variant-image-input" 
                           accept="image/*" multiple
                           style="display: none;">
                    <button type="button" class="variant-select-image-btn" 
                            style="padding: 0.5rem 1rem; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                        📷 Chọn ảnh
                    </button>
                    <span class="variant-file-count" style="color: #666; font-size: 0.875rem; display: none;">
                        <span class="variant-file-count-number">0</span> ảnh đã chọn
                    </span>
                </div>
                <small style="color: #666; font-size: 0.875rem; display: block; margin-bottom: 0.5rem;">
                    Mỗi biến thể có thể có 1-3 ảnh (JPG, PNG, GIF, WEBP - tối đa 10MB mỗi ảnh)
                </small>
                <div class="variant-preview-container" style="display: none; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 0.75rem; margin-top: 0.5rem;">
                    <!-- Preview images will be inserted here -->
                </div>
            </div>
        `;
        
        variantsContainer.appendChild(variantDiv);
        
        // Initialize variant image management
        initVariantImageManagement(variantDiv);
        
        // Remove variant button handler
        var removeBtn = variantDiv.querySelector('.remove-variant-btn');
        removeBtn.addEventListener('click', function() {
            variantDiv.remove();
        });
    }
    
    // Initialize image management for a variant
    function initVariantImageManagement(variantDiv) {
        var imageInput = variantDiv.querySelector('.variant-image-input');
        var selectBtn = variantDiv.querySelector('.variant-select-image-btn');
        var previewContainer = variantDiv.querySelector('.variant-preview-container');
        var fileCountDisplay = variantDiv.querySelector('.variant-file-count');
        var fileCountNumber = variantDiv.querySelector('.variant-file-count-number');
        
        var allFiles = [];
        var fileMap = {};
        
        selectBtn.addEventListener('click', function() {
            imageInput.click();
        });
        
        imageInput.addEventListener('change', function(e) {
            var newFiles = Array.from(e.target.files);
            
            if (newFiles.length === 0) {
                return;
            }
            
            // Validate và thêm files
            var addedCount = 0;
            for (var i = 0; i < newFiles.length; i++) {
                var file = newFiles[i];
                
                // Validate file type
                if (!file.type.match('image.*')) {
                    alert('File "' + file.name + '" không phải là ảnh hợp lệ');
                    continue;
                }
                
                // Validate file size (10MB)
                if (file.size > 10 * 1024 * 1024) {
                    alert('File "' + file.name + '" quá lớn. Kích thước tối đa: 10MB');
                    continue;
                }
                
                // Validate số lượng ảnh (tối đa 3)
                if (allFiles.length >= 3) {
                    alert('Mỗi biến thể chỉ được tối đa 3 ảnh');
                    break;
                }
                
                // Kiểm tra duplicate
                var isDuplicate = false;
                for (var j = 0; j < allFiles.length; j++) {
                    if (allFiles[j].name === file.name && allFiles[j].size === file.size) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    allFiles.push(file);
                    addVariantImagePreview(file, allFiles.length - 1);
                    addedCount++;
                }
            }
            
            if (addedCount > 0) {
                previewContainer.style.display = 'grid';
                updateVariantFileCount();
            }
            
            imageInput.value = '';
        });
        
        function addVariantImagePreview(file, index) {
            var fileId = file.name + '_' + file.size + '_' + file.lastModified;
            fileMap[fileId] = file;
            
            var reader = new FileReader();
            reader.onload = function(e) {
                var previewDiv = document.createElement('div');
                previewDiv.style.position = 'relative';
                previewDiv.style.border = '1px solid #ddd';
                previewDiv.style.borderRadius = '4px';
                previewDiv.style.overflow = 'hidden';
                previewDiv.style.aspectRatio = '1';
                previewDiv.dataset.fileId = fileId;
                
                if (index === 0) {
                    var primaryLabel = document.createElement('span');
                    primaryLabel.textContent = 'Ảnh chính';
                    primaryLabel.style.position = 'absolute';
                    primaryLabel.style.top = '4px';
                    primaryLabel.style.left = '4px';
                    primaryLabel.style.background = 'rgba(0, 123, 255, 0.8)';
                    primaryLabel.style.color = 'white';
                    primaryLabel.style.padding = '2px 6px';
                    primaryLabel.style.borderRadius = '3px';
                    primaryLabel.style.fontSize = '0.75rem';
                    primaryLabel.style.zIndex = '5';
                    previewDiv.appendChild(primaryLabel);
                }
                
                var img = document.createElement('img');
                img.src = e.target.result;
                img.style.width = '100%';
                img.style.height = '100%';
                img.style.objectFit = 'cover';
                img.style.display = 'block';
                
                var removeBtn = document.createElement('button');
                removeBtn.type = 'button';
                removeBtn.innerHTML = '×';
                removeBtn.style.position = 'absolute';
                removeBtn.style.top = '4px';
                removeBtn.style.right = '4px';
                removeBtn.style.background = 'rgba(255, 0, 0, 0.8)';
                removeBtn.style.color = 'white';
                removeBtn.style.border = 'none';
                removeBtn.style.borderRadius = '50%';
                removeBtn.style.width = '24px';
                removeBtn.style.height = '24px';
                removeBtn.style.cursor = 'pointer';
                removeBtn.style.fontSize = '18px';
                removeBtn.style.lineHeight = '1';
                removeBtn.style.display = 'flex';
                removeBtn.style.alignItems = 'center';
                removeBtn.style.justifyContent = 'center';
                removeBtn.style.zIndex = '10';
                removeBtn.onclick = function() {
                    var fileIdToRemove = previewDiv.dataset.fileId;
                    previewDiv.remove();
                    delete fileMap[fileIdToRemove];
                    // Remove file from allFiles array
                    var fileIndex = -1;
                    for (var k = 0; k < allFiles.length; k++) {
                        var f = allFiles[k];
                        var id = f.name + '_' + f.size + '_' + f.lastModified;
                        if (id === fileIdToRemove) {
                            fileIndex = k;
                            break;
                        }
                    }
                    if (fileIndex >= 0) {
                        allFiles.splice(fileIndex, 1);
                    }
                    
                    updateVariantFileCount();
                    updateVariantPrimaryLabel();
                    if (allFiles.length === 0) {
                        previewContainer.style.display = 'none';
                    }
                };
                
                previewDiv.appendChild(img);
                previewDiv.appendChild(removeBtn);
                previewContainer.appendChild(previewDiv);
            };
            
            reader.readAsDataURL(file);
        }
        
        function updateVariantFileCount() {
            var count = allFiles.length;
            if (count > 0) {
                fileCountNumber.textContent = count;
                fileCountDisplay.style.display = 'inline';
            } else {
                fileCountDisplay.style.display = 'none';
            }
        }
        
        function updateVariantPrimaryLabel() {
            var labels = previewContainer.querySelectorAll('span');
            labels.forEach(function(label) {
                if (label.textContent === 'Ảnh chính') {
                    label.remove();
                }
            });
            
            var firstPreview = previewContainer.firstElementChild;
            if (firstPreview) {
                var primaryLabel = document.createElement('span');
                primaryLabel.textContent = 'Ảnh chính';
                primaryLabel.style.position = 'absolute';
                primaryLabel.style.top = '4px';
                primaryLabel.style.left = '4px';
                primaryLabel.style.background = 'rgba(0, 123, 255, 0.8)';
                primaryLabel.style.color = 'white';
                primaryLabel.style.padding = '2px 6px';
                primaryLabel.style.borderRadius = '3px';
                primaryLabel.style.fontSize = '0.75rem';
                primaryLabel.style.zIndex = '5';
                firstPreview.insertBefore(primaryLabel, firstPreview.firstChild);
            }
        }
        
        // Store reference to allFiles array in variant div for form submission
        // This will be accessed when submitting the form
        variantDiv._getVariantFiles = function() {
            return allFiles.slice(); // Return a copy
        };
    }
    
    // Add variant button handler
    addVariantBtn.addEventListener('click', function() {
        createVariantForm();
    });
    
    // Form submit handler
    var form = document.getElementById('create-product-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            // Collect all variants data
            var variants = [];
            var variantItems = variantsContainer.querySelectorAll('.variant-item');
            
            if (variantItems.length === 0) {
                e.preventDefault();
                alert('Vui lòng thêm ít nhất một biến thể sản phẩm');
                return false;
            }
            
            for (var i = 0; i < variantItems.length; i++) {
                var variantItem = variantItems[i];
                var name = variantItem.querySelector('.variant-name').value.trim();
                var priceStr = variantItem.querySelector('.variant-price').value.trim();
                var files = variantItem._getVariantFiles ? variantItem._getVariantFiles() : [];
                
                // Validation
                if (!name) {
                    e.preventDefault();
                    alert('Vui lòng nhập tên cho tất cả các biến thể');
                    return false;
                }
                
                if (!priceStr || parseFloat(priceStr) <= 0) {
                    e.preventDefault();
                    alert('Vui lòng nhập giá hợp lệ cho tất cả các biến thể');
                    return false;
                }
                
                if (files.length === 0) {
                    e.preventDefault();
                    alert('Vui lòng chọn ít nhất 1 ảnh cho biến thể: ' + name);
                    return false;
                }
                
                if (files.length > 3) {
                    e.preventDefault();
                    alert('Mỗi biến thể chỉ được tối đa 3 ảnh. Biến thể: ' + name);
                    return false;
                }
                
                // Create variant code from name
                var variantCode = name.toLowerCase()
                    .replace(/[^a-z0-9\s]/g, '')
                    .replace(/\s+/g, '_')
                    .substring(0, 50);
                
                variants.push({
                    variant_code: variantCode + '_' + i,
                    name: name,
                    price: parseFloat(priceStr),
                    files: files,
                    inventory_count: 0,
                    status: 'Available'
                });
            }
            
            // Store variants data in hidden input
            var variantsInput = document.getElementById('variants-json');
            if (!variantsInput) {
                variantsInput = document.createElement('input');
                variantsInput.type = 'hidden';
                variantsInput.id = 'variants-json';
                variantsInput.name = 'variantsJson';
                form.appendChild(variantsInput);
            }
            
            // Note: We'll send files separately via multipart/form-data
            // Store variant metadata in JSON (without image URLs, they'll be added server-side)
            var variantsMetadata = variants.map(function(v) {
                return {
                    variant_code: v.variant_code,
                    name: v.name,
                    price: v.price,
                    inventory_count: v.inventory_count,
                    status: v.status
                };
            });
            
            variantsInput.value = JSON.stringify(variantsMetadata);
            
            // Use FormData API to properly send files
            // Convert form to FormData
            var formData = new FormData(form);
            
            // Remove old variant files if any
            var oldVariantInputs = form.querySelectorAll('input[name^="variantImages_"]');
            oldVariantInputs.forEach(function(input) {
                input.remove();
            });
            
            // Append files to FormData with variant index
            for (var i = 0; i < variants.length; i++) {
                var variant = variants[i];
                for (var j = 0; j < variant.files.length; j++) {
                    var file = variant.files[j];
                    formData.append('variantImages_' + i + '_' + j, file);
                }
            }
            
            // Also store variant indices
            formData.append('variantIndices', variants.map(function(v, idx) {
                return idx + ':' + v.files.length;
            }).join(','));
            
            // Prevent default form submit and use fetch instead
            e.preventDefault();
            
            // Submit using fetch
            fetch(form.action, {
                method: 'POST',
                body: formData
            })
            .then(function(response) {
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    return response.text();
                }
            })
            .then(function(html) {
                if (html) {
                    // If there's an error, replace the page content
                    document.open();
                    document.write(html);
                    document.close();
                }
            })
            .catch(function(error) {
                console.error('Error submitting form:', error);
                alert('Có lỗi xảy ra khi đăng sản phẩm. Vui lòng thử lại.');
            });
            
            return false;
        });
    }
    
    // Create first variant by default
    createVariantForm();
})();
</script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
