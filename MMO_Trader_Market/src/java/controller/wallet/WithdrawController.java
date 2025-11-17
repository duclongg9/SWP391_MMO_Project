/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.wallet;

import dao.user.WalletsDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.WithdrawService;
import java.sql.SQLException;

/**
 *
 * @author D E L L
 */
@WebServlet(name = "WithdrawController", urlPatterns = {"/withdraw"})
public class WithdrawController extends HttpServlet {
    
    String ABSOLUTE_PATH = "D:\\DH_FPT\\Ky_7\\SWP391_MMO_Project\\MMO_Trader_Market\\web\\assets\\images\\QRcode";
    
    WithdrawService withdrawService = new WithdrawService();
    WalletsDAO wdao = new WalletsDAO();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet WithdrawController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet WithdrawController at " + request.getContextPath() + "</h1>");
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

        /*Kiểm tra tài khoản đã được đăng nhập hay chưa*/
        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        try {
            // Cache nhẹ 5 phút trong ServletContext để giảm gọi API
            @SuppressWarnings("unchecked")
            List<model.BankDTO> cached = (List<model.BankDTO>) getServletContext().getAttribute("banks_cache"); //lấy list ngân hàng được lưu trong serveletContext(nếu có)
            Long ts = (Long) getServletContext().getAttribute("banks_cache_ts");//Lấy giá trị thời gian được lưu ở thời điểm gọi trước
            long now = System.currentTimeMillis();
            if (cached == null || ts == null || (now - ts) > 5 * 60_000) {
                List<model.BankDTO> banks = new service.BankSevice().getAllBanks();
                getServletContext().setAttribute("banks_cache", banks);
                getServletContext().setAttribute("banks_cache_ts", now);
                cached = banks;
            }
            request.setAttribute("banks", cached);
        } catch (Exception ex) {
            request.setAttribute("emg", "Không tải được danh sách ngân hàng. Vui lòng thử lại.");
        }
        request.getRequestDispatcher("WEB-INF/views/wallet/withdraw.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        
        String accountName = request.getParameter("accountName");
        String accountNumber = request.getParameter("accountNumber");
        String bankCode = request.getParameter("bankCode");
        String transferNote = request.getParameter("transferNote");
        long amountParam = Long.parseLong(request.getParameter("amount"));
        
        BigDecimal amount = BigDecimal.valueOf(amountParam);
        
        if(amount.compareTo(wdao.getWalletBalanceByUserId(user)) > 0){
            session.setAttribute("emg", "Số dư không đủ vui lòng nạp thêm");
            response.sendRedirect(request.getContextPath() + "/withdraw");
            return;
        }

        //tạo url chuẩn form VietQR
        String qrUrl = WithdrawService.buildUrl(bankCode, accountNumber, amountParam, transferNote, accountName);
        
        Path folder = Paths.get(ABSOLUTE_PATH);
        String imagePath = null;
        try {
            imagePath = WithdrawService.downloadPng(qrUrl, folder);
        } catch (IllegalArgumentException e) {
            session.setAttribute("emg", e);
            response.sendRedirect(request.getContextPath() + "/withdraw");
        } catch (Exception ex) {
            Logger.getLogger(WithdrawController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //tạo bản ghi withdraw request vào database 
        try {
            int result = withdrawService.createWithdrawRequest(user, amount, imagePath);
        } catch (IllegalArgumentException e) {
            session.setAttribute("emg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/withdraw");
        } catch (RuntimeException e) {
            // Lỗi hệ thống: ghi nhận flash chung và quay lại trang hồ sơ.
            response.sendRedirect(request.getContextPath() + "/withdraw");
        }
        
        //Trừ tiền trong ví
        try {
            wdao.decreaseBalance(user, amount);
            session.setAttribute("msg","Tạo yêu cầu thành công");
            response.sendRedirect(request.getContextPath() + "/withdraw");
        } catch (IllegalArgumentException e) {
            session.setAttribute("emg",e);
            response.sendRedirect(request.getContextPath() + "/withdraw");
        } catch (SQLException ex) {
            Logger.getLogger(WithdrawController.class.getName()).log(Level.SEVERE, null, ex);
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
