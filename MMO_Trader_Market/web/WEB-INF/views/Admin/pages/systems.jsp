<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Cấu hình hệ thống</h4>

    <c:if test="${not empty flash}">
        <div class="alert alert-success shadow-sm">${flash}</div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger shadow-sm">${flashError}</div>
    </c:if>

    <form class="row g-3 shadow-sm bg-white p-4 rounded" method="post"
          action="${pageContext.request.contextPath}/admin/systems/escrow">
        <div class="col-md-6">
            <label class="form-label" for="escrowHoldHours">Thời gian giữ tiền escrow (giờ)</label>
            <input type="number" class="form-control" id="escrowHoldHours" name="escrowHoldHours"
                   min="1" max="720" required value="${escrowHoldHours}" />
            <div class="form-text">
                Tiền của người mua sẽ được giữ tối đa <strong>${escrowHoldHours}</strong> giờ
                (${escrowHoldSeconds} giây) trước khi hệ thống tự động giải ngân cho người bán nếu không phát sinh khiếu nại.
            </div>
        </div>
        <div class="col-12 text-end mt-3">
            <button class="btn btn-primary" type="submit"><i class="bi bi-save"></i> Lưu cấu hình</button>
        </div>
    </form>
</div>
