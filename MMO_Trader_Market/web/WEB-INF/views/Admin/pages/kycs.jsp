<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="mmo" uri="http://mmo.trader/tags/util" %>
<div class="container-fluid">
    <h4 class="mb-4">Danh sách KYC cần duyệt</h4>
    <table class="table table-bordered align-middle shadow-sm">
        <thead class="table-secondary">
        <tr>
            <th>ID</th>
            <th>Tên người dùng</th>
            <th>CMND/CCCD</th>
            <th>Ảnh giấy tờ mặt trước</th>
            <th>Ảnh giấy tờ mặt sau</th>
            <th>Ngày gửi</th>
            <th>Trạng thái</th>
            <th>Nội dung</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>12</td><td>Phạm Ngọc Anh</td><td>07920300xxx</td>
            <td>
                <img src="${mmo:resolveDocumentImage(pageContext.request.contextPath, null)}" class="rounded" width="60" alt="Ảnh giấy tờ" />
            </td>
            <td>10/10/2025</td>
            <td><span class="badge bg-warning text-dark">Chờ duyệt</span></td>
            <td>
                <button class="btn btn-sm btn-success">Duyệt</button>
                <button class="btn btn-sm btn-danger">Từ chối</button>
            </td>
            <td><button class="btn btn-sm btn-close-white">Xem Chi Tiết</button></td>
        </tr>
        </tbody>
    </table>
</div>
