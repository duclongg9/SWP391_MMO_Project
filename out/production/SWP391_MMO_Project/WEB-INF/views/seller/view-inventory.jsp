<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

    <section class="panel">
        <div class="panel__header">
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 class="panel__title">📦 Hàng tồn kho</h2>
                    <p class="panel__subtitle">Xem chi tiết tồn kho và danh sách tài khoản/key theo từng biến thể</p>
                </div>
                <div style="display: flex; gap: 0.5rem;">
                    <c:if test="${not empty product}">
                        <button type="button" onclick="openAddCredentialModal()" 
                                class="button button--primary" 
                                style="white-space: nowrap;">
                            ➕ Thêm sản phẩm
                        </button>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/seller/inventory" 
                       class="button" 
                       style="text-decoration: none; white-space: nowrap;">
                        ← Quay lại
                    </a>
                </div>
            </div>
        </div>
        <div class="panel__body">
            <c:if test="${not empty product}">
                <div style="margin-bottom: 1.5rem; padding: 1rem; background-color: #f8f9fa; border-radius: 4px;">
                    <p style="margin: 0; font-weight: 500; font-size: 1.1rem;">Sản phẩm: <strong>${product.name}</strong></p>
                    <p style="margin: 0.5rem 0 0 0; color: #666; font-size: 0.875rem;">
                        Loại: ${product.productType} / ${product.productSubtype} | 
                        Tồn kho tổng: <strong>${product.inventoryCount != null ? product.inventoryCount : 0}</strong>
                    </p>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty variants && variants.size() > 0}">
                    <%-- Sản phẩm có biến thể --%>
                    <c:forEach var="variant" items="${variants}">
                        <c:set var="normalizedCode" value="${variant.variantCode != null ? fn:toLowerCase(fn:trim(variant.variantCode)) : ''}" />
                        <c:set var="inventoryInfo" value="${variantInventoryMap[normalizedCode]}" />
                        
                        <c:if test="${not empty inventoryInfo}">
                            <div style="margin-bottom: 2rem; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 1rem 1.5rem;">
                                    <h3 style="margin: 0; font-size: 1.1rem; font-weight: 600;">
                                        ${variant.name != null ? variant.name : variant.variantCode}
                                    </h3>
                                    <p style="margin: 0.5rem 0 0 0; font-size: 0.875rem; opacity: 0.9;">
                                        Mã biến thể: <code style="background: rgba(255,255,255,0.2); padding: 0.2rem 0.4rem; border-radius: 3px;">${variant.variantCode}</code>
                                    </p>
                                </div>
                                
                                <div style="padding: 1.5rem;">
                                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
                                        <div style="padding: 1rem; background: #e8f5e9; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Tồn kho cấu hình</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #2e7d32;">
                                                ${inventoryInfo.configuredInventory != null ? inventoryInfo.configuredInventory : 0}
                                            </div>
                                        </div>
                                        <div style="padding: 1rem; background: #e3f2fd; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Tồn kho thực tế</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #1976d2;">
                                                ${inventoryInfo.actualInventory != null ? inventoryInfo.actualInventory : 0}
                                            </div>
                                        </div>
                                        <div style="padding: 1rem; background: #fff3e0; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Số credentials</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #f57c00;">
                                                ${inventoryInfo.credentials != null ? inventoryInfo.credentials.size() : 0}
                                            </div>
                                        </div>
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty inventoryInfo.credentials && inventoryInfo.credentials.size() > 0}">
                                            <h4 style="margin: 0 0 1rem 0; font-size: 1rem; color: #333;">Danh sách tài khoản/key:</h4>
                                            <div style="overflow-x: auto;">
                                                <table class="table" style="width: 100%; border-collapse: collapse;">
                                                    <thead>
                                                        <tr style="background-color: #f5f5f5;">
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">ID</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Tên đăng nhập / Key</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Mật khẩu</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Ngày tạo</th>
                                                            <th style="padding: 0.75rem; text-align: center; border-bottom: 2px solid #ddd; font-weight: 600;">Thao tác</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="credential" items="${inventoryInfo.credentials}">
                                                            <tr style="border-bottom: 1px solid #eee;">
                                                                <td style="padding: 0.75rem;">${credential.id}</td>
                                                                <td style="padding: 0.75rem;">
                                                                    <code style="background: #f5f5f5; padding: 0.25rem 0.5rem; border-radius: 3px; font-family: monospace;">
                                                                        ${credential.username}
                                                                    </code>
                                                                </td>
                                                                <td style="padding: 0.75rem;">
                                                                    <code style="background: #f5f5f5; padding: 0.25rem 0.5rem; border-radius: 3px; font-family: monospace;">
                                                                        ${credential.password}
                                                                    </code>
                                                                </td>
                                                                <td style="padding: 0.75rem; color: #666; font-size: 0.875rem;">
                                                                    <c:if test="${credential.createdAt != null}">
                                                                        <fmt:formatDate value="${credential.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                                                    </c:if>
                                                                </td>
                                                                <td style="padding: 0.75rem; text-align: center;">
                                                                    <button type="button" 
                                                                            onclick="openEditCredentialModal(${credential.id}, '${fn:escapeXml(credential.username)}', '${fn:escapeXml(credential.password)}', '${fn:escapeXml(credential.variantCode != null ? credential.variantCode : '')}')"
                                                                            style="padding: 0.25rem 0.75rem; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                                                                        Sửa
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="padding: 2rem; text-align: center; color: #999; background: #f9f9f9; border-radius: 6px;">
                                                <p style="margin: 0;">Chưa có tài khoản/key nào trong kho cho biến thể này.</p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <%-- Sản phẩm không có biến thể --%>
                    <c:set var="inventoryInfo" value="${variantInventoryMap['']}" />
                    
                    <c:choose>
                        <c:when test="${not empty inventoryInfo}">
                            <div style="border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 1rem 1.5rem;">
                                    <h3 style="margin: 0; font-size: 1.1rem; font-weight: 600;">Tồn kho chung</h3>
                                    <p style="margin: 0.5rem 0 0 0; font-size: 0.875rem; opacity: 0.9;">
                                        Sản phẩm không có biến thể
                                    </p>
                                </div>
                                
                                <div style="padding: 1.5rem;">
                                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
                                        <div style="padding: 1rem; background: #e8f5e9; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Tồn kho cấu hình</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #2e7d32;">
                                                ${inventoryInfo.configuredInventory != null ? inventoryInfo.configuredInventory : 0}
                                            </div>
                                        </div>
                                        <div style="padding: 1rem; background: #e3f2fd; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Tồn kho thực tế</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #1976d2;">
                                                ${inventoryInfo.actualInventory != null ? inventoryInfo.actualInventory : 0}
                                            </div>
                                        </div>
                                        <div style="padding: 1rem; background: #fff3e0; border-radius: 6px;">
                                            <div style="font-size: 0.875rem; color: #666; margin-bottom: 0.25rem;">Số credentials</div>
                                            <div style="font-size: 1.5rem; font-weight: 600; color: #f57c00;">
                                                ${inventoryInfo.credentials != null ? inventoryInfo.credentials.size() : 0}
                                            </div>
                                        </div>
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty inventoryInfo.credentials && inventoryInfo.credentials.size() > 0}">
                                            <h4 style="margin: 0 0 1rem 0; font-size: 1rem; color: #333;">Danh sách tài khoản/key:</h4>
                                            <div style="overflow-x: auto;">
                                                <table class="table" style="width: 100%; border-collapse: collapse;">
                                                    <thead>
                                                        <tr style="background-color: #f5f5f5;">
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">ID</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Tên đăng nhập / Key</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Mật khẩu</th>
                                                            <th style="padding: 0.75rem; text-align: left; border-bottom: 2px solid #ddd; font-weight: 600;">Ngày tạo</th>
                                                            <th style="padding: 0.75rem; text-align: center; border-bottom: 2px solid #ddd; font-weight: 600;">Thao tác</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="credential" items="${inventoryInfo.credentials}">
                                                            <tr style="border-bottom: 1px solid #eee;">
                                                                <td style="padding: 0.75rem;">${credential.id}</td>
                                                                <td style="padding: 0.75rem;">
                                                                    <code style="background: #f5f5f5; padding: 0.25rem 0.5rem; border-radius: 3px; font-family: monospace;">
                                                                        ${credential.username}
                                                                    </code>
                                                                </td>
                                                                <td style="padding: 0.75rem;">
                                                                    <code style="background: #f5f5f5; padding: 0.25rem 0.5rem; border-radius: 3px; font-family: monospace;">
                                                                        ${credential.password}
                                                                    </code>
                                                                </td>
                                                                <td style="padding: 0.75rem; color: #666; font-size: 0.875rem;">
                                                                    <c:if test="${credential.createdAt != null}">
                                                                        <fmt:formatDate value="${credential.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                                                    </c:if>
                                                                </td>
                                                                <td style="padding: 0.75rem; text-align: center;">
                                                                    <button type="button" 
                                                                            onclick="openEditCredentialModal(${credential.id}, '${fn:escapeXml(credential.username)}', '${fn:escapeXml(credential.password)}', '${fn:escapeXml(credential.variantCode != null ? credential.variantCode : '')}')"
                                                                            style="padding: 0.25rem 0.75rem; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                                                                        Sửa
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="padding: 2rem; text-align: center; color: #999; background: #f9f9f9; border-radius: 6px;">
                                                <p style="margin: 0;">Chưa có tài khoản/key nào trong kho.</p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="padding: 2rem; text-align: center; color: #999; background: #f9f9f9; border-radius: 6px;">
                                <p style="margin: 0;">Không có dữ liệu tồn kho cho sản phẩm này.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%-- Modal để thêm sản phẩm --%>
    <c:if test="${not empty product}">
        <div id="addCredentialModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
            <div style="background: white; border-radius: 8px; padding: 2rem; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                <button type="button" id="closeModalBtn" style="position: absolute; top: 1rem; right: 1rem; background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #666; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;">&times;</button>
                
                <h2 style="margin: 0 0 0.5rem 0; font-size: 1.5rem; font-weight: 600;">Thêm sản phẩm vào kho</h2>
                <p style="margin: 0 0 1.5rem 0; color: #666; font-size: 0.875rem;">
                    Mỗi khi thêm một sản phẩm, số lượng tồn kho sẽ tự động tăng lên 1.
                </p>
                
                <div style="margin-bottom: 1.5rem; padding: 1rem; background-color: #f8f9fa; border-radius: 4px;">
                    <p style="margin: 0; font-weight: 500;">Sản phẩm: <strong>${product.name}</strong></p>
                    <p style="margin: 0.5rem 0 0 0; color: #666; font-size: 0.875rem;">
                        Tồn kho hiện tại: <strong>${product.inventoryCount != null ? product.inventoryCount : 0}</strong>
                    </p>
                </div>
                
                <div id="modalErrors" style="display: none; background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
                    <ul id="errorList" style="margin: 0; padding-left: 1.5rem;"></ul>
                </div>
                
                <form id="addCredentialForm" action="${pageContext.request.contextPath}/seller/products/add-credential" method="post" style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <input type="hidden" name="productId" value="${product.id}"/>
                    
                    <div>
                        <c:set var="isEmailType" value="${product.productType == 'EMAIL' || (product.productType == 'SOCIAL' && product.productSubtype == 'FACEBOOK')}" />
                        <c:set var="inputType" value="${isEmailType ? 'email' : 'text'}" />
                        <c:set var="inputPlaceholder" value="${isEmailType ? 'example@gmail.com hoặc user@yahoo.com' : 'username123 hoặc thông tin tài khoản'}" />
                        
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="modalUsername">
                            <c:choose>
                                <c:when test="${isEmailType}">
                                    Email <span style="color: red;">*</span>
                                </c:when>
                                <c:otherwise>
                                    Tên đăng nhập / Tài khoản <span style="color: red;">*</span>
                                </c:otherwise>
                            </c:choose>
                        </label>
                        <input class="form-input" 
                               type="${inputType}" 
                               id="modalUsername" 
                               name="username" 
                               placeholder="${inputPlaceholder}" 
                               required 
                               maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                        <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                            <c:choose>
                                <c:when test="${isEmailType}">
                                    <span style="color: #d9534f;">⚠️</span> Vui lòng nhập địa chỉ email hợp lệ (ví dụ: example@gmail.com, user@yahoo.com, name@outlook.com, v.v.)
                                </c:when>
                                <c:otherwise>
                                    Nhập tên đăng nhập hoặc thông tin tài khoản tương ứng với loại sản phẩm
                                </c:otherwise>
                            </c:choose>
                        </small>
                    </div>
                    
                    <div>
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="modalPassword">
                            Mật khẩu <span style="color: red;">*</span>
                        </label>
                        <input class="form-input" 
                               type="password" 
                               id="modalPassword" 
                               name="password" 
                               placeholder="Nhập mật khẩu" 
                               required 
                               maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                        <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                            Mật khẩu của tài khoản
                        </small>
                    </div>
                    
                    <div>
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="modalVariantCode">
                            Mã biến thể <c:if test="${not empty variants && variants.size() > 0}"><span style="color: red;">*</span></c:if>
                        </label>
                        <c:choose>
                            <c:when test="${not empty variants && variants.size() > 0}">
                                <c:set var="hasSingleVariant" value="${variants.size() == 1}" />
                                <select class="form-input" 
                                        id="modalVariantCode" 
                                        name="variantCode" 
                                        required
                                        style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                                    <c:if test="${!hasSingleVariant}">
                                        <option value="">-- Chọn biến thể --</option>
                                    </c:if>
                                    <c:forEach var="variant" items="${variants}">
                                        <option value="${variant.variantCode}" ${hasSingleVariant ? 'selected' : ''}>
                                            ${variant.name != null && !variant.name.isEmpty() ? variant.name : variant.variantCode} 
                                            <c:if test="${variant.price != null}">- <fmt:formatNumber value="${variant.price}" type="number" groupingUsed="true"/> ₫</c:if>
                                        </option>
                                    </c:forEach>
                                </select>
                                <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                                    <c:choose>
                                        <c:when test="${hasSingleVariant}">
                                            Sản phẩm này chỉ có 1 biến thể, đã được tự động chọn.
                                        </c:when>
                                        <c:otherwise>
                                            Vui lòng chọn biến thể sản phẩm để thêm sản phẩm vào đúng loại
                                        </c:otherwise>
                                    </c:choose>
                                </small>
                            </c:when>
                            <c:otherwise>
                                <input class="form-input" 
                                       type="text" 
                                       id="modalVariantCode" 
                                       name="variantCode" 
                                       placeholder="Để trống nếu sản phẩm không có biến thể" 
                                       maxlength="50"
                                       style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                                <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                                    Sản phẩm này không có biến thể. Để trống nếu không cần.
                                </small>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #eee;">
                        <button type="button" 
                                id="cancelModalBtn"
                                class="button" 
                                style="background: #6c757d; color: white; border: none; padding: 0.5rem 1.5rem; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                            Hủy
                        </button>
                        <button type="submit" 
                                class="button button--primary" 
                                style="background: #007bff; color: white; border: none; padding: 0.5rem 1.5rem; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                            ➕ Thêm sản phẩm
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <%-- Modal để sửa sản phẩm --%>
    <c:if test="${not empty product}">
        <div id="editCredentialModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1001; align-items: center; justify-content: center;">
            <div style="background: white; border-radius: 8px; padding: 2rem; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                <button type="button" id="closeEditModalBtn" style="position: absolute; top: 1rem; right: 1rem; background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #666; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;">&times;</button>
                
                <h2 style="margin: 0 0 0.5rem 0; font-size: 1.5rem; font-weight: 600;">Sửa sản phẩm</h2>
                <p style="margin: 0 0 1.5rem 0; color: #666; font-size: 0.875rem;">
                    Cập nhật thông tin tài khoản/key trong kho
                </p>
                
                <div style="margin-bottom: 1.5rem; padding: 1rem; background-color: #f8f9fa; border-radius: 4px;">
                    <p style="margin: 0; font-weight: 500;">Sản phẩm: <strong>${product.name}</strong></p>
                    <p style="margin: 0.5rem 0 0 0; color: #666; font-size: 0.875rem;">
                        ID Credential: <strong id="editCredentialId">-</strong>
                    </p>
                </div>
                
                <div id="editModalErrors" style="display: none; background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
                    <ul id="editErrorList" style="margin: 0; padding-left: 1.5rem;"></ul>
                </div>
                
                <form id="editCredentialForm" action="${pageContext.request.contextPath}/seller/products/edit-credential" method="post" style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <input type="hidden" name="credentialId" id="editCredentialIdInput"/>
                    <input type="hidden" name="productId" value="${product.id}"/>
                    
                    <div>
                        <c:set var="isEmailType" value="${product.productType == 'EMAIL' || (product.productType == 'SOCIAL' && product.productSubtype == 'FACEBOOK')}" />
                        <c:set var="inputType" value="${isEmailType ? 'email' : 'text'}" />
                        <c:set var="inputPlaceholder" value="${isEmailType ? 'example@gmail.com hoặc user@yahoo.com' : 'username123 hoặc thông tin tài khoản'}" />
                        
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="editModalUsername">
                            <c:choose>
                                <c:when test="${isEmailType}">
                                    Email <span style="color: red;">*</span>
                                </c:when>
                                <c:otherwise>
                                    Tên đăng nhập / Tài khoản <span style="color: red;">*</span>
                                </c:otherwise>
                            </c:choose>
                        </label>
                        <input class="form-input" 
                               type="${inputType}" 
                               id="editModalUsername" 
                               name="username" 
                               placeholder="${inputPlaceholder}" 
                               required 
                               maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                        <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                            <c:choose>
                                <c:when test="${isEmailType}">
                                    <span style="color: #d9534f;">⚠️</span> Vui lòng nhập địa chỉ email hợp lệ (ví dụ: example@gmail.com, user@yahoo.com, name@outlook.com, v.v.)
                                </c:when>
                                <c:otherwise>
                                    Nhập tên đăng nhập hoặc thông tin tài khoản tương ứng với loại sản phẩm
                                </c:otherwise>
                            </c:choose>
                        </small>
                    </div>
                    
                    <div>
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="editModalPassword">
                            Mật khẩu <span style="color: red;">*</span>
                        </label>
                        <input class="form-input" 
                               type="password" 
                               id="editModalPassword" 
                               name="password" 
                               placeholder="Nhập mật khẩu" 
                               required 
                               maxlength="255"
                               style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                        <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                            Mật khẩu của tài khoản
                        </small>
                    </div>
                    
                    <div>
                        <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="editModalVariantCode">
                            Mã biến thể <c:if test="${not empty variants && variants.size() > 0}"><span style="color: red;">*</span></c:if>
                        </label>
                        <c:choose>
                            <c:when test="${not empty variants && variants.size() > 0}">
                                <c:set var="hasSingleVariant" value="${variants.size() == 1}" />
                                <select class="form-input" 
                                        id="editModalVariantCode" 
                                        name="variantCode" 
                                        required
                                        style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                                    <c:if test="${!hasSingleVariant}">
                                        <option value="">-- Chọn biến thể --</option>
                                    </c:if>
                                    <c:forEach var="variant" items="${variants}">
                                        <option value="${variant.variantCode}">
                                            ${variant.name != null && !variant.name.isEmpty() ? variant.name : variant.variantCode} 
                                            <c:if test="${variant.price != null}">- <fmt:formatNumber value="${variant.price}" type="number" groupingUsed="true"/> ₫</c:if>
                                        </option>
                                    </c:forEach>
                                </select>
                                <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                                    <c:choose>
                                        <c:when test="${hasSingleVariant}">
                                            Sản phẩm này chỉ có 1 biến thể, đã được tự động chọn.
                                        </c:when>
                                        <c:otherwise>
                                            Vui lòng chọn biến thể sản phẩm
                                        </c:otherwise>
                                    </c:choose>
                                </small>
                            </c:when>
                            <c:otherwise>
                                <input class="form-input" 
                                       type="text" 
                                       id="editModalVariantCode" 
                                       name="variantCode" 
                                       placeholder="Để trống nếu sản phẩm không có biến thể" 
                                       maxlength="50"
                                       style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;">
                                <small style="color: #666; font-size: 0.875rem; display: block; margin-top: 0.25rem;">
                                    Sản phẩm này không có biến thể. Để trống nếu không cần.
                                </small>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #eee;">
                        <button type="button" 
                                id="cancelEditModalBtn"
                                class="button" 
                                style="background: #6c757d; color: white; border: none; padding: 0.5rem 1.5rem; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                            Hủy
                        </button>
                        <button type="submit" 
                                class="button button--primary" 
                                style="background: #28a745; color: white; border: none; padding: 0.5rem 1.5rem; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                            💾 Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<script>
(function() {
    'use strict';
    
    function openAddCredentialModal() {
        var modal = document.getElementById('addCredentialModal');
        if (modal) {
            modal.style.display = 'flex';
            // Reset form và ẩn errors
            var form = document.getElementById('addCredentialForm');
            if (form) {
                form.reset();
            }
            var errorDiv = document.getElementById('modalErrors');
            if (errorDiv) {
                errorDiv.style.display = 'none';
            }
        }
    }
    
    function closeAddCredentialModal() {
        var modal = document.getElementById('addCredentialModal');
        if (modal) {
            modal.style.display = 'none';
            var form = document.getElementById('addCredentialForm');
            if (form) {
                form.reset();
            }
            var errorDiv = document.getElementById('modalErrors');
            if (errorDiv) {
                errorDiv.style.display = 'none';
            }
        }
    }
    
    function openEditCredentialModal(credentialId, username, password, variantCode) {
        var modal = document.getElementById('editCredentialModal');
        if (modal) {
            // Set giá trị vào form
            document.getElementById('editCredentialIdInput').value = credentialId;
            document.getElementById('editCredentialId').textContent = credentialId;
            document.getElementById('editModalUsername').value = username || '';
            document.getElementById('editModalPassword').value = password || '';
            
            // Set variant code
            var variantSelect = document.getElementById('editModalVariantCode');
            if (variantSelect) {
                if (variantSelect.tagName === 'SELECT') {
                    // Nếu là select, tìm option có value khớp
                    variantCode = variantCode || '';
                    var found = false;
                    for (var i = 0; i < variantSelect.options.length; i++) {
                        if (variantSelect.options[i].value === variantCode) {
                            variantSelect.selectedIndex = i;
                            found = true;
                            break;
                        }
                    }
                    // Nếu không tìm thấy variant code hiện tại
                    if (!found) {
                        // Nếu chỉ có 1 option thực sự (không kể option "-- Chọn biến thể --")
                        var realOptions = 0;
                        var firstRealOptionIndex = -1;
                        for (var j = 0; j < variantSelect.options.length; j++) {
                            if (variantSelect.options[j].value !== '') {
                                realOptions++;
                                if (firstRealOptionIndex === -1) {
                                    firstRealOptionIndex = j;
                                }
                            }
                        }
                        // Nếu chỉ có 1 variant thực sự, tự động chọn nó
                        if (realOptions === 1 && firstRealOptionIndex >= 0) {
                            variantSelect.selectedIndex = firstRealOptionIndex;
                        }
                    }
                } else {
                    // Nếu là input text
                    variantSelect.value = variantCode || '';
                }
            }
            
            // Reset errors
            var errorDiv = document.getElementById('editModalErrors');
            if (errorDiv) {
                errorDiv.style.display = 'none';
            }
            
            modal.style.display = 'flex';
        }
    }
    
    function closeEditCredentialModal() {
        var modal = document.getElementById('editCredentialModal');
        if (modal) {
            modal.style.display = 'none';
            var form = document.getElementById('editCredentialForm');
            if (form) {
                form.reset();
            }
            var errorDiv = document.getElementById('editModalErrors');
            if (errorDiv) {
                errorDiv.style.display = 'none';
            }
        }
    }
    
    // Đăng ký hàm global
    window.openAddCredentialModal = openAddCredentialModal;
    window.closeAddCredentialModal = closeAddCredentialModal;
    window.openEditCredentialModal = openEditCredentialModal;
    window.closeEditCredentialModal = closeEditCredentialModal;
    
    // Khởi tạo khi DOM ready
    document.addEventListener('DOMContentLoaded', function() {
        // Modal thêm
        var addModal = document.getElementById('addCredentialModal');
        if (addModal) {
            addModal.addEventListener('click', function(e) {
                if (e.target === addModal) {
                    closeAddCredentialModal();
                }
            });
            
            var closeBtn = document.getElementById('closeModalBtn');
            if (closeBtn) {
                closeBtn.addEventListener('click', closeAddCredentialModal);
            }
            
            var cancelBtn = document.getElementById('cancelModalBtn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', closeAddCredentialModal);
            }
        }
        
        // Modal sửa
        var editModal = document.getElementById('editCredentialModal');
        if (editModal) {
            editModal.addEventListener('click', function(e) {
                if (e.target === editModal) {
                    closeEditCredentialModal();
                }
            });
            
            var closeEditBtn = document.getElementById('closeEditModalBtn');
            if (closeEditBtn) {
                closeEditBtn.addEventListener('click', closeEditCredentialModal);
            }
            
            var cancelEditBtn = document.getElementById('cancelEditModalBtn');
            if (cancelEditBtn) {
                cancelEditBtn.addEventListener('click', closeEditCredentialModal);
            }
        }
    });
})();
</script>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

