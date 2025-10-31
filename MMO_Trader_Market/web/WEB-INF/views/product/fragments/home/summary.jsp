<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<c:set var="cPath" value="${pageContext.request.contextPath}" />

<div class="landing__hero-main">
    <h2>Chợ tài khoản MMO chuyên nghiệp, UY TÍN </h2>
    <p class="landing__lead">
        Điều mà chúng tôi đã đạt được:
    </p>
    <c:choose>
        <c:when test="${not empty summary}">
            <ul class="landing__metrics">
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.totalCompletedOrders}" type="number" />
                    </strong> đơn đã hoàn tất
                </li>
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.activeShopCount}" type="number" />
                    </strong> shop đang hoạt động
                </li>
                <li>
                    <strong>
                        <fmt:formatNumber value="${summary.activeBuyerCount}" type="number" />
                    </strong> người mua đã xác minh
                </li>
            </ul>
        </c:when>
        <c:otherwise>
            <p>Dữ liệu thống kê sẽ được cập nhật trong giây lát.</p>
        </c:otherwise>
    </c:choose>
</div>

<aside class="landing__categories" id="product-types">
    <h3 class="landing__aside-title">Danh mục chính</h3>
    <c:choose>
        <c:when test="${empty productCategories}">
            <p>Đang cập nhật dữ liệu.</p>
        </c:when>
        <c:otherwise>
            <ul class="category-menu">
                <c:forEach var="category" items="${productCategories}">
                    <c:url var="categoryUrl" value="/products">
                        <c:param name="type" value="${category.typeCode}" />
                    </c:url>
                    <li class="category-menu__item">
                        <span class="category-menu__icon">🏷️</span>
                        <div>
                            <strong><a href="${categoryUrl}"><c:out value="${category.typeLabel}" /></a></strong>
                            <p><fmt:formatNumber value="${category.availableProducts}" type="number" /> sản phẩm khả dụng</p>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>
</aside>
