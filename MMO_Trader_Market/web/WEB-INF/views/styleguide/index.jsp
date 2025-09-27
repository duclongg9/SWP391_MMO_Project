<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thư viện giao diện - MMO Trader Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body class="layout">
<header class="layout__header layout__header--split">
    <div>
        <h1>Thư viện giao diện</h1>
        <p class="subtitle">Template UI cho dự án Servlet + JSP (MVC)</p>
    </div>
    <nav class="menu menu--horizontal">
        <a class="menu__item" href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
        <a class="menu__item" href="${pageContext.request.contextPath}/products">Sản phẩm</a>
        <a class="menu__item menu__item--ghost" href="${pageContext.request.contextPath}/auth">Đăng nhập</a>
    </nav>
</header>
<main class="layout__content guide">
    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Cách sử dụng template</h2>
        </div>
        <div class="panel__body">
            <ol class="guide__steps">
                <li>Sử dụng class <code>layout</code> để tạo khung trang cơ bản gồm header, nội dung và footer.</li>
                <li>Mọi thành phần UI chính đều có class bắt đầu bằng <code>layout__</code>, <code>menu__</code>, <code>search-bar__</code>, <code>table__</code>... giúp tái sử dụng.</li>
                <li>Copy nguyên block HTML trong từng ví dụ bên dưới và điều chỉnh nội dung động bằng EL (<code>&#36;{}</code>) hoặc scriptlet.</li>
                <li>Icon sử dụng emoji hoặc thư viện ngoài (Material Symbols, FontAwesome) thông qua class <code>icon</code> để canh giữa.</li>
            </ol>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Thanh tìm kiếm</h2>
            <code class="panel__tag">.search-bar</code>
        </div>
        <div class="panel__body">
            <p>Thanh tìm kiếm gọn nhẹ với icon nằm trong label. Bạn có thể đặt trong form GET hoặc POST.</p>
            <form class="search-bar" role="search">
                <label class="search-bar__icon" for="search-sample">🔍</label>
                <input class="search-bar__input" id="search-sample" name="q" type="search" placeholder="Tìm kiếm giao dịch, sản phẩm...">
                <button class="button button--primary" type="submit">Tìm</button>
            </form>
            <pre class="code-block"><code>&lt;form class="search-bar" method="get" action="${pageContext.request.contextPath}/products"&gt;
    &lt;label class="search-bar__icon" for="keyword"&gt;🔍&lt;/label&gt;
    &lt;input class="search-bar__input" id="keyword" name="keyword" type="search" placeholder="Nhập từ khóa..."&gt;
    &lt;button class="button button--primary" type="submit"&gt;Tìm kiếm&lt;/button&gt;
&lt;/form&gt;</code></pre>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Menu điều hướng</h2>
            <code class="panel__tag">.menu</code>
        </div>
        <div class="panel__body">
            <nav class="menu menu--horizontal menu--boxed">
                <a class="menu__item menu__item--active" href="#">Tổng quan</a>
                <a class="menu__item" href="#">Sản phẩm</a>
                <a class="menu__item" href="#">Đơn hàng</a>
                <a class="menu__item menu__item--ghost" href="#">Cài đặt</a>
            </nav>
            <pre class="code-block"><code>&lt;nav class="menu menu--horizontal"&gt;
    &lt;a class="menu__item menu__item--active" href="${pageContext.request.contextPath}/dashboard"&gt;Dashboard&lt;/a&gt;
    &lt;a class="menu__item" href="${pageContext.request.contextPath}/products"&gt;Sản phẩm&lt;/a&gt;
    &lt;a class="menu__item menu__item--danger" href="${pageContext.request.contextPath}/auth?action=logout"&gt;Đăng xuất&lt;/a&gt;
&lt;/nav&gt;</code></pre>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Bảng dữ liệu</h2>
            <code class="panel__tag">.table</code>
        </div>
        <div class="panel__body">
            <table class="table table--interactive">
                <thead>
                <tr>
                    <th>Mã</th>
                    <th>Tên</th>
                    <th>Mô tả</th>
                    <th>Giá</th>
                    <th>Trạng thái</th>
                    <th class="table__actions">Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>SP-001</td>
                    <td>Gói coin VIP</td>
                    <td>2.000 coin dùng trong game</td>
                    <td>350.000 đ</td>
                    <td><span class="badge">ACTIVE</span></td>
                    <td class="table__actions">
                        <button class="button button--ghost" type="button">Sửa</button>
                        <button class="button button--danger" type="button">Xóa</button>
                    </td>
                </tr>
                <tr>
                    <td>SP-002</td>
                    <td>Tài khoản luyện sẵn</td>
                    <td>Level 80, trang bị full</td>
                    <td>1.500.000 đ</td>
                    <td><span class="badge badge--warning">PENDING</span></td>
                    <td class="table__actions">
                        <button class="button button--ghost" type="button">Sửa</button>
                        <button class="button button--danger" type="button">Xóa</button>
                    </td>
                </tr>
                </tbody>
            </table>
            <pre class="code-block"><code>&lt;table class="table table--interactive"&gt;
    &lt;thead&gt;...&lt;/thead&gt;
    &lt;tbody&gt;
    &lt;% for (Product product : products) { %&gt;
        &lt;tr&gt;
            &lt;td&gt;&lt;%= product.getId() %&gt;&lt;/td&gt;
            &lt;td&gt;&lt;%= product.getName() %&gt;&lt;/td&gt;
            &lt;td&gt;&lt;%= product.getDescription() %&gt;&lt;/td&gt;
            &lt;td&gt;&lt;%= product.getPrice() %&gt; đ&lt;/td&gt;
            &lt;td&gt;&lt;span class="badge"&gt;&lt;%= product.getStatus() %&gt;&lt;/span&gt;&lt;/td&gt;
        &lt;/tr&gt;
    &lt;% } %&gt;
    &lt;/tbody&gt;
&lt;/table&gt;</code></pre>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Phân trang</h2>
            <code class="panel__tag">.pagination</code>
        </div>
        <div class="panel__body">
            <nav class="pagination" aria-label="Ví dụ phân trang">
                <a class="pagination__item pagination__item--disabled" href="#" aria-disabled="true">«</a>
                <a class="pagination__item pagination__item--active" href="#">1</a>
                <a class="pagination__item" href="#">2</a>
                <a class="pagination__item" href="#">3</a>
                <a class="pagination__item" href="#">»</a>
            </nav>
            <pre class="code-block"><code>&lt;nav class="pagination" aria-label="Phân trang"&gt;
    &lt;a class="pagination__item pagination__item--disabled" href="#"&gt;«&lt;/a&gt;
    &lt;a class="pagination__item pagination__item--active" href="#"&gt;1&lt;/a&gt;
    &lt;a class="pagination__item" href="#"&gt;2&lt;/a&gt;
    &lt;a class="pagination__item" href="#"&gt;3&lt;/a&gt;
    &lt;a class="pagination__item" href="#"&gt;»&lt;/a&gt;
&lt;/nav&gt;</code></pre>
        </div>
    </section>

    <section class="panel">
        <div class="panel__header">
            <h2 class="panel__title">Icon và badge</h2>
            <code class="panel__tag">.icon / .badge</code>
        </div>
        <div class="panel__body icon-showcase">
            <div class="icon icon--primary">📈</div>
            <div class="icon icon--accent">⭐</div>
            <div class="icon icon--danger">⚠️</div>
            <div class="icon icon--muted">ℹ️</div>
            <span class="badge">ACTIVE</span>
            <span class="badge badge--warning">PENDING</span>
            <span class="badge badge--danger">BLOCKED</span>
        </div>
        <pre class="code-block"><code>&lt;div class="icon icon--primary"&gt;📈&lt;/div&gt;
&lt;span class="badge badge--warning"&gt;PENDING&lt;/span&gt;</code></pre>
    </section>
</main>
<footer class="layout__footer">
    <small>&copy; 2024 MMO Trader Market</small>
</footer>
</body>
</html>
