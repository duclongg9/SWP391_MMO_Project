package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.Users;
import service.BusinessException;
import service.ShopService;
import service.dto.ShopAction;

/**
 * Controller xử lý thay đổi trạng thái shop (ẩn/khôi phục).
 * Chỉ xử lý POST request để thay đổi trạng thái:
 * - action=HIDE: đổi trạng thái sang 'Suspended' và unlist sản phẩm
 * - action=UNHIDE: đổi trạng thái sang 'Active'
 */
@WebServlet(name = "SellerShopStatusController", urlPatterns = {"/seller/shops/status"})
public class SellerShopStatusController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

        /**
         * Thay đổi trạng thái shop dựa trên action HIDE/UNHIDE.
         */
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
                Users currentUser = (Users) request.getSession().getAttribute("currentUser");
                // Lấy ID shop từ form parameter
                String idStr = request.getParameter("id");
                String actionParam = request.getParameter("action");
                int id;
                try {
                        id = Integer.parseInt(idStr);
                } catch (Exception ex) {
                        // ID không hợp lệ
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                }
                if (actionParam == null || actionParam.isBlank()) {
                        setFlash(request, "Thiếu thông tin hành động.", "error");
                        response.sendRedirect(request.getContextPath() + "/seller/shops");
                        return;
                }
                try {
                        ShopAction action = ShopAction.valueOf(actionParam);
                        shopService.changeStatus(currentUser.getId(), id, action);
                        if (action == ShopAction.HIDE) {
                                setFlash(request, "Đã ẩn shop và tạm dừng mọi sản phẩm.", "success");
                        } else {
                                setFlash(request, "Đã bật lại shop.", "success");
                        }
                        response.sendRedirect(request.getContextPath() + "/seller/shops");
                } catch (BusinessException e) {
                        // Lỗi nghiệp vụ (không tìm thấy shop hoặc không có quyền)
                        setFlash(request, translateError(e.getMessage()), "error");
                        response.sendRedirect(request.getContextPath() + "/seller/shops");
                } catch (IllegalArgumentException invalid) {
                        setFlash(request, "Hành động không hợp lệ.", "error");
                        response.sendRedirect(request.getContextPath() + "/seller/shops");
                } catch (SQLException e) {
                        throw new ServletException(e);
		}
	}

	/**
	 * Helper method để set flash message vào session.
	 *
	 * @param request HttpServletRequest
	 * @param msg Nội dung thông báo
	 * @param type Loại thông báo: "success", "error", "warning"
	 */
        private void setFlash(HttpServletRequest request, String msg, String type) {
                HttpSession session = request.getSession();
                session.setAttribute("flashMessage", msg);
                session.setAttribute("flashType", type);
        }

        private String translateError(String code) {
                return switch (code) {
                case "FORBIDDEN" -> "Bạn không có quyền thao tác trên shop này.";
                case "UNSUPPORTED_ACTION" -> "Hành động không hợp lệ.";
                default -> code;
                };
        }
}


