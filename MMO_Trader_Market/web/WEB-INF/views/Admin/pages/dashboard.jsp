<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/components/insight.css"/>
<fmt:setLocale value="vi_VN" />
<div class="container-fluid">
    <h4 class="mb-4">Tổng quan hệ thống</h4>

    <div class="card mt-4 p-4">
        <h5 class="mb-3"><i class="bi bi-bar-chart"></i>Doanh thu tháng hiện tại</h5>
    <div class="row g-3">
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-cart-check display-6 text-primary"></i>
                <h6 class="mt-2">Đơn hàng hôm nay</h6>
                <h3 class="fw-bold text-dark">${orderByMonth}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
                <i class="bi bi-shop display-6 text-warning"></i>
                <h6 class="mt-2">Cửa hàng hoạt động</h6>
                <h3 class="fw-bold text-dark">${shopByMonth}</h3>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card shadow-sm text-center p-3">
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
            <div class="card shadow-sm text-center p-3">
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
        <h5 class="mb-0"><i class="bi bi-bar-chart"></i> Biểu đồ doanh thu theo năm</h5>
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
            <div class="d-flex justify-content-between align-items-center mb-3">
                 <canvas id="chartRevenue" height="120"></canvas>
            </div>    
</div>
            <style>
                .shadow-sm{
                    height: 190px;
                }
            </style>    
            
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    
   <script>
    const depositData = ${depositByMonth};
    const withdrawData = ${withdrawByMonth};
    

    const labels = ["Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"];

    new Chart(document.getElementById("chartRevenue"), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                { label: "Tiền nạp", data: depositData, backgroundColor: "rgba(37,99,235,0.6)" },
                { label: "Tiền rút", data: withdrawData, backgroundColor: "rgba(239,68,68,0.6)" }
            ]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true } }
        }
    });
</script>

