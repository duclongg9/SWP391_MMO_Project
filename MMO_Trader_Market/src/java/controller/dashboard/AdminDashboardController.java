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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.AdnimDashboardService;
import com.google.gson.Gson;
import java.math.RoundingMode;

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
        //Phần chọn năm
        String yearParam = request.getParameter("year");
        int selectedYear;

        if (yearParam != null && !yearParam.isEmpty()) {
            selectedYear = Integer.parseInt(yearParam);
        } else {
            // Mặc định năm hiện tại nếu không có param
            selectedYear = LocalDate.now().getYear();
        }

        //Lấy toàn bộ thông tin của năm đó
        BigDecimal[] listDeposit = new BigDecimal[12];
        BigDecimal[] listWithdraw = new BigDecimal[12];
        try {
            listDeposit = adminDashboard.arrayDepositByMonth(selectedYear);
            listWithdraw = adminDashboard.arrayWithdrawByMonth(selectedYear);
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Chuyển sang JSON
        Gson gson = new Gson();
        String depositJson = gson.toJson(listDeposit);
        String withdrawJson = gson.toJson(listWithdraw);

        //Lấy tháng năm hiện tại
        LocalDate now = LocalDate.now();
        int presentMonth = now.getMonthValue();
        int presentYear = now.getYear();

        //Lấy tháng trước
        int lastMonth;
        int lastYear = 0;
        if (presentMonth == 1) {
            lastMonth = 12;
            lastYear = presentYear - 1;
        } else {
            lastMonth = presentMonth - 1;
            lastYear = presentYear;
        }

        //Lấy tổng tiền tháng trước
        BigDecimal totalDepositLastMonth = BigDecimal.ZERO;
        BigDecimal totalWithdrawLastMonth = BigDecimal.ZERO;
        try {
            totalDepositLastMonth = adminDashboard.totalDeposite(lastMonth, lastYear);
            totalWithdrawLastMonth = adminDashboard.totalWithdraw(lastMonth, lastYear);
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Integer> avaliableYear = new ArrayList<>();
        try {
            //Lấy những năm có thông tin
            avaliableYear = adminDashboard.getAvailableYears();
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }

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

        //Tính toán % thay đổi
        BigDecimal persentDepositChanged = units.PercentChange.calculatePercentChange(totalDeposit, totalDepositLastMonth);
        BigDecimal persentWithdrawChanged = units.PercentChange.calculatePercentChange(totalWithdraw, totalWithdrawLastMonth);

        //Gửi thông tin lên frontend
        //Thông tin năm có dữ liệu
        request.setAttribute("years", avaliableYear);
        //thông tin về finance theo tháng
        request.setAttribute("orderByMonth", orderByMonth);
        request.setAttribute("shopByMonth", shopByMonth);
        request.setAttribute("totalDeposit", totalDeposit);
        request.setAttribute("totalWithdraw", totalWithdraw);

        request.setAttribute("persentDepositChanged", persentDepositChanged);
        request.setAttribute("persentWithdrawChanged", persentWithdrawChanged);

        request.setAttribute("selectedYear", selectedYear);
        request.setAttribute("depositByMonth", depositJson);
        request.setAttribute("withdrawByMonth", withdrawJson);

        request.getRequestDispatcher("WEB-INF/views/Admin/pages/dashboard.jsp").forward(request, response);
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
