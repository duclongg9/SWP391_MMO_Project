package controller.auth;

import controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Hiển thị trang hướng dẫn kiểm tra email sau khi người dùng yêu cầu gửi OTP/reset mật khẩu.
 */
@WebServlet(name = "EmailNoticeController", urlPatterns = {"/auth/email-sent"})
public class EmailNoticeController extends BaseController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            moveFlash(session, request, "emailNoticeMessage", "noticeMessage");
            moveFlash(session, request, "emailNoticeType", "noticeType");
            moveFlash(session, request, "emailNoticeEmail", "noticeEmail");
        }
        forward(request, response, "auth/email-sent");
    }

    private void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }
}
