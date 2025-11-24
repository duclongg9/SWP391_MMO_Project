/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import service.KycRequestService;

/**
 *
 * @author D E L L
 */
@WebServlet(name = "KycRequestController", urlPatterns = {"/kycRequest"})
@MultipartConfig(
        fileSizeThreshold = 1 * 1024 * 1024, // 1MB cache
        maxFileSize = 10 * 1024 * 1024, // 10MB/file
        maxRequestSize = 20 * 1024 * 1024 // 20MB/tổng
)
public class KycRequestController extends HttpServlet {

    private final KycRequestService service = new KycRequestService();

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

        
        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.getRequestDispatcher("WEB-INF/views/kyc/kycRequestForm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            boolean success = service.handleKycRequest(
                    getServletContext(),
                    user,
                    request.getParameter("cccd_number"),
                    request.getPart("front"),
                    request.getPart("back"),
                    request.getPart("selfie")
            );

            if (success) {
                request.getSession().setAttribute("msg", "Yêu cầu KYC đã gửi thành công!");
                
            }
        } catch (IllegalStateException e) {
            request.getSession().setAttribute("emg", e.getMessage());
        } catch (IOException e) {
            request.getSession().setAttribute("emg", "Lỗi xử lý KYC: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/kycRequest");
    }

}
