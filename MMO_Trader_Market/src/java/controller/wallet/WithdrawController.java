/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.wallet;

import dao.user.WalletTransactionDAO;
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
    
    String ABSOLUTE_PATH = "D:\\SWP391_MMO_Project\\MMO_Trader_Market\\web\\assets\\images\\QRcode";
    
    WithdrawService withdrawService = new WithdrawService();
    WalletsDAO wdao = new WalletsDAO();
    WalletTransactionDAO wtdao = new WalletTransactionDAO();
    
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

        // 1. Bắt buộc đăng nhập
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // 2. Lấy tham số form
        String accountName   = request.getParameter("accountName");
        String accountNumber = request.getParameter("accountNumber");
        String bankCode      = request.getParameter("bankCode");
        String transferNote  = request.getParameter("transferNote");
        long amountParam     = Long.parseLong(request.getParameter("amount"));

        BigDecimal amount = BigDecimal.valueOf(amountParam);

        // 3. Kiểm tra số dư ví (chỉ check, chưa trừ ở đây)
        BigDecimal currentBalance = wdao.getWalletBalanceByUserId(user);
        if (amount.compareTo(currentBalance) > 0) {
            session.setAttribute("emg", "Số dư không đủ, vui lòng nạp thêm.");
            response.sendRedirect(request.getContextPath() + "/withdraw");
            return;
        }

        // 4. Tạo URL VietQR cho yêu cầu rút tiền
        String qrUrl = WithdrawService.buildUrl(
                bankCode,
                accountNumber,
                amountParam,
                transferNote,
                accountName
        );

        // 5. Tải và lưu ảnh QR về thư mục server
        Path folder = Paths.get(ABSOLUTE_PATH);
        String imagePath = null;
        try {
            imagePath = WithdrawService.downloadPng(qrUrl, folder);
        } catch (Exception ex) {
            Logger.getLogger(WithdrawController.class.getName())
                    .log(Level.SEVERE, "Lỗi tải ảnh QR", ex);
            session.setAttribute("emg", "Không tạo được mã QR, vui lòng thử lại.");
            response.sendRedirect(request.getContextPath() + "/withdraw");
            return;
        }

        // 6. Tạo yêu cầu rút tiền (chỉ ghi vào bảng withdrawal_requests)
        try {
            withdrawService.createWithdrawRequest(user, amount, imagePath);
            session.setAttribute(
                    "msg",
                    "Tạo yêu cầu rút tiền thành công, vui lòng chờ admin duyệt."
            );
        } catch (IllegalArgumentException e) {
            session.setAttribute("emg", e.getMessage());
        } catch (RuntimeException e) {
            session.setAttribute("emg", "Có lỗi hệ thống, vui lòng thử lại.");
        }

        // 7. Quay lại trang rút tiền
        response.sendRedirect(request.getContextPath() + "/withdraw");
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
