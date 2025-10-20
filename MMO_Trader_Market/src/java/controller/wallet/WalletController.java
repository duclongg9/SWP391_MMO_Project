/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.wallet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.sql.SQLException;
import java.util.List;
import service.WalletService;
import model.Wallets;
import model.WalletTransactions;
import org.apache.tomcat.dbcp.dbcp2.SQLExceptionList;

/**
 *
 * @author D E L L
 */
@WebServlet(name = "WalletController", urlPatterns = {"/wallet"})
public class WalletController extends HttpServlet {

    private final WalletService walletService = new WalletService(new dao.user.WalletsDAO(), new dao.user.WalletTransactionDAO());

    private static final int PAGE_SIZE = 5;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet WalletController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet WalletController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
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
        // TODO: lấy userId thật từ session
        int userId = 1;

        // Đọc tham số phân trang: page/pageSize (khớp với JSP)
        String indexPage = request.getParameter("index");
        int index;
        if (indexPage == null) {
            index = 1;
        } else {
            index = Integer.parseInt(indexPage);
        }

        try {
            // Lấy ví + kiểm quyền
            Wallets wallet = walletService.viewUserWallet(userId);
            int walletId = wallet.getId();
            List<WalletTransactions> walletTransaction = walletService.viewUserTransactionList(1, userId, index, PAGE_SIZE);
            int totalTransaction = walletService.totalPage(userId);
            int endPage;
            endPage = totalTransaction % PAGE_SIZE == 0 ? totalTransaction / PAGE_SIZE : totalTransaction / PAGE_SIZE + 1;
            // Thiết lập attributes cho JSP
            request.setAttribute("wallet", wallet);
            request.setAttribute("listTransaction", walletTransaction);
            request.setAttribute("currentPage", index);
            request.setAttribute("endPage", endPage);

            // Forward
            request.getRequestDispatcher("/WEB-INF/views/wallet/wallet.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            request.setAttribute("emg", e);
            request.getRequestDispatcher("WEB-INF/views/wallet/wallet.jsp").forward(request, response);
            return;
        } catch (RuntimeException e) {
            request.setAttribute("emg", "Có lỗi hệ thống xảy ra.Vui lòng thử lại.");
            request.getRequestDispatcher("WEB-INF/views/wallet/wallet.jsp").forward(request, response);
            return;
        }

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
