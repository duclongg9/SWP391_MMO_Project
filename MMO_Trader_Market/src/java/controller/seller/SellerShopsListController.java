package controller.seller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.Users;
import model.view.ShopListItem;
import service.BusinessException;
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
         * Hiển thị danh sách shop của seller theo bộ lọc truy vấn.
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
                String fromParam = request.getParameter("from");
                String toParam = request.getParameter("to");
                LocalDate from = parseDateOrNull(fromParam);
                LocalDate to = parseDateOrNull(toParam);
                if (from != null && to != null && from.isAfter(to)) {
                        LocalDate tmp = from;
                        from = to;
                        to = tmp;
                }
                ShopFilters filters = ShopFilters.builder()
                                .keyword(keyword)
                                .fromDate(from)
                                .toDate(to)
                                .build();
                try {
                        List<ShopListItem> shops = shopService.listByOwner(currentUser.getId(), filters);
                        request.setAttribute("shops", shops);
                        request.setAttribute("filterKeyword", keyword);
                        request.setAttribute("filterFrom", from != null ? from.toString() : "");
                        request.setAttribute("filterTo", to != null ? to.toString() : "");
                        request.setAttribute("today", LocalDate.now().toString());
                        request.setAttribute("pageTitle", "Danh sách shop - Quản lý cửa hàng");
                        request.setAttribute("bodyClass", "layout");
                        request.setAttribute("headerModifier", "layout__header--split");
                        forward(request, response, "seller/shops/list");
                } catch (BusinessException e) {
                        request.setAttribute("filterError", translateError(e.getMessage()));
                        request.setAttribute("shops", List.of());
                        request.setAttribute("filterKeyword", keyword);
                        request.setAttribute("filterFrom", fromParam);
                        request.setAttribute("filterTo", toParam);
                        request.setAttribute("today", LocalDate.now().toString());
                        request.setAttribute("pageTitle", "Danh sách shop - Quản lý cửa hàng");
                        request.setAttribute("bodyClass", "layout");
                        request.setAttribute("headerModifier", "layout__header--split");
                        forward(request, response, "seller/shops/list");
                } catch (SQLException e) {
                        throw new ServletException(e);
                }
        }

        private LocalDate parseDateOrNull(String value) {
                if (value == null || value.isBlank()) {
                        return null;
                }
                try {
                        return LocalDate.parse(value);
                } catch (DateTimeParseException ex) {
                        return null;
                }
        }

        private String translateError(String code) {
                return switch (code) {
                case "DATE_IN_FUTURE_NOT_ALLOWED" -> "Không thể chọn ngày trong tương lai.";
                case "DATE_RANGE_INVALID" -> "Khoảng thời gian không hợp lệ.";
                default -> code;
                };
        }
}


