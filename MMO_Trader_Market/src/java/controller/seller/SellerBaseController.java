package controller.seller;

import controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Base controller cho các màn hình dành riêng cho người bán. Đảm bảo người dùng
 * đã đăng nhập và có vai trò SELLER trước khi truy cập trang quản lý cửa hàng.
 */
public abstract class SellerBaseController extends BaseController {

    private static final long serialVersionUID = 1L;

    protected boolean ensureSellerAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        Object role = session.getAttribute("userRole");
        if (!(role instanceof Integer) || ((Integer) role) != 2) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        return true;
    }
}
