<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Cấu hình hệ thống</h4>

    <form class="row g-3 shadow-sm bg-white p-4 rounded">
        <div class="col-md-4">
            <label class="form-label">Phí sàn (%)</label>
            <input type="number" class="form-control" value="2.5">
        </div>
        <div class="col-md-4">
            <label class="form-label">Phí nạp (%)</label>
            <input type="number" class="form-control" value="0.5">
        </div>
        <div class="col-md-4">
            <label class="form-label">Phí rút (%)</label>
            <input type="number" class="form-control" value="1.0">
        </div>
        <div class="col-md-6">
            <label class="form-label">Giới hạn rút / ngày (₫)</label>
            <input type="text" class="form-control" value="10000000">
        </div>
        <div class="col-md-6">
            <label class="form-label">Thời gian Escrow (giờ)</label>
            <input type="number" class="form-control" value="72">
        </div>
        <div class="col-12 text-end mt-3">
            <button class="btn btn-primary"><i class="bi bi-save"></i> Lưu cấu hình</button>
        </div>
    </form>
</div>
