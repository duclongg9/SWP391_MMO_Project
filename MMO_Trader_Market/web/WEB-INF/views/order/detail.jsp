<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN" scope="request" />
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>
<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Chi tiết đơn hàng #<c:out value="${order.id}" /></h2>
        </div>
        <div class="panel__body">
            <div class="grid grid--two-columns">
                <div class="card">
                    <h3 class="card__title">Thông tin đơn</h3>
                    <ul class="definition-list">
                        <li><span>Mã đơn:</span> #<c:out value="${order.id}" /></li>
                        <li><span>Sản phẩm:</span> <c:out value="${product.name}" /></li>
                        <c:if test="${not empty order.variantCode}">
                            <li><span>Biến thể:</span> <c:out value="${order.variantCode}" /></li>
                        </c:if>
                        <c:if test="${order.quantity ne null}">
                            <li><span>Số lượng:</span> <c:out value="${order.quantity}" /></li>
                        </c:if>
                        <c:if test="${order.unitPrice ne null}">
                            <li><span>Đơn giá:</span>
                                <fmt:formatNumber value="${order.unitPrice}" type="currency" currencySymbol="" /> đ
                            </li>
                        </c:if>
                        <li><span>Tổng tiền:</span>
                            <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="" /> đ
                        </li>
                        <li><span>Trạng thái:</span> <c:out value="${statusLabel}" /></li>
                        <li><span>Ngày tạo:</span>
                            <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                        </li>
                    </ul>
                </div>
                <div class="card">
                    <h3 class="card__title">Sản phẩm</h3>
                    <p><strong>Tên:</strong> <c:out value="${product.name}" /></p>
                    <p><strong>Mô tả:</strong> <c:out value="${product.description}" /></p>
                    <p><strong>Tồn kho hiện tại:</strong> <c:out value="${product.inventoryCount}" /></p>
                </div>
            </div>
            <div class="panel panel--nested">
                <div class="panel__header">
                    <h3 class="panel__title">Thông tin bàn giao</h3>
                </div>
                <div class="panel__body">
                    <c:choose>
                        <c:when test="${order.status eq 'Completed'}">
                            <c:if test="${not empty credentials}">
                                <ul class="list">
                                    <c:forEach var="cred" items="${credentials}">
                                        <li><c:out value="${cred}" /></li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                            <c:if test="${empty credentials}">
                                <p class="empty">Đơn hàng đã hoàn thành nhưng chưa có dữ liệu bàn giao.</p>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <p class="empty">Đơn đang được xử lý, vui lòng chờ...</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
