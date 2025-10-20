<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Duyệt nạp / rút tiền</h4>
    <table class="table table-striped align-middle shadow-sm">
        <thead class="table-primary">
        <tr>
            <th>ID</th>
            <th>Người dùng</th>
            <th>Loại giao dịch</th>
            <th>Số tiền</th>
            <th>Ngày yêu cầu</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>1</td><td>Lê Thanh Tùng</td><td><span class="badge bg-success">Nạp</span></td>
            <td>2.000.000₫</td><td>18/10/2025</td>
            <td><span class="badge bg-warning text-dark">Chờ duyệt</span></td>
            <td>
                <button class="btn btn-sm btn-success">Xác nhận</button>
                <button class="btn btn-sm btn-danger">Từ chối</button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
