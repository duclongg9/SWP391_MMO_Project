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
        <div class="col-lg-8 col-md-10">
            <label class="form-label" for="escrowHoldHours">Thời gian giữ tiền escrow</label>
            <div class="d-flex flex-wrap gap-2">
                <div class="input-group" style="max-width: 160px;">
                    <input type="number" class="form-control" id="escrowHoldHours" name="escrowHoldHours"
                           min="0" max="720" value="${escrowHoldHoursPart}" />
                    <span class="input-group-text">Giờ</span>
                </div>
                <div class="input-group" style="max-width: 160px;">
                    <input type="number" class="form-control" id="escrowHoldMinutes" name="escrowHoldMinutes"
                           min="0" max="59" value="${escrowHoldMinutesPart}" />
                    <span class="input-group-text">Phút</span>
                </div>
                <div class="input-group" style="max-width: 160px;">
                    <input type="number" class="form-control" id="escrowHoldSeconds" name="escrowHoldSeconds"
                           min="0" max="59" value="${escrowHoldSecondsPart}" />
                    <span class="input-group-text">Giây</span>
                </div>
            </div>
            <div class="form-text mt-2">
                Tiền của người mua sẽ được giữ tối đa <strong>${escrowHoldDurationLabel}</strong>
                (<strong>${escrowHoldTotalSeconds}</strong> giây) trước khi hệ thống tự động giải ngân cho người bán nếu không phát sinh khiếu nại.
            </div>
        </div>
        <div class="col-12 text-end mt-3">
            <button class="btn btn-primary" type="submit"><i class="bi bi-save"></i> Lưu cấu hình</button>
        </div>
    </form>
</div>
