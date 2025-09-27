package com.mmo.trader.controller;

import com.mmo.trader.model.User;
import com.mmo.trader.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles login and logout requests following the MVC pattern.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends BaseController {

    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        forward(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = userService.authenticate(username, password);
        if (user == null) {
            request.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
            forward(request, response, "auth/login");
            return;
        }
        request.getSession().setAttribute("currentUser", user);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
