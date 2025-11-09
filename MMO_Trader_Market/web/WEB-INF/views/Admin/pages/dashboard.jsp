<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/components/insight.css"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/components/chart.css"/>
<fmt:setLocale value="vi_VN" />
<div class="container-fluid">
    <div class="card mt-4 p-4">
        <h5 class="mb-3"><i class="bi bi-bar-chart"></i>Doanh thu tháng hiện tại</h5>
    <div class="row g-3">
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3 chart-toggle">
                <i class="bi bi-cart-check display-6 text-primary"></i>
                <h6 class="mt-2">Đơn hàng hôm nay</h6>
                <h3 class="fw-bold text-dark">${orderByMonth}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3 chart-toggle" data-target="chartShopContainer" data-chart-type="shop">
                <i class="bi bi-shop display-6 text-warning"></i>
                <h6 class="mt-2">Cửa hàng hoạt động</h6>
                <h3 class="fw-bold text-dark">${shopByMonth}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3 chart-toggle" data-target="chartRevenueContainer" data-chart-type="revenue">
                <i class="bi bi-cash-coin display-6 text-primary"></i>
                <h6 class="mt-2">Tổng tiền nạp</h6>
                <h3 class="fw-bold text-dark"><fmt:formatNumber value="${totalDeposit}" type="currency" currencySymbol="VND" groupingUsed="true"/></h3>
                <c:choose>
                    <c:when test="${persentDepositChanged > 0}">
                        <div class="insight-chip up">${persentDepositChanged}% so với tháng trước</div>
                    </c:when>
                    <c:when test="${persentDepositChanged < 0}">
                        <div class="insight-chip down">${persentDepositChanged}% so với tháng trước</div>
                    </c:when>
                    <c:otherwise>
                        <div class="insight-chip neutral">0% so với tháng trước</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="col-md-3">  
            <div class="card shadow-sm text-center p-3 chart-toggle" data-target="chartRevenueContainer" data-chart-type="revenue">
                <i class="bi bi-cash-coin display-6 text-danger"></i>
                <h6 class="mt-2">Tổng tiền rút</h6>
                <h3 class="fw-bold text-dark"><fmt:formatNumber value="${totalWithdraw}" type="currency" currencySymbol="VND" groupingUsed="true"/></h3>
                <c:choose>
                    <c:when test="${persentWithdrawChanged > 0}">
                        <div class="insight-chip up">${persentWithdrawChanged}% so với tháng trước</div>
                    </c:when>
                    <c:when test="${persentWithdrawChanged < 0}">
                        <div class="insight-chip down">${persentWithdrawChanged}% so với tháng trước</div>
                    </c:when>
                    <c:otherwise>
                        <div class="insight-chip neutral">0% so với tháng trước</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    </div>
            
    <div class="card mt-4 p-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="mb-0"><i class="bi bi-bar-chart"></i> Biểu đồ tổng quan theo năm</h5>
        <!-- Dropdown chọn năm -->
        <form action="${pageContext.request.contextPath}/admin/dashboard" method="get">
            <label for="yearSelect" class="me-2">Chọn năm:</label>
            <select id="yearSelect" name="year" class="form-select w-auto d-inline-block" onchange="this.form.submit()">
<c:forEach var="y" items="${years}">
                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
</c:forEach>
            </select>
        </form>
           
    </div>
           <!-- Khung biểu đồ dùng chung -->
            <div id="chartRevenueContainer" class="chart-container mt-3" >
              <canvas id="chartRevenueDetail"></canvas>
            </div>
           <div id="chartShopContainer" class="chart-container mt-3" style="display:none;">
                <canvas id="chartShopStatus"></canvas>
           </div>
</div>
            
            
            <style>
                .shadow-sm{
                    height: 180px;
                }
              #chartShopStatus {
                    max-width: 500px;   /* chiều rộng tối đa */
                    max-height: 500px;  /* chiều cao tối đa */
                    margin: 0.5 auto;     /* căn giữa */
                }
            </style>
            
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels"></script>
    
   <script>
  document.querySelectorAll('.chart-toggle').forEach(card => {
  card.addEventListener('click', () => {
    const targetId = card.getAttribute('data-target');
    const container = document.getElementById(targetId);
    if (!container) return;

    // Kiểm tra container đang mở hay chưa
    const isOpen = container.style.display === "block";

    // Đóng tất cả container và bỏ active
    document.querySelectorAll('.chart-container').forEach(el => el.style.display = "none");
    document.querySelectorAll('.chart-toggle').forEach(c => c.classList.remove('active'));

    if (!isOpen) {
        // Mở container và active card
        container.style.display = "block";
        card.classList.add('active');

        // Kiểm tra loại chart
        const chartType = card.dataset.chartType;
        if (!container.dataset.loaded) {
            if (chartType === "revenue") {
                drawRevenueChart();
            } else if (chartType === "shop") {
                drawShopChart();
            }
            container.dataset.loaded = "true";
        }
    }
  });
});

// Biểu đồ cột: Tiền nạp / Tiền rút
function drawRevenueChart() {
    const ctx = document.getElementById('chartRevenueDetail');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["Th1","Th2","Th3","Th4","Th5","Th6","Th7","Th8","Th9","Th10","Th11","Th12"],
            datasets: [
                {
                    label: "Tiền nạp",
                    data: ${depositByMonth},
                    backgroundColor: "rgba(37,99,235,0.6)"
                },
                {
                    label: "Tiền rút",
                    data: ${withdrawByMonth},
                    backgroundColor: "rgba(239,68,68,0.6)"
                }
            ]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true } }
        }
    });
}

// Biểu đồ tròn: Trạng thái shop
function drawShopChart() {
    const ctx = document.getElementById('chartShopStatus');
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Hoạt Động', 'Đang chờ duyệt', 'Bị khóa'],
            datasets: [{
                data: [
                    ${totalActiveShop},
                    ${totalPendingShop},
                    ${totalSuspended}
                ],
                backgroundColor: [
                    'rgba(37,99,235,0.7)',  // Active
                    'rgba(250,204,21,0.7)', // Pending
                    'rgba(239,68,68,0.7)'   // Suspended
                ],
                borderColor: [
                    'rgba(37,99,235,1)',
                    'rgba(250,204,21,1)',
                    'rgba(239,68,68,1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = context.raw;
                            return `${context.label}: ${value}`; // hiển thị số lượng khi hover
                        }
                    }
                }
            }
        }
    });
}

</script>

