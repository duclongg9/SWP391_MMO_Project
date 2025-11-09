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

/**
 * Controller xử lý thay đổi trạng thái shop (ẩn/khôi phục).
 * Chỉ xử lý POST request để thay đổi trạng thái:
 * - /seller/shops/hide: Đổi trạng thái sang 'Suspended' (ẩn shop)
 * - /seller/shops/restore: Đổi trạng thái sang 'Active' (khôi phục shop)
 */
@WebServlet(name = "SellerShopStatusController", urlPatterns = {"/seller/shops/hide", "/seller/shops/restore"})
public class SellerShopStatusController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

	/**
	 * Xử lý POST request: Thay đổi trạng thái shop (ẩn hoặc khôi phục).
	 * Dựa vào servletPath để xác định hành động (hide hoặc restore).
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
		int id;
		try {
			id = Integer.parseInt(idStr);
		} catch (Exception ex) {
			// ID không hợp lệ
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			// Xác định hành động dựa vào servletPath
			String path = request.getServletPath();
			if ("/seller/shops/hide".equals(path)) {
				// Ẩn shop: đổi trạng thái sang 'Suspended'
                                shopService.hideShop(id, currentUser.getId());
                                setFlash(request, "Ẩn shop thành công. Tất cả sản phẩm đã tạm ngừng bán.", "success");
			} else if ("/seller/shops/restore".equals(path)) {
				// Khôi phục shop: đổi trạng thái sang 'Active'
				shopService.restoreShop(id, currentUser.getId());
				setFlash(request, "Khôi phục shop thành công.", "success");
			} else {
				// Path không hợp lệ
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			// Redirect về danh sách shop
			response.sendRedirect(request.getContextPath() + "/seller/shops");
		} catch (BusinessException e) {
			// Lỗi nghiệp vụ (không tìm thấy shop hoặc không có quyền)
			setFlash(request, e.getMessage(), "error");
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
}


