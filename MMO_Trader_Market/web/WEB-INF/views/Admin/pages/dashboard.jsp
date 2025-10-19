<%@ page contentType="text/html; charset=UTF-8" %>
<div class="container-fluid">
    <h4 class="mb-4">Tổng quan hệ thống</h4>

    <div class="row g-3">
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-cart-check display-6 text-primary"></i>
                <h6 class="mt-2">Đơn hàng hôm nay</h6>
                <h3 class="fw-bold text-dark">128</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-people-fill display-6 text-success"></i>
                <h6 class="mt-2">Người dùng mới</h6>
                <h3 class="fw-bold text-dark">35</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-shop display-6 text-warning"></i>
                <h6 class="mt-2">Cửa hàng hoạt động</h6>
                <h3 class="fw-bold text-dark">67</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-cash-coin display-6 text-danger"></i>
                <h6 class="mt-2">Doanh thu hôm nay</h6>
                <h3 class="fw-bold text-dark">42.5M₫</h3>
            </div>
        </div>
    </div>

    <div class="card mt-4 p-4">
        <h5 class="mb-3"><i class="bi bi-bar-chart"></i> Biểu đồ doanh thu 7 ngày</h5>
        <canvas id="chartRevenue" height="120"></canvas>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script>
        new Chart(document.getElementById("chartRevenue"), {
            type: "line",
            data: {
                labels: ["T2","T3","T4","T5","T6","T7","CN"],
                datasets: [{
                    label: "Doanh thu (triệu ₫)",
                    data: [12, 15, 11, 9, 18, 20, 14],
                    borderColor: "#2563eb",
                    backgroundColor: "rgba(37,99,235,0.1)",
                    fill: true,
                    tension: 0.4
                }]
            },
            options: { plugins: { legend: { display: false } } }
        });
    </script>
</div>
