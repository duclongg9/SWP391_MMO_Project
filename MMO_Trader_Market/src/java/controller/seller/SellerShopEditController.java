package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import model.Shops;
import model.Users;
import service.BusinessException;
import service.ShopService;

/**
 * Controller xử lý yêu cầu chỉnh sửa shop của seller.
 * GET: Hiển thị form chỉnh sửa với dữ liệu shop hiện tại
 * POST: Xử lý submit form, validate, cập nhật shop và redirect về danh sách
 */
@WebServlet(name = "SellerShopEditController", urlPatterns = {"/seller/shops/edit"})
public class SellerShopEditController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

	/**
	 * Xử lý GET request: Hiển thị form chỉnh sửa shop.
	 * Kiểm tra shop có tồn tại và thuộc về seller trước khi hiển thị form.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
		Users currentUser = (Users) request.getSession().getAttribute("currentUser");
		// Lấy ID shop từ query parameter
		String idStr = request.getParameter("id");
		int id;
		try {
			id = Integer.parseInt(idStr);
		} catch (Exception ex) {
			// ID không hợp lệ, trả về lỗi 400
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			// Tìm shop theo ID và owner (kiểm tra quyền)
                        Optional<Shops> opt = shopService.findByIdAndOwner(id, currentUser.getId());
                        if (opt.isEmpty()) {
                                // Shop không tồn tại hoặc không thuộc về seller
                                HttpSession session = request.getSession();
                                session.setAttribute("flashMessage", "Không tìm thấy shop hoặc bạn không có quyền.");
				session.setAttribute("flashType", "error");
				response.sendRedirect(request.getContextPath() + "/seller/shops");
				return;
			}
			// Lấy dữ liệu shop và set vào request để hiển thị trong form
			Shops s = opt.get();
			request.setAttribute("formName", s.getName());
                        request.setAttribute("formDescription", s.getDescription());
                        request.setAttribute("shopId", s.getId());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        if (s.getCreatedAt() != null) {
                                request.setAttribute("formCreatedAt", formatter.format(s.getCreatedAt()));
                        }
                        if (s.getUpdatedAt() != null) {
                                request.setAttribute("formUpdatedAt", formatter.format(s.getUpdatedAt()));
                        }
                        request.setAttribute("pageTitle", "Chỉnh sửa shop - Quản lý cửa hàng");
                        request.setAttribute("bodyClass", "layout");
                        request.setAttribute("headerModifier", "layout__header--split");
                        forward(request, response, "seller/shops/form");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Xử lý POST request: Submit form chỉnh sửa shop.
	 * Validate dữ liệu, cập nhật shop và redirect về danh sách.
	 * Nếu có lỗi validation, hiển thị lại form với thông báo lỗi.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
		Users currentUser = (Users) request.getSession().getAttribute("currentUser");
		// Lấy dữ liệu từ form
		String idStr = request.getParameter("id");
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		int id;
		try {
			id = Integer.parseInt(idStr);
		} catch (Exception ex) {
			// ID không hợp lệ
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			// Gọi service để cập nhật shop (sẽ validate và kiểm tra quyền)
                        shopService.updateShop(currentUser.getId(), id, name, description);
                        // Set flash message thành công và redirect về danh sách
                        HttpSession session = request.getSession();
                        session.setAttribute("flashMessage", "Cập nhật shop thành công.");
                        session.setAttribute("flashType", "success");
			response.sendRedirect(request.getContextPath() + "/seller/shops");
		} catch (BusinessException e) {
			// Nếu có lỗi nghiệp vụ (tên không hợp lệ, không có quyền), hiển thị lại form với lỗi
			request.setAttribute("errorMessage", e.getMessage());
			// Giữ lại giá trị đã nhập
                        Map<String, String> fieldErrors = new HashMap<>();
                        switch (e.getMessage()) {
                        case "SHOP_NAME_INVALID" -> fieldErrors.put("name", "Tên shop phải từ 3-60 ký tự, chỉ bao gồm chữ, số và khoảng trắng.");
                        case "SHOP_NAME_DUPLICATED" -> fieldErrors.put("name", "Bạn đã có shop với tên này. Vui lòng chọn tên khác.");
                        case "DESCRIPTION_TOO_SHORT" -> fieldErrors.put("description", "Mô tả cần tối thiểu 20 ký tự.");
                        case "FORBIDDEN" -> request.setAttribute("formError", "Bạn không có quyền thao tác trên shop này.");
                        default -> request.setAttribute("formError", e.getMessage());
                        }
                        try {
                                Optional<Shops> latest = shopService.findByIdAndOwner(id, currentUser.getId());
                                if (latest.isPresent()) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                        Shops data = latest.get();
                                        if (data.getCreatedAt() != null) {
                                                request.setAttribute("formCreatedAt", formatter.format(data.getCreatedAt()));
                                        }
                                        if (data.getUpdatedAt() != null) {
                                                request.setAttribute("formUpdatedAt", formatter.format(data.getUpdatedAt()));
                                        }
                                }
                        } catch (SQLException ignored) {
                                // bỏ qua, chỉ ảnh hưởng phần meta
                        }
                        request.setAttribute("fieldErrors", fieldErrors);
                        request.setAttribute("formName", name);
                        request.setAttribute("formDescription", description);
                        request.setAttribute("shopId", id);
                        request.setAttribute("pageTitle", "Chỉnh sửa shop - Quản lý cửa hàng");
                        request.setAttribute("bodyClass", "layout");
                        request.setAttribute("headerModifier", "layout__header--split");
                        forward(request, response, "seller/shops/form");
                } catch (SQLException e) {
                        throw new ServletException(e);
                }
        }
}


