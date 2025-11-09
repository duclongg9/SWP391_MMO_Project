package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.ShopStatsView;
import model.Users;
import service.ShopService;

/**
 * Controller xử lý hiển thị danh sách shop của seller.
 * Hiển thị danh sách shop kèm thống kê (số sản phẩm, lượng bán, tồn kho) và hỗ trợ sắp xếp.
 */
@WebServlet(name = "SellerShopsListController", urlPatterns = {"/seller/shops"})
public class SellerShopsListController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

	/**
	 * Xử lý GET request: Hiển thị danh sách shop của seller với thống kê.
	 * Hỗ trợ sắp xếp theo tham số sortBy (sales_desc, created_desc, name_asc).
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Kiểm tra quyền seller
		if (!ensureSellerAccess(request, response)) {
			return;
		}
		// Lấy thông tin user từ session
		Users currentUser = (Users) request.getSession().getAttribute("currentUser");
		// Lấy tham số sắp xếp từ query string
                String sortBy = request.getParameter("sortBy");
                String search = request.getParameter("search");
                if (search != null) {
                        search = search.trim();
                }
                try {
                        // Lấy danh sách shop kèm thống kê (tránh N+1 query)
                        List<ShopStatsView> shops = shopService.listMyShops(currentUser.getId(), sortBy, search);
                        // Set các attribute cho JSP
                        request.setAttribute("shops", shops);
                        request.setAttribute("sortBy", sortBy == null ? "sales_desc" : sortBy);
                        request.setAttribute("search", search);
			request.setAttribute("pageTitle", "Danh sách shop - Quản lý cửa hàng");
			request.setAttribute("bodyClass", "layout");
			request.setAttribute("headerModifier", "layout__header--split");
			// Forward đến trang list JSP
			forward(request, response, "seller/shops/list");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}


