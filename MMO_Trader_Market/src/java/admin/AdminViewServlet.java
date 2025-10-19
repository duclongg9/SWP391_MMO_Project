package  admin;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="AdminRouter", urlPatterns={"/admin/*"})
public class AdminViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();               // /users, /shops, null...
        if (path == null || "/".equals(path)) path = "/dashboard";

        String content; String title; String active;
        switch (path.toLowerCase()) {
            case "/users":
                content = "/WEB-INF/views/Admin/pages/users.jsp";
                title   = "Quản lý người dùng"; active = "users"; break;
            case "/shops":
                content = "/WEB-INF/views/Admin/pages/shops.jsp";
                title   = "Quản lý gian hàng"; active = "shops"; break;
            case "/kycs":
                content = "/WEB-INF/views/Admin/pages/kycs.jsp";
                title   = "Duyệt KYC"; active = "kycs"; break;
            case "/cashs":
                content = "/WEB-INF/views/Admin/pages/cashs.jsp";
                title   = "Nạp / Rút"; active = "cashs"; break;
            case "/systems":
                content = "/WEB-INF/views/Admin/pages/systems.jsp";
                title   = "Cấu hình hệ thống"; active = "systems"; break;
            case "/dashboard":
            default:
                content = "/WEB-INF/views/Admin/pages/dashboard.jsp";
                title   = "Tổng quan"; active = "dashboard";
        }

        req.setAttribute("pageTitle", title);
        req.setAttribute("active", active);
        req.setAttribute("content", content);

        // layout chung (sidebar + header + nút Đăng xuất)
        req.getRequestDispatcher("/WEB-INF/views/Admin/_layout.jsp").forward(req, resp);
    }
}
