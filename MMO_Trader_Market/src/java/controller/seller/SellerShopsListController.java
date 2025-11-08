package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Users;
import model.view.ShopListItem;
import service.ShopService;
import service.dto.ShopFilters;

/**
 * Controller xử lý hiển thị danh sách shop của seller.
 * Hiển thị danh sách shop kèm thống kê (số sản phẩm, lượng bán, tồn kho) và hỗ trợ sắp xếp.
 */
@WebServlet(name = "SellerShopsListController", urlPatterns = {"/seller/shops"})
public class SellerShopsListController extends SellerBaseController {

	private static final long serialVersionUID = 1L;
	private final ShopService shopService = new ShopService();

        /**
         * Hiển thị danh sách shop của seller theo từ khóa tìm kiếm (nếu có).
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
                String keyword = request.getParameter("q");
                if (keyword != null) {
                        keyword = keyword.trim();
                }
                ShopFilters filters = ShopFilters.builder()
                                .keyword(keyword)
                                .build();
                try {
                        List<ShopListItem> shops = shopService.listByOwner(currentUser.getId(), filters);
                        request.setAttribute("shops", shops);
                        request.setAttribute("filterKeyword", keyword);
                        request.setAttribute("pageTitle", "Danh sách shop - Quản lý cửa hàng");
                        request.setAttribute("bodyClass", "layout");
                        request.setAttribute("headerModifier", "layout__header--split");
                        forward(request, response, "seller/shops/list");
                } catch (SQLException e) {
                        throw new ServletException(e);
                }
        }
}


