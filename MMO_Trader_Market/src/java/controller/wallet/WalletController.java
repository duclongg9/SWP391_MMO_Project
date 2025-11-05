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
import java.util.List;
import service.WalletService;
import model.Wallets;
import model.WalletTransactions;

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

        /*Kiểm tra tài khoản đã được đăng nhập hay chưa
         */
        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
        //response.sendRedirect(request.getContextPath() + "/login.jsp");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Đọc tham số phân trang: page/pageSize (khớp với JSP)
        String indexPage = request.getParameter("index");
        int index;
        if (indexPage == null || indexPage.isBlank() || indexPage.isEmpty()) {
            index = 1;
        } else {
            index = Integer.parseInt(indexPage);
        }
        String transactionType = request.getParameter("type");
        if (transactionType != null) {
            transactionType = transactionType.trim();
        }
        String minStr = request.getParameter("minAmount");
        String maxStr = request.getParameter("maxAmount");
        Double minAmount = (minStr != null && !minStr.isBlank()) ? Double.valueOf(minStr) : null;
        Double maxAmount = (maxStr != null && !maxStr.isBlank()) ? Double.valueOf(maxStr) : null;

        String preset = request.getParameter("preset"); // "", "today", "7d", "30d"
        String startStr = request.getParameter("start");  // "yyyy-MM-dd" hoặc rỗng/null
        String endStr = request.getParameter("end");    // "yyyy-MM-dd" hoặc rỗng/null

        java.time.LocalDate s = (startStr == null || startStr.isBlank()) ? null : java.time.LocalDate.parse(startStr);
        java.time.LocalDate e = (endStr == null || endStr.isBlank()) ? null : java.time.LocalDate.parse(endStr);

        if ((s == null && e == null) && preset != null && !preset.isBlank()) {
            java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
            switch (preset) {
                case "today" -> {
                    if (s == null) {
                        s = today;
                    }
                    if (e == null) {
                        e = today;
                    }
                }
                case "7d" -> {
                    if (s == null) {
                        s = today.minusDays(6);
                    }
                    if (e == null) {
                        e = today;
                    }
                }
                case "30d" -> {
                    if (s == null) {
                        s = today.minusDays(29);
                    }
                    if (e == null) {
                        e = today;
                    }
                }
                default -> {
                }
            }

        }
        java.sql.Date startDate = (s == null) ? null : java.sql.Date.valueOf(s);
        java.sql.Date endDate = (e == null) ? null : java.sql.Date.valueOf(e.plusDays(1));

        try {
            // Lấy ví + kiểm quyền
            Wallets wallet = walletService.viewUserWallet(user);
            int walletId = wallet.getId();
            List<WalletTransactions> walletTransaction = walletService.viewUserTransactionList(walletId, user, index, PAGE_SIZE, transactionType, minAmount, maxAmount, startDate, endDate);
            int totalTransaction = walletService.totalPage(user);
            int endPage;
            endPage = totalTransaction % PAGE_SIZE == 0 ? totalTransaction / PAGE_SIZE : totalTransaction / PAGE_SIZE + 1;
            // Thiết lập attributes cho JSP
            request.setAttribute("wallet", wallet);
            request.setAttribute("listTransaction", walletTransaction);
            request.setAttribute("currentPage", index);
            request.setAttribute("endPage", endPage);
            request.setAttribute("preset", preset);
            request.setAttribute("start", (s == null) ? null : s.toString()); // yyyy-MM-dd
            request.setAttribute("end", (e == null) ? null : e.toString()); // yyyy-MM-dd

            // Forward
            request.getRequestDispatcher("/WEB-INF/views/wallet/wallet.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("emg", ex);
            request.getRequestDispatcher("/WEB-INF/views/wallet/wallet.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            request.setAttribute("emg", "Có lỗi hệ thống xảy ra.Vui lòng thử lại.");
            request.getRequestDispatcher("/WEB-INF/views/wallet/wallet.jsp").forward(request, response);
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
