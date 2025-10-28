<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
        <div class="panel__header" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h2 class="panel__title">Quản lý kho hàng</h2>
                <p class="panel__subtitle">Danh sách sản phẩm trong cửa hàng của bạn</p>
            </div>
            <a href="${pageContext.request.contextPath}/seller/products/create" 
               class="button button--primary" style="text-decoration: none;">
                + Thêm sản phẩm mới
            </a>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${empty products}">
                    <p style="text-align: center; padding: 2rem; color: #666;">
                        Chưa có sản phẩm nào. <a href="${pageContext.request.contextPath}/seller/products/create">Thêm sản phẩm đầu tiên</a>
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
                <tbody>
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
                                        <div style="display: flex; gap: 0.5rem; justify-content: center;">
                                            <a href="${pageContext.request.contextPath}/seller/products/edit?id=${product.id}" 
                                               style="padding: 0.25rem 0.75rem; background: #007bff; color: white; text-decoration: none; border-radius: 4px; font-size: 0.875rem;">
                                                Sửa
                                            </a>
                                            <c:choose>
                                                <c:when test="${product.status == 'Available'}">
                                                    <form action="${pageContext.request.contextPath}/seller/inventory" method="post" style="display: inline; margin: 0;">
                                                        <input type="hidden" name="action" value="stop"/>
                                                        <input type="hidden" name="productId" value="${product.id}"/>
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
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
