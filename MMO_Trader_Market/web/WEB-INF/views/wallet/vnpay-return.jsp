<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    request.setAttribute("pageTitle", "Kết quả thanh toán VNPAY");
    request.setAttribute("bodyClass", "layout");
%>
<%@ include file="/WEB-INF/views/shared/page-start.jspf" %>
<%@ include file="/WEB-INF/views/shared/header.jspf" %>

<main class="layout__content">
    <section class="panel">
        <div class="panel__header">
            <h1 class="panel__title">Kết quả thanh toán</h1>
        </div>
        <div class="panel__body">
            <c:choose>
                <c:when test="${not checksumValid}">
                    <div class="alert alert--danger">Chữ ký xác thực không hợp lệ. Vui lòng không làm mới trang và liên hệ hỗ trợ nếu bạn đã thanh toán.</div>
                </c:when>
                <c:when test="${paymentSuccess}">
                    <div class="alert alert--success">Thanh toán thành công! Số dư ví sẽ được cộng sau khi hệ thống xác nhận IPN.</div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert--warning">Giao dịch chưa thành công. Bạn có thể thử lại hoặc kiểm tra lịch sử giao dịch.</div>
                </c:otherwise>
            </c:choose>

            <table class="table table--interactive" role="presentation">
                <tbody>
                    <tr>
                        <th scope="row">Mã giao dịch</th>
                        <td><c:out value="${txnRef}" /></td>
                    </tr>
                    <tr>
                        <th scope="row">Số tiền</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty amount}">
                                    <fmt:formatNumber value="${amount}" type="number" minFractionDigits="0" /> VNĐ
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th scope="row">Mã phản hồi</th>
                        <td><c:out value="${responseCode}" /></td>
                    </tr>
                    <tr>
                        <th scope="row">Trạng thái giao dịch</th>
                        <td><c:out value="${transactionStatus}" /></td>
                    </tr>
                </tbody>
            </table>

            <p class="text-muted">Lưu ý: Số dư ví chỉ được cộng khi máy chủ nhận IPN thành công từ VNPAY. Nếu trạng thái vẫn chưa cập nhật sau vài phút, vui lòng liên hệ bộ phận hỗ trợ.</p>

            <a href="${pageContext.request.contextPath}/wallet" class="btn btn--primary">Quay lại ví</a>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/shared/footer.jspf" %>
<%@ include file="/WEB-INF/views/shared/page-end.jspf" %>
