/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.auth;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Users;
import service.UserService;

/**
 * Servlet quản lý trang hồ sơ cá nhân của người dùng.
 * <p>
 * - Cho phép xem thông tin tài khoản và thông báo flash. - Nhận yêu cầu cập
 * nhật thông tin cá nhân hoặc đổi mật khẩu và xử lý tương ứng.
 *
 * @author D E L L
 */
@WebServlet(name = "ProfileController", urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 1 * 1024 * 1024, // 1MB cache
        maxFileSize = 10 * 1024 * 1024, // 10MB/file
        maxRequestSize = 20 * 1024 * 1024 // 20MB/tổng
)
public class ProfileController extends HttpServlet {

    // Dịch vụ người dùng phục vụ các thao tác xem và cập nhật hồ sơ.
    private final UserService viewProfileService = new UserService(new dao.user.UserDAO());

    // Phương thức khung do NetBeans sinh, không dùng tới trong luồng thực tế.
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Hiển thị trang hồ sơ cùng các thông báo flash dành cho người dùng đã
     * đăng nhập.
     */
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
//           response.sendRedirect(request.getContextPath() + "/login.jsp");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }
        try {
            // Lấy thông tin hồ sơ của chính người dùng và đẩy xuống view.
            Users myProfile = viewProfileService.viewMyProfile(user);
            request.setAttribute("myProfile", myProfile);
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            // Hiển thị lỗi nghiệp vụ như dữ liệu không tồn tại.
            request.setAttribute("emg", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        } catch (RuntimeException e) {
            // Thông báo lỗi hệ thống chung nếu truy vấn thất bại bất ngờ.
            request.setAttribute("emg", "Có lỗi hệ thống xảy ra.Vui lòng thử lại.");
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        }

    }

    /**
     * Xử lý yêu cầu cập nhật thông tin cá nhân hoặc mật khẩu từ biểu mẫu.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*Kiểm tra tài khoản đã được đăng nhập hay chưa*/
        Integer user = (Integer) request.getSession().getAttribute("userId");
        if (user == null) {
//           response.sendRedirect(request.getContextPath() + "/login.jsp");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

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
                    Part avatar = request.getPart("avatar");
                    // Cập nhật tên hiển thị của người dùng hiện tại.
                    viewProfileService.updateMyProfile(user, name, avatar);
                    session.setAttribute("msg", "Thông tin đã được cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    break;
                }
                case "updatePassword": {
                    String oldPass = request.getParameter("oldPass");
                    String newPass = request.getParameter("newPass");
                    // Đổi mật khẩu sau khi xác thực mật khẩu cũ.
                    viewProfileService.updatePassword(user, oldPass, newPass);
                    session.setAttribute("msg", "Mật khẩu đã được cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    break;
                }
                default: {
                    // Không xác định hành động => trả về lỗi để tránh trạng thái không mong muốn.
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            // Lưu thông báo lỗi nghiệp vụ để hiển thị sau khi redirect.
            session.setAttribute("emg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profile");
        } catch (RuntimeException e) {
            // Lỗi hệ thống: ghi nhận flash chung và quay lại trang hồ sơ.
            response.sendRedirect(request.getContextPath() + "/profile");
        } catch (SQLException ex) {
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Điều khiển trang hồ sơ người dùng";
    }// </editor-fold>

}
