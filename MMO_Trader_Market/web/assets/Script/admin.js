// admin.js
(() => {
    const base = window.APP_BASE || "";

    const titleMap = {
        dashboard: "Tổng quan",
        users: "Quản lý người dùng",
        shops: "Quản lý gian hàng",
        kyc: "Duyệt KYC",
        cash: "Nạp / Rút",
        settings: "Cấu hình hệ thống"
    };

    document.addEventListener("DOMContentLoaded", () => {
        // ----- Nav giả lập (nếu bạn dùng) -----
        document.querySelectorAll('.nav-link[data-tab]').forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                document.querySelectorAll('.nav-link').forEach(a => a.classList.remove('active'));
                link.classList.add('active');
                const tab = link.dataset.tab;
                const titleEl = document.getElementById('pageTitle');
                const contentEl = document.getElementById('tabContent');
                if (titleEl)
                    titleEl.textContent = titleMap[tab] || "";
                if (contentEl) {
                    contentEl.innerHTML =
                            `<div class="card p-3 mt-2">
               <h5>${titleMap[tab] || ""}</h5>
               <p>Nội dung tab: <b>${tab}</b></p>
             </div>`;
                }
            });
        });

        // ----- Logout (nếu có nút) -----
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', e => {
                e.preventDefault();
                if (confirm('Bạn có chắc muốn đăng xuất không?')) {
                    window.location.href = base + "/index.html";
                }
            });
        }

        // ====== FLATPICKR ======
        if (typeof flatpickr === "undefined") {
            // Nếu thiếu CDN flatpickr sẽ không chạy
            console.warn("flatpickr chưa được nạp!");
            return;
        }

        const fromInput = document.getElementById("fromDate");
        const toInput = document.getElementById("toDate");

        let fromPicker = null, toPicker = null;

        if (fromInput) {
            fromPicker = flatpickr(fromInput, {
                dateFormat: "Y-m-d",
                maxDate: "today",
                allowInput: true,
                onChange: ([date]) => {
                    if (toPicker)
                        toPicker.set("minDate", date || null);
                }
            });
        }

        if (toInput) {
            toPicker = flatpickr(toInput, {
                dateFormat: "Y-m-d",
                maxDate: "today",
                allowInput: true,
                onChange: ([date]) => {
                    if (fromPicker)
                        fromPicker.set("maxDate", date || "today");
                }
            });
        }

        // Nút mở lịch (nếu có)
        const btnFrom = document.getElementById('btnFrom');
        if (btnFrom && fromPicker)
            btnFrom.addEventListener('click', () => fromPicker.open());

        const btnTo = document.getElementById('btnTo');
        if (btnTo && toPicker)
            btnTo.addEventListener('click', () => toPicker.open());

        // Tìm kiếm (nếu có)
        const btnSearch = document.getElementById('btnSearch');
        if (btnSearch) {
            btnSearch.addEventListener('click', () => {
                const q = (document.getElementById('keyword') || {}).value?.trim() || "";
                const rol = (document.getElementById('role') || {}).value || "";
                const f = (fromInput || {}).value || "";
                const t = (toInput || {}).value || "";
                console.log('Search with:', {q, rol, f, t});

                // TODO: fetch(`${base}/admin/users?keyword=${encodeURIComponent(q)}&role=${rol}&from=${f}&to=${t}`)
            });
        }
    });
})();
