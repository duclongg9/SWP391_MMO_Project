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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.UserService;

/**
 *
 * @author D E L L
 */
@WebServlet(name="ProfileController", urlPatterns={"/profile"})
public class ProfileController extends HttpServlet {
   
    private UserService viewProfileService;
   
    
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
        
//        /*Kiểm tra tài khoản đã được đăng nhập hay chưa*/
//        Integer user = (Integer)request.getSession().getAttribute("userId");
//        if(user == null){
//           response.sendRedirect(request.getContextPath() + "/login.jsp");
//           return;
//        }
        
        try {
            User myProfile = viewProfileService.viewMyProfile(user);
            request.setAttribute("myProfile", myProfile);
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            request.setAttribute("emg", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        }catch(RuntimeException e){
            request.setAttribute("emg","Có lỗi hệ thống xảy ra.Vui lòng thử lại.");
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (SQLException ex) {
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    } 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
