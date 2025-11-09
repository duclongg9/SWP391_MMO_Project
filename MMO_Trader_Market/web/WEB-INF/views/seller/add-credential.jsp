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

    <form action="${pageContext.request.contextPath}/seller/products/add-credential" method="post">
        <input type="hidden" name="productId" value="${product.id}"/>
        <section class="panel">
            <div class="panel__header">
                <h2 class="panel__title">Thêm sản phẩm</h2>
                <p class="panel__subtitle">Mỗi khi thêm một sản phẩm, số lượng tồn kho sẽ tự động tăng lên 1.</p>
            </div>
            <div style="padding: 1.5rem;">
                <div style="margin-bottom: 1.5rem; padding: 1rem; background-color: #f8f9fa; border-radius: 4px;">
                    <p style="margin: 0; font-weight: 500;">Sản phẩm: <strong>${product.name}</strong></p>
                    <p style="margin: 0.5rem 0 0 0; color: #666; font-size: 0.875rem;">
                        Tồn kho hiện tại: <strong>${product.inventoryCount != null ? product.inventoryCount : 0}</strong>
                    </p>
                </div>
                
                <div style="margin-bottom: 1.25rem;">
                    <c:set var="isEmailType" value="${product.productType == 'EMAIL' || (product.productType == 'SOCIAL' && product.productSubtype == 'FACEBOOK')}" />
                    <c:set var="inputType" value="${isEmailType ? 'email' : 'text'}" />
                    <c:set var="inputPlaceholder" value="${isEmailType ? 'example@gmail.com hoặc user@yahoo.com' : 'username123 hoặc thông tin tài khoản'}" />
                    
                    <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="username">
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
                           id="username" name="username" 
                           placeholder="${inputPlaceholder}" 
                           value="${username}" required maxlength="255"
                           style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    <small style="color: #666; font-size: 0.875rem;">
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
                
                <div style="margin-bottom: 1.25rem;">
                    <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="password">
                        Mật khẩu <span style="color: red;">*</span>
                    </label>
                    <input class="form-input" type="password" id="password" name="password" 
                           placeholder="Nhập mật khẩu" 
                           value="${password}" required maxlength="255"
                           style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    <small style="color: #666; font-size: 0.875rem;">
                        Mật khẩu của tài khoản
                    </small>
                </div>
                
                <div style="margin-bottom: 1.25rem;">
                    <label style="display: block; font-weight: 500; margin-bottom: 0.5rem;" for="variantCode">
                        Mã biến thể <c:if test="${not empty variants}"><span style="color: red;">*</span></c:if>
                    </label>
                    <c:choose>
                        <c:when test="${not empty variants}">
                            <c:set var="hasSingleVariant" value="${variants.size() == 1}" />
                            <select class="form-input" id="variantCode" name="variantCode" 
                                    required
                                    style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                                <c:if test="${!hasSingleVariant}">
                                    <option value="">-- Chọn biến thể --</option>
                                </c:if>
                                <c:forEach var="variant" items="${variants}">
                                    <option value="${variant.variantCode}" ${variantCode == variant.variantCode ? 'selected' : ''}>
                                        ${variant.name != null && !variant.name.isEmpty() ? variant.name : variant.variantCode} 
                                        <c:if test="${variant.price != null}">- ${variant.price} ₫</c:if>
                                    </option>
                                </c:forEach>
                            </select>
                            <small style="color: #666; font-size: 0.875rem;">
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
                            <input class="form-input" type="text" id="variantCode" name="variantCode" 
                                   placeholder="Để trống nếu sản phẩm không có biến thể" 
                                   value="${variantCode}" maxlength="50"
                                   style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                            <small style="color: #666; font-size: 0.875rem;">
                                Sản phẩm này không có biến thể. Để trống nếu không cần.
                            </small>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="panel__footer" style="display: flex; gap: 1rem; padding: 1.5rem;">
                <button class="button button--primary" type="submit">
                    ➕ Thêm sản phẩm
                </button>
                <a href="${pageContext.request.contextPath}/seller/inventory" 
                   class="button button--ghost" style="text-decoration: none;">
                    Hủy
                </a>
            </div>
        </section>
    </form>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>

