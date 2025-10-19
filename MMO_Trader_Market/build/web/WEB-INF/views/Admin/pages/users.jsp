<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Quản lý người dùng</h4>

    <div class="mb-3 d-flex justify-content-between">
        <input type="text" class="form-control w-25" placeholder="Tìm theo tên hoặc email...">
        <button class="btn btn-primary"><i class="bi bi-plus-circle"></i> Thêm người dùng</button>
    </div>

    <table class="table table-hover align-middle shadow-sm">
        <thead class="table-dark">
        <tr>
            <th>#</th>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Vai Trò</th>
            <th class="text-center">Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>1</td><td>Trần Minh Khoa</td><td>khoa@gmail.com</td>
            <td><span class="badge bg-success">Hoạt động</span></td>
            <td>12/10/2025</td>
            <td>Khách Hàng</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-info"><i class="bi bi-pencil">Xóa</i></button>
                <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash">Báo Cáo</i></button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
