<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shop me-2"></i>Quản lý cửa hàng</h4>

    <div class="card shadow-sm">
        <div class="card-body">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                <tr>
                    <th>#</th>
                    <th>Tên cửa hàng</th>
                    <th>Email</th>
                    <th>Danh mục</th>
                    <th>Trạng thái</th>
                    <th>Ngày đăng ký</th>
                    <th class="text-center">Hành động</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>1</td>
                    <td><i class="bi bi-shop me-1"></i> Cửa hàng A</td>
                    <td>a@shop.vn</td>
                    <td><span class="badge bg-info text-dark">Thời trang</span></td>
                    <td><span class="badge bg-success">Hoạt động</span></td>
                    <td>10/10/2025</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-primary"><i class="bi bi-eye"></i> Xem</button>
                    </td>
                </tr>
                <tr>
                    <td>2</td>
                    <td><i class="bi bi-shop me-1"></i> Cửa hàng B</td>
                    <td>b@shop.vn</td>
                    <td><span class="badge bg-warning text-dark">Điện tử</span></td>
                    <td><span class="badge bg-secondary">Chờ duyệt</span></td>
                    <td>15/10/2025</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-success"><i class="bi bi-check-circle"></i> Duyệt</button>
                        <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i> Không Duyệt </button>
                    </td>
                </tr>
                <tr>
                    <td>3</td>
                    <td><i class="bi bi-shop me-1"></i> Cửa hàng C</td>
                    <td>c@shop.vn</td>
                    <td><span class="badge bg-primary">Mỹ phẩm</span></td>
                    <td><span class="badge bg-danger">Đã khóa</span></td>
                    <td>05/10/2025</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-arrow-clockwise"></i> Mở lại</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<style>
    .card {
        border-radius: 10px;
    }
    th {
        white-space: nowrap;
    }
    td {
        vertical-align: middle;
    }
    table {
        font-size: 15px;
    }
</style>
