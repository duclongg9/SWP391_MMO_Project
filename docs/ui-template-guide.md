# UI Template Guide

Trang `/styleguide` trong dự án được xây dựng như một style guide mini giúp bạn (đặc biệt là người mới) lấy component UI nhanh chóng. Tài liệu này mô tả chi tiết từng khối giao diện, class CSS liên quan và cách gắn dữ liệu động từ Servlet/JSP.

## 1. Layout cơ bản

```jsp
<body class="layout">
    <header class="layout__header">...</header>
    <main class="layout__content">...</main>
    <footer class="layout__footer">...</footer>
</body>
```

* `layout`: tạo khung 3 phần header/content/footer, kéo dài toàn màn hình.
* `layout--center`: dùng khi muốn nội dung chính canh giữa (ví dụ trang đăng nhập).
* `layout__header--split`: thêm border dưới và flex layout hai cột cho tiêu đề + menu.

## 2. Thanh tìm kiếm (`.search-bar`)

```jsp
<form class="search-bar" method="get" action="${pageContext.request.contextPath}/products">
    <label class="search-bar__icon" for="keyword">🔍</label>
    <input class="search-bar__input" id="keyword" name="keyword" type="search" placeholder="Nhập từ khóa...">
    <button class="button button--primary" type="submit">Tìm kiếm</button>
</form>
```

* Icon được đặt trong `<label>` để dễ thay thế (có thể dùng emoji, Material Symbols...).
* Input không viền, bám sát nền xám nhạt.
* Button có thể chuyển sang `button--ghost` nếu cần kiểu trung tính.

## 3. Menu điều hướng (`.menu`)

```jsp
<nav class="menu menu--horizontal">
    <a class="menu__item menu__item--active" href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
    <a class="menu__item" href="${pageContext.request.contextPath}/products">Sản phẩm</a>
    <a class="menu__item menu__item--danger" href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
</nav>
```

* `menu--horizontal`: xếp item theo hàng, tự co giãn theo màn hình.
* `menu__item--active`: highlight tab hiện tại.
* `menu__item--danger` hoặc `menu__item--ghost`: dùng cho thao tác hủy, đăng xuất, cấu hình.

## 4. Bảng dữ liệu (`.table`)

```jsp
<table class="table table--interactive">
    <thead>...</thead>
    <tbody>
    <% for (Product product : products) { %>
        <tr>
            <td><%= product.getId() %></td>
            <td><%= product.getName() %></td>
            <td><%= product.getDescription() %></td>
            <td><%= product.getPrice() %> đ</td>
            <td><span class="badge"><%= product.getStatus() %></span></td>
        </tr>
    <% } %>
    </tbody>
</table>
```

* `table--interactive` bổ sung hiệu ứng hover cho từng dòng.
* `table__actions` kết hợp với `.button` để tạo cột thao tác.
* Các badge trạng thái (`badge`, `badge--warning`, `badge--danger`) giúp phân biệt màu sắc trực quan.

## 5. Phân trang (`.pagination`)

```jsp
<nav class="pagination" aria-label="Phân trang">
    <a class="pagination__item pagination__item--disabled" href="#" aria-disabled="true">«</a>
    <a class="pagination__item pagination__item--active" href="#">1</a>
    <a class="pagination__item" href="#">2</a>
    <a class="pagination__item" href="#">3</a>
    <a class="pagination__item" href="#">»</a>
</nav>
```

* `pagination__item--active`: trang hiện tại.
* `pagination__item--disabled`: dùng cho nút Previous/Next khi không thể bấm.
* Các thẻ `<a>` có thể thay bằng `<button>` nếu bạn submit form.

## 6. Icon & Badge

```jsp
<div class="icon icon--primary">📈</div>
<span class="badge badge--warning">PENDING</span>
```

* `icon` tạo khối vuông bo tròn, nền mờ. Kết hợp emoji hoặc `<span class="material-symbols-outlined">` nếu đã import font.
* `icon--primary`, `icon--accent`, `icon--danger`, `icon--muted` định nghĩa bảng màu.
* Badge dùng cho trạng thái, đếm số, tag nhanh.

## 7. Panel & Stat card

```jsp
<section class="panel">
    <div class="panel__header">
        <h2 class="panel__title">Tiêu đề</h2>
        <code class="panel__tag">.panel</code>
    </div>
    <div class="panel__body">...</div>
</section>
```

* `panel`: hộp nội dung có shadow, bo góc, nền trắng.
* `stat-card`: dùng trong dashboard cho KPI nhanh, kết hợp icon + số liệu.

```jsp
<article class="stat-card">
    <div class="icon icon--accent">💰</div>
    <div>
        <p class="stat-card__label">Doanh thu</p>
        <p class="stat-card__value">86.500.000 đ</p>
    </div>
</article>
```

## 8. Mẹo khi bind dữ liệu động

* Đặt dữ liệu vào `request.setAttribute()` trong Servlet, sau đó sử dụng `<%= %>` để in ra hoặc EL `${}` khi phù hợp.
* Đối với danh sách, bạn có thể lặp bằng scriptlet `for` như ví dụ ở bảng. Nếu team muốn dùng JSTL, chỉ cần bổ sung JAR JSTL và đổi sang `<c:forEach>`.
* Giữ nguyên class CSS để đảm bảo mọi trang có cùng phong cách.

> 💡 **Tip:** Hãy mở trang `/styleguide` trực tiếp trong trình duyệt khi chạy server. Copy nguyên block HTML + class tương ứng rồi thay nội dung động là cách nhanh nhất cho người mới.
