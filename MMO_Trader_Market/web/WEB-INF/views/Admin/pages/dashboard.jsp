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
    <div class="row g-3">
        <div class="col-md-4">
            <div class="card shadow-sm text-center p-3 chart-toggle" data-target="chartUserContainer" data-chart-type="user">
                <i class="bi bi-people display-6 text-primary"></i>
                <h6 class="mt-2">Số lượng người dùng</h6>
                <h3 class="fw-bold text-dark">${totalUser}</h3>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card shadow-sm text-center p-3 chart-toggle"data-target="chartOrderContainer" data-chart-type="order">
                <i class="bi bi-cart-check display-6 text-primary"></i>
                <h6 class="mt-2">Đơn hàng tháng này</h6>
                <h3 class="fw-bold text-dark">${totalOrder}</h3>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card shadow-sm text-center p-3 chart-toggle" data-target="chartShopContainer" data-chart-type="shop">
                <i class="bi bi-shop display-6 text-warning"></i>
                <h6 class="mt-2">Cửa hàng hoạt động</h6>
                <h3 class="fw-bold text-dark">${shopByMonth}</h3>
            </div>
        </div>
            </div>
            <div class="row g-3">
        <div class="col-md-6">
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
        <div class="col-md-6">  
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
           <div id="chartOrderContainer" class="chart-container mt-3" >
              <canvas id="chartOrderDetail"></canvas>
            </div>
</div>
            
            
            <style>
                .shadow-sm{
                    height: 180px;
                }
              #chartShopStatus {
                display: block;           /* để margin auto có tác dụng */
                width: 1000px;             /* chiều rộng cố định */
                height: 1000px;            /* chiều cao cố định */
                margin: 50px auto;        /* căn giữa */
}
            </style>
            
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"></script>
    
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

            // Nếu là Revenue, muốn cả 2 card nạp/rút cùng đậm
            if (card.dataset.chartType === "revenue") {
                document.querySelectorAll('.chart-toggle[data-chart-type="revenue"]').forEach(c => c.classList.add('active'));
            } else {
                card.classList.add('active');
            }

            // Kiểm tra loại chart và vẽ nếu chưa load
            const chartType = card.dataset.chartType;
            if (!container.dataset.loaded) {
                if (chartType === "revenue") {
                    drawRevenueChart();
                } else if (chartType === "shop") {
                    drawShopChart();
                } else if(chartType === "order"){
                    drawOrderChart();
                }
                container.dataset.loaded = "true";
            }
        }
        // Nếu đang mở, click lại sẽ ẩn và bỏ đậm
        // (đã thực hiện ở bước trên khi bỏ tất cả active và display = "none")
    });
});

// Biểu đồ cột: Tiền nạp / Tiền rút
function drawRevenueChart() {
    const ctx = document.getElementById('chartRevenueDetail');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"],
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

function drawShopChart() {
    const ctx = document.getElementById('chartShopStatus');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Hoạt Động', 'Đang chờ duyệt', 'Bị khóa'],
            datasets: [{
                label: 'Số lượng shop',
                data: [${totalActiveShop}, ${totalPendingShop}, ${totalSuspended}],
                backgroundColor: [
                    'rgba(37,99,235,0.7)',
                    'rgba(250,204,21,0.7)',
                    'rgba(239,68,68,0.7)'
                ]
            }]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });

}

function drawOrderChart() {
    const ctx = document.getElementById('chartOrderDetail');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"],
            datasets: [
                { label: "Số lượng đơn hàng", data: ${orderByMonth}, backgroundColor: "rgba(37,99,235,0.6)" }
            ]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
            plugins: { legend: { position: 'bottom' } }
        }
    });
}

</script>

