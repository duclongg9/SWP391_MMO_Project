<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>

<c:set var="base" value="${pageContext.request.contextPath}" />

<div class="container-fluid">
    <h4 class="mb-4"><i class="bi bi-shield-check me-2"></i>Danh sách KYC cần duyệt</h4>

    <div class="card shadow-sm">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                    <tr>
                        <th>#</th>
                        <th>Người dùng</th>
                        <th class="text-center">Mặt trước</th>
                        <th class="text-center">Mặt sau</th>
                        <th class="text-center">Selfie</th>
                        <th>Số giấy tờ</th>
                        <th>Ngày gửi</th>
                        <th>Trạng thái</th>
                        <th class="text-center" style="width:140px">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach var="k" items="${kycList}" varStatus="st">
                        <tr>
                            <td>${st.index + 1}</td>

                            <td>
                                <div class="fw-semibold">${k.userName}</div>
                                <div class="text-muted small">${k.userEmail}</div>
                            </td>

                            <td class="text-center">
                                <img src="${k.frontImageUrl}" alt="front"
                                     class="img-thumbnail" style="width:80px;height:56px;object-fit:cover;">
                            </td>
                            <td class="text-center">
                                <img src="${k.backImageUrl}" alt="back"
                                     class="img-thumbnail" style="width:80px;height:56px;object-fit:cover;">
                            </td>
                            <td class="text-center">
                                <img src="${k.selfieImageUrl}" alt="selfie"
                                     class="img-thumbnail" style="width:80px;height:56px;object-fit:cover;">
                            </td>

                            <td>${k.idNumber}</td>
                            <td><fmt:formatDate value="${k.createdAt}" pattern="HH:mm:ss, dd-MM-yyyy"/></td>

                            <td>
                <span class="badge
                  <c:choose>
                    <c:when test="${k.statusName eq 'Pending'}">bg-warning text-dark</c:when>
                    <c:when test="${k.statusName eq 'Approved'}">bg-success</c:when>
                    <c:when test="${k.statusName eq 'Rejected'}">bg-danger</c:when>
                    <c:otherwise>bg-secondary</c:otherwise>
                  </c:choose>">
                        ${k.statusName}
                </span>
                            </td>

                            <td class="text-center">
                                <button class="btn btn-sm btn-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#kycModal_${k.id}">
                                    <i class="bi bi-eye"></i> Xem chi tiết
                                </button>
                            </td>
                        </tr>

                        <!-- Modal chi tiết (mỗi KYC một modal) -->
                        <div class="modal fade" id="kycModal_${k.id}" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-dark text-white">
                                        <h5 class="modal-title">KYC #${k.id} – ${k.userName}</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                    </div>

                                    <div class="modal-body">
                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <div class="small text-muted mb-1">Email</div>
                                                <div class="fw-semibold">${k.userEmail}</div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="small text-muted mb-1">Số giấy tờ</div>
                                                <div class="fw-semibold">${k.idNumber}</div>
                                            </div>

                                            <div class="col-12"><hr/></div>

                                            <div class="col-md-4 text-center">
                                                <div class="small text-muted mb-2">Mặt trước</div>
                                                <img src="${k.frontImageUrl}" class="img-fluid rounded shadow-sm" alt="front">
                                            </div>
                                            <div class="col-md-4 text-center">
                                                <div class="small text-muted mb-2">Mặt sau</div>
                                                <img src="${k.backImageUrl}" class="img-fluid rounded shadow-sm" alt="back">
                                            </div>
                                            <div class="col-md-4 text-center">
                                                <div class="small text-muted mb-2">Selfie</div>
                                                <img src="${k.selfieImageUrl}" class="img-fluid rounded shadow-sm" alt="selfie">
                                            </div>

                                            <div class="col-12 mt-3">
                                                <div class="small text-muted mb-1">Ghi chú/Phản hồi quản trị</div>
                                                <form action="${base}/admin/kycs/status" method="post">
                                                    <input type="hidden" name="id" value="${k.id}"/>
                                                    <textarea name="feedback" class="form-control" rows="3"
                                                              placeholder="Ghi chú cho người dùng (bắt buộc khi từ chối)">${k.adminFeedback}</textarea>

                                                    <div class="d-flex gap-2 mt-3">
                                                        <button class="btn btn-success"  name="action" value="approve">
                                                            <i class="bi bi-check-circle"></i> Accept
                                                        </button>
                                                        <button class="btn btn-danger"   name="action" value="reject"
                                                                onclick="return confirm('Từ chối KYC này?');">
                                                            <i class="bi bi-x-circle"></i> Reject
                                                        </button>
                                                        <button type="button" class="btn btn-secondary ms-auto" data-bs-dismiss="modal">
                                                            Đóng
                                                        </button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </div>
                        <!-- /Modal -->
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<style>.table td,.table th{vertical-align:middle}</style>
