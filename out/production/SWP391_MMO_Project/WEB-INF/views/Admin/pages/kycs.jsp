<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Danh sách KYC cần duyệt</h4>
    <table class="table table-bordered align-middle shadow-sm">
        <thead class="table-secondary">
        <tr>
            <th>ID</th>
            <th>Tên người dùng</th>
            <th>Ảnh giấy tờ mặt trước</th>
            <th>Ảnh giấy tờ mặt sau</th>
            <th>Ảnh selfie</th>
            <th>số điện thoại</th>
            <th>Ngày gửi</th>
            <th>Trạng thái</th>
            <th>Nội dung</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="k" items="${kycList}">
            <tr>
                <td>${k.id}</td>
                <td>${k.userName}</td>
                <td>${k.frontImageUrl}</td>
                <td>${k.backImageUrl}</td>
                <td>${k.selfieImageUrl}</td>
                <td>${k.idNumber}</td>
                <td><fmt:formatDate value="${k.createdAt}" pattern="HH:mm:ss, dd-MM-yyyy"/></td>
                <td>${k.statusName}</td>
                <td>${k.adminFeedback}</td>

                <td>
                    <a class="btn btn-sm btn-success" href="${base}/admin/kycs/approve?id=${k.id}">Duyệt</a>
                    <a class="btn btn-sm btn-danger"  href="${base}/admin/kycs/reject?id=${k.id}">Từ chối</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
