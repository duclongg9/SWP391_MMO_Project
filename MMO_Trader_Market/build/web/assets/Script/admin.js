

const base = '${pageContext.request.contextPath}';
const titleMap = {
    dashboard: 'Tổng quan',
    users: 'Quản lý người dùng',
    shops: 'Quản lý gian hàng',
    kyc: 'Duyệt KYC',
    cash: 'Nạp / Rút',
    settings: 'Cấu hình hệ thống'
};

// Chuyển tab (giả lập load partial)
document.querySelectorAll('.nav-link[data-tab]').forEach(link => {
    link.addEventListener('click', e => {
        e.preventDefault();
        document.querySelectorAll('.nav-link').forEach(a => a.classList.remove('active'));
        link.classList.add('active');
        const tab = link.dataset.tab;
        document.getElementById('pageTitle').textContent = titleMap[tab];
        document.getElementById('tabContent').innerHTML = `<div class="card p-3 mt-2"><h5>${titleMap[tab]}</h5><p>Nội dung tab: <b>${tab}</b></p></div>`;
    });
});

// Nút đăng xuất (chỉ là alert / redirect)
document.getElementById('logoutBtn').addEventListener('click', e => {
    e.preventDefault();
    if (confirm('Bạn có chắc muốn đăng xuất không?')) {
        window.location.href = base + "/index.html"; // quay về trang chính hoặc login
    }
});