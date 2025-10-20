<%@ page contentType="text/html; charset=UTF-8" %>

<div class="container-fluid"><!-- Bộ lọc -->
    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <div class="row g-2 align-items-end">
                <!-- Từ khóa -->
                <div class="col-12 col-md-3">
                    <label class="form-label mb-1">Từ khóa</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input id="keyword" type="text" class="form-control" placeholder="Tên hoặc email...">
                    </div>
                </div>

                <!-- Vai trò -->
                <div class="col-12 col-md-2">
                    <label class="form-label mb-1">Vai trò</label>
                    <select id="role" class="form-select">
                        <option value="">Tất cả</option>
                        <option value="buyer">Buyer</option>
                        <option value="seller">Seller</option>
                        <option value="admin">Admin</option>
                    </select>
                </div>

                <!-- Từ ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1">Từ ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                        <input id="fromDate" type="text" class="form-control" placeholder="DD-MM-YYYY">
                    </div>
                </div>

                <!-- Đến ngày -->
                <div class="col-6 col-md-2">
                    <label class="form-label mb-1">Đến ngày</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-calendar-check"></i></span>
                        <input id="toDate" type="text" class="form-control" placeholder="DD-MM-YYYY">
                    </div>
                </div>

                <!-- Nút -->
                <div class="col-12 col-md-3 d-flex gap-2">
                    <button id="btnSearch" class="btn btn-dark flex-fill">
                        <i class="bi bi-funnel"></i> Tìm kiếm
                    </button>
                    <button id="btnCreate" class="btn btn-primary flex-fill">
                        <i class="bi bi-plus-circle"></i> Thêm người dùng
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bảng -->
    <div class="table-responsive shadow-sm rounded-3">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
            <tr>
                <th class="text-center" style="width:72px">#</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th style="width:140px">Trạng thái</th>
                <th style="width:160px">Ngày tạo</th>
                <th style="width:140px">Vai trò</th>
                <th class="text-center" style="width:170px">Hành động</th>
            </tr>
            </thead>
            <tbody id="userTableBody">
            <!-- Demo 1 dòng -->
            <tr>
                <td class="text-center">1</td>
                <td>Trần Minh Khoa</td>
                <td>khoa@gmail.com</td>
                <td><span class="badge bg-success rounded-pill">Hoạt động</span></td>
                <td>2025-10-12</td>
                <td>Khách hàng</td>
                <td class="text-center">
                    <div class="btn-group">
                        <button class="btn btn-sm btn-outline-info">
                            <i class="bi bi-pencil-square"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="text-center">1</td>
                <td>Trần Minh c</td>
                <td>khoa@gmail.com</td>
                <td><span class="badge bg-success rounded-pill">Hoạt động</span></td>
                <td>2025-10-12</td>
                <td>Khách hàng</td>
                <td class="text-center">
                    <div class="btn-group">
                        <button class="btn btn-sm btn-outline-info">
                            <i class="bi bi-pencil-square"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="text-center">1</td>
                <td>Trần Minh b</td>
                <td>khoa@gmail.com</td>
                <td><span class="badge bg-success rounded-pill">Hoạt động</span></td>
                <td>2025-10-12</td>
                <td>Khách hàng</td>
                <td class="text-center">
                    <div class="btn-group">
                        <button class="btn btn-sm btn-outline-info">
                            <i class="bi bi-pencil-square"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
            <!-- /Demo -->
            </tbody>
        </table>
    </div>
</div>