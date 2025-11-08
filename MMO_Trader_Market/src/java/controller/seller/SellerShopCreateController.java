package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.Users;
import service.BusinessException;
import service.ShopService;

/**
 * Controller xử lý yêu cầu tạo shop mới cho seller.
 * GET: Hiển thị form tạo shop
 * POST: Xử lý submit form, validate, tạo shop và redirect về danh sách
 */
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

        /**
         * Tạo shop mới dựa trên dữ liệu form và hiển thị lỗi tương ứng.
         */
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
                        shopService.createShop(currentUser.getId(), name, description);
                        HttpSession session = request.getSession();
                        session.setAttribute("flashMessage", "Tạo shop thành công. Vui lòng chờ duyệt.");
                        session.setAttribute("flashType", "success");
                        response.sendRedirect(request.getContextPath() + "/seller/shops");
                } catch (BusinessException e) {
                        Map<String, String> fieldErrors = new HashMap<>();
                        switch (e.getMessage()) {
                        case "SHOP_NAME_INVALID" -> fieldErrors.put("name", "Tên shop phải từ 3-60 ký tự, chỉ bao gồm chữ, số và khoảng trắng.");
                        case "SHOP_NAME_DUPLICATED" -> fieldErrors.put("name", "Bạn đã có shop với tên này. Vui lòng chọn tên khác.");
                        case "DESCRIPTION_TOO_SHORT" -> fieldErrors.put("description", "Mô tả cần tối thiểu 20 ký tự.");
                        default -> request.setAttribute("formError", e.getMessage());
                        }
                        request.setAttribute("fieldErrors", fieldErrors);
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


