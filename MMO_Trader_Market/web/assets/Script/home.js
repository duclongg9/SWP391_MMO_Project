(function () {
    function loadFragment(container) {
        var url = container.getAttribute('data-fragment-url');
        if (!url) {
            return;
        }
        fetch(url, {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(function (html) {
                container.innerHTML = html;
                container.classList.remove('fragment--error');
            })
            .catch(function () {
                container.classList.add('fragment--error');
                container.innerHTML = '<p>Không thể tải dữ liệu. Vui lòng thử lại sau.</p>';
            });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var containers = document.querySelectorAll('[data-fragment-url]');
        containers.forEach(loadFragment);
    });
})();
