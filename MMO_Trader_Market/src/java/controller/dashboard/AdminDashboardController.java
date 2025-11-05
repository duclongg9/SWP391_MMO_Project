/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.AdnimDashboardService;

/**
 *
 * @author D E L L
 */
@WebServlet(name = "AdminDashboardController", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    AdnimDashboardService adminDashboard = new AdnimDashboardService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AdminDashboardController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AdminDashboardController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //Lấy ngày tháng hiện tại
        LocalDate now = LocalDate.now();
        int presentYear = now.getYear();
        int presentMonth = now.getMonthValue();
        
        //Lấy những năm có thông tin
        
        
        //Lấy thông tin theo ngày hiện tại
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalWithdraw = BigDecimal.ZERO;
        try {
            totalDeposit = adminDashboard.totalDeposite(presentMonth, presentYear);
            totalWithdraw = adminDashboard.totalWithdraw(presentMonth, presentYear);
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        int orderByMonth = adminDashboard.totalOrder(presentMonth, presentYear);
        int shopByMonth = adminDashboard.totalShop();
        //Gửi thông tin lên frontend
        //thông tin về finance theo tháng
        request.setAttribute("orderByMonth", orderByMonth);
        request.setAttribute("shopByMonth", shopByMonth);
        request.setAttribute("totalDeposit", totalDeposit);
        request.setAttribute("totalWithdraw", totalWithdraw);

        request.getRequestDispatcher("WEB-INF/views/Admin/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
