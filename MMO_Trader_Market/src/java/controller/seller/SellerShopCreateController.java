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


@WebServlet(name = "SellerShopCreateController", urlPatterns = {"/seller/shops/create"})
public class SellerShopCreateController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

	/**
	 * Xử lý GET request: Hiển thị form tạo shop.
	 * Chỉ cho phép seller đã đăng nhập (role = 2) truy cập.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
		// Set các attribute cho layout và forward đến form JSP
		request.setAttribute("pageTitle", "Tạo shop - Quản lý cửa hàng");
		request.setAttribute("bodyClass", "layout");
		request.setAttribute("headerModifier", "layout__header--split");
		forward(request, response, "seller/shops/form");
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
		// Lấy thông tin user từ session
		Users currentUser = (Users) request.getSession().getAttribute("currentUser");
		// Lấy dữ liệu từ form
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		try {
			// Gọi service để tạo shop (sẽ validate và kiểm tra giới hạn 5 shop)
			shopService.createShop(currentUser.getId(), name, description);
			// Set flash message thành công và redirect về danh sách
			HttpSession session = request.getSession();
			session.setAttribute("flashMessage", "Tạo shop thành công.");
			session.setAttribute("flashType", "success");
			response.sendRedirect(request.getContextPath() + "/seller/shops");
		} catch (BusinessException e) {
			// Nếu có lỗi nghiệp vụ (vượt quá 5 shop, tên không hợp lệ), hiển thị lại form với lỗi
			request.setAttribute("errorMessage", e.getMessage());
			// Giữ lại giá trị đã nhập để user không phải nhập lại
			request.setAttribute("formName", name);
			request.setAttribute("formDescription", description);
			request.setAttribute("pageTitle", "Tạo shop - Quản lý cửa hàng");
			request.setAttribute("bodyClass", "layout");
			request.setAttribute("headerModifier", "layout__header--split");
			forward(request, response, "seller/shops/form");
		} catch (SQLException e) {
			// Lỗi database, ném ServletException để container xử lý
			throw new ServletException(e);
		}
	}
}


