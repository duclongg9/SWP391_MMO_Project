/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.auth;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Users;
import service.UserService;

/**
 *
 * @author D E L L
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile"})
public class ProfileController extends HttpServlet {

    private final UserService viewProfileService = new UserService(new dao.user.UserDAO());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // FLASH: chuyển từ session sang request rồi xóa (dùng 1 lần)
        HttpSession ss = request.getSession(false);
        if (ss != null) {
            Object ok = ss.getAttribute("msg");
            Object err = ss.getAttribute("emg");
            if (ok != null) {
                request.setAttribute("msg", ok.toString());
                ss.removeAttribute("msg");
            }
            if (err != null) {
                request.setAttribute("emg", err.toString());
                ss.removeAttribute("emg");
            }
        }

//        /*Kiểm tra tài khoản đã được đăng nhập hay chưa*/
//        Integer user = (Integer)request.getSession().getAttribute("userId");
//        if(user == null){
//           response.sendRedirect(request.getContextPath() + "/login.jsp");
//           return;
//        }
        try {
            Users myProfile = viewProfileService.viewMyProfile(1);
            request.setAttribute("myProfile", myProfile);
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            request.setAttribute("emg", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (RuntimeException e) {
            request.setAttribute("emg", "Có lỗi hệ thống xảy ra.Vui lòng thử lại.");
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*Kiểm tra tài khoản đã được đăng nhập hay chưa*/
//        Integer user = (Integer)request.getSession().getAttribute("userId");
//        if(user == null){
//           response.sendRedirect(request.getContextPath() + "/login.jsp");
//           return;
//        }

        /*Phần phân chia 2 hành động cập nhật mật khẩu và cập nhật lại thông tin người dùng*/
        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu action");
        }
        action = action.trim();
        HttpSession session = request.getSession();
        try {
            switch (action) {
                case "updateProfile": {
                    String name = request.getParameter("fullName");
                    viewProfileService.updateMyProfile(1, name); //đang test với Id = 1
                    session.setAttribute("msg", "Thông tin đã được cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    break;
                }
                case "updatePassword": {
                    String oldPass = request.getParameter("oldPass");
                    String newPass = request.getParameter("newPass");
                    viewProfileService.updatePassword(1, oldPass, newPass); //đang test với id = 1
                    session.setAttribute("msg", "Mật khẩu đã được cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("emg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profile");
        } catch (RuntimeException e) {
            response.sendRedirect(request.getContextPath() + "/profile");
        }

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
