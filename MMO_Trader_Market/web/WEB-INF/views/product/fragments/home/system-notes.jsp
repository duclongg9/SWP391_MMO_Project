<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="panel__header">
    <h3 class="panel__title">Cấu hình hệ thống</h3>
</div>
<c:if test="${empty systemNotes}">
    <p>Chưa có dữ liệu.</p>
</c:if>
<c:if test="${not empty systemNotes}">
    <ol class="tips-list">
        <c:forEach var="config" items="${systemNotes}">
            <li>
                <strong><c:out value="${config.description}" /></strong>
            </li>
        </c:forEach>
    </ol>
</c:if>
