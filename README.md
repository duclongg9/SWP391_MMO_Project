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
│   │       └── product/
│   ├── assets/css/
│   └── login.jsp
└── docs/diagrams/
    ├── architecture-overview.puml
    ├── package-diagram.puml
    └── ui-wireframe.puml
```

## Getting started

1. Open the `MMO_Trader_Market` folder in NetBeans as an Ant-based web application.
2. Adjust the JDBC properties inside `src/conf/database.properties` to match your MySQL instance when you are ready to connect the DAOs to a real database.
3. Generate the diagrams with PlantUML (VS Code extension, IntelliJ plugin, or the PlantUML CLI) from the `docs/diagrams` folder.
4. Deploy the application to a Jakarta EE compatible servlet container (Tomcat 10+, GlassFish 6+, etc.). Make sure the container provides the JSTL implementation (Jakarta Standard Tag Library) or drop the corresponding JARs inside `web/WEB-INF/lib`.

## MVC walkthrough

* **Controllers** (`controller` and its subpackages) receive HTTP requests and forward to views using the shared `BaseController` helper.
* **Services** (`service`) encapsulate business logic and orchestrate calls to DAOs.
* **DAOs** (`dao` and its subpackages) are ready for MySQL integration via the shared `BaseDAO` class. For now they expose an in-memory stubbed dataset so the UI can render without a database.
* **Views** live under `web/WEB-INF/views` and are resolved via `ViewResolver`, keeping JSP files protected from direct access while still being easy to reference from controllers.

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
