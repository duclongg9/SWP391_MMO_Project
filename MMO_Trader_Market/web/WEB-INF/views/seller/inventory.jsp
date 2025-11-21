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
    <c:if test="${not empty successMessage}">
        <div style="background-color: #d4edda; border: 1px solid #c3e6cb; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #155724;">
            ${successMessage}
        </div>
    </c:if>
    
    <c:if test="${not empty errorMessage}">
        <div style="background-color: #fee; border: 1px solid #fcc; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #c00;">
            ${errorMessage}
        </div>
    </c:if>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Sinh credential ảo</h2>
            <p class="panel__subtitle">Tạo nhanh dữ liệu bàn giao để kiểm thử luồng mua hàng trước khi kết nối hệ thống thực tế.</p>
        </div>
        <div class="panel__body">
            <c:if test="${not empty flashSuccess}">
                <div style="background-color: #d4edda; border: 1px solid #c3e6cb; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #155724;">
                    ${flashSuccess}
                </div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div style="background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 1rem; margin-bottom: 1rem; border-radius: 4px; color: #721c24;">
                    ${flashError}
                </div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/seller/credentials/generate"
                  style="display:flex;flex-direction:column;gap:12px;max-width:560px;">
                <c:if test="${not empty shopId}">
                    <input type="hidden" name="shopId" value="${shopId}" />
                </c:if>
                <p class="text-muted" style="margin:0;">
                    Nhấn nút bên dưới để hệ thống sinh credential ảo cho <strong>toàn bộ sản phẩm</strong> và các biến thể
                    đã cấu hình tồn kho trong cơ sở dữ liệu. Mỗi SKU sẽ được bổ sung đủ số lượng theo tồn kho hiện tại.
                </p>
                <div>
                    <button type="submit" class="button button--primary">Sinh credential cho toàn bộ sản phẩm</button>
                </div>
            </form>
        </div>
    </section>
    <section class="panel">
        <div class="panel__header">
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 class="panel__title">Tồn kho tổng quan</h2>
                    <p class="panel__subtitle">Dữ liệu mô phỏng phục vụ giao diện quản lý, sẽ kết nối với API thực tế trong giai đoạn kế tiếp.</p>
                </div>
                <c:url var="createProductUrl" value="/seller/products/create">
                    <c:if test="${not empty shopId}">
                        <c:param name="shopId" value="${shopId}" />
                    </c:if>
                </c:url>
                <a href="${createProductUrl}" 
                   class="button button--primary" 
                   style="text-decoration: none; white-space: nowrap;">
                    ➕ Tạo sản phẩm mới
                </a>
            </div>
        </div>
        <div class="panel__body">
            <div style="margin-bottom: 1rem;">
                <form method="get" action="${pageContext.request.contextPath}/seller/inventory" 
                      style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                    <c:if test="${not empty shopId}">
                        <input type="hidden" name="shopId" value="${shopId}" />
                    </c:if>
                    <input type="text" name="keyword" 
                           placeholder="Tìm kiếm sản phẩm..." 
                           value="${keyword != null ? keyword : ''}"
                           style="flex: 1; min-width: 200px; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                    <button type="submit" class="button button--primary">🔍 Tìm kiếm</button>
                    <c:if test="${not empty keyword}">
                        <c:url var="clearUrl" value="${pageContext.request.contextPath}/seller/inventory">
                            <c:if test="${not empty shopId}">
                                <c:param name="shopId" value="${shopId}" />
                            </c:if>
                        </c:url>
                        <a href="${clearUrl}" class="button">Xóa</a>
                    </c:if>
                </form>
            </div>
            <c:choose>
                <c:when test="${empty products}">
                    <p style="text-align: center; padding: 2rem; color: #666;">
                        <c:choose>
                            <c:when test="${not empty keyword}">
                                Chưa có sản phẩm trùng khớp với từ khóa "<strong>${fn:escapeXml(keyword)}</strong>".
                                <br>
                                <c:url var="allProductsUrl" value="${pageContext.request.contextPath}/seller/inventory">
                                    <c:if test="${not empty shopId}">
                                        <c:param name="shopId" value="${shopId}" />
                                    </c:if>
                                </c:url>
                                <a href="${allProductsUrl}" style="margin-top: 0.5rem; display: inline-block;">Xem tất cả sản phẩm</a>
                                hoặc
                                <c:url var="createProductUrl" value="/seller/products/create">
                                    <c:if test="${not empty shopId}">
                                        <c:param name="shopId" value="${shopId}" />
                                    </c:if>
                                </c:url>
                                <a href="${createProductUrl}">Tạo sản phẩm mới</a>
                            </c:when>
                            <c:otherwise>
                                Chưa có sản phẩm nào. 
                                <c:url var="createProductUrl" value="/seller/products/create">
                                    <c:if test="${not empty shopId}">
                                        <c:param name="shopId" value="${shopId}" />
                                    </c:if>
                                </c:url>
                                <a href="${createProductUrl}">Thêm sản phẩm đầu tiên</a>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:when>
                <c:otherwise>
                    <table class="table table--interactive" style="width: 100%;">
                <thead>
                    <tr>
                                <th>ID</th>
                                <th>Tên sản phẩm</th>
                                <th>Loại</th>
                                <th>Giá (VNĐ)</th>
                                <th>Tồn kho</th>
                                <th>Đã bán</th>
                                <th>Trạng thái</th>
                                <th style="text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>`
                            <c:forEach var="product" items="${products}">
                                <tr>
                                    <td>${product.id}</td>
                                    <td><strong>${product.name}</strong></td>
                                    <td>${product.productType} / ${product.productSubtype}</td>
                                    <td><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/></td>
                                    <td>${product.inventoryCount != null ? product.inventoryCount : 0}</td>
                                    <td>${product.soldCount != null ? product.soldCount : 0}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${product.status == 'Available'}">
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #d4edda; color: #155724; border-radius: 4px; font-size: 0.875rem;">
                                                    Đang bán
                                                </span>
                                            </c:when>
                                            <c:when test="${product.status == 'Unlisted'}">
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #f8d7da; color: #721c24; border-radius: 4px; font-size: 0.875rem;">
                                                    Ngừng bán
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="display: inline-block; padding: 0.25rem 0.5rem; background: #fff3cd; color: #856404; border-radius: 4px; font-size: 0.875rem;">
                                                    ${product.status}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;">
                                        <div style="display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap;">
                                            <c:url var="editProductUrl" value="/seller/products/edit">
                                                <c:param name="id" value="${product.id}" />
                                                <c:if test="${not empty shopId}">
                                                    <c:param name="shopId" value="${shopId}" />
                                                </c:if>
                                            </c:url>
                                            <a href="${editProductUrl}" 
                                               style="padding: 0.25rem 0.75rem; background: #007bff; color: white; text-decoration: none; border-radius: 4px; font-size: 0.875rem;">
                                                Sửa
                                            </a>
                                            <c:url var="viewInventoryUrl" value="/seller/inventory/view">
                                                <c:param name="productId" value="${product.id}" />
                                                <c:if test="${not empty shopId}">
                                                    <c:param name="shopId" value="${shopId}" />
                                                </c:if>
                                            </c:url>
                                            <a href="${viewInventoryUrl}" 
                                               style="padding: 0.25rem 0.75rem; background: #17a2b8; color: white; text-decoration: none; border-radius: 4px; font-size: 0.875rem;">
                                                📦 Hàng tồn kho
                                            </a>
                                            <c:choose>
                                                <c:when test="${product.status == 'Available'}">
                                                    <form action="${pageContext.request.contextPath}/seller/inventory" method="post" style="display: inline; margin: 0;">
                                                        <input type="hidden" name="action" value="stop"/>
                                                        <input type="hidden" name="productId" value="${product.id}"/>
                                                        <input type="hidden" name="page" value="${page}"/>
                                                        <c:if test="${not empty shopId}">
                                                            <input type="hidden" name="shopId" value="${shopId}"/>
                                                        </c:if>
                                                        <c:if test="${not empty keyword}">
                                                            <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}"/>
                                                        </c:if>
                                                        <button type="submit" 
                                                                style="padding: 0.25rem 0.75rem; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;"
                                                                onclick="return confirm('Bạn có chắc muốn ngừng bán sản phẩm này?')">
                                                            Ngừng bán
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:when test="${product.status == 'Unlisted'}">
                                                    <form action="${pageContext.request.contextPath}/seller/inventory" method="post" style="display: inline; margin: 0;">
                                                        <input type="hidden" name="action" value="resume"/>
                                                        <input type="hidden" name="productId" value="${product.id}"/>
                                                        <input type="hidden" name="page" value="${page}"/>
                                                        <c:if test="${not empty shopId}">
                                                            <input type="hidden" name="shopId" value="${shopId}"/>
                                                        </c:if>
                                                        <c:if test="${not empty keyword}">
                                                            <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}"/>
                                                        </c:if>
                                                        <button type="submit" 
                                                                style="padding: 0.25rem 0.75rem; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 0.875rem;">
                                                            Mở bán lại
                                                        </button>
                                                    </form>
                                                </c:when>
                                            </c:choose>
                                        </div>
                                    </td>
                    </tr>
                            </c:forEach>
                </tbody>
            </table>
            <c:if test="${totalPages > 1}">
                <div style="display: flex; justify-content: center; align-items: center; gap: 0.5rem; margin-top: 2rem; padding: 1rem;">
                    <c:if test="${page > 1}">
                        <c:url var="prevUrl" value="${pageContext.request.contextPath}/seller/inventory">
                            <c:if test="${not empty shopId}">
                                <c:param name="shopId" value="${shopId}" />
                            </c:if>
                            <c:param name="page" value="${page - 1}" />
                            <c:if test="${not empty keyword}">
                                <c:param name="keyword" value="${keyword}" />
                            </c:if>
                        </c:url>
                        <a href="${prevUrl}" class="button">‹ Trước</a>
                    </c:if>
                    <c:forEach var="i" begin="${page > 2 ? page - 2 : 1}" end="${page + 2 < totalPages ? page + 2 : totalPages}">
                        <c:choose>
                            <c:when test="${i == page}">
                                <span class="button button--primary" style="pointer-events: none;">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <c:url var="pageUrl" value="${pageContext.request.contextPath}/seller/inventory">
                                    <c:if test="${not empty shopId}">
                                        <c:param name="shopId" value="${shopId}" />
                                    </c:if>
                                    <c:param name="page" value="${i}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                </c:url>
                                <a href="${pageUrl}" class="button">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${page < totalPages}">
                        <c:url var="nextUrl" value="${pageContext.request.contextPath}/seller/inventory">
                            <c:if test="${not empty shopId}">
                                <c:param name="shopId" value="${shopId}" />
                            </c:if>
                            <c:param name="page" value="${page + 1}" />
                            <c:if test="${not empty keyword}">
                                <c:param name="keyword" value="${keyword}" />
                            </c:if>
                        </c:url>
                        <a href="${nextUrl}" class="button">Sau ›</a>
                    </c:if>
                </div>
            </c:if>
            <c:if test="${not empty totalProducts}">
                <p style="text-align: center; color: #666; margin-top: 1rem;">
                    Hiển thị ${(page - 1) * pageSize + 1} - ${page * pageSize > totalProducts ? totalProducts : page * pageSize} trong tổng số ${totalProducts} sản phẩm
                </p>
            </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
