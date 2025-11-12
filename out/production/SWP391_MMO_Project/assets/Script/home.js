(function () {
     // Tải 1 fragment vào container được chỉ định
    function loadFragment(container) {
        var url = container.getAttribute('data-fragment-url');
        if (!url) {
            return;
        }
        fetch(url, {
            headers: {
                // Header gợi ý cho server: đây là request AJAX
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok'); // HTTP 4xx/5xx → coi như lỗi
                }
                return response.text();// nhận HTML dạng text
            })
            .then(function (html) {
                container.innerHTML = html; // render fragment
                container.classList.remove('fragment--error');
            })
            .catch(function () {
                container.classList.add('fragment--error');
                container.innerHTML = '<p>Không thể tải dữ liệu. Vui lòng thử lại sau.</p>';
            });
    }
// Khi DOM đã sẵn sàng, quét và nạp tất cả fragment
    document.addEventListener('DOMContentLoaded', function () {
        var containers = document.querySelectorAll('[data-fragment-url]');
        containers.forEach(loadFragment);
    });
})();
