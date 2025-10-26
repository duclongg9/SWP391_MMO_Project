# SWP391 MMO Trader Market

This repository contains a NetBeans (Ant) web application skeleton for the MMO Trader Market system. It follows a classic MVC layering with Servlets, JSP and a service/DAO structure. The project also includes PlantUML sources for package, UI flow and overall architecture diagrams so you can quickly generate design documentation from the provided `.puml` files.

## Project layout

```
MMO_Trader_Market/
├── build.xml
├── src/
│   ├── conf/
│   │   └── database.properties
│   └── java/
│       ├── conf/
│       ├── controller/
│       │   ├── auth/
│       │   ├── dashboard/
│       │   ├── guide/
│       │   └── product/
│       ├── dao/
│       │   ├── product/
│       │   └── user/
│       ├── model/
│       └── service/
├── web/
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   └── views/
│   │       ├── auth/
│   │       ├── dashboard/
│   │       ├── product/
│   │       ├── shared/   # header, footer, layout fragment JSPF files
│   │       └── styleguide/
│   ├── assets/css/
│   ├── login.jsp
│   └── index.html
└── docs/diagrams/
    ├── architecture-overview.puml
    ├── package-diagram.puml
    └── ui-wireframe.puml
```

## Getting started

1. Open the `MMO_Trader_Market` folder in NetBeans as an Ant-based web application.
2. Adjust the JDBC properties inside `src/conf/database.properties` to match your MySQL instance when you are ready to connect the DAOs to a real database.
3. Generate the diagrams with PlantUML (VS Code extension, IntelliJ plugin, or the PlantUML CLI) from the `docs/diagrams` folder.
4. Deploy the application to a Jakarta EE compatible servlet container (Tomcat 10+, GlassFish 6+, etc.). The current JSPs chỉ sử dụng scriptlet/EL nên không yêu cầu JSTL, tuy nhiên nếu bạn thêm JSTL hãy chắc rằng đã bổ sung JAR tương ứng vào `web/WEB-INF/lib`.
5. Truy cập `/styleguide` sau khi deploy để xem thư viện UI và copy/paste các component vào trang của bạn.

## MVC walkthrough

* **Controllers** (`controller` và các gói con) nhận HTTP request và forward sang view thông qua `BaseController`.
* **Services** (`service`) chứa business logic và điều phối DAO.
* **DAOs** (`dao` và các gói con) đã sẵn stub dữ liệu in-memory để UI hiển thị mà chưa cần kết nối database.
* **Views** nằm dưới `web/WEB-INF/views` và được resolve thông qua `ViewResolver`, bảo vệ JSP khỏi truy cập trực tiếp đồng thời giúp tham chiếu dễ dàng từ controller.

## UI template & style guide

* Đã bổ sung trang `/styleguide` tổng hợp cách sử dụng layout, menu, thanh tìm kiếm, bảng dữ liệu, phân trang, icon/badge và button.
* Các phần HTML dùng chung như header, footer và phần mở/đóng tài liệu đã được tách vào thư mục `WEB-INF/views/shared` (`page-start.jspf`, `header.jspf`, `footer.jspf`, `page-end.jspf`) để tái sử dụng bằng `<%@ include %>`.
* Tất cả component sử dụng chung file `web/assets/css/main.css`. Có thể tận dụng lại class đã khai báo để giữ giao diện đồng nhất trong toàn bộ dự án.
* Xem thêm hướng dẫn chi tiết trong [`docs/ui-template-guide.md`](docs/ui-template-guide.md) để hiểu ý nghĩa từng class và các mẹo tùy chỉnh nhanh.

## Diagrams

All diagrams are written in PlantUML for ease of maintenance:

* `package-diagram.puml` – visualizes dependencies between the MVC packages.
* `ui-wireframe.puml` – sketches the UI navigation between login, dashboard and product list pages.
* `architecture-overview.puml` – illustrates the layered architecture and external actors/services.

Render them with your favorite PlantUML workflow, e.g.:

```bash
plantuml docs/diagrams/package-diagram.puml
plantuml docs/diagrams/ui-wireframe.puml
plantuml docs/diagrams/architecture-overview.puml
```

The generated PNG/SVG files can be embedded into documentation or shared with your team.

## Troubleshooting

* Gặp lỗi Google Sign-In `DEVELOPER_ERROR (10)` khi build ứng dụng Android? Tham khảo hướng dẫn chi tiết trong [`docs/troubleshooting-google-signin.md`](docs/troubleshooting-google-signin.md) để rà soát lại SHA fingerprint, package name và cấu hình OAuth trên Google Cloud.
* Muốn tự động điền `google.clientId`, `google.clientSecret` và `google.redirectUri` trong cấu hình web? Tải file OAuth JSON từ Google Cloud rồi chạy:

  ```bash
  ./scripts/apply_google_oauth.py path/to/client_secret.json
  ```

  Script sẽ cập nhật cả `MMO_Trader_Market/src/conf/database.properties` và `MMO_Trader_Market/conf/database.properties` bằng giá trị mới. Dùng tùy chọn `--redirect` nếu muốn override redirect URI mặc định trong JSON.
